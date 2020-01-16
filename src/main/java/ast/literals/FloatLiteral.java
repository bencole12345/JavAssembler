package ast.literals;

import ast.types.PrimitiveType;
import ast.types.Type;

public class FloatLiteral implements LiteralValue {

    private float value;

    public FloatLiteral(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return PrimitiveType.Float;
    }
}
