package jmstransport;


import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

public class ReSubscribingJmsTransport extends SimpleJmsTransport {

	private Thread monitor;

	public ReSubscribingJmsTransport(final JmsErrorListener jmsErrorListener, ConnectionFactory connectionFactory) {
		super(jmsErrorListener, connectionFactory);
	}

	@Override
	public synchronized void stop() {
		try {
			super.stop();
			monitor.interrupt();
			monitor.join();
		} catch (Exception e) {
			jmsErrorListener.onError("Error while closing connection", e);
		}
	}

	@Override
	public synchronized void start() {
		try {
			super.start();
			startConnectionMonitor();
		} catch (Exception e) {
			throw new EnvironmentError("Error while starting EMS connection", e);
		}
	}

	private void startConnectionMonitor() {
		monitor = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					do{                                      
						Thread.sleep(1000);
						try {
							getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE).close();
						} catch (JMSException e) {
							if (!isStarted()) return;
							jmsErrorListener.onError("Connection is probably down.\n Restarting connection...", e);
							recoverFromConnectionFailure();
						}
					} while(isStarted());
				} catch (InterruptedException e) {
					if (isStarted()) throw new EnvironmentError("Unexpectedly interrupted",e);
				}
			}
		});
		monitor.start();
	}


	private void recoverFromConnectionFailure() throws InterruptedException {
		try { stopConnection(); } catch (JMSException ignored) {}
		int retryDelay = 100;
		while(isStarted()){
			try {
				initConnection(jmsErrorListener, connectionFactory);
				for (JmsSubscription subscription : listeners) {
					subscription.subscribe(jmsErrorListener, getConnection());
				}
				startConnection();
				break;
			} catch (Exception e) {
				jmsErrorListener.onError("Failed to restart connection. Trying again in "+retryDelay/1000.0+" seconds", e);
				Thread.sleep(retryDelay);
				if(retryDelay < 10000) retryDelay *= 2;
			}
		}
	}

}
