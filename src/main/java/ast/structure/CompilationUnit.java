package ast.structure;

import ast.ASTNode;

/**
 * Represents a .java file.
 */
public class CompilationUnit implements ASTNode {

    // TODO: Package?
    private Imports imports;
    private JavaClass javaClass;

    public CompilationUnit(Imports imports, JavaClass javaClass) {
        this.imports = imports;
        this.javaClass = javaClass;
    }

    public Imports getImports() {
        return this.imports;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }
}
