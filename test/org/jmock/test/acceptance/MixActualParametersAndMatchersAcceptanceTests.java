package org.jmock.test.acceptance;

import junit.framework.TestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

public class MixActualParametersAndMatchersAcceptanceTests  extends TestCase {
    public interface TestData {
        void method1(int integer1, Boolean boolean2, Runnable runnable3);
    }

    private final Mockery mockery = new Mockery();
    private final TestData testData = mockery.mock(TestData.class);

    private final int expectedInteger1 = 1;
    private final boolean expectedBoolean2 = true;
    private final Runnable expectedRunnable = new Runnable() {
        public void run() {}
    };


    @Test
    public void testPrimitiveIsMatches() throws Exception {
        mockery.checking(new Expectations() {
            protected void expect() throws Exception {
                oneOf(testData).method1(with(equal(expectedInteger1)), expectedBoolean2, expectedRunnable);
            }
        });

        testData.method1(expectedInteger1, expectedBoolean2, expectedRunnable);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testWrappedPrimitiveIsMatches() throws Exception {
        mockery.checking(new Expectations() {
            protected void expect() throws Exception {
                oneOf(testData).method1(expectedInteger1, with(equal(expectedBoolean2)), expectedRunnable);
            }
        });

        testData.method1(expectedInteger1, expectedBoolean2, expectedRunnable);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testObjectsIsMatches() throws Exception {
        mockery.checking(new Expectations() {
            protected void expect() throws Exception {
                oneOf(testData).method1(expectedInteger1, expectedBoolean2, with(equal(expectedRunnable)));
            }
        });

        testData.method1(expectedInteger1, expectedBoolean2, expectedRunnable);

        mockery.assertIsSatisfied();
    }
}
