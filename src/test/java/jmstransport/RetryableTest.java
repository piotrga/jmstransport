package jmstransport;

import jmstransport.RetryException;
import jmstransport.Retryable;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class RetryableTest {
	private static final int HUGE_NUMBER = 55555;

	@Test(expected = RetryException.class)
	public void failsIfNumberOfTriesExceeded() throws Exception {
		new Retryable<Integer>(){
			@Override
			public  Integer call(int tryIndex) {
				if(tryIndex< HUGE_NUMBER) throw new RuntimeException(tryIndex +" failed");
				return tryIndex;
			}
		}.tryRunning(3);
	}

	@Test
	public void exceptionMessageContainsUnderlyingErrorMessages() throws Exception {
		try {
			new Retryable<Integer>(){ public  Integer call(int tryIndex) {
				throw new RuntimeException(tryIndex +" failed");
			}}.tryRunning(3);
		} catch (RuntimeException e) {
			Assert.assertThat(e.getMessage(), Matchers.allOf(
					containsString("1 failed"),
					containsString("2 failed"),
					containsString("3 failed")));
		}
	}

	@Test
	public void triesExactlyAsManyTimesAsSpecified() throws Exception {
		final AtomicInteger counter = new AtomicInteger(0);
		try {
			new Retryable<Integer>(){ public  Integer call(int tryIndex) {
				counter.incrementAndGet();
				throw new RuntimeException(tryIndex +" failed");
			}}.tryRunning(3);
		} catch (RuntimeException e) {
			Assert.assertThat(counter.intValue(), equalTo(3));
		}
	}

	@Test
	public void retriesUntilNoError() throws Exception {
		int result = new Retryable<Integer>(){
			@Override
			public  Integer call(int tryIndex) {
				if(tryIndex<2) throw new RuntimeException(tryIndex +" failed");
				return tryIndex;
			}
		}.tryRunning(3);
		Assert.assertThat(result, equalTo(2));
	}

	@Test
	public void worksFineIfNoError() throws Exception {
		int result = new Retryable<Integer>(){
			@Override
			public  Integer call(int tryIndex) {
				return tryIndex;
			}
		}.tryRunning(HUGE_NUMBER);

		Assert.assertThat(result, equalTo(1));
	}


}
