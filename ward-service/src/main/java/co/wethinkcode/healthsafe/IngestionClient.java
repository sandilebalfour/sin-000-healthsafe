package co.wethinkcode.healthsafe;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class IngestionClient {
    private static final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public List<Ward> fetchWards() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7030/wards")).GET().build();

        for (int i=0; i<5; i++) {
            try {
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() == 200) {
                    return mapper.readValue(res.body(), new TypeReference<>() {});
                }
            } catch (Exception e) {
                System.out.println("Waiting for ingestion-service... " + e.getMessage());
                Thread.sleep(2000);
            }
        }
        throw new RuntimeException("ingestion-service not reachable");
    }
}
