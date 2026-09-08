package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.mq.MqConfig;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EquipmentAlertServiceApp {

    // In-memory store for guaranteed delivery verification
    private static final List<String> alerts = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws Exception {
        Javalin app = Javalin.create().start(7034);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/alerts", ctx -> ctx.json(alerts));
        app.get("/alerts/count", ctx -> ctx.result(String.valueOf(alerts.size())));

        // --- Queue Consumer: guaranteed delivery ---
        // Why Queue and not Topic? Queue = exactly ONE consumer gets it,
        // and ActiveMQ persists it if this service is down. Topic = broadcast to all, lost if down.

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(MqConfig.QUEUE);
        MessageConsumer consumer = session.createConsumer(queue);

        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage) {
                    String text = ((TextMessage) message).getText();
                    alerts.add(text);
                    System.out.println("[ALERT RECEIVED] " + text);
                    // TODO: here you would page on-call, write to DB, etc..
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        System.out.println("EquipmentAlertService listening on " + MqConfig.QUEUE + " at " + MqConfig.BROKER_URL);
        System.out.println("HTTP on :7034");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { consumer.close(); session.close(); connection.close(); } catch (Exception ignored) {}
        }));
    }

    public static List<String> getAlerts() {
        return Collections.unmodifiableList(alerts);
    }
}