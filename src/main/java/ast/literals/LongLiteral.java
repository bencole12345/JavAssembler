package ast.literals;

public class LongLiteral implements LiteralValue {

    private long value;

    public LongLiteral(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
