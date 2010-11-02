package jmstransport;


public interface Transport  {
	Subscription listenTo(String topicName, Receiver receiver);
	Sender sender(String destinationName);

}
