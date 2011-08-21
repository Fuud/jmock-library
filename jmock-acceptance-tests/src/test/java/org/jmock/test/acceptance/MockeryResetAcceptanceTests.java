package org.jmock.test.acceptance;

import org.jmock.ExpectationsExt;
import org.jmock.Mockery;
import org.jmock.api.ExpectationError;
import org.junit.Test;
import testdata.MockedType;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class MockeryResetAcceptanceTests {
    Mockery mockery = new Mockery();

    MockedType mock = mockery.mock(MockedType.class, "mock");

    @Test
    public void testResetEmpty() throws Exception {
        mockery.reset();
        mockery.assertIsSatisfied();
        mockery.assertIsSatisfiedAndReset();
    }

    @Test
    public void testResetNotSatisfied() throws Exception {
        mockery.checking(new ExpectationsExt() {
            @Override
            protected void expect() throws Exception {
                oneOf(mock).doSomething();
            }
        });

        mockery.reset();
        mockery.assertIsSatisfied();
    }

    @Test
    public void testResetSatisfied() throws Exception {
        mockery.checking(new ExpectationsExt() {
            @Override
            protected void expect() throws Exception {
                allowing(mock).returnInt();
                will(returnValue(1));
            }
        });
        assertEquals(1, mock.returnInt());
        mockery.reset();

        mockery.checking(new ExpectationsExt() {
            @Override
            protected void expect() throws Exception {
                allowing(mock).returnInt();
                will(returnValue(2));
            }
        });
        assertEquals(2, mock.returnInt());
    }

    @Test
    public void testAssertAndResetNotSatisfied() throws Exception {
        mockery.checking(new ExpectationsExt() {
            @Override
            protected void expect() throws Exception {
                oneOf(mock).doSomething();
            }
        });

        try{
            mockery.assertIsSatisfiedAndReset();
            fail("should throw ExpectationError");
        }catch (ExpectationError e){}
    }
}
