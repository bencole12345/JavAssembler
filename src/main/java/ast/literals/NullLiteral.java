package ast.literals;

import ast.types.NullType;
import ast.types.Type;

public class NullLiteral implements LiteralValue {
    @Override
    public Type getType() {
        return new NullType();
    }
}
