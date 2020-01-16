package ast.literals;

import ast.types.PrimitiveType;
import ast.types.Type;

public class ShortLiteral implements LiteralValue {

    private short value;

    public ShortLiteral(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return PrimitiveType.Short;
    }
}
