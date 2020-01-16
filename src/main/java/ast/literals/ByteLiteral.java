package ast.literals;

import ast.types.PrimitiveType;
import ast.types.Type;

public class ByteLiteral implements LiteralValue {

    private byte value;

    public ByteLiteral(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return PrimitiveType.Byte;
    }
}
