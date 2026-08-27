package co.wethinkcode.healthsafe;

public enum Code {
    RED("Fire / smoke present."), BLUE("cardiac arrest"), GREEN("evacuate"), YELLOW("Internal Disaster"), PINK("An infant or child abduction."), ORANGE("A hazardous material spill."), WHITE(""), AMBER("abduction"), BLACK("A bomb threat or suspicious package.");

    private String status;

    Code(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Code{" +
                "status='" + status + '\'' +
                '}';
    }
}
