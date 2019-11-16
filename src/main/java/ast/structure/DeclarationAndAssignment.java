package ast.structure;

import ast.expressions.Expression;
import ast.types.Type;

public class DeclarationAndAssignment implements Statement {

    private Type type;
    private String variableName;
    private Expression expression;

    public DeclarationAndAssignment(Type type, String variableName, Expression expression) {
        this.type = type;
        this.variableName = variableName;
        this.expression = expression;
    }

    public Type getType() {
        return type;
    }

    public String getVariableName() {
        return variableName;
    }

    public Expression getExpression() {
        return expression;
    }
}
