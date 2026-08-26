package co.wethinkcode.healthsafe.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class EquipmentQueuePublisher {
    public void publish(String message) {
        try {
            var factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
            try (Connection conn = factory.createConnection()) {
                conn.start();
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue queue = session.createQueue(MqConfig.QUEUE);
                MessageProducer producer = session.createProducer(queue);
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                producer.send(session.createTextMessage(message));
                System.out.println("[QUEUE] " + message);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}