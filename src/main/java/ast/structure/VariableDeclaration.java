package ast.structure;

import ast.ASTNode;
import ast.types.Type;


/**
 * Denotes a variable declaration, including the type of that variable
 */
public class VariableDeclaration implements ASTNode {

    private Type variableType;
    private String variableName;

    public VariableDeclaration(Type variableType, String variableName) {
        this.variableType = variableType;
        this.variableName = variableName;
    }

    public Type getVariableType() {
        return variableType;
    }

    public String getVariableName() {
        return variableName;
    }
}
