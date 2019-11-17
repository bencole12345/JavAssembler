package ast.structure;

import ast.ASTNode;

import java.util.List;

public class JavaClass implements ASTNode {

    // TODO: Decide whether to separate static and non-static declarations

    private AccessModifier visibility;
    private String name;
    private VariableScope variableScope;
    private List<ClassMethod> methods;

    public JavaClass(AccessModifier visibility, String name, VariableScope variableScope, List<ClassMethod> methods) {
        this.visibility = visibility;
        this.name = name;
        this.variableScope = variableScope;
        this.methods = methods;
    }

    public AccessModifier getVisibility() {
        return visibility;
    }

    public String getName() {
        return name;
    }

    public VariableScope getVariableScope() {
        return variableScope;
    }

    public List<ClassMethod> getMethods() {
        return methods;
    }
}
