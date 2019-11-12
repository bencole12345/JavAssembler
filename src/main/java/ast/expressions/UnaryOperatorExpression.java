package ast.expressions;

// TODO: Decide whether NEGATE should be separate from the rest since
// the others all require a change of state.
public class UnaryOperatorExpression extends Expression {

    public enum Operation {
        NEGATE,
        PRE_INCREMENT,
        PRE_DECREMENT,
        POST_INCREMENT,
        POST_DECREMENT
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
