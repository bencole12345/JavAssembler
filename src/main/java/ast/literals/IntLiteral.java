package ast.literals;

public class IntLiteral implements ValueExpression {

    private int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
