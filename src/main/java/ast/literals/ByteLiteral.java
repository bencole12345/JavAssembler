package ast.literals;

public class ByteLiteral implements ValueExpression {

    private byte value;

    public ByteLiteral(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
