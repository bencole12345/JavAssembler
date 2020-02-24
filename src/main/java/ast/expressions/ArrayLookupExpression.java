package ast.expressions;

import ast.types.ObjectArray;
import ast.types.PrimitiveType;
import ast.types.Type;
import errors.IncorrectTypeException;

public class ArrayLookupExpression implements VariableExpression {

    private Expression arrayExpression;
    private Expression indexExpression;

    public ArrayLookupExpression(Expression arrayExpression, Expression indexExpression) throws IncorrectTypeException {
        if (!(arrayExpression.getType() instanceof ObjectArray)) {
            String message = "Cannot perform indexed lookup on non-array type "
                    + arrayExpression.getType();
            throw new IncorrectTypeException(message);
        }
        if (!indexExpression.getType().isSubtypeOf(PrimitiveType.Int)) {
            String message = "Array index must be an integer, got " + indexExpression.getType();
            throw new IncorrectTypeException(message);
        }
        this.arrayExpression = arrayExpression;
        this.indexExpression = indexExpression;
    }

    public Expression getArrayExpression() {
        return arrayExpression;
    }

    public Expression getIndexExpression() {
        return indexExpression;
    }

    @Override
    public Type getType() {
        ObjectArray arrayType = (ObjectArray) arrayExpression.getType();
        return arrayType.getElementType();
    }
}
