package jmstransport;

import org.joda.time.Period;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Queue{

	Transport transport;
	String queueName;

	public void clearMessages(Period timeout) throws InterruptedException {
		final AtomicLong lastProcessed = new AtomicLong(System.currentTimeMillis());

		Subscription subscription = transport.listenTo(queueName, new Receiver() {
			@Override
			public void onMessage(String ignoredMesage) {
				lastProcessed.set(System.currentTimeMillis());
				System.out.println("ignored message: " + ignoredMesage);
			}
		});
		lastProcessed.set(System.currentTimeMillis());
		try {
			while (System.currentTimeMillis() - lastProcessed.get() < timeout.toStandardDuration().getMillis()){
				Thread.sleep(10);
			}
		} finally {
			subscription.unsubscribe();
		}
	}

	public Queue(Transport delegate, String queueName) {
		this.transport = delegate;
		this.queueName = queueName;
	}


	public void send(String message) {
		transport.sender(queueName).send(message);
	}

	public String waitForMessage(Period timeout) throws InterruptedException {
		final CountDownLatch messageReceivedLatch = new CountDownLatch(1);
		final AtomicReference<String> messageText = new AtomicReference<String>();
		Subscription subscription = transport.listenTo(queueName, new Receiver() {
			@Override
			public void onMessage(String message) {
				messageReceivedLatch.countDown();
				messageText.set(message);
			}
		});

		try {
			messageReceivedLatch.await(timeout.toStandardDuration().getMillis(), TimeUnit.MILLISECONDS);
		} finally {
			subscription.unsubscribe();
		}

		return messageText.get();
	}
}
