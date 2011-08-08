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

@SuppressWarnings({"unchecked"})
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
            assertEquals(new HashSet<Boolean>(Arrays.asList(true)), e.getDuplicateBooleans());
            assertEquals(Collections.<Byte>emptySet(), e.getDuplicateNumbers());
            assertEquals(Collections.<Character>emptySet(), e.getDuplicateCharacters());
        }
    }

    @Test
    public void testMixParametersActualValueAndMatchers_EqualTwoChars() throws Throwable {

        final MockCaptureControl mockCaptureControl = new MockCaptureControl();

        final Matcher matcher1 = equal('x');

        final Object generatedStubValueForMatcher1 = 'x';


        final InvocationExpectationBuilder builder = new InvocationExpectationBuilder();
        builder.setCardinality(Cardinality.exactly(1));
        builder.of(mockCaptureControl);
        builder.putParameterValueToMatcher(generatedStubValueForMatcher1, matcher1);

        // imitate method invocation
        final Object object = "not-used-object";
        final Method method = TestData.class.getMethod("methodWithTwoChars", char.class, char.class);

        try{
            mockCaptureControl.capture.createExpectationFrom(new Invocation(object, method, generatedStubValueForMatcher1, 'x'));//duplicate generatedStubValueForMatcher1=='x'
            fail("DuplicatePrimitiveValuesFromWithAndFromActualParametersException must be thrown");
        }catch (InvocationExpectationBuilder.DuplicatePrimitiveValuesFromWithAndFromActualParametersException e){
            assertEquals(Collections.<Boolean>emptySet(), e.getDuplicateBooleans());
            assertEquals(Collections.<Byte>emptySet(), e.getDuplicateNumbers());
            assertEquals(new HashSet(Arrays.asList('x')), e.getDuplicateCharacters());
        }
    }

    @Test
    public void testMixParametersActualValueAndMatchers_EqualTwoBytes() throws Throwable {

        final MockCaptureControl mockCaptureControl = new MockCaptureControl();

        final Matcher matcher1 = equal(2);

        final Object generatedStubValueForMatcher1 = (byte)2;


        final InvocationExpectationBuilder builder = new InvocationExpectationBuilder();
        builder.setCardinality(Cardinality.exactly(1));
        builder.of(mockCaptureControl);
        builder.putParameterValueToMatcher(generatedStubValueForMatcher1, matcher1);

        // imitate method invocation
        final Object object = "not-used-object";
        final Method method = TestData.class.getMethod("methodWithTwoChars", char.class, char.class);

        try{
            mockCaptureControl.capture.createExpectationFrom(new Invocation(object, method, generatedStubValueForMatcher1, (byte)2));//duplicate generatedStubValueForMatcher1==2
            fail("DuplicatePrimitiveValuesFromWithAndFromActualParametersException must be thrown");
        }catch (InvocationExpectationBuilder.DuplicatePrimitiveValuesFromWithAndFromActualParametersException e){
            assertEquals(Collections.<Boolean>emptySet(), e.getDuplicateBooleans());
            assertEquals(new HashSet(Arrays.asList((byte)2)), e.getDuplicateNumbers());
            assertEquals(Collections.<Character>emptySet(), e.getDuplicateCharacters());
        }
    }

    @Test
    public void testParametersFromWithShouldBeInOrder() throws Throwable {

        final Object object1 = new Object();
        final Integer object2 = 3;
        final Object object3 = new Object();

        final MockCaptureControl mockCaptureControl = new MockCaptureControl();
        final InvocationExpectationBuilder builder = new InvocationExpectationBuilder();
        builder.setCardinality(Cardinality.exactly(1));
        builder.of(mockCaptureControl);
        builder.putParameterValueToMatcher(object1, equal(true));
        builder.putParameterValueToMatcher(object2, equal(true));
        builder.putParameterValueToMatcher(object3, equal(true));

        assertEquals(object1, builder.getCapturedParameterMatchersStupValues().get(0));
        assertEquals(object2, builder.getCapturedParameterMatchersStupValues().get(1));
        assertEquals(object3, builder.getCapturedParameterMatchersStupValues().get(2));
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

    @SuppressWarnings({"UnusedDeclaration"})
    private static interface TestData {
        public void someMethod(String param1, String param2);

        public void methodWithManyBooleans(boolean arg1, boolean arg2, boolean arg3);

        public void methodWithTwoBooleans(boolean arg1, boolean arg2);

        public void methodWithTwoChars(char arg1, char arg2);

        public void methodWithTwoBytes(byte arg1, byte arg2);
    }
}
