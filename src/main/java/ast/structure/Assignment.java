package ast.structure;

import ast.expressions.Expression;

public class Assignment implements Statement {

    private String variableName;
    private Expression expression;

    public Assignment(String variableName, Expression expression) {
        this.variableName = variableName;
        this.expression = expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public Expression getExpression() {
        return expression;
    }
}
