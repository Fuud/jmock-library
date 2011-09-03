package org.jmock.lib.action;


import org.jmock.ExpectationsExt;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.jmock.lib.action.DelegateAction.delegateTo;

public class DelegateActionTest {
    private class Foo {
        public int sum(int... args) {
            int result = 0;
            for (int arg : args) {
                result += arg;
            }
            return result;
        }

        public void throwException(Exception e) throws Exception {
            throw e;
        }
    }


    private final Mockery mockery = new Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private final Foo mockFoo = mockery.mock(Foo.class);
    private final Foo foo = new Foo();

    @Before
    public void setUp() throws Exception {

        mockery.checking(new ExpectationsExt() {
            @Override
            protected void expect() throws Exception {
                allowing(mockFoo);
                will(delegateTo(foo));
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        mockery.assertIsSatisfied();
    }


    @Test
    public void shouldReturnValueFromOriginalObject() throws Exception {
        assertEquals(20, mockFoo.sum(10, 5, 5));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldRethrowExceptionsFromOriginalObject() throws Exception {
        mockFoo.throwException(new IllegalStateException());
    }
}
