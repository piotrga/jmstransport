package jmstransport;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.Ignore;

import javax.jms.ConnectionFactory;
import java.io.IOException;

@Ignore("This is not a test")
public class TestingJmsBroker {
	BrokerService broker;
	int port;

	public TestingJmsBroker() throws IOException {
		port = NetworkUtils.getFreePort();
	}

	public TestingJmsBroker start() throws Exception {
		broker = new BrokerService();
		broker.setPersistent(false);
		broker.setUseJmx(false);
		broker.setEnableStatistics(false);
		broker.setSupportFailOver(false);
		broker.addConnector("tcp://localhost:" + port);
		broker.start();
		return this;
	}

	public void stop() throws Exception {
		broker.stop();
	}
	

	public ConnectionFactory connectionFactory() {
		return new ActiveMQConnectionFactory("tcp://localhost:"+ port);
	}
}