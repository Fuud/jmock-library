package org.jmock.lib.action;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;
import org.jmock.api.Action;
import org.jmock.api.ExpectationError;
import org.jmock.api.Invocation;

import javax.swing.*;

public class CheckIsEDTAction implements Action{
    @Override
    public Object invoke(Invocation invocation) throws Throwable {
        if (!SwingUtilities.isEventDispatchThread()){
            final String message = "Should be invoked in EDT thread";
            throw new ExpectationError(message, new SelfDescribing() {
                @Override
                public void describeTo(Description description) {
                    description.appendText(message);
                }
            });
        }
        return null;
    }

    @Override
    public void describeTo(Description description) {
       description.appendText("check current thread is EDT");
    }
}
