package ast.expressions;

public class IntegerLiteral extends ValueExpression {

    private int value;

    public IntegerLiteral(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
