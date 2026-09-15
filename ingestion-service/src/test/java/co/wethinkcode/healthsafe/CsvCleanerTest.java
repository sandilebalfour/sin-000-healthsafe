package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class CsvCleanerTest {

    private final CsvCleaner cleaner = new CsvCleaner();


    @Test
    void normalizesIdsAndTitleCase() throws Exception {
        List<WardRecord> wards = cleaner.loadAndClean();
        Map<String, WardRecord> byId = wards.stream()
                .collect(Collectors.toMap(w -> w.wardId, w -> w));

        // w-02 -> W-02, paediatrics -> Paediatrics
        assertTrue(byId.containsKey("W-02"));
        assertEquals("West Wing", byId.get("W-02").wing);
        assertEquals("Paediatrics", byId.get("W-02").department);

        // W-03 ,east wing -> East Wing, W-10 double space
        assertEquals("East Wing", byId.get("W-03").wing);
        assertEquals("South Wing", byId.get("W-10").wing);

        // W-11 Pediatrics -> Paediatrics fix
        assertEquals("Paediatrics", byId.get("W-11").department);
    }

    @Test
    void handlesMissingWing() throws Exception {
        List<WardRecord> wards = cleaner.loadAndClean();
        WardRecord w08 = wards.stream().filter(w -> w.wardId.equals("W-08")).findFirst().orElseThrow();
        assertNull(w08.wing);
        assertTrue(w08.notes.contains("wing was missing"));
        assertEquals(4, w08.bedsAvailable);
    }

    @Test
    void handlesNullTokensForBeds() throws Exception {
        List<WardRecord> wards = cleaner.loadAndClean();
        Map<String, WardRecord> byId = wards.stream()
                .collect(Collectors.toMap(w -> w.wardId, w -> w));

        // N/A, TBD, unknown -> null
        assertNull(byId.get("W-02").bedsAvailable);
        assertNull(byId.get("W-07").bedsAvailable);
        assertNull(byId.get("W-09").bedsAvailable);
        assertNull(byId.get("W-15").bedsAvailable);
    }

    @Test
    void flagsWordNumbersNotConverts() throws Exception {
        List<WardRecord> wards = cleaner.loadAndClean();
        // w-05 duplicate has "five" - should be flagged, not converted to 5
        WardRecord w05 = wards.stream().filter(w -> w.wardId.equals("W-05")).findFirst().orElseThrow();

        // The first W-05 has 5 valid, so final should keep 5
        assertEquals(5, w05.bedsAvailable);
        // But notes must contain flag for the word "five"
        assertTrue(w05.notes.contains("five") && w05.notes.contains("flagged"),
                "Should flag word number 'five': " + w05.notes);
        assertTrue(w05.notes.contains("duplicate row"), "Should note duplicate merge");

        // w-12 "full" -> non-numeric flagged
        WardRecord w12 = wards.stream().filter(w -> w.wardId.equals("W-12")).findFirst().orElseThrow();
        assertNull(w12.bedsAvailable);
        assertTrue(w12.notes.contains("full"));
    }

    @Test
    void flagsUnrealisticBeds() throws Exception {
        List<WardRecord> wards = cleaner.loadAndClean();
        Map<String, WardRecord> byId = wards.stream()
                .collect(Collectors.toMap(w -> w.wardId, w -> w));

        assertNull(byId.get("W-04").bedsAvailable);
        assertTrue(byId.get("W-04").notes.contains("unrealistic (-1)"));

        assertNull(byId.get("W-14").bedsAvailable);
        assertTrue(byId.get("W-14").notes.contains("unrealistic (-2)"));

        assertNull(byId.get("W-13").bedsAvailable);
        assertTrue(byId.get("W-13").notes.contains("unrealistic (2023)"));
    }

    @Test
    void deduplicationMergesNotesAndKeepsFirstValidBeds() throws Exception {
        List<WardRecord> wards = cleaner.loadAndClean();
        WardRecord w05 = wards.stream().filter(w -> w.wardId.equals("W-05")).findFirst().orElseThrow();

        // Ensure only one W-05 exists
        long countW05 = wards.stream().filter(w -> w.wardId.equals("W-05")).count();
        assertEquals(1, countW05);

        // Notes contain both flag and merge info
        assertTrue(w05.notes.contains("duplicate row 7") || w05.notes.contains("duplicate row"),
                "Notes: " + w05.notes);
    }
}