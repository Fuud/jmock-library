package org.jmock.test.acceptance;

import org.jmock.ExpectationsExt;
import org.jmock.Mockery;
import org.jmock.api.ExpectationError;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Test;
import testdata.MockedType;

import static junit.framework.Assert.fail;

public class MockeryTimeoutsAcceptanceTests {
    Mockery mockery = new Mockery(){{
        setThreadingPolicy(new Synchroniser());
    }};

    MockedType mock = mockery.mock(MockedType.class, "mock");


    @Test(timeout = 1000)
    public void testWaitUntilSatisfaction() throws Exception{
        mockery.checking(new ExpectationsExt() {
            @Override
            protected void expect() throws Exception {
                oneOf(mock).doSomething();
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep(500);
                mock.doSomething();//unexpected
            }
        });

        thread.start();

        mockery.waitForSatisfaction();

        mockery.assertIsSatisfied();
    }

    @Test(timeout = 1000)
    public void testExitImmediatelyIfErrorAlreadyOccurred() throws Exception{
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                mock.doSomething();//unexpected
            }
        });

        thread.start();
        thread.join();

        try{
            mockery.waitForSatisfaction();
            fail("ExpectationError was expected");
        }catch (ExpectationError e){}
    }

    @Test(timeout = 10000)
    public void testExitWithErrorIfErrorWillThrown() throws Exception{
        mockery.checking(new ExpectationsExt() {
            @Override
            protected void expect() throws Exception {
                oneOf(mock).method1();
            }
        });

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                sleep(5000);
                mock.doSomething();//unexpected
            }
        });

        thread.start();

        try{
            mockery.waitForSatisfaction();
            fail("ExpectationError was expected");
        }catch (ExpectationError e){}
    }

    //-------------------------------------------------------------------------

    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }
}
