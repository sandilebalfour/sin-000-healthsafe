package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

public class AlertLevelServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7032);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Tracks the hospital Emergency Status (0-8, 8 = full Code Blue).)
        // Add domain endpoints for alert-level-service here.

        app.get("/alert-service/{level}", ctx -> {
            var alert = new EmergencyStatus();
            Code c = alert.getCodeLevel(Integer.parseInt(ctx.pathParam("level")));
            ctx.result(alert.getLevel()+ " : "+c.toString() );
        });

    }
}
