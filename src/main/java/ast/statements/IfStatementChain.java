package ast.statements;

import ast.expressions.Expression;
import ast.structure.CodeBlock;

import java.util.List;

/**
 * Wraps a chain of if-else blocks.
 *
 * Optionally includes an extra else block without a condition.
 */
public class IfStatementChain implements Statement {

    /**
     * The ith condition expression should be the Boolean expression for the ith code block.
     */
    private List<Expression> conditions;
    private List<CodeBlock> codeBlocks;
    private CodeBlock elseBlock;

    public IfStatementChain(List<Expression> conditions, List<CodeBlock> codeBlocks) {
        this.conditions = conditions;
        this.codeBlocks = codeBlocks;
        elseBlock = null;
    }

    public IfStatementChain(List<Expression> conditions, List<CodeBlock> codeBlocks, CodeBlock elseBlock) {
        this.conditions = conditions;
        this.codeBlocks = codeBlocks;
        this.elseBlock = elseBlock;
    }

    public boolean hasElseBlock() {
        return elseBlock != null;
    }

    public void prependBlock(Expression condition, CodeBlock codeBlock) {
        conditions.add(0, condition);
        codeBlocks.add(0, codeBlock);
    }
}
