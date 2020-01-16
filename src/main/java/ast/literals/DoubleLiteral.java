package ast.literals;

import ast.types.PrimitiveType;
import ast.types.Type;

public class DoubleLiteral implements LiteralValue {

    private double value;

    public DoubleLiteral(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return PrimitiveType.Double;
    }
}
