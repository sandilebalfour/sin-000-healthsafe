package co.wethinkcode.healthsafe;

import java.io.InputStream;

public class WardRecord {

    private String wardId;
    private String wing;
    private String department;
    private String  bedsAvailable;
    private String notes;

    public WardRecord(String wardId, String wing, String department, String  bedsAvailable) {

        this.wardId = wardId;
        if(wing.isEmpty()) throw new IllegalArgumentException("");
        this.wing = wing;
        this.department = department;
        this.bedsAvailable = bedsAvailable;
    }

    public String getWardId() {
        return wardId;
    }

    public void setWardId(String wardId) {
        this.wardId = wardId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getWing() {
        return wing;
    }

    public void setWing(String wing) {
        this.wing = wing;
    }

    public String getBedsAvailable() {
        return bedsAvailable;
    }

    public void setBedsAvailable(String bedsAvailable) {
        this.bedsAvailable = bedsAvailable;
    }

    @Override
    public String toString() {
        return "WardRecord{" +
                "wardId:'" + wardId + '\'' +
                ", wing:'" + wing + '\'' +
                ", department:'" + department + '\'' +
                ", bedsAvailable:" + bedsAvailable +
                ", notes:'" + notes + '\'' +
                '}';
    }

}
