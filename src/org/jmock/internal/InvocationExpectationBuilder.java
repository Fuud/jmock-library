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
    private Map<Object, Matcher<?>> objectParametersValueToMatchers = new IdentityHashMap<Object, Matcher<?>>(); // do not rely on equals on unknown objects
    private Map<Object, Matcher<?>> primitiveParametersValueToMatchers = new HashMap<Object, Matcher<?>>(); // boxing-unboxing breaks identity. Use equals.

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
        if (BoxingUtils.isWrapperType(parameterValue.getClass())) {
            primitiveParametersValueToMatchers.put(parameterValue, parameterMatcher);
        } else {
            objectParametersValueToMatchers.put(parameterValue, parameterMatcher);
        }
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

    public void createExpectationFrom(Invocation invocation) throws TooManyBooleansInMixParametersException {
        expectation.setMethod(invocation.getInvokedMethod());

        List<Matcher<?>> parameterMatchers = new ArrayList<Matcher<?>>();

        if (capturedParameterMatchers.size() == invocation.getParameterCount()) {// all parameters is matchers
            expectation.setParametersMatcher(new AllParametersMatcher(capturedParameterMatchers));
        } else if (capturedParameterMatchers.isEmpty()) {
            expectation.setParametersMatcher(new AllParametersMatcher(invocation.getParametersAsArray()));
        } else {
            checkForBooleans(invocation);
            for (Object parameterValue : invocation.getParametersAsArray()) {
                if (objectParametersValueToMatchers.containsKey(parameterValue)) {
                    parameterMatchers.add(objectParametersValueToMatchers.get(parameterValue));
                } else if (primitiveParametersValueToMatchers.containsKey(parameterValue)) {

                } else {
                    parameterMatchers.add(Matchers.equalTo(parameterValue));
                }
            }
            expectation.setParametersMatcher(new AllParametersMatcher(parameterMatchers));
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

    public static final class TooManyBooleansInMixParametersException extends Exception {
        public TooManyBooleansInMixParametersException() {
            super("If you mix boolean matchers and boolean actual values, there should not be more that 2 boolean argument");
        }
    }

    public static final class DuplicatePrimitiveValuesFromWithAndFromActualParametersException extends Exception {
        private final Set<Boolean> duplicateBooleans;
        private final Set<Character> duplicateChars;
        private final Set<Short> duplicateShorts;
        private final Set<Integer> duplicateIntegers;
        private final Set<Long> duplicateLongs;
        private final Set<Float> duplicateFloats;
        private final Set<Double> duplicateDoubles;

        public DuplicatePrimitiveValuesFromWithAndFromActualParametersException(
                Set<Boolean> duplicateBooleans,
                Set<Character> duplicateChars,
                Set<Short> duplicateShorts,
                Set<Integer> duplicateIntegers,
                Set<Long> duplicateLongs,
                Set<Float> duplicateFloats,
                Set<Double> duplicateDoubles) {
            this.duplicateBooleans = duplicateBooleans;
            this.duplicateChars = duplicateChars;
            this.duplicateShorts = duplicateShorts;
            this.duplicateIntegers = duplicateIntegers;
            this.duplicateLongs = duplicateLongs;
            this.duplicateFloats = duplicateFloats;
            this.duplicateDoubles = duplicateDoubles;
        }

        public Set<Boolean> getDuplicateBooleans() {
            return duplicateBooleans;
        }

        public Set<Character> getDuplicateChars() {
            return duplicateChars;
        }

        public Set<Short> getDuplicateShorts() {
            return duplicateShorts;
        }

        public Set<Integer> getDuplicateIntegers() {
            return duplicateIntegers;
        }

        public Set<Long> getDuplicateLongs() {
            return duplicateLongs;
        }

        public Set<Float> getDuplicateFloats() {
            return duplicateFloats;
        }

        public Set<Double> getDuplicateDoubles() {
            return duplicateDoubles;
        }
    }
}
