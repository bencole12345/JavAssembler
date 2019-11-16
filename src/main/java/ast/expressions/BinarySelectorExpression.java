package ast.expressions;

/**
 * Wraps a binary selector expression.
 *
 * (That's one of the form
 *      cond ? e1 : e2)
 */
public class BinarySelectorExpression implements Expression {

    private Expression condition;
    private Expression trueExpression;
    private Expression falseExpression;

    public BinarySelectorExpression(Expression condition, Expression trueExpression, Expression falseExpression) {
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getTrueExpression() {
        return trueExpression;
    }

    public Expression getFalseExpression() {
        return falseExpression;
    }
}
