package org.jmock.internal;

import org.hamcrest.Matcher;
import org.jmock.api.Expectation;
import org.jmock.api.Invocation;
import org.jmock.lib.action.VoidAction;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static junit.framework.Assert.*;
import static org.jmock.Expectations.equal;
import static org.junit.Assert.assertTrue;

public class InvocationExpectationBuilderTest {
    /*
        possible workflows

        setCardinality() -> of(Matcher<?> objectMatcher) -> [method(regexp or matcher) -> [with() or withNoArguments()]] -> other

        setCardinality() -> of(MockObject (create new mock and register method listener)) -> [*putParameterValueToMatcher] -> createExpectationFrom(Invocation) -> other

        other == [setAction()] -> [*addOrderingConstraint()] -> [*addSideEffect] ->[*inSequence]
    */

    @Test
    public void testAllParameterIsActualValues() throws Throwable {
        final MockCaptureControl mockCaptureControl = new MockCaptureControl();


        final InvocationExpectationBuilder builder = new InvocationExpectationBuilder();
        builder.setCardinality(Cardinality.exactly(1));
        builder.of(mockCaptureControl);

        // imitate method invocation
        final Object object = "not-used-object";
        final Method method = TestData.class.getMethod("someMethod", String.class, String.class);
        mockCaptureControl.capture.createExpectationFrom(new Invocation(object, method, "value1", "value2"));

        final Expectation expectation = builder.toExpectation(new VoidAction());

        assertTrue(expectation.matches(new Invocation(mockCaptureControl, method, "value1", "value2")));
        assertFalse(expectation.matches(new Invocation(mockCaptureControl, method, "value3", "value4")));
    }

    @Test
    public void testAllParameterIsMatchers() throws Throwable {
        final MockCaptureControl mockCaptureControl = new MockCaptureControl();

        final Matcher matcher1 = equal("value1");
        final Matcher matcher2 = equal("value2");

        final Object generatedStubValueForMatcher1 = new String();
        final Object generatedStubValueForMatcher2 = new String();


        final InvocationExpectationBuilder builder = new InvocationExpectationBuilder();
        builder.setCardinality(Cardinality.exactly(1));
        builder.of(mockCaptureControl);
        builder.putParameterValueToMatcher(generatedStubValueForMatcher1, matcher1);// in order
        builder.putParameterValueToMatcher(generatedStubValueForMatcher2, matcher2);

        // imitate method invocation
        final Object object = "not-used-object";
        final Method method = TestData.class.getMethod("someMethod", String.class, String.class);
        mockCaptureControl.capture.createExpectationFrom(new Invocation(object, method, generatedStubValueForMatcher1, generatedStubValueForMatcher2));

        final Expectation expectation = builder.toExpectation(new VoidAction());

        assertTrue(expectation.matches(new Invocation(mockCaptureControl, method, "value1", "value2")));
        assertFalse(expectation.matches(new Invocation(mockCaptureControl, method, "value3", "value4")));
    }

    @Test
    public void testAllParameterIsMatchers_MoreThanTwoBooleans() throws Throwable {
        final MockCaptureControl mockCaptureControl = new MockCaptureControl();

        final Matcher matcher1 = equal(true);
        final Matcher matcher2 = equal(true);
        final Matcher matcher3 = equal(true);

        final Object generatedStubValueForMatcher1 = new Boolean(true);
        final Object generatedStubValueForMatcher2 = new Boolean(false);
        final Object generatedStubValueForMatcher3 = new Boolean(true); //non-unique value. If bool params more than two should be or all matchers or all actual params


        final InvocationExpectationBuilder builder = new InvocationExpectationBuilder();
        builder.setCardinality(Cardinality.exactly(1));
        builder.of(mockCaptureControl);
        builder.putParameterValueToMatcher(generatedStubValueForMatcher1, matcher1);
        builder.putParameterValueToMatcher(generatedStubValueForMatcher2, matcher2);
        builder.putParameterValueToMatcher(generatedStubValueForMatcher3, matcher3);

        // imitate method invocation
        final Object object = "not-used-object";
        final Method method = TestData.class.getMethod("methodWithManyBooleans", boolean.class, boolean.class, boolean.class);
        mockCaptureControl.capture.createExpectationFrom(new Invocation(object, method, generatedStubValueForMatcher1, generatedStubValueForMatcher2, generatedStubValueForMatcher3));

        final Expectation expectation = builder.toExpectation(new VoidAction());

        assertTrue(expectation.matches(new Invocation(mockCaptureControl, method, true, true, true)));
        assertFalse(expectation.matches(new Invocation(mockCaptureControl, method, true, false, true))); //does not match
    }

