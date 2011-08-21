package org.jmock.lib.action;

import org.jmock.api.Invocation;
import org.junit.Test;

import static junit.framework.Assert.assertSame;

public class RememberParametersActionTest {
    @Test
    public void testRemember() throws Throwable {
        final RememberParametersAction rememberParameters = new RememberParametersAction();
        Object arg1 = new Object();
        Object arg2 = new Object();
        final Invocation invocation = new Invocation("not-used", null /*not-used*/, arg1, arg2);
        rememberParameters.invoke(invocation);
        assertSame(arg1, rememberParameters.get(0));
        assertSame(arg2, rememberParameters.get(1));
    }
}
