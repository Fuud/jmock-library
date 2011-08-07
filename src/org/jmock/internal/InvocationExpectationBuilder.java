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
        if (BoxingUtils.isWrapperType(parameterValue.getClass())) {
            objectParametersValueToMatchers.put(parameterValue, parameterMatcher);
        } else {
            primitiveParametersValueToMatchers.put(parameterValue, parameterMatcher);
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

        Object capturingImposter = ((CaptureControl)mockObject).captureExpectationTo(this);

        return asMockedType(mockObject, capturingImposter);
    }

    @SuppressWarnings("unchecked")
    private <T> T asMockedType(@SuppressWarnings("unused") T mockObject,
                               Object capturingImposter)
    {
        return (T) capturingImposter;
    }

    public void createExpectationFrom(Invocation invocation) {
        expectation.setMethod(invocation.getInvokedMethod());

        List<Matcher<?>> parameterMatchers = new ArrayList<Matcher<?>>();

        for (Object parameterValue : invocation.getParametersAsArray()) {
            if (objectParametersValueToMatchers.containsKey(parameterValue)) {
                parameterMatchers.add(objectParametersValueToMatchers.get(parameterValue));
            }else if (primitiveParametersValueToMatchers.containsKey(parameterValue)){

            } else {
                parameterMatchers.add(Matchers.equalTo(parameterValue));
            }
        }

        expectation.setParametersMatcher(new AllParametersMatcher(parameterMatchers));
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
}
