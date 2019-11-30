package ast.literals;

public class DoubleLiteral implements ValueExpression {

    private double value;

    public DoubleLiteral(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
