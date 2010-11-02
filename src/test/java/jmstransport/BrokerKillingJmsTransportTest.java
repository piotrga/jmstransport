package jmstransport;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SuppressWarnings({ "FieldCanBeLocal", "ThrowableResultOfMethodCallIgnored" })
public class BrokerKillingJmsTransportTest {

    @Mock Receiver receiver;
    @Mock JmsErrorListener jmsErrorListener;

    {
        MockitoAnnotations.initMocks(this);
    }

    ReSubscribingJmsTransport transport;

    Sender sender;

    
    @Test
    public void listenersKeepListeningEvenAfterTemporaryConnectionFailure() throws Exception {
        TestingJmsBroker broker = new TestingJmsBroker().start();
        transport = new ReSubscribingJmsTransport(jmsErrorListener, broker.connectionFactory());
        transport.listenTo("Q1", receiver);

        transport.start();
        try {
            broker.stop();
            broker.start();
            Thread.sleep(500); // this is to let recovery process finish

            sender = transport.sender("Q1");

            sender.send("test message");
            Thread.sleep(500);

            verify(receiver).onMessage("test message");
            verify(jmsErrorListener, atLeastOnce()).onError(any(String.class), any(Exception.class));
        } finally {
            transport.stop();
        }
    }


    @Test(expected = Exception.class)
    public void sendersDoNotSurviveTemporaryConnectionFailure() throws Exception {
        TestingJmsBroker broker = new TestingJmsBroker().start();
        ReSubscribingJmsTransport transport = new ReSubscribingJmsTransport(jmsErrorListener, broker.connectionFactory());

        transport.start();
        try {
            sender = transport.sender("Q1");
            broker.stop();
            broker.start();
            Thread.sleep(500); // this is to let recovery process finish

            sender.send("test message");
        } finally {
            transport.stop();
        }
    }

}
