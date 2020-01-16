package ast.expressions;

import ast.types.PrimitiveType;
import ast.types.Type;
import errors.IncorrectTypeException;

/**
 * Wraps a binary selector expression.
 *
 * (That's one of the form
 *      cond ? e1 : e2)
 */
public class BinarySelectorExpression implements Expression {

    private Expression condition;
    private Expression trueExpression;
    private Expression falseExpression;

    public BinarySelectorExpression(Expression condition, Expression trueExpression, Expression falseExpression) throws IncorrectTypeException {
        validateTypes(condition.getType(), trueExpression.getType(), falseExpression.getType());
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getTrueExpression() {
        return trueExpression;
    }

    public Expression getFalseExpression() {
        return falseExpression;
    }

    @Override
    public Type getType() {
        return null;
    }

    /**
     * Validates that the types are legal for this expression.
     *
     * The condition must be a Boolean, and the true and false expressions must
     * both be of the same type.
     *
     * @param conditionType The type of the condition
     * @param trueExpressionType The type of the true expression
     * @param falseExpressionType The type of the false expression
     * @throws IncorrectTypeException if the types are incorrect
     */
    private void validateTypes(Type conditionType, Type trueExpressionType, Type falseExpressionType) throws IncorrectTypeException {
        if (!trueExpressionType.equals(falseExpressionType)) {
            String message = "Incorrect selector expression: cannot unify types "
                    + trueExpression.getType()
                    + " and " + falseExpression.getType();
            throw new IncorrectTypeException(message);
        }
        if (!conditionType.equals(PrimitiveType.Boolean)) {
            String message = "Incorrect selector expression: condition must be a Boolean, but got "
                    + condition.getType();
            throw new IncorrectTypeException(message);
        }
    }
}
