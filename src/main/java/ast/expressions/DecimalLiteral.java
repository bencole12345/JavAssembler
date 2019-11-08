package ast.expressions;

public class DecimalLiteral extends ValueExpression {

    private double value;

    public DecimalLiteral(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
