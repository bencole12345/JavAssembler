package ast.literals;

public class ShortLiteral implements ValueExpression {

    private short value;

    public ShortLiteral(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}
