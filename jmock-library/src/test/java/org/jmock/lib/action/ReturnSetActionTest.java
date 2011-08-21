package org.jmock.lib.action;


import org.jmock.api.Invocation;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

public class ReturnSetActionTest {
        private final Object object1 = new Object();
    private final Object object2 = new Object();
    private final Object object3 = new Object();
    private final Invocation invocation = new Invocation("not-used", null /*not-used*/);

    @Test
    public void testCollection() throws Throwable {
        final Set<?> result = new ReturnSetAction(Arrays.asList(object1, object2, object3)).invoke(invocation);
        assertEquals(new HashSet(Arrays.asList(object1, object2, object3)), result);
    }

    @Test
    public void testVarArgs() throws Throwable {
        final Set<?> result = new ReturnSetAction(object1, object2, object3).invoke(invocation);
        assertEquals(new HashSet(Arrays.asList(object1, object2, object3)), result);
    }
}
