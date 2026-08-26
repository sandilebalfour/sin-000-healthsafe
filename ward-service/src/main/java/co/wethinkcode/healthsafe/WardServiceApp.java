package co.wethinkcode.healthsafe;

import co.wethinkcode.healthsafe.mq.EquipmentQueuePublisher;
import co.wethinkcode.healthsafe.mq.StaffingTopicListener;
import io.javalin.Javalin;

public class WardServiceApp {
    public static void main(String[] args) throws Exception {
        var repo = new WardRepository();
        var ingestion = new IngestionClient();
        var queuePublisher = new EquipmentQueuePublisher();

        // 1. Load from ingestion-service (GET /wards)
        repo.saveAll(ingestion.fetchWards());
        System.out.println("Loaded " + repo.size() + " wards");

        // 2. Subscribe to staffing-events-topic
        new StaffingTopicListener(repo).start();

        Javalin app = Javalin.create().start(7031);
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/wards", ctx -> ctx.json(repo.findAll()));

        app.get("/wards/{id}", ctx -> {
            var ward = repo.findById(ctx.pathParam("id"));
            if (ward == null) ctx.status(404).result("Unknown ward: " + ctx.pathParam("id"));
            else ctx.json(ward);
        });

        app.post("/wards/{id}/equipment-failure", ctx -> {
            var ward = repo.findById(ctx.pathParam("id"));
            if (ward == null) { ctx.status(404).result("Unknown ward"); return; }
            String msg = ctx.body().isEmpty()? "Equipment failure in " + ward.wardId : ctx.body();
            queuePublisher.publish(msg);
            ctx.result("Queued failure for " + ward.wardId);
        });
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.healthsafe.mq.MqConfig)
// MQ TODO: publishes to ActiveMQ queue MqConfig.QUEUE when it detects an equipment failure on one of its wards.
