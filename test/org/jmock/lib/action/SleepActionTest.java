package org.jmock.lib.action;

import org.jmock.api.Invocation;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class SleepActionTest {
    private final Invocation invocation = new Invocation("not-used", null /*not-used*/);

    @Test
    public void test() throws Throwable {
        long startTime = System.currentTimeMillis();
        new SleepAction(1000).invoke(invocation);
        long finishTime = System.currentTimeMillis();

        assertThat(finishTime - startTime, greaterThan(900L));
    }
}
