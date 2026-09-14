package co.wethinkcode.healthsafe;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static java.util.Map.entry;

public class CsvCleaner {

    private static final Set<String> NULL_TOKENS = Set.of("", "n/a", "na", "tbd", "unknown", "-", "nan", "null");
    private static final Map<String, Integer> WORD_NUMS = Map.ofEntries(
            entry("zero", 0), entry("one", 1), entry("two", 2),
            entry("three", 3), entry("four", 4), entry("five", 5),
            entry("six", 6), entry("seven", 7), entry("eight", 8),
            entry("nine", 9), entry("ten", 10)
    );

    public List<WardRecord> loadAndClean() throws IOException {
        InputStream is = getClass().getResourceAsStream("/wards-outdated.csv");
        if (is == null) throw new FileNotFoundException("wards-outdated.csv not in resources");

        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        br.readLine(); // skip header

        Map<String, WardRecord> dedup = new LinkedHashMap<>();
        String line;
        int rowNum = 1;

        while ((line = br.readLine())!= null) {
            rowNum++;
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length < 4) continue;

            String rawId = cleanString(parts[0]);
            String rawWing = cleanString(parts[1]);
            String rawDept = cleanString(parts[2]);
            String rawBeds = cleanString(parts[3]);

            if (isNullToken(rawId)) continue; // id is mandatory

            String normalizedKey = rawId.toLowerCase().replaceAll("[^a-z0-9]", "");
            String wardId = normalizeId(rawId);
            String wing = toTitleCase(rawWing);
            String department = toTitleCase(rawDept);

            List<String> notesList = new ArrayList<>();
            Integer beds = parseBeds(rawBeds, notesList);

            if (rawWing.isEmpty() || isNullToken(rawWing)) {
                notesList.add("wing was missing");
            }
            if (rawDept.isEmpty() || isNullToken(rawDept)) {
                notesList.add("department was missing");
            }

            WardRecord record = new WardRecord(wardId, wing, department, beds, String.join("; ", notesList));
            record.rawLine = line;

            if (dedup.containsKey(normalizedKey)) {
                WardRecord existing = dedup.get(normalizedKey);
                // merge notes for near-duplicates like w-05 vs W-05
                if (!record.notes.isEmpty()) {
                    existing.notes = existing.notes.isEmpty()? record.notes : existing.notes + "; " + record.notes;
                }
                existing.notes += "; duplicate row " + rowNum + " merged (was: " + line + ")";

                // fill missing values from duplicate if we have them
                if (existing.wing == null && record.wing!= null) existing.wing = record.wing;
                if (existing.department == null && record.department!= null) existing.department = record.department;
                if (existing.bedsAvailable == null && record.bedsAvailable!= null) existing.bedsAvailable = record.bedsAvailable;
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
        if (s == null) return true;
        return NULL_TOKENS.contains(s.trim().toLowerCase());
    }

    private String normalizeId(String id) {
        return id.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private String toTitleCase(String s) {

        if (s == null || isNullToken(s)) return null;
        s = s.trim().toLowerCase();
        if (s.contains("pediatr") || s.contains("pediatry")) {
            s = "paediatrics";
        }
        String[] words = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private Integer parseBeds(String raw, List<String> notes) {
        if (raw == null || raw.isEmpty() || isNullToken(raw)) {
            return null;
        }
        String lower = raw.toLowerCase().trim();

        // Check for word numbers but FLAG, don't convert - per spec
        if (WORD_NUMS.containsKey(lower)) {
            notes.add("bedsAvailable was non-numeric ('" + raw + "') — flagged for follow-up");
            return null;
        }

        try {
            int v = Integer.parseInt(lower);
            if (v < 0 || v > 500) {
                notes.add("bedsAvailable was unrealistic (" + raw + ") — flagged for follow-up");
                return null;
            }
            return v;
        } catch (NumberFormatException e) {
            notes.add("bedsAvailable was non-numeric ('" + raw + "') — flagged for follow-up");
            return null;
        }
    }
}