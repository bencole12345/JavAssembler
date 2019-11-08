package ast.structure;

import ast.expressions.Expression;

public class ReturnStatement extends Statement {

    private Expression expression;

    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }
}
