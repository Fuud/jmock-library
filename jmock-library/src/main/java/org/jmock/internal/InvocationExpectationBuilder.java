package org.jmock.internal;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jmock.Sequence;
import org.jmock.api.Action;
import org.jmock.api.Expectation;
import org.jmock.api.Invocation;
import org.jmock.internal.matcher.AllParametersMatcher;
import org.jmock.internal.matcher.MethodNameMatcher;
import org.jmock.internal.matcher.MockObjectMatcher;
import org.jmock.syntax.MethodClause;
import org.jmock.syntax.ParametersClause;
import org.jmock.syntax.ReceiverClause;

import java.lang.reflect.Method;
import java.util.*;

public class InvocationExpectationBuilder
        implements ExpectationCapture,
        ReceiverClause, MethodClause, ParametersClause {
    private final InvocationExpectation expectation = new InvocationExpectation();

    private boolean isFullySpecified = false;
    private boolean needsDefaultAction = true;
    private List<Matcher<?>> capturedParameterMatchers = new ArrayList<Matcher<?>>();
    private List<Object> capturedParameterMatchersStupValues = new ArrayList<Object>();
    private Map<Object, Matcher<?>> objectParametersValueToMatchers = new IdentityHashMap<Object, Matcher<?>>(); // do not rely on equals on unknown objects
    private Map<Object, Matcher<?>> primitiveParametersValueToMatchers = new HashMap<Object, Matcher<?>>(); // boxing-unboxing breaks identity. Use equals.

    private Set<Boolean> forbiddenBooleans = new HashSet<Boolean>();
    private Set<Character> forbiddenCharacter = new HashSet<Character>();
    private Set<Byte> forbiddenNumbers = new HashSet<Byte>();

    private BuildPhase buildPhase;
    private boolean oldStyleBuilding = false;

    public Expectation toExpectation(Action defaultAction) {
        if (needsDefaultAction) {
            expectation.setDefaultAction(defaultAction);
        }

        return expectation;
    }

    public void setCardinality(Cardinality cardinality) {
        expectation.setCardinality(cardinality);
    }

    public void putParameterValueToMatcher(Object parameterValue, Matcher<?> parameterMatcher) {
        capturedParameterMatchers.add(parameterMatcher);
        capturedParameterMatchersStupValues.add(parameterValue);
        if (BoxingUtils.isWrapperType(parameterValue.getClass())) {
            primitiveParametersValueToMatchers.put(parameterValue, parameterMatcher);
        } else {
            objectParametersValueToMatchers.put(parameterValue, parameterMatcher);
        }
    }

    /**
     * Add captured matcher
     *
     * @param matcher matcher to put in list
     * @deprecated Do not use it. Use putParameterValueToMatcher instead.
     */
    @Deprecated()
    public void addParameterMatcher(Matcher<?> matcher) {
        oldStyleBuilding = true;
        capturedParameterMatchers.add(matcher);
    }

    public void addOrderingConstraint(OrderingConstraint constraint) {
        expectation.addOrderingConstraint(constraint);
    }

    public void addInSequenceOrderingConstraint(Sequence sequence) {
        sequence.constrainAsNextInSequence(expectation);
    }

    public void setAction(Action action) {
        expectation.setAction(action);
        needsDefaultAction = false;
    }

    public void addSideEffect(SideEffect sideEffect) {
        expectation.addSideEffect(sideEffect);
    }

    private <T> T captureExpectedObject(T mockObject) {
        if (!(mockObject instanceof CaptureControl)) {
            throw new IllegalArgumentException("can only set expectations on mock objects");
        }

        expectation.setObjectMatcher(new MockObjectMatcher(mockObject));
        isFullySpecified = true;

        Object capturingImposter = ((CaptureControl) mockObject).captureExpectationTo(this);

        return asMockedType(mockObject, capturingImposter);
    }

    @SuppressWarnings("unchecked")
    private <T> T asMockedType(@SuppressWarnings("unused") T mockObject,
                               Object capturingImposter) {
        return (T) capturingImposter;
    }

    public void createExpectationFrom(Invocation invocation) throws TooManyBooleansInMixParametersException, DuplicatePrimitiveValuesFromWithAndFromActualParametersException {
        expectation.setMethod(invocation.getInvokedMethod());

        List<Matcher<?>> parameterMatchers = new ArrayList<Matcher<?>>();

        if (capturedParameterMatchers.size() == invocation.getParameterCount()) {// all parameters is matchers
            expectation.setParametersMatcher(new AllParametersMatcher(capturedParameterMatchers));
        } else if (capturedParameterMatchers.isEmpty()) {
            expectation.setParametersMatcher(new AllParametersMatcher(invocation.getParametersAsArray()));
        } else if (oldStyleBuilding) {
            throw new IllegalArgumentException("not all parameters were given explicit matchers: either all parameters must be specified by matchers or all must be specified by values, you cannot mix matchers and values");
        } else {
            checkForBooleans(invocation);
            checkForDuplicates(invocation);
            for (Object parameterValue : invocation.getParametersAsArray()) {
                if (objectParametersValueToMatchers.containsKey(parameterValue)) {
                    parameterMatchers.add(objectParametersValueToMatchers.get(parameterValue));
                } else if (primitiveParametersValueToMatchers.containsKey(parameterValue)) {
                    parameterMatchers.add(primitiveParametersValueToMatchers.get(parameterValue));
                } else {
                    parameterMatchers.add(Matchers.equalTo(parameterValue));
                }
            }
            expectation.setParametersMatcher(new AllParametersMatcher(parameterMatchers));
        }
    }

    private void checkForDuplicates(Invocation invocation) throws DuplicatePrimitiveValuesFromWithAndFromActualParametersException {
        Set<Boolean> duplicateBooleans = new HashSet<Boolean>();
        Set<Byte> duplicateNumbers = new HashSet<Byte>();
        Set<Character> duplicateCharacters = new HashSet<Character>();

        final Object[] parameterValues = invocation.getParametersAsArray();
        for (int i = 0; i < parameterValues.length; i++) {
            Object value1 = parameterValues[i];
            if (!primitiveParametersValueToMatchers.containsKey(value1)) {
                continue;
            }
            for (int j = i + 1; j < parameterValues.length; j++) {
                Object value2 = parameterValues[j];
                if (value1.equals(value2)) {
                    if (value1 instanceof Boolean) {
                        duplicateBooleans.add((Boolean) value1);
                    } else if (value1 instanceof Character) {
                        duplicateCharacters.add((Character) value1);
                    } else if (value1 instanceof Number) {
                        final Number number = (Number) value1;
                        if (number.longValue() < Byte.MIN_VALUE || number.longValue() > Byte.MAX_VALUE) {
                            throw new IllegalStateException("Primitive number values for with() clause should be in byte range");
                        }
                        duplicateNumbers.add(number.byteValue());
                    }
                }
            }
        }

        forbiddenBooleans.addAll(duplicateBooleans);
        forbiddenCharacter.addAll(duplicateCharacters);
        forbiddenNumbers.addAll(duplicateNumbers);

        for (Object parameterValue : parameterValues) {
            if (primitiveParametersValueToMatchers.containsKey(parameterValue)) {//already added if duplicate
                continue;
            }
            if (BoxingUtils.isWrapperType(parameterValue.getClass())) {
                if (parameterValue instanceof Boolean) {
                    forbiddenBooleans.add((Boolean) parameterValue);
                } else if (parameterValue instanceof Character) {
                    forbiddenCharacter.add((Character) parameterValue);
                } else if (parameterValue instanceof Number) {
                    final Number number = (Number) parameterValue;
                    if (number.longValue() >= Byte.MIN_VALUE && number.longValue() <= Byte.MAX_VALUE) {
                        forbiddenNumbers.add(number.byteValue());
                    }
                }
            }
        }

        if (!duplicateBooleans.isEmpty() || !duplicateNumbers.isEmpty() || !duplicateCharacters.isEmpty()) {
            throw new DuplicatePrimitiveValuesFromWithAndFromActualParametersException();
        }
    }

    private void checkForBooleans(Invocation invocation) throws TooManyBooleansInMixParametersException {
        boolean booleanMatcherExists = primitiveParametersValueToMatchers.containsKey(true) || primitiveParametersValueToMatchers.containsKey(false);
        if (!booleanMatcherExists) {
            return;
        }
        int booleanParamsCount = 0;
        for (Class<?> parameterClass : invocation.getInvokedMethod().getParameterTypes()) {
            if (parameterClass.equals(Boolean.class) || parameterClass.equals(boolean.class)) {
                booleanParamsCount++;
            }
        }
        if (booleanParamsCount > 2) {
            throw new TooManyBooleansInMixParametersException();
        }
    }

    public void checkWasFullySpecified() {
        if (!isFullySpecified) {
            throw new IllegalStateException("expectation was not fully specified");
        }
    }

    /* 
     * Syntactic sugar
     */

    public <T> T of(T mockObject) {
        return captureExpectedObject(mockObject);
    }

    public MethodClause of(Matcher<?> objectMatcher) {
        expectation.setObjectMatcher(objectMatcher);
        isFullySpecified = true;
        return this;
    }

    public ParametersClause method(Matcher<Method> methodMatcher) {
        expectation.setMethodMatcher(methodMatcher);
        return this;
    }

    public ParametersClause method(String nameRegex) {
        return method(new MethodNameMatcher(nameRegex));
    }

    public void with(Matcher<?>... parameterMatchers) {
        expectation.setParametersMatcher(new AllParametersMatcher(Arrays.asList(parameterMatchers)));
    }

    public void withNoArguments() {
        with();
    }

    public List<Object> getCapturedParameterMatchersStupValues() {
        return Collections.unmodifiableList(capturedParameterMatchersStupValues);
    }

    public Set<Boolean> getForbiddenBooleans() {
        return Collections.unmodifiableSet(forbiddenBooleans);
    }

    public Set<Character> getForbiddenCharacter() {
        return Collections.unmodifiableSet(forbiddenCharacter);
    }

    public Set<Byte> getForbiddenNumbers() {
        return Collections.unmodifiableSet(forbiddenNumbers);
    }

    public BuildPhase getBuildPhase() {
        return buildPhase;
    }

    public void setBuildPhase(BuildPhase buildPhase) {
        this.buildPhase = buildPhase;
    }

    public static final class TooManyBooleansInMixParametersException extends RuntimeException {
        public TooManyBooleansInMixParametersException() {
            super("If you mix boolean matchers and boolean actual values, there should not be more that 2 boolean argument");
        }
    }

    public static final class DuplicatePrimitiveValuesFromWithAndFromActualParametersException extends RuntimeException {
    }

    public enum BuildPhase {
        SEARCH_FOR_ACCESSIBLE_TYPES,
        SEARCH_FOR_VALUES,
        BUILD_FINISHED
    }
}
