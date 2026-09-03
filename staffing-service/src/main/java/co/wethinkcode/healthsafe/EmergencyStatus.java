package co.wethinkcode.healthsafe;

public class EmergencyStatus {
    private int level;
    private Code code;

    public EmergencyStatus() {}

    public EmergencyStatus(int level) {
        setLevel(level);
    }

    public int getLevel() { return level; }

    public void setLevel(int level) {
        this.level = level;
        this.code = getCodeLevel(level); // THIS WAS MISSING
    }

    public Code getCode() { return code; }

    public static Code getCodeLevel(int level){
        return switch (level) {
            case 0 -> Code.ORANGE;
            case 1 -> Code.GREEN;
            case 2 -> Code.YELLOW;
            case 3 -> Code.PINK;
            case 4 -> Code.RED;
            case 5 -> Code.AMBER;
            case 6 -> Code.BLACK;
            case 7 -> Code.WHITE;
            case 8 -> Code.BLUE;
            default -> throw new IllegalArgumentException("Invalid level: " + level);
        };
    }
}
