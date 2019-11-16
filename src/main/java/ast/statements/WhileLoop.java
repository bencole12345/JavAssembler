package ast.statements;

import ast.expressions.Expression;
import ast.structure.CodeBlock;

public class WhileLoop implements Statement {

    private Expression condition;
    private CodeBlock codeBlock;

    public WhileLoop(Expression condition, CodeBlock codeBlock) {
        this.condition = condition;
        this.codeBlock = codeBlock;
    }

    public Expression getCondition() {
        return condition;
    }

    public CodeBlock getCodeBlock() {
        return codeBlock;
    }
}
