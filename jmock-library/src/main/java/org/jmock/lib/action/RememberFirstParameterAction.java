package org.jmock.lib.action;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

public class RememberFirstParameterAction<T> implements Action {
    private final RememberParametersAction rpa = new RememberParametersAction();
    @Override
    public Object invoke(Invocation invocation) throws Throwable {
       return rpa.invoke(invocation);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("remeber first parameter");
    }

    public T get(){
        return rpa.<T>get(0);
    }
}
