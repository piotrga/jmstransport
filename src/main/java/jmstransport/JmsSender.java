package jmstransport;


import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Queue;

import static jmstransport.Retryable.times;

public class JmsSender implements Sender{
	private MessageProducer sender;
	private Session session;
	private Connection connection;
	private String destination;


	public JmsSender(Connection connection, String destination) {
		this.connection = connection;
		this.destination = destination;
		initConnectionAndSession();
	}

	@Override
	public void send(final String messageText) {
		new Retryable() {
			@Override
			protected Object call(int tryIndex) throws Exception {
				internalSendMessage(messageText);
				return null;
			}

			@Override
			protected void logError(int tryIndex, Exception e) {}
		}.tryRunning(times(2));
	}

	private void internalSendMessage(String messageText) {
		try {
			sender.send(session.createTextMessage(messageText));			
		} catch (Throwable e) {
			throw new EnvironmentError("Can not send JMS message", e);
		}
	}

	private void initConnectionAndSession() {
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(destination);
			sender = session.createProducer(queue);
		} catch (Exception e) {
			session = null;
			throw new EnvironmentError("Error while initializing JMS session for destination " + destination, e);
		}

	}

	@Override
    public void stop() {
		try {
			sender.close();
			session.close();
		} catch (JMSException e) {
			throw new EnvironmentError("Can not stop JmsSender for destination "+ destination, e);
		}
	}
}
