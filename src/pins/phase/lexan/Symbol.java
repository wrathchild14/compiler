package pins.phase.lexan;

public class Symbol {
    private final int row;
    private final int charStart;
    private final int charEnd;
    private final String name;
    private final String type;

    public Symbol(int row, int charStart, int charEnd, String name, String type) {
        this.row = row;
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.name = name;
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public int getCharStart() {
        return charStart;
    }

    public int getCharEnd() {
        return charEnd;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void print() {
        System.out.printf("%d:%d - %d:%d : %s : %s\n", row, charStart, row, charEnd, name, type);
    }
}
