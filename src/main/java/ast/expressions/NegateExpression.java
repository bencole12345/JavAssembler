package ast.expressions;

public class NegateExpression implements Expression {

    private Expression expression;

    public NegateExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }
}
