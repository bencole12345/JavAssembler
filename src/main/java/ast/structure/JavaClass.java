package ast.structure;

import ast.ASTNode;

import java.util.List;

public class JavaClass implements ASTNode {

    // TODO: Decide whether to separate static and non-static declarations

    private AccessModifier visibility;
    private String name;
    private VariableDeclarationGroup declarations;
    private List<ClassMethod> methods;

    public JavaClass(AccessModifier visibility, String name, VariableDeclarationGroup declarations, List<ClassMethod> methods) {
        this.visibility = visibility;
        this.name = name;
        this.declarations = declarations;
        this.methods = methods;
    }

    public AccessModifier getVisibility() {
        return visibility;
    }

    public String getName() {
        return name;
    }

    public VariableDeclarationGroup getDeclarations() {
        return declarations;
    }

    public List<ClassMethod> getMethods() {
        return methods;
    }

}
