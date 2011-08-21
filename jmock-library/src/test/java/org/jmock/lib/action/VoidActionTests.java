/*  Copyright (c) 2000-2004 jMock.org
 */
package org.jmock.lib.action;

import junit.framework.TestCase;

import org.hamcrest.StringDescription;
import org.jmock.api.Invocation;
import org.jmock.lib.action.VoidAction;
import org.jmock.support.AssertThat;
import org.jmock.support.MethodFactory;


public class VoidActionTests extends TestCase {
    Invocation invocation;
    VoidAction voidAction;

    @Override
    public void setUp() {
        MethodFactory methodFactory = new MethodFactory();
        invocation = new Invocation("INVOKED-OBJECT", methodFactory.newMethodReturning(void.class), new Object[0]);
        voidAction = new VoidAction();
    }

    public void testReturnsNullWhenInvoked() throws Throwable {
        assertNull("Should return null",
                   new VoidAction().invoke(invocation));
    }

    public void testIncludesVoidInDescription() {
        AssertThat.stringIncludes("contains 'void' in description",
            "void", StringDescription.toString(voidAction));
    }
}
