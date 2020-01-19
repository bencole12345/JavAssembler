package ast.statements;

import ast.expressions.Expression;
import ast.structure.CodeBlock;
import ast.types.PrimitiveType;
import errors.IncorrectTypeException;

public class ForLoop implements Statement {

    private Statement initialiser;
    private Expression condition;
    private Expression updater;
    private CodeBlock codeBlock;

    public ForLoop(Statement initialiser, Expression condition, Expression updater, CodeBlock codeBlock) throws IncorrectTypeException {
        if (!condition.getType().equals(PrimitiveType.Boolean)) {
            String message = "for-loop conditions must be booleans, but got "
                    + condition.getType();
            throw new IncorrectTypeException(message);
        }
        this.initialiser = initialiser;
        this.condition = condition;
        this.updater = updater;
        this.codeBlock = codeBlock;
    }

    public Statement getInitialiser() {
        return initialiser;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getUpdater() {
        return updater;
    }

    public CodeBlock getCodeBlock() {
        return codeBlock;
    }
}
