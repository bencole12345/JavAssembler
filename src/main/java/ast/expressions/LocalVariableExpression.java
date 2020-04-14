package ast.expressions;

import ast.structure.VariableScope;
import ast.types.Type;

/**
 * This is one of the 'base case' expressions, denoting a variable name.
 */
public class LocalVariableExpression implements VariableExpression {

    private String variableName;
    private VariableScope containingScope;

    public LocalVariableExpression(String variableName, VariableScope containingScope) {
        this.variableName = variableName;
        this.containingScope = containingScope;
    }

    public String getVariableName() {
        return variableName;
    }

    @Override
    public Type getType() {
        return containingScope
                .getVariableWithName(variableName)
                .getType();
    }

    @Override
    public String toString() {
        return variableName;
    }
}
