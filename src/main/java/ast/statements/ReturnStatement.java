package ast.statements;

import ast.expressions.Expression;

public class ReturnStatement implements Statement {

    private Expression expression;

    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }
}
