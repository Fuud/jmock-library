package org.jmock.lib.action;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

public class RememberParametersAction implements Action {
    private Object[] parameters;

    @Override
    public Object invoke(Invocation invocation) throws Throwable {
        parameters = invocation.getParametersAsArray();
        return null;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("remember parameters");
    }

    public <T> T get(int parameterNum){
        if (parameterNum>=parameters.length){
            throw new AssertionError("There is no enough parameters. You want to take "+parameterNum+" but there is only "+parameters.length+" parameters");
        }
        return (T) parameters[parameterNum];
    }
}
