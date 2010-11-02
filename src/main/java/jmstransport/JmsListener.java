package jmstransport;


import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Queue;

public class JmsListener {


    private JmsErrorListener jmsErrorListener;
    private Receiver receiver;

    private Connection connection;
    private String queueName;
    private MessageConsumer messageConsumer;

    public JmsListener(Receiver receiver, JmsErrorListener jmsErrorListener, Connection connection, String queueName) {
        this.receiver = receiver;
        this.jmsErrorListener = jmsErrorListener;

        this.connection = connection;
        this.queueName = queueName;
        start();
    }

    private void start() {
        try {
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue(queueName);

            messageConsumer = session.createConsumer(queue);
            messageConsumer.setMessageListener(new DelegatingMessageListener(session));
        } catch (Exception e) {
            throw new EnvironmentError("Can not start JMS Listener for configuration " + queueName, e);
        }
    }

    public void stop() throws JMSException {
        messageConsumer.close();
    }

    private class DelegatingMessageListener implements MessageListener {
        private Session session;

        public DelegatingMessageListener(Session session) {
            this.session = session;
        }

        @Override
        public void onMessage(Message message) {
            try {
                String messageText = ((TextMessage) message).getText();

                receiver.onMessage(messageText);
                session.commit();
            } catch (Exception e) {
                jmsErrorListener.onErrorProcessingMessage("Error while processing message - returning to the queue.", message, e);

                try {
                    session.commit();
                    session.rollback();
                } catch (JMSException rollbackException) {
                    jmsErrorListener.onError("Error while returning message to the queue.", rollbackException);
                }

            }
        }
    }
}
