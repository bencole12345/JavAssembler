package ast.literals;

public class BooleanLiteral implements LiteralValue {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
