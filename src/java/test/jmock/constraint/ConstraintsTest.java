/* Copyright (c) 2000-2003, jMock.org. See LICENSE.txt */
package test.jmock.constraint;

import java.util.EventObject;

import junit.framework.TestCase;

import org.jmock.Constraint;
import org.jmock.constraint.And;
import org.jmock.constraint.IsAnything;
import org.jmock.constraint.IsCloseTo;
import org.jmock.constraint.IsEventFrom;
import org.jmock.constraint.IsGreaterThan;
import org.jmock.constraint.IsInstanceOf;
import org.jmock.constraint.IsLessThan;
import org.jmock.constraint.IsNot;
import org.jmock.constraint.IsNull;
import org.jmock.constraint.IsSame;
import org.jmock.constraint.Or;
import org.jmock.constraint.StringContains;

public class ConstraintsTest extends TestCase {
    class True implements Constraint {
        public boolean eval(Object o) {
            return true;
        }
    }

    class False implements Constraint {
        public boolean eval(Object o) {
            return false;
        }
    }

    /**
     * Creates a new instance of Test_Predicates
     */
    public ConstraintsTest(String test) {
        super(test);
    }

    public void testIsNull() {
        Constraint p = new IsNull();

        assertTrue(p.eval(null));
        assertTrue(!p.eval(new Object()));
    }

    public void testIsSame() {
        Object o1 = new Object();
        Object o2 = new Object();
        Constraint p = new IsSame(o1);

        assertTrue(p.eval(o1));
        assertTrue(!p.eval(o2));
    }

    public void testIsGreaterThan() {
        Constraint p = new IsGreaterThan(new Integer(1));

        assertTrue(!p.eval(new Integer(0)));
        assertTrue(!p.eval(new Integer(1)));
        assertTrue(p.eval(new Integer(2)));
    }

    public void testIsLessThan() {
        Constraint p = new IsLessThan(new Integer(1));

        assertTrue(p.eval(new Integer(0)));
        assertTrue(!p.eval(new Integer(1)));
        assertTrue(!p.eval(new Integer(2)));
    }

    public void testIsAnything() {
        Constraint p = new IsAnything();
        assertTrue(p.eval(null));
        assertTrue(p.eval(new Object()));
    }

    public void testIsInstanceOf() {
        Constraint p = new IsInstanceOf(Number.class);
        assertTrue(p.eval(new Integer(1)));
        assertTrue(p.eval(new Double(1.0)));
        assertTrue(!p.eval("a string"));
        assertTrue(!p.eval(null));
    }

    public void testIsNot() {
        Constraint p = new IsNot(new True());
        assertTrue(!p.eval(null));
        assertTrue(!p.eval(new Object()));
    }

    public void testAnd() {
        Object o = new Object();
        assertTrue(new And(new True(), new True()).eval(o));
        assertTrue(!new And(new False(), new True()).eval(o));
        assertTrue(!new And(new True(), new False()).eval(o));
        assertTrue(!new And(new False(), new False()).eval(o));
    }

    public void testOr() {
        Object o = new Object();
        assertTrue(new Or(new True(), new True()).eval(o));
        assertTrue(new Or(new False(), new True()).eval(o));
        assertTrue(new Or(new True(), new False()).eval(o));
        assertTrue(!new Or(new False(), new False()).eval(o));
    }

    public void testIsEventFrom() {
        Object o = new Object();
        EventObject ev = new EventObject(o);
        EventObject ev2 = new EventObject(new Object());

        Constraint p = new IsEventFrom(o);

        assertTrue(p.eval(ev));
        assertTrue("p should eval to false for an event not from o",
                !p.eval(ev2));
        assertTrue("p should eval to false for objects that are not events",
                !p.eval(o));
    }

    private static class DerivedEvent extends EventObject {
        public DerivedEvent(Object source) {
            super(source);
        }
    }

    public void testIsEventSubtypeFrom() {
        Object o = new Object();
        DerivedEvent good_ev = new DerivedEvent(o);
        DerivedEvent wrong_source = new DerivedEvent(new Object());
        EventObject wrong_type = new EventObject(o);
        EventObject wrong_source_and_type = new EventObject(new Object());

        Constraint p = new IsEventFrom(DerivedEvent.class, o);

        assertTrue(p.eval(good_ev));
        assertTrue("p should eval to false for an event not from o",
                !p.eval(wrong_source));
        assertTrue("p should eval to false for an event of the wrong type",
                !p.eval(wrong_type));
        assertTrue("p should eval to false for an event of the wrong type " +
                "and from the wrong source",
                !p.eval(wrong_source_and_type));
    }

    public void testIsCloseTo() {
        Constraint p = new IsCloseTo(1.0, 0.5);

        assertTrue(p.eval(new Double(1.0)));
        assertTrue(p.eval(new Double(0.5)));
        assertTrue(p.eval(new Double(1.5)));

        assertTrue(p.eval(new Float(1.0)));
        assertTrue(p.eval(new Integer(1)));

        assertTrue("number too large", !p.eval(new Double(2.0)));
        assertTrue("number too small", !p.eval(new Double(0.0)));

        try {
            p.eval("wrong type");
            fail("ClassCastException expected for wrong type of argument");
        } catch (ClassCastException ex) {
            // expected
        }
    }

    public void testStringContains() {
    	final String EXCERPT = "EXCERPT";
    	Constraint p = new StringContains(EXCERPT);
    	
    	assertTrue( "should be true if excerpt is entire string", p.eval(EXCERPT) );
    	assertTrue( "should be true if excerpt at beginning", p.eval(EXCERPT+"END") );
    	assertTrue( "should be true if excerpt at end", p.eval("START"+EXCERPT) );
    	assertTrue( "should be true if excerpt in middle", p.eval("START"+EXCERPT+"END") );
    	assertTrue( "should be true if excerpt is repeated", p.eval(EXCERPT+EXCERPT) );
    	
    	assertFalse( "should not be true if excerpt is not in string", p.eval("Something else") );
    	assertFalse( "should not be true if part of excerpt is in string", p.eval( EXCERPT.substring(1)) );
    }
}