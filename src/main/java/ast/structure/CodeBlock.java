package ast.structure;

import ast.ASTNode;
import ast.statements.Statement;

import java.util.List;

public class CodeBlock implements ASTNode {

    private VariableScope variableScope;
    private List<Statement> statements;

    public CodeBlock(VariableScope variableScope, List<Statement> statements) {
        this.statements = statements;
        this.variableScope = variableScope;
    }

    public VariableScope getVariableScope() {
        return variableScope;
    }

    public List<Statement> getStatements() {
        return statements;
    }
}
