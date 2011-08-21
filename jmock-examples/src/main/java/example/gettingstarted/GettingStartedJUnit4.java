package example.gettingstarted;

import org.jmock.ExpectationsExt;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMock.class)
public class GettingStartedJUnit4 {
    Mockery context = new JUnit4Mockery();

    @Test
    public void oneSubscriberReceivesAMessage() {
        // set up
        final Subscriber subscriber = context.mock(Subscriber.class);

        Publisher publisher = new Publisher();
        publisher.add(subscriber);

        final String message = "message";

        // expectations
        context.checking(new ExpectationsExt() {protected void expect() throws Exception{
            oneOf(subscriber).receive(message);
        }});

        // execute
        publisher.publish(message);
    }
}