package co.wethinkcode.healthsafe;

import io.javalin.Javalin;

import java.io.*;

public class IngestionServiceApp {

    public static void main(String[] args) throws IOException {
        Javalin app = Javalin.create().start(7030);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO: read and clean src/main/resources/wards-outdated.csv (wards, wings, specialist departments data —
        // trim whitespace, fix casing, normalize dates/booleans) and expose the
        // cleaned records here for the other services to consume.
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader( new FileReader("/home/wtc/SystemsIntegration/sin-000-healthsafe/ingestion-service/src/main/resources/wards-outdated.csv"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String line ;
        // TODO: Clean Data
        while ((line = bufferedReader.readLine()) != null){
            String[] splitLines = line.split(",") ;
            String WardId = splitLines[0];
            String wing = splitLines[1];
            String departments = splitLines[2];
            String  bedsAvailable = splitLines[3];
            WardRecord wardRecord = new WardRecord(WardId, wing, departments, bedsAvailable);
            System.out.println(wardRecord);
        }
    }
}
