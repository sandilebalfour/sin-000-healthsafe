package co.wethinkcode.healthsafe;

public class EmergencyStatus {
    private int level;
    private Code code;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Code getCode() {
        return code;
    }

    public String turnEnum(){
        return String.valueOf(getCode());
    }

    public void setCode(Code code) {
        this.code = code;
    }


    public Code getCodeLevel(int level){
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
            default -> null;
        };
    }
    @Override
    public String toString() {
        return "EmergencyStatus{" +
                "level=" + getLevel() +
                ", code=" + turnEnum() +
                '}';
    }
}
