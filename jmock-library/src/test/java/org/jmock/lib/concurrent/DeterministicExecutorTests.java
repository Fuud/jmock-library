package org.jmock.lib.concurrent;

import junit.framework.TestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.api.Action;

public class DeterministicExecutorTests extends TestCase {
    DeterministicExecutor scheduler = new DeterministicExecutor();

    Mockery mockery = new Mockery();
    @Override
    protected void tearDown() throws Exception {
        mockery.assertIsSatisfied();
    }
    
    Runnable commandA = mockery.mock(Runnable.class, "commandA");
    Runnable commandB = mockery.mock(Runnable.class, "commandB");
    Runnable commandC = mockery.mock(Runnable.class, "commandC");
    Runnable commandD = mockery.mock(Runnable.class, "commandD");


    public void testRunsPendingCommands() {
        scheduler.execute(commandA);
        scheduler.execute(commandB);
        
        final Sequence executionOrder = mockery.sequence("executionOrder");

        mockery.checking(new Expectations() {protected void expect() throws Exception{
            oneOf (commandA).run(); inSequence(executionOrder);
            oneOf (commandB).run(); inSequence(executionOrder);
        }});
        
        scheduler.runPendingCommands();
    }
    
    public void testCanLeaveCommandsSpawnedByExecutedCommandsPendingForLaterExecution() {
        scheduler.execute(commandA);
        scheduler.execute(commandB);
        
        final Sequence executionOrder = mockery.sequence("executionOrder");
        
        mockery.checking(new Expectations() {protected void expect() throws Exception{
            oneOf (commandA).run(); inSequence(executionOrder); will(schedule(commandC));
            oneOf (commandB).run(); inSequence(executionOrder); will(schedule(commandD));
            never (commandC).run();
            never (commandD).run();
        }});
        
        scheduler.runPendingCommands();
    }
    
    public void testCanRunCommandsSpawnedByExecutedCommandsUntilNoCommandsArePending() {
        scheduler.execute(commandA);
        scheduler.execute(commandB);
        
        final Sequence executionOrder = mockery.sequence("executionOrder");
        
        mockery.checking(new Expectations() {protected void expect() throws Exception{
            oneOf (commandA).run(); inSequence(executionOrder); will(schedule(commandC));
            oneOf (commandB).run(); inSequence(executionOrder); will(schedule(commandD));
            oneOf (commandC).run(); inSequence(executionOrder);
            oneOf (commandD).run(); inSequence(executionOrder);
        }});
        
        scheduler.runUntilIdle();
    }

    protected Action schedule(final Runnable command) {
        return ScheduleOnExecutorAction.schedule(scheduler, command);
    }
}
