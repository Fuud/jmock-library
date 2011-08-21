package org.jmock;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.core.*;
import org.jmock.api.Action;
import org.jmock.internal.*;
import org.jmock.lib.action.*;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.syntax.*;
import org.objenesis.ObjenesisHelper;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Provides most of the syntax of jMock's "domain-specific language" API.
 * The methods of this class don't make any sense on their own, so the
 * Javadoc is rather sparse.  Consult the documentation on the jMock
 * website for information on how to use this API.
 *
 * @author nat
 */
public abstract class Expectations implements ExpectationBuilder,
        CardinalityClause, ArgumentConstraintPhrases, ActionClause {

    private List<InvocationExpectationBuilder> builders = new ArrayList<InvocationExpectationBuilder>();
    private int currentPosInBuilders = -1;
    private InvocationExpectationBuilder currentBuilder = null;
    private boolean isBuildNow = false;

    private List<Object> objectsFromWith = new ArrayList<Object>();
    private int lastVerifiedPosInObjects = -1;
    private int currentPosIsObjectsFromWith = -1;

    private Queue<Object> stubValuesGeneratedEarlier = new ArrayDeque<Object>();

    private class IncompatibleClass {
    }


    protected abstract void expect() throws Exception;

    private void build() {
        while (true) {
            try {
                currentBuilder = null;
                expect();
                return; // all is right
            } catch (ClassCastException e) {
                System.out.println(e.getMessage());
                lastVerifiedPosInObjects++;
                Object objectOfExpectedClass = createObjectOfExpectedClass(e);
                if (lastVerifiedPosInObjects < objectsFromWith.size()) {
                    objectsFromWith.set(lastVerifiedPosInObjects, objectOfExpectedClass);
                } else {
                    objectsFromWith.add(objectOfExpectedClass);
                }
                currentPosIsObjectsFromWith = -1;
                currentPosInBuilders = -1;
            } catch (InvocationExpectationBuilder.DuplicatePrimitiveValuesFromWithAndFromActualParametersException e) {
                currentBuilder().setBuildPhase(InvocationExpectationBuilder.BuildPhase.SEARCH_FOR_VALUES);
                currentPosInBuilders = -1;
                currentPosIsObjectsFromWith = -1; // start again
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Object createObjectOfExpectedClass(ClassCastException e) {
        /*
        should be processed correctly:
        1) Objects
        2) -- Primitives - need special case (we can not compare boxing-unboxing primitives by identity)
        3) Arrays
        */
        final String message = e.getMessage();
        final int pos = message.indexOf("cannot be cast to ") + "cannot be cast to ".length();
        final String className = message.substring(pos);

        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isArray()) {
                return Array.newInstance(clazz.getComponentType(), 0);
            } else if (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers())) {
                return ClassImposteriser.INSTANCE.imposterise(new ReturnDefaultValueAction(), clazz); // ReturnDefaultValueAction - bad hashCode - always returns 0
            } else {
                return ObjenesisHelper.newInstance(clazz);
            }
        } catch (ClassNotFoundException e1) {
            throw new RuntimeException(e1);
        }
    }

    private Object getObjectFromWith() {
        if (currentBuilder().getBuildPhase() == InvocationExpectationBuilder.BuildPhase.SEARCH_FOR_VALUES) {
            return stubValuesGeneratedEarlier.poll();
        } else if (currentBuilder().getBuildPhase() == InvocationExpectationBuilder.BuildPhase.SEARCH_FOR_ACCESSIBLE_TYPES) {
            currentPosIsObjectsFromWith++;
            if (objectsFromWith.size() == currentPosIsObjectsFromWith) {
                objectsFromWith.add(new IncompatibleClass());
            }
            return objectsFromWith.get(currentPosIsObjectsFromWith);
        } else {
            throw new IllegalStateException("Invalid currency state: " + currentBuilder().getBuildPhase());
        }
    }

    private void checkWeBuildingNow() {
        if (!isBuildNow) {
            throw new IllegalStateException("You should specify expectations only in 'expect(){...} method'");
        }
    }

    protected final WithClause with = new WithClause() {
        public boolean booleanIs(Matcher<?> matcher) {
            return (Boolean) with(matcher);// ClassCastException is expected here
        }

        public byte byteIs(Matcher<?> matcher) {
            return (Byte) with(matcher); // ClassCastException is expected here
        }

        public char charIs(Matcher<?> matcher) {
            return (Character) with(matcher);// ClassCastException is expected here
        }

        public double doubleIs(Matcher<?> matcher) {
            return (Double) with(matcher);// ClassCastException is expected here
        }

        public float floatIs(Matcher<?> matcher) {
            return (Float) with(matcher);// ClassCastException is expected here
        }

        public int intIs(Matcher<?> matcher) {
            return (Integer) with(matcher);// ClassCastException is expected here
        }

        public long longIs(Matcher<?> matcher) {
            return (Long) with(matcher);// ClassCastException is expected here
        }

        public short shortIs(Matcher<?> matcher) {
            return (Short) with(matcher);// ClassCastException is expected here
        }

        @SuppressWarnings({"unchecked"})
        public <T> T is(Matcher<?> matcher) {
            return (T) with(matcher);// ClassCastException may be thrown here
        }
    };


    private void initialiseExpectationCapture(Cardinality cardinality) {
        checkLastExpectationWasFullySpecified();
        stubValuesGeneratedEarlier.clear();
        currentPosInBuilders++;
        if (currentPosInBuilders < builders.size()) {
            final InvocationExpectationBuilder oldBuilderAtThisPosition = builders.get(currentPosInBuilders);
            final List<Object> capturedParameterStupValues = new ArrayList<Object>(oldBuilderAtThisPosition.getCapturedParameterMatchersStupValues());
            final Set<Boolean> forbiddenBooleans = new HashSet<Boolean>(oldBuilderAtThisPosition.getForbiddenBooleans());
            final Set<Character> forbiddenCharacter = new HashSet<Character>(oldBuilderAtThisPosition.getForbiddenCharacter());
            final Set<Byte> forbiddenNumbers = new HashSet<Byte>(oldBuilderAtThisPosition.getForbiddenNumbers());

            for (int i = 0; i < capturedParameterStupValues.size(); i++) {
                Object capturedValue = capturedParameterStupValues.get(i);
                if (BoxingUtils.isWrapperType(capturedValue.getClass())) {
                    if (capturedValue instanceof Boolean) {
                        final boolean booleanValue = (Boolean) capturedValue;
                        if (forbiddenBooleans.contains(booleanValue)) {
                            capturedParameterStupValues.set(i, !(Boolean) capturedValue);
                        }
                    }
                    if (capturedValue instanceof Character) {
                        final char charValue = (Character) capturedValue;
                        if (forbiddenCharacter.contains(charValue)) {
                            char newValue = Character.MIN_VALUE;
                            while (forbiddenCharacter.contains(newValue)) {
                                newValue++;
                            }
                            capturedParameterStupValues.set(i, newValue);
                            forbiddenCharacter.add(newValue);
                        }
                    }
                    if (capturedValue instanceof Number) {
                        Number numberValue = (Number) capturedValue;
                        if (numberValue.longValue() >= Byte.MIN_VALUE && numberValue.longValue() <= Byte.MAX_VALUE) {
                            byte byteValue = numberValue.byteValue();
                            if (forbiddenNumbers.contains(byteValue)) {
                                byte newValue = Byte.MIN_VALUE;
                                while (forbiddenNumbers.contains(newValue)) {
                                    newValue++;
                                }
                                if (capturedValue instanceof Byte) {
                                    capturedParameterStupValues.set(i, newValue);
                                } else if (capturedValue instanceof Short) {
                                    capturedParameterStupValues.set(i, (short) newValue);
                                } else if (capturedValue instanceof Integer) {
                                    capturedParameterStupValues.set(i, (int) newValue);
                                } else if (capturedValue instanceof Long) {
                                    capturedParameterStupValues.set(i, (long) newValue);
                                } else if (capturedValue instanceof Float) {
                                    capturedParameterStupValues.set(i, (float) newValue);
                                } else if (capturedValue instanceof Double) {
                                    capturedParameterStupValues.set(i, (double) newValue);
                                } else {
                                    throw new IllegalStateException("No case for class: " + capturedValue.getClass());
                                }
                                forbiddenNumbers.add(newValue);
                            }
                        }

                    }
                }
            }
            stubValuesGeneratedEarlier.addAll(capturedParameterStupValues);
        }

        currentBuilder = new InvocationExpectationBuilder();
        currentBuilder.setCardinality(cardinality);
        if (currentPosInBuilders < builders.size()) {
            if (builders.size() > 0) {
                currentBuilder.setBuildPhase(builders.get(builders.size() - 1).getBuildPhase());
            } else {
                throw new IllegalStateException();
            }
            builders.set(currentPosInBuilders, currentBuilder);
        } else {
            if (builders.size() > 0) {
                builders.get(builders.size() - 1).setBuildPhase(InvocationExpectationBuilder.BuildPhase.BUILD_FINISHED); // passed
            }
            currentBuilder.setBuildPhase(InvocationExpectationBuilder.BuildPhase.SEARCH_FOR_ACCESSIBLE_TYPES);
            builders.add(currentBuilder);
        }
    }

    public void buildExpectations(Action defaultAction, ExpectationCollector collector) {
        isBuildNow = true;
        build();
        isBuildNow = false;

        checkLastExpectationWasFullySpecified();

        for (InvocationExpectationBuilder builder : builders) {
            collector.add(builder.toExpectation(defaultAction));
        }
    }

    protected InvocationExpectationBuilder currentBuilder() {
        if (currentBuilder == null) {
            throw new IllegalStateException("no expectations have been specified " +
                    "(did you forget to to specify the cardinality of the first expectation?)");
        }
        return currentBuilder;
    }

    private void checkLastExpectationWasFullySpecified() {
        if (currentBuilder != null) {
            currentBuilder.checkWasFullySpecified();
        }
    }

    /* 
     * Syntactic sugar
     */

    public ReceiverClause exactly(int count) {
        checkWeBuildingNow();
        initialiseExpectationCapture(Cardinality.exactly(count));
        return currentBuilder;
    }

    // Makes the entire expectation more readable than one
    public <T> T oneOf(T mockObject) {
        checkWeBuildingNow();
        return exactly(1).of(mockObject);
    }

    /**
     * @deprecated Use {@link #oneOf(Object) oneOf} instead.
     */
    public <T> T one(T mockObject) {
        checkWeBuildingNow();
        return oneOf(mockObject);
    }

    public ReceiverClause atLeast(int count) {
        checkWeBuildingNow();
        initialiseExpectationCapture(Cardinality.atLeast(count));
        return currentBuilder;
    }

    public ReceiverClause between(int minCount, int maxCount) {
        checkWeBuildingNow();
        initialiseExpectationCapture(Cardinality.between(minCount, maxCount));
        return currentBuilder;
    }

    public ReceiverClause atMost(int count) {
        checkWeBuildingNow();
        initialiseExpectationCapture(Cardinality.atMost(count));
        return currentBuilder;
    }

    public MethodClause allowing(Matcher<?> mockObjectMatcher) {
        checkWeBuildingNow();
        return atLeast(0).of(mockObjectMatcher);
    }

    public <T> T allowing(T mockObject) {
        checkWeBuildingNow();
        return atLeast(0).of(mockObject);
    }

    public <T> T ignoring(T mockObject) {
        checkWeBuildingNow();
        return allowing(mockObject);
    }

    public MethodClause ignoring(Matcher<?> mockObjectMatcher) {
        checkWeBuildingNow();
        return allowing(mockObjectMatcher);
    }

    public <T> T never(T mockObject) {
        checkWeBuildingNow();
        return exactly(0).of(mockObject);
    }

    /**
     * Alternatively, use with.<T>is instead, which will work with untyped Hamcrest matchers
     */
    @SuppressWarnings({"unchecked"})
    public <T> T with(Matcher<T> matcher) {
        checkWeBuildingNow();
        final T objectFromWith = (T) getObjectFromWith();
        if (objectFromWith == null) {
            throw new AssertionError("Internal jmock error: zero was returned from getObjectFromWith");
        }
        currentBuilder().putParameterValueToMatcher(objectFromWith, matcher);
        return objectFromWith;
    }

    public <T> T with(T value) {
        checkWeBuildingNow();
        return with(equal(value));
    }

    public void will(Action... actions) {
        checkWeBuildingNow();
        if (actions.length == 1) {
            currentBuilder().setAction(actions[0]);
        } else {
            currentBuilder().setAction(doAll(actions));
        }

    }

    /* Common constraints
     */

    public static <T> Matcher<T> equal(T value) {
        return new IsEqual<T>(value);
    }

    public static <T> Matcher<T> same(T value) {
        return new IsSame<T>(value);
    }

    public static <T> Matcher<T> any(Class<T> type) {
        return CoreMatchers.any(type);
    }

    public static <T> Matcher<T> anything() {
        return new IsAnything<T>();
    }

    /**
     * @deprecated use {@link #aNonNull} or {@link #any} until type inference actually works in a future version of Java
     */
    @Deprecated
    public static Matcher<Object> a(Class<?> type) {
        return new IsInstanceOf(type);
    }

    /**
     * @deprecated use {@link #aNonNull} or {@link #any} until type inference actually works in a future version of Java
     */
    @Deprecated
    public static Matcher<Object> an(Class<?> type) {
        return new IsInstanceOf(type);
    }

    public static <T> Matcher<T> aNull(@SuppressWarnings("unused") Class<T> type) {
        return new IsNull<T>();
    }

    public static <T> Matcher<T> aNonNull(@SuppressWarnings("unused") Class<T> type) {
        return new IsNot<T>(new IsNull<T>());
    }

    /* Common actions
     */

    public static Action returnValue(Object result) {
        return new ReturnValueAction(result);
    }

    public static Action throwException(Throwable throwable) {
        return new ThrowAction(throwable);
    }

    public static Action returnIterator(Collection<?> collection) {
        return new ReturnIteratorAction(collection);
    }

    public static <T> Action returnIterator(T... items) {
        return new ReturnIteratorAction(items);
    }

    public static Action returnEnumeration(Collection<?> collection) {
        return new ReturnEnumerationAction(collection);
    }

    public static <T> Action returnEnumeration(T... items) {
        return new ReturnEnumerationAction(items);
    }

    public static Action doAll(Action... actions) {
        return new DoAllAction(actions);
    }

    public static Action onConsecutiveCalls(Action... actions) {
        return new ActionSequence(actions);
    }

    /* Naming and ordering
     */

    public void when(StatePredicate predicate) {
        checkWeBuildingNow();
        currentBuilder().addOrderingConstraint(new InStateOrderingConstraint(predicate));
    }

    public void then(State state) {
        checkWeBuildingNow();
        currentBuilder().addSideEffect(new ChangeStateSideEffect(state));
    }

    public void inSequence(Sequence sequence) {
        checkWeBuildingNow();
        currentBuilder().addInSequenceOrderingConstraint(sequence);
    }

    public void inSequences(Sequence... sequences) {
        checkWeBuildingNow();
        for (Sequence sequence : sequences) {
            inSequence(sequence);
        }
    }
}
