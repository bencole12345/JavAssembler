package ast.literals;

public class ByteLiteral implements LiteralValue {

    private byte value;

    public ByteLiteral(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }
}
