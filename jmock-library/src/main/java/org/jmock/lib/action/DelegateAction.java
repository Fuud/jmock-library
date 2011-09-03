package org.jmock.lib.action;


import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DelegateAction implements Action {
    private final Object delegate;

    public DelegateAction(final Object delegate) {
        this.delegate = delegate;
    }

    public static DelegateAction delegateTo(final Object delegate){
        return new DelegateAction(delegate);
    }

    public Object invoke(Invocation invocation) throws Throwable {
        final Method method = invocation.getInvokedMethod();
        final Object[] parameters = invocation.getParametersAsArray();
        method.setAccessible(true);
        try {
            return method.invoke(delegate, parameters);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public void describeTo(Description description) {
        description.appendText("delegate invocation to " + delegate);
    }
}
