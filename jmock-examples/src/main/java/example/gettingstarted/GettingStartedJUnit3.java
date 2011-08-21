package example.gettingstarted;

import org.jmock.ExpectationsExt;
import org.jmock.integration.junit3.MockObjectTestCase;

public class GettingStartedJUnit3 extends MockObjectTestCase {
    public void testOneSubscriberReceivesAMessage() {
        // set up
        final Subscriber subscriber = mock(Subscriber.class);

        Publisher publisher = new Publisher();
        publisher.add(subscriber);

        final String message = "message";

        // expectations
        checking(new ExpectationsExt() {protected void expect() throws Exception{
            oneOf(subscriber).receive(message);
        }});

        // execute
        publisher.publish(message);
    }
}
