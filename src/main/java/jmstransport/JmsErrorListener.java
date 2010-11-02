package jmstransport;

import javax.jms.Message;

public interface JmsErrorListener {
	void onErrorProcessingMessage(String errorMessage, Message message, Throwable e);	
	void onError(String errorMessage, Exception e);
}
