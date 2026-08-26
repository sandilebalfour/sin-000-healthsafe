package co.wethinkcode.healthsafe;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.Map.entry;

public class CsvCleaner {

    private static final Set<String> NULL_TOKENS = Set.of("", "n/a", "na", "tbd", "unknown", "-", "nan", "null");
    private static final Map<String, Integer> WORD_NUMS = Map.ofEntries(
            entry("zero", 0),
            entry("one", 1),
            entry("two", 2),
            entry("three", 3),
            entry("four", 4),
            entry("five", 5),
            entry("six", 6),
            entry("seven", 7),
            entry("eight", 8),
            entry("nine", 9),
            entry("ten", 10)
    );

    public List<WardRecord> loadAndClean() throws IOException {
        InputStream is = getClass().getResourceAsStream("/wards-outdated.csv");
        if (is == null) throw new FileNotFoundException("wards-outdated.csv not in resources");

        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String header = br.readLine(); // skip header

        Map<String, WardRecord> dedup = new LinkedHashMap<>();
        String line;
        int rowNum = 1;

        while ((line = br.readLine())!= null) {
            rowNum++;
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",", -1); // -1 keeps empty trailing fields
            if (parts.length < 4) continue;

            String rawId = cleanString(parts[0]);
            String rawWing = cleanString(parts[1]);
            String rawDept = cleanString(parts[2]);
            String rawBeds = cleanString(parts[3]);

            String normalizedKey = rawId.toLowerCase().replaceAll("[^a-z0-9]", ""); // w-05 and W-05 -> w05

            String wardId = normalizeId(rawId);
            String wing = toTitleCase(rawWing);
            String department = toTitleCase(rawDept);

            List<String> notesList = new ArrayList<>();
            Integer beds = parseBeds(rawBeds, notesList);

            if (rawWing.isEmpty() || isNullToken(rawWing)) {
                notesList.add("wing was missing");
            }

            WardRecord record = new WardRecord(wardId, wing, department, beds, String.join("; ", notesList));
            record.rawLine = line;

            if (dedup.containsKey(normalizedKey)) {
                WardRecord existing = dedup.get(normalizedKey);
                // merge note about duplicate
                existing.notes = (existing.notes == null? "" : existing.notes + "; ")
                        + "duplicate row " + rowNum + " merged (was: " + line + ")";
            } else {
                dedup.put(normalizedKey, record);
            }
        }
        return new ArrayList<>(dedup.values());
    }

    private String cleanString(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s{2,}", " ");
    }

    private boolean isNullToken(String s) {
        return NULL_TOKENS.contains(s.toLowerCase());
    }

    private String normalizeId(String id) {
        if (isNullToken(id)) return null;
        return id.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private String toTitleCase(String s) {
        if (s == null || isNullToken(s)) return null;
        s = s.trim().toLowerCase();
        // handle spelling variants
        s = s.replace("pediatry", "paediatrics").replace("pediatrics", "paediatrics");
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private Integer parseBeds(String raw, List<String> notes) {
        if (isNullToken(raw)) return null;
        String lower = raw.toLowerCase().trim();
        try {
            int v = Integer.parseInt(lower);
            if (v < 0 || v > 500) { // unrealistic
                notes.add("bedsAvailable was unrealistic (" + raw + ") — flagged for follow-up");
                return null;
            }
            return v;
        } catch (NumberFormatException e) {
            if (WORD_NUMS.containsKey(lower)) {
                notes.add("bedsAvailable was non-numeric ('" + raw + "') — converted to " + WORD_NUMS.get(lower));
                return WORD_NUMS.get(lower);
            }
            notes.add("bedsAvailable was non-numeric ('" + raw + "') — flagged for follow-up");
            return null;
        }
    }
}
