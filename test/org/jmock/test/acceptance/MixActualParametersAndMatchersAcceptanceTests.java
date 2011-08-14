package org.jmock.test.acceptance;

import junit.framework.TestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

public class MixActualParametersAndMatchersAcceptanceTests extends TestCase {
    public interface TestData {
        void method1(int integer1, Boolean boolean2, Runnable runnable3);

        void method2(Byte byte0, byte byte1, Byte byte2, byte byte3, Byte byte4, byte byte5, Byte byte6, byte byte7, Byte byte8, byte byte9, Byte byte10, byte byte11, Byte byte12, byte byte13, Byte byte14, byte byte15, Byte byte16, byte byte17, Byte byte18, byte byte19, Byte byte20, byte byte21, Byte byte22, byte byte23, Byte byte24, byte byte25);

        void method3(Byte byte0, byte byte1, Byte byte2, byte byte3, Byte byte4, byte byte5, Byte byte6, byte byte7, Byte byte8, byte byte9, Byte byte10, byte byte11, Byte byte12, byte byte13, Byte byte14, byte byte15, Byte byte16, byte byte17, Byte byte18, byte byte19, Byte byte20, byte byte21, Byte byte22, byte byte23, Byte byte24, byte byte25);
    }

    private final Mockery mockery = new Mockery();
    private final TestData testData = mockery.mock(TestData.class);

    private final int expectedInteger1 = 1;
    private final boolean expectedBoolean2 = true;
    private final Runnable expectedRunnable = new Runnable() {
        public void run() {
        }
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

    @Test
    public void testManyValues() throws Exception {
        mockery.checking(new Expectations() {
            protected void expect() throws Exception {
                oneOf(testData).method2((byte) 0, (byte) 1, with(equal((byte) 2)), with(equal((byte) 3)), with(equal((byte) 4)), with(equal((byte) 5)), with(equal((byte) 6)), with(equal((byte) 7)), with(equal((byte) 8)), with(equal((byte) 9)), with(equal((byte) 10)), with(equal((byte) 11)), with(equal((byte) 12)), with(equal((byte) 13)), with(equal((byte) 14)), with(equal((byte) 15)), with(equal((byte) 16)), with(equal((byte) 17)), with(equal((byte) 18)), with(equal((byte) 19)), with(equal((byte) 20)), with(equal((byte) 21)), with(equal((byte) 22)), with(equal((byte) 23)), with(equal((byte) 24)), with(equal((byte) 25)));
            }
        });

        testData.method2((byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testManyCheckings() throws Exception {
        mockery.checking(new Expectations() {
            protected void expect() throws Exception {
                oneOf(testData).method2((byte) 0, (byte) 1, with(equal((byte) 2)), with(equal((byte) 3)), with(equal((byte) 4)), with(equal((byte) 5)), with(equal((byte) 6)), with(equal((byte) 7)), with(equal((byte) 8)), with(equal((byte) 9)), with(equal((byte) 10)), with(equal((byte) 11)), with(equal((byte) 12)), with(equal((byte) 13)), with(equal((byte) 14)), with(equal((byte) 15)), with(equal((byte) 16)), with(equal((byte) 17)), with(equal((byte) 18)), with(equal((byte) 19)), with(equal((byte) 20)), with(equal((byte) 21)), with(equal((byte) 22)), with(equal((byte) 23)), with(equal((byte) 24)), with(equal((byte) 25)));
                oneOf(testData).method3((byte) 25, (byte) 24, with(equal((byte) 23)), with(equal((byte) 22)), with(equal((byte) 21)), with(equal((byte) 20)), with(equal((byte) 19)), with(equal((byte) 18)), with(equal((byte) 17)), with(equal((byte) 16)), with(equal((byte) 15)), with(equal((byte) 14)), with(equal((byte) 13)), with(equal((byte) 12)), with(equal((byte) 11)), with(equal((byte) 10)), with(equal((byte) 9)), with(equal((byte) 8)), with(equal((byte) 7)), with(equal((byte) 6)), with(equal((byte) 5)), with(equal((byte) 4)), with(equal((byte) 3)), with(equal((byte) 2)), with(equal((byte) 1)), with(equal((byte) 0)));

            }
        });

        testData.method2((byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 25);
        testData.method3((byte) 25, (byte) 24, (byte) 23, (byte) 22, (byte) 21, (byte) 20, (byte) 19, (byte) 18, (byte) 17, (byte) 16, (byte) 15, (byte) 14, (byte) 13, (byte) 12, (byte) 11, (byte) 10, (byte) 9, (byte) 8, (byte) 7, (byte) 6, (byte) 5, (byte) 4, (byte) 3, (byte) 2, (byte) 1, (byte) 0);

        mockery.assertIsSatisfied();
    }
}
