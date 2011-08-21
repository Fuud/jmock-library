package org.jmock.lib.action;

import org.jmock.api.ExpectationError;
import org.junit.Test;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class CheckNotEDTActionTest {
    @Test
    public void shouldThrowExceptionIfCurrentThreadIsNotEDT() throws Throwable {
        new CheckNotEDTAction().invoke(null);
    }

    @Test(expected = ExpectationError.class)
    public void shouldNotThrowExceptionIfCurrentThreadIsEDT() throws Throwable {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try {
                        new CheckNotEDTAction().invoke(null);
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            throw e.getTargetException().getCause();
        }
    }
}
