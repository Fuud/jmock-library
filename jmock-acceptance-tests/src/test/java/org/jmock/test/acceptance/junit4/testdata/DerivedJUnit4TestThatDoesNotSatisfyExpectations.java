package org.jmock.test.acceptance.junit4.testdata;

import org.jmock.ExpectationsExt;
import org.junit.Test;

public class DerivedJUnit4TestThatDoesNotSatisfyExpectations extends BaseClassWithMockery {
    private Runnable runnable = context.mock(Runnable.class);
    
    @Test
    public void doesNotSatisfyExpectations() {
        context.checking(new ExpectationsExt() {protected void expect() throws Exception {
            oneOf (runnable).run();
        }});
        
        // Return without satisfying the expectation for runnable.run()
    }
}
