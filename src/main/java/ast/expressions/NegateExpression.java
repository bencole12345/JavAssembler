package ast.expressions;

import ast.types.PrimitiveType;
import ast.types.Type;
import errors.IncorrectTypeException;

public class NegateExpression implements Expression {

    private Expression expression;

    public NegateExpression(Expression expression) throws IncorrectTypeException {
        Type type = expression.getType();
        if (!isValidType(type))
            throw new IncorrectTypeException("Cannot negate type " + type);
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    /**
     * Determines whether the type is valid for a negation expression.
     *
     * @param type The type to check
     * @return true if valid; false otherwise
     */
    private boolean isValidType(Type type) {
        if (!(type instanceof PrimitiveType))
            return false;
        return ((PrimitiveType) type).isNumericType();
    }
}
