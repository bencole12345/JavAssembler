package ast.literals;

public class DoubleLiteral implements LiteralValue {

    private double value;

    public DoubleLiteral(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
