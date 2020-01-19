package ast.expressions;

import ast.types.PrimitiveType;
import ast.types.Type;
import errors.IncorrectTypeException;

/**
 * Represents a Boolean not expression.
 */
public class NotExpression implements Expression {

    private Expression expression;

    public NotExpression(Expression expression) throws IncorrectTypeException {
        Type type = expression.getType();
        if (!type.isSubtypeOf(PrimitiveType.Boolean)) {
            String message = "Cannot negate an expression of type " + type;
            throw new IncorrectTypeException(message);
        }

        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public Type getType() {
        return PrimitiveType.Boolean;
    }
}
