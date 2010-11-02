package jmstransport;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleJmsTransport implements Transport {
	private Connection connection;

	protected JmsErrorListener jmsErrorListener;
	protected AtomicBoolean started = new AtomicBoolean(false);
	protected List<JmsSubscription> listeners = new LinkedList<JmsSubscription>();
	protected ConnectionFactory connectionFactory;

	public SimpleJmsTransport(final JmsErrorListener jmsErrorListener, ConnectionFactory connectionFactory) {
		this.jmsErrorListener = jmsErrorListener;
		this.connectionFactory = connectionFactory;
		initConnection(jmsErrorListener, connectionFactory);
	}

	@Override
	public synchronized Subscription listenTo(String topicName, Receiver receiver){
		JmsSubscription subscription = new JmsSubscription(receiver, topicName);
		subscription.subscribe(jmsErrorListener, getConnection());
		listeners.add(subscription);
		return subscription;
	}

	@Override
	public synchronized Sender sender(String destinationName){
		return new ConnectionAliveSender(new JmsSender(getConnection(), destinationName));
	}

	private void assertConnectionStarted() {
		if (!started.get()) throw new Defect("Connection has not started");
	}

    public synchronized void start() {
		if (started.get()) throw new IllegalStateException("Transport already started");
		try {
			getConnection().start();
			started.set(true);
		} catch (JMSException e) {
			throw new EnvironmentError("Error while starting EMS connection", e);
		}
	}

    public void stop() {
		try {
			started.set(false);
			getConnection().close();
		} catch (Exception e) {
			jmsErrorListener.onError("Error while closing connection", e);
		}
	}

	protected void initConnection(final JmsErrorListener jmsErrorListener, ConnectionFactory connectionFactory) {
		try {
			this.connection = connectionFactory.createConnection();
			getConnection().setExceptionListener(new ExceptionListener() {
				@Override
				public void onException(JMSException e) {
					jmsErrorListener.onError("JMS Exception", e);
				}
			});
		} catch (JMSException e) {
			throw new RuntimeException("Can not create JMS connection", e);
		}
	}

	protected Connection getConnection() {
		return connection;
	}

	protected void stopConnection() throws JMSException {
		getConnection().stop();
	}

	protected void startConnection() throws JMSException {
		getConnection().start();
	}

	protected boolean isStarted() {
		return started.get();
	}

	private class ConnectionAliveSender implements Sender {
		private final Sender sender;

		public ConnectionAliveSender(Sender sender) {
			this.sender = sender;
		}

		@Override
		public void send(String messageText) {
			assertConnectionStarted();
			sender.send(messageText);
		}

		@Override
		public void stop() {
			sender.stop();
		}
	}

	public static class JmsSubscription implements Subscription {
		final Receiver receiver;
		final String subject;

        private JmsListener jmsListener;

        private JmsSubscription(Receiver receiver, String subject) {
			this.receiver = receiver;
            this.subject = subject;
        }

        void subscribe(JmsErrorListener jmsErrorListener, Connection connection) {
            jmsListener = new JmsListener(this.receiver, jmsErrorListener, connection, this.subject);
        }

		@Override
		public void unsubscribe() {
			try {
				jmsListener.stop();
			} catch (JMSException e) {
				throw new EnvironmentError("Can not unsubscribe from subject "+ subject, e);
			}
		}
	}
}
