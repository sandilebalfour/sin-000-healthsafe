package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

public class AlertLevelServiceApp {

    // Store current hospital status - 8 = full Code Blue per TODO
    private static EmergencyStatus currentStatus = new EmergencyStatus(0);

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7032);

        app.get("/health", ctx -> ctx.result("OK"));

        // Contract required by staffing-service
        app.get("/alert-level", ctx -> {
            ctx.json(currentStatus);
        });

        // Set new level - staffing or admin calls this
        app.post("/alert-level/{level}", ctx -> {
            int level = Integer.parseInt(ctx.pathParam("level"));
            currentStatus = new EmergencyStatus(level);
            ctx.json(currentStatus);
        });

        // Your old endpoint - keep for testing conversion
        app.get("/alert-level/{level}", ctx -> {
            int level = Integer.parseInt(ctx.pathParam("level"));
            var status = new EmergencyStatus(level);
            ctx.json(status);
        });
    }
}