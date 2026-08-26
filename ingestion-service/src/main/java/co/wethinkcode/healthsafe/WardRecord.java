package co.wethinkcode.healthsafe;


public class WardRecord {
    public String wardId;
    public String wing;
    public String department;
    public Integer bedsAvailable;
    public String notes;

    // keep original for duplicate debugging
    public transient String rawLine;

    public WardRecord(String wardId, String wing, String department, Integer bedsAvailable, String notes) {
        this.wardId = wardId;
        this.wing = wing;
        this.department = department;
        this.bedsAvailable = bedsAvailable;
        this.notes = notes;
    }

    @Override
    public String toString() {
        return wardId + " | " + wing + " | " + department + " | " + bedsAvailable + " | " + notes;
    }
}
