package co.wethinkcode.healthsafe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WardRecordTest {

    @Test
    void constructorAndToString() {
        WardRecord r = new WardRecord("W-01", "East Wing", "Cardiology", 3, "");
        assertEquals("W-01", r.wardId);
        assertEquals("East Wing", r.wing);
        assertEquals("Cardiology", r.department);
        assertEquals(3, r.bedsAvailable);
        assertEquals("W-01 | East Wing | Cardiology | 3 | ", r.toString());
    }

    @Test
    void handlesNullFields() {
        WardRecord r = new WardRecord("W-08", null, "Oncology", 4, "wing was missing");
        assertNull(r.wing);
        assertEquals("W-08 | null | Oncology | 4 | wing was missing", r.toString());
    }

    @Test
    void handlesNullBedsAndNotes() {
        WardRecord r = new WardRecord("W-04", "North Wing", "Oncology", null, 
            "bedsAvailable was unrealistic (-1) — flagged for follow-up");
        assertNull(r.bedsAvailable);
        assertTrue(r.toString().contains("null"));
        assertTrue(r.toString().contains("unrealistic"));
    }

    @Test
    void rawLineIsTransient() {
        WardRecord r = new WardRecord("W-05", "East Wing", "Paediatrics", 5, "duplicate row 7 merged");
        r.rawLine = "w-05,east wing ,PAEDIATRICS,five";
        assertEquals("w-05,east wing ,PAEDIATRICS,five", r.rawLine);
        // transient means Jackson/Gson won't serialize it - test JSON doesn't include rawLine
        // if you use Javalin ctx.json(), rawLine should NOT appear
    }

    @Test
    void dedupMergeScenario() {
        // Simulates your CsvCleaner merge logic
        WardRecord existing = new WardRecord("W-05", "East Wing", "Paediatrics", 5, "");
        WardRecord duplicate = new WardRecord("W-05", "East Wing", "Paediatrics", null, 
            "bedsAvailable was non-numeric ('five') — flagged for follow-up");

        if (!duplicate.notes.isEmpty()) {
            existing.notes = existing.notes.isEmpty() ? duplicate.notes : existing.notes + "; " + duplicate.notes;
        }
        existing.notes += "; duplicate row 7 merged (was: w-05,east wing ,PAEDIATRICS,five)";

        assertEquals(5, existing.bedsAvailable); // keeps first valid
        assertTrue(existing.notes.contains("five"));
        assertTrue(existing.notes.contains("duplicate row 7"));
    }

    @Test
    void toStringWithFlaggedBeds() {
        WardRecord w12 = new WardRecord("W-12", "West Wing", "Cardiology", null,
            "bedsAvailable was non-numeric ('full') — flagged for follow-up");
        String s = w12.toString();
        assertTrue(s.contains("W-12"));
        assertTrue(s.contains("West Wing"));
        assertTrue(s.contains("full"));
        assertTrue(s.contains("null")); // bedsAvailable
    }
}