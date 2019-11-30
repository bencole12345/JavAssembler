package ast.literals;

public class BooleanLiteral implements ValueExpression {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }
}
