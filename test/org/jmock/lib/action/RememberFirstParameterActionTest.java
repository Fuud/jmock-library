package org.jmock.lib.action;

import org.jmock.api.Invocation;
import org.jmock.example.announcer.AnnouncerTests;
import org.junit.Test;

import static junit.framework.Assert.assertSame;

public class RememberFirstParameterActionTest {
    @Test
    public void testRemember() throws Throwable {
        final RememberFirstParameterAction<Object> rememberAction = new RememberFirstParameterAction<Object>();
        Object arg1 = new Object();
        Object arg2 = new Object();
        final Invocation invocation = new Invocation("not-used", null /*not-used*/, arg1, arg2);
        rememberAction.invoke(invocation);
        assertSame(arg1, rememberAction.get());
    }
}