    @Test
    public void testMixParametersActualValueAndMatchers() throws Throwable {
        final MockCaptureControl mockCaptureControl = new MockCaptureControl();

        final Matcher matcher1 = equal("value1");

        final Object generatedStubValueForMatcher1 = new String();


        final InvocationExpectationBuilder builder = new InvocationExpectationBuilder();
        builder.setCardinality(Cardinality.exactly(1));
        builder.of(mockCaptureControl);
        builder.putParameterValueToMatcher(generatedStubValueForMatcher1, matcher1);// only for first

        // imitate method invocation
        final Object object = "not-used-object";
        final Method method = TestData.class.getMethod("someMethod", String.class, String.class);
        mockCaptureControl.capture.createExpectationFrom(new Invocation(object, method, generatedStubValueForMatcher1, "value2"));

        final Expectation expectation = builder.toExpectation(new VoidAction());

        assertTrue(expectation.matches(new Invocation(mockCaptureControl, method, "value1", "value2")));
        assertFalse(expectation.matches(new Invocation(mockCaptureControl, method, "value3", "value4")));
    }

    @Test
    public void testMixParametersActualValueAndMatchers_EqualTwoBooleans() throws Throwable {
        final MockCaptureControl mockCaptureControl = new MockCaptureControl();

        final Matcher matcher1 = equal(true);

        final Object generatedStubValueForMatcher1 = new Boolean(true);


        final InvocationExpectationBuilder builder = new InvocationExpectationBuilder();
        builder.setCardinality(Cardinality.exactly(1));
        builder.of(mockCaptureControl);
        builder.putParameterValueToMatcher(generatedStubValueForMatcher1, matcher1);

        // imitate method invocation
        final Object object = "not-used-object";
        final Method method = TestData.class.getMethod("methodWithTwoBooleans", boolean.class, boolean.class);

        try{
            mockCaptureControl.capture.createExpectationFrom(new Invocation(object, method, generatedStubValueForMatcher1, true));//duplicate generatedStubValueForMatcher1==true
            fail("DuplicatePrimitiveValuesFromWithAndFromActualParametersException must be thrown");
        }catch (InvocationExpectationBuilder.DuplicatePrimitiveValuesFromWithAndFromActualParametersException e){
            assertEquals(e.getDuplicateBooleans(), new HashSet<Boolean>(Arrays.asList(true)));
            assertEquals(e.getDuplicateChars(), Collections.<Character>emptySet());
            assertEquals(e.getDuplicateShorts(), Collections.<Short>emptySet());
            assertEquals(e.getDuplicateIntegers(), Collections.<Integer>emptySet());
            assertEquals(e.getDuplicateLongs(), Collections.<Long>emptySet());
            assertEquals(e.getDuplicateFloats(), Collections.<Float>emptySet());
            assertEquals(e.getDuplicateDoubles(), Collections.<Double>emptySet());
        }
    }


    //-----------------------------------------------------------------------------------------

    private static class MockCaptureControl implements CaptureControl {
        private ExpectationCapture capture;

        @Override
        public Object captureExpectationTo(ExpectationCapture capture) {
            assertNull("multiply assigment to capture", this.capture);
            this.capture = capture;
            return null;
        }
    }

    private static interface TestData {
        public void someMethod(String param1, String param2);

        public void methodWithManyBooleans(boolean arg1, boolean arg2, boolean arg3);

        public void methodWithTwoBooleans(boolean arg1, boolean arg2);
    }
}
