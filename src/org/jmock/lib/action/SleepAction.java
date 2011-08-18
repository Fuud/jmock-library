package org.jmock.lib.action;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

public class SleepAction implements Action {
    private final long milliseconds;

    public SleepAction(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    @Override
    public Object invoke(Invocation invocation) throws Throwable {
        Thread.sleep(milliseconds);
        return null;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("sleep for " + milliseconds + " ms");
    }
}
