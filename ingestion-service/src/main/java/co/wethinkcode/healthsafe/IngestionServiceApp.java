package co.wethinkcode.healthsafe;

import io.javalin.Javalin;
import java.util.List;

public class IngestionServiceApp {
    public static void main(String[] args) throws Exception {
        List<WardRecord> cleaned = new CsvCleaner().loadAndClean();
        System.out.println("Loaded " + cleaned.size() + " unique wards");
        cleaned.forEach(System.out::println);

        Javalin app = Javalin.create().start(7030);
        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/wards", ctx -> ctx.json(cleaned));
        app.get("/wards/{id}", ctx -> {
            String id = ctx.pathParam("id").toUpperCase();
            cleaned.stream().filter(w -> w.wardId.equalsIgnoreCase(id)).findFirst()
                    .ifPresentOrElse(ctx::json, () -> ctx.status(404).result("Not found"));
        });
    }
}
