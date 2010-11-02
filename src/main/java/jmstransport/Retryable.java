package jmstransport;

import java.util.ArrayList;
import java.util.List;

public abstract class Retryable<T> {

	public final T tryRunning(int maxTries) throws RuntimeException {
		List<Exception> exceptions = new ArrayList<Exception>();
		int tryIndex = 1;
		while(true){
			try{
				return call(tryIndex);
			} catch(Exception e){
				logError(tryIndex, e);
				exceptions.add(e);
				if (tryIndex >= maxTries) throw new RetryException("Giving up after "+tryIndex+ " tries.\nExceptions:"+exceptions, e);
				tryIndex++;
			}
		}
	}

	protected abstract T call(int tryIndex) throws Exception;
	public static int times(int times){return times;}

	protected void logError(int tryIndex, Exception e){
		// do nothing, optionally override in subclasses
	}
}
