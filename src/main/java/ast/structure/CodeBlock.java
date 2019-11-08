package ast.structure;

import ast.ASTNode;
import ast.types.Type;

import java.util.List;
import java.util.Map;

public class CodeBlock implements ASTNode {

    public static class VariableDeclarationPlaceholder {}

    // TODO: Figure out how to handle this - should it be done while building the AST?
    private CodeBlock enclosingBlock;

    private Map<String, Type> variableDeclarations;
    private List<Statement> statements;  // Note: an if statement will be counted as a single statement

    public CodeBlock(Map<String, Type> variableDeclarations, List<Statement> statements) {
        this.variableDeclarations = variableDeclarations;
        this.statements = statements;
    }

    public Map<String, Type> getVariableDeclarations() {
        return variableDeclarations;
    }

    public List<Statement> getStatements() {
        return statements;
    }
}
