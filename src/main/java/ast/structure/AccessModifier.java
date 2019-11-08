package ast.structure;

import ast.ASTNode;

public class AccessModifier implements ASTNode {

    public enum AccessModifierType {
        PUBLIC,
        PROTECTED,
        PRIVATE,
        DEFAULT
    }

    private AccessModifierType modifier;

    public AccessModifier(AccessModifierType modifier) {
        this.modifier = modifier;
    }

    public AccessModifierType getModifier() {
        return modifier;
    }
}
