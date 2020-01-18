package ast.expressions;

import ast.operations.BinaryOp;
import ast.types.PrimitiveType;
import ast.types.Type;
import errors.IncorrectTypeException;

public class BinaryOperatorExpression implements Expression {

    private Expression left;
    private Expression right;
    private BinaryOp op;

    public BinaryOperatorExpression(Expression left, Expression right, BinaryOp op) throws IncorrectTypeException {
        if (!typesAreLegal(left.getType(), right.getType(), op)) {
            String message = "Cannot apply operation " + op.name() + " to types "
                    + left.getType() + " and " + right.getType();
            throw new IncorrectTypeException(message);
        }
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public BinaryOp getOp() {
        return op;
    }

    @Override
    public PrimitiveType getType() {
        // We already have the guarantee that the left and right are of the
        // same type, thanks to the constructor
        switch (op.getOpType()) {
            case Combiner:
                // TODO: Handle typing judgements correctly.
                // Think: int * double vs double * int
                // These are all legal in Java, and a double comes out
                // Probably need to implement a method in PrimitiveType
                // like .getResultingTypeWhenCombinedWith(PrimitiveType other)
                // There should definitely be a good reference for this
                return (PrimitiveType) left.getType();
            case Comparison:
                return PrimitiveType.Boolean;
            default:
                return null;
        }
    }

    /**
     * Determines whether the operation is legal given the types of the left
     * and right expressions.
     *
     * @param leftType The type of the left expression
     * @param rightType The type of the right expression
     * @param op The operation performed
     * @return true if the types are legal; false otherwise
     */
    private boolean typesAreLegal(Type leftType, Type rightType, BinaryOp op) {

        // We only permit binary operations on primitive types
        // (no overloading!)
        if (!(leftType instanceof PrimitiveType))
            return false;
        if (!(rightType instanceof PrimitiveType))
            return false;

        return leftType.equals(rightType);
    }
}
