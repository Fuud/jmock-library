package org.jmock.lib.action;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

import java.util.*;

public class ReturnSetAction implements Action {
    private Set<?> collection;

    public ReturnSetAction(Collection<?> collection) {
        this.collection = new HashSet<Object>(collection);
    }

    public ReturnSetAction(Object... array) {
        this(Arrays.asList(array));
    }

    public Set<?> invoke(Invocation invocation) throws Throwable {
        return new HashSet<Object>(collection); //copy
    }

    public void describeTo(Description description) {
        description.appendValueList("return set over ", ", ", "", collection);
    }
}
