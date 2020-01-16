package ast.expressions;

import ast.structure.VariableScope;
import ast.types.Type;

/**
 * This is one of the 'base case' expressions, denoting a variable name.
 */
public class VariableNameExpression implements Expression {

    private String variableName;
    private VariableScope containingScope;

    public VariableNameExpression(String variableName, VariableScope containingScope) {
        // TODO: Check the variable has been declared; throw exception if not
        // Note: this should be an exception for "referenced before declaration";
        // it should be thrown even if the variable is declared later on, that is,
        // x = 1;
        // int x;
        // should not be valid.
        // (See ASTBuilder.visitStatementList)
        this.variableName = variableName;
        this.containingScope = containingScope;
    }

    public String getVariableName() {
        return variableName;
    }

    @Override
    public Type getType() {
        return containingScope.lookupVariableType(variableName);
    }
}
