package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.mq.MqConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class StaffingServiceApp {

    private static final HttpClient http = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        Javalin app = Javalin.create(cfg -> {
            cfg.jsonMapper(new io.javalin.json.JavalinJackson(mapper));
        }).start(7033);

        app.get("/health", ctx -> ctx.result("OK"));

        // MAIN ENDPOINT: GET /staffing/{wardId}
        app.get("/staffing/{wardId}", ctx -> {
            String wardId = ctx.pathParam("wardId");

            // 1. Validate ward - call ward-service, return 404 if unknown (required contract)
            try {
                var req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:7031/wards/" + wardId))
                        .GET().build();
                var res = http.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 404) {
                    ctx.status(404).result("Unknown ward: " + wardId);
                    return;
                }
            } catch (Exception e) {
                ctx.status(503).result("ward-service down");
                return;
            }

            // 2. Read Emergency Status - call alert-level-service GET /alert-level
            int level = 0;
            try {
                var req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:7032/alert-level"))
                        .GET().build();
                var res = http.send(req, HttpResponse.BodyHandlers.ofString());
                level = mapper.readTree(res.body()).get("level").asInt();
            } catch (Exception ignored) {}

            // 3. Size on-call based on level (0-8, 8 = full Code Blue)
            int doctors = level <= 2 ? 2 : level <= 5 ? 4 : level <= 7 ? 6 : 10;
            String code = EmergencyStatus.getCodeLevel(level).toString();

            var schedule = new Schedule(wardId, level, code, doctors);

            // 4. Publish to staffing-events-topic
            publish(mapper.writeValueAsString(schedule));

            ctx.json(schedule);
        });
    }

    private static void publish(String json) {
        try {
            ConnectionFactory cf = new ActiveMQConnectionFactory(MqConfig.BROKER_URL);
            Connection conn = cf.createConnection();
            conn.start();
            Session s = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = s.createTopic(MqConfig.TOPIC);
            s.createProducer(topic).send(s.createTextMessage(json));
            conn.close();
        } catch (Exception e) {
            System.err.println("Topic publish failed: " + e.getMessage());
        }
    }

    public static class Schedule {
        public String wardId;
        public int emergencyLevel;
        public String code;
        public int doctorsOnCall;
        public Schedule(String wardId, int level, String code, int doctors) {
            this.wardId = wardId; this.emergencyLevel = level; this.code = code; this.doctorsOnCall = doctors;
        }
    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
