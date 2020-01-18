package ast.literals;

import ast.types.PrimitiveType;
import ast.types.Type;

public class IntLiteral implements LiteralValue {

    private int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return PrimitiveType.Int;
    }
}
