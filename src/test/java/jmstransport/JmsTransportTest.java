package jmstransport;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import static org.mockito.Mockito.verify;

@SuppressWarnings({"FieldCanBeLocal", "ThrowableResultOfMethodCallIgnored"})
public class JmsTransportTest {

	@Mock Receiver receiver;
	@Mock JmsErrorListener jmsErrorListener;
	@Mock ConnectionFactory connectionFactory;

	{MockitoAnnotations.initMocks(this);}

	ReSubscribingJmsTransport transport;

	Sender sender;
	static TestingJmsBroker jmsBroker;

    @BeforeClass
    public static void startBroker() throws Exception {
        jmsBroker = new TestingJmsBroker();
        jmsBroker.start();
    }

    @AfterClass
    public static void stopBroker() throws Exception {
        jmsBroker.stop();
    }

    @Before
	public void setUp() throws JMSException {
		connectionFactory = jmsBroker.connectionFactory();
		transport = new ReSubscribingJmsTransport(jmsErrorListener, connectionFactory);
	}
	//@After public void tearDown() throws JMSException { connectionFactory.close();	}

	@Test
	public void startsAndStopsCorrectly() throws Exception {
		transport.start();
		transport.stop();
	}

	@Test(expected = Defect.class)
	public void sendingBeforeStartingTransportThrows() throws Exception {
		transport.sender("some queue").send("ifjwpoefk");
	}

	@Test
	public void sendsAndReceivesMessages() throws Exception {
		transport.listenTo("Q1", receiver);

		transport.start();
		try {
			sender = transport.sender("Q1");
			sender.send("test message");
			Thread.sleep(100);

			verify(receiver).onMessage("test message");
		} finally {
			transport.stop();
		}
	}
}
