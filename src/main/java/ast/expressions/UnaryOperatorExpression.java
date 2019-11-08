package ast.expressions;

public class UnaryOperatorExpression extends Expression {

    public enum Operation {
        NEGATE,
        INCREMENT,
        DECREMENT
    }

    private Expression expression;
    private Operation op;

    public UnaryOperatorExpression(Expression expression, Operation op) {
        this.expression = expression;
        this.op = op;
    }

    public Expression getExpression() {
        return expression;
    }

    public Operation getOp() {
        return op;
    }
}
