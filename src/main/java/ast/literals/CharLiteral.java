package ast.literals;

import ast.types.PrimitiveType;
import ast.types.Type;

public class CharLiteral implements LiteralValue {

    private char value;

    public CharLiteral(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return PrimitiveType.Char;
    }
}
