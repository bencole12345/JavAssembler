package ast.literals;

import ast.types.PrimitiveType;
import ast.types.Type;

public class BooleanLiteral implements LiteralValue {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return PrimitiveType.Boolean;
    }
}
