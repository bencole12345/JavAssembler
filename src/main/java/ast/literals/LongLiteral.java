package ast.literals;

public class LongLiteral implements ValueExpression {

    private long value;

    public LongLiteral(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
