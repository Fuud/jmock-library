package org.jmock.lib;

import static org.hamcrest.StringDescription.asString;
import static org.jmock.lib.CurrentStateMatcher.isCurrently;
import static org.jmock.lib.CurrentStateMatcher.isNotCurrently;

import junit.framework.TestCase;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.jmock.States;
import org.jmock.internal.StateMachine;


public class CurrentStateMatcherTests extends TestCase {
    States stateMachine = new StateMachine("stateMachine");
    Matcher<States> isCurrentlyS = isCurrently("S");
    Matcher<States> isNotCurrentlyS = isNotCurrently("S");
    
    public void testMatchesStateMachineCurrentlyInNamedState() {
        stateMachine.become("S");
        
        assertTrue("should match", isCurrently("S").matches(stateMachine));
        assertTrue("should not match", !isNotCurrently("S").matches(stateMachine));
    }
    
    public void testDoesNotMatchStateMachineCurrentlyInOtherState() {
        stateMachine.become("T");
        
        assertTrue("should not match", !isCurrently("S").matches(stateMachine));
        assertTrue("should match", isNotCurrently("S").matches(stateMachine));
    }

    public void testDoesNotMatchStateMachineInAnonymousInitialState() {
        assertTrue("should not match", !isCurrently("S").matches(stateMachine));
        assertTrue("should match", isNotCurrently("S").matches(stateMachine));
    }

    public void testDoesNotMatchNull() {
        assertTrue("should not match", !isCurrentlyS.matches(null));
    }

    public void testDoesNotMatchOtherTypesOfObject() {
        assertTrue("should not match", !isCurrentlyS.matches("something else"));
    }
    
    public void testHasReadableDescription() {
        assertEquals("a state machine that is S", asString(isCurrently("S")));
        assertEquals("a state machine that is not S", asString(isNotCurrently("S")));
    }
    
    public void testHasReadableDescriptionWhenPassedToAssertThat() {
        stateMachine.become("X");
        
        assertMismatchDescription("was not S", isCurrently("S"), stateMachine);
    }

    protected Matcher<?> createMatcher() {
        return isCurrentlyS;
    }

    public static <T> void assertMismatchDescription(String expected, Matcher<? super T> matcher, T arg) {
        Description description = new StringDescription();
        assertFalse("Precondtion: Matcher should not match item.", matcher.matches(arg));
        matcher.describeMismatch(arg, description);
        assertEquals("Expected mismatch description", expected, description.toString());
    }
}
