package ast.structure;

import ast.ASTNode;
import ast.functions.FunctionTable;

/**
 * Represents a .java file.
 */
public class CompilationUnit implements ASTNode {

    // TODO: Package?
    private Imports imports;
    private JavaClass javaClass;
    private FunctionTable functionTable;

    public CompilationUnit(Imports imports, JavaClass javaClass, FunctionTable functionTable) {
        this.imports = imports;
        this.javaClass = javaClass;
        this.functionTable = functionTable;
    }

    public Imports getImports() {
        return this.imports;
    }

    public JavaClass getJavaClass() {
        return javaClass;
    }

    public FunctionTable getFunctionTable() {
        return functionTable;
    }
}
