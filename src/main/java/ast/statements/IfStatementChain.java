package ast.statements;

import ast.expressions.Expression;
import ast.structure.CodeBlock;

/**
 * Wraps a chain of if-else blocks.
 *
 * Optionally includes an extra else block without a condition.
 */
public class IfStatementChain implements Statement {

    private Expression condition;
    private CodeBlock ifBlock;
    private IfStatementChain nextIfStatementChain;
    private CodeBlock elseBlock;

    public IfStatementChain(Expression condition, CodeBlock ifBlock) {
        this.condition = condition;
        this.ifBlock = ifBlock;
        nextIfStatementChain = null;
        elseBlock = null;
    }

    public IfStatementChain(Expression condition, CodeBlock ifBlock, IfStatementChain nextIfStatementChain) {
        this(condition, ifBlock);
        this.nextIfStatementChain = nextIfStatementChain;
    }

    public IfStatementChain(Expression condition, CodeBlock ifBlock, CodeBlock elseBlock) {
        this(condition, ifBlock);
        this.elseBlock = elseBlock;
    }

    public boolean hasNextIfStatementChain() {
        return nextIfStatementChain != null;
    }

    public boolean hasElseBlock() {
        return elseBlock != null;
    }

    public Expression getCondition() {
        return condition;
    }

    public CodeBlock getIfBlock() {
        return ifBlock;
    }

    public IfStatementChain getNextInChain() {
        return nextIfStatementChain;
    }

    public CodeBlock getElseBlock() {
        return elseBlock;
    }

}
