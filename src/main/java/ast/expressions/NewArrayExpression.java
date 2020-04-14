package ast.expressions;

import ast.types.HeapObjectReference;
import ast.types.ObjectArray;
import ast.types.PrimitiveType;
import ast.types.Type;
import errors.IncorrectTypeException;

public class NewArrayExpression implements Expression {

    private Type elementType;
    private Expression lengthExpression;
    private ObjectArray arrayType;

    public NewArrayExpression(Type elementType, Expression lengthExpression) throws IncorrectTypeException {
        if (!lengthExpression.getType().equals(PrimitiveType.Int)) {
            String message = "Invalid type: array length must be an integer.";
            throw new IncorrectTypeException(message);
        }
        this.elementType = elementType;
        this.lengthExpression = lengthExpression;
        arrayType = new ObjectArray(elementType);
    }

    public Type getElementType() {
        return elementType;
    }

    public Expression getLengthExpression() {
        return lengthExpression;
    }

    @Override
    public Type getType() {
        return arrayType;
    }
}
