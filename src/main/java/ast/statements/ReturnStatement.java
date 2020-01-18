package ast.statements;

import ast.expressions.Expression;
import ast.types.Type;
import errors.IncorrectTypeException;

public class ReturnStatement implements Statement {

    private Expression expression;

    public ReturnStatement(Expression expression, Type expectedType) throws IncorrectTypeException {
        if (!expression.getType().isSubtypeOf(expectedType)) {
            String message = "Cannot return type " + expression.getType()
                    + " when method return type is " + expectedType;
            throw new IncorrectTypeException(message);
        }
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }
}
