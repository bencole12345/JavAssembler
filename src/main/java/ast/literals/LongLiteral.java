package ast.literals;

import ast.types.PrimitiveType;
import ast.types.Type;

public class LongLiteral implements LiteralValue {

    private long value;

    public LongLiteral(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return PrimitiveType.Long;
    }
}
