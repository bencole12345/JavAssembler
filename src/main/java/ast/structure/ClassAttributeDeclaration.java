package ast.structure;

import ast.types.Type;

public class ClassAttributeDeclaration extends VariableDeclaration {

    private AccessModifier visibility;

    public ClassAttributeDeclaration(Type variableType, String variableName, AccessModifier visibility) {
        super(variableType, variableName);
        this.visibility = visibility;
    }

    public AccessModifier getVisibility() {
        return visibility;
    }
}
