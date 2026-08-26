package co.wethinkcode.healthsafe.mq;

import co.wethinkcode.healthsafe.Ward;
import co.wethinkcode.healthsafe.WardRepository;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

public class StaffingTopicListener {
    private final WardRepository repo;

    public StaffingTopicListener(WardRepository repo) { this.repo = repo; }

    public void start() {
        new Thread(() -> {
            try {
                var factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
                Connection conn = factory.createConnection();
                conn.start();
                Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Topic topic = session.createTopic(MqConfig.TOPIC);
                MessageConsumer consumer = session.createConsumer(topic);
                consumer.setMessageListener(msg -> {
                    try {
                        String text = ((TextMessage) msg).getText();
                        System.out.println("[TOPIC] " + text);
                        if (text.contains(":")) {
                            String wardId = text.split(":")[0].trim();
                            Ward w = repo.findById(wardId);
                            if (w!= null) w.lastStaffingEvent = text;
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                });
                System.out.println("Subscribed to " + MqConfig.TOPIC);
            } catch (Exception e) {
                System.err.println("Topic subscribe failed: " + e.getMessage());
            }
        }).start();
    }
}
