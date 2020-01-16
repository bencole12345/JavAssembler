package ast.statements;

import ast.expressions.Expression;
import ast.structure.CodeBlock;
import ast.types.PrimitiveType;
import errors.IncorrectTypeException;

public class WhileLoop implements Statement {

    private Expression condition;
    private CodeBlock codeBlock;

    public WhileLoop(Expression condition, CodeBlock codeBlock) throws IncorrectTypeException {
        if (!condition.getType().equals(PrimitiveType.Boolean)) {
            String message = "while-loop conditions must be booleans, but got "
                    + condition.getType();
            throw new IncorrectTypeException(message);
        }
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
