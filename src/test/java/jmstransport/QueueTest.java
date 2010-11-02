package jmstransport;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.joda.time.Period.millis;
import static org.junit.Assert.fail;

public class QueueTest {
	@Mock JmsErrorListener listener;
	private TestingJmsBroker broker;

	{ MockitoAnnotations.initMocks(this);}

	@Test
	public void clearsTheQueue() throws Exception {
		ReSubscribingJmsTransport transport = new ReSubscribingJmsTransport(listener, broker.connectionFactory());
		transport.start();
		try {
			Queue queue = new Queue(transport, "Q1");

			queue.send("This message should be cleared.");

			queue.clearMessages(millis(100));

			String receivedMessage = queue.waitForMessage(millis(100));

			if (receivedMessage != null) {
				fail("Messages were not cleared (message: " + receivedMessage + ").");
			}
		} finally {
			transport.stop();
		}
	}

	@Before
	public  void setup() throws Exception {
		broker = new TestingJmsBroker();
		broker.start();
	}

	@After
	public void teardown() throws Exception {
		broker.stop();
	}
}
