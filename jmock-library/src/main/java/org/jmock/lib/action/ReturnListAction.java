package org.jmock.lib.action;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

import java.util.*;

public class ReturnListAction implements Action {
    private List<?> collection;

    public ReturnListAction(Collection<?> collection) {
        this.collection = new ArrayList<Object>(collection);
    }

    public ReturnListAction(Object... array) {
        this(Arrays.asList(array));
    }

    public List<?> invoke(Invocation invocation) throws Throwable {
        return new ArrayList<Object>(collection); //copy
    }

    public void describeTo(Description description) {
        description.appendValueList("return set over ", ", ", "", collection);
    }
}
