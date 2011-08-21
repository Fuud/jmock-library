package org.jmock.lib.action;

import org.jmock.api.ExpectationError;
import org.junit.Test;

import javax.swing.*;

public class CheckIsEDTActionTest {
    @Test(expected = ExpectationError.class)
    public void shouldThrowExceptionIfCurrentThreadIsNotEDT() throws Throwable {
        new CheckIsEDTAction().invoke(null);
    }

    @Test
    public void shouldNotThrowExceptionIfCurrentThreadIsEDT() throws Throwable {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    new CheckIsEDTAction().invoke(null);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        });
    }
}
