package ast.expressions;

import ast.operations.IncrementOp;
import ast.statements.Statement;
import ast.types.PrimitiveType;
import ast.types.Type;
import errors.IncorrectTypeException;

public class VariableIncrementExpression implements Expression, Statement {

    // TODO: Swap to VariableExpression
    private LocalVariableExpression localVariableExpression;
    private IncrementOp incrementOp;

    public VariableIncrementExpression(LocalVariableExpression localVariableExpression, IncrementOp incrementOp) throws IncorrectTypeException {
        if (!typeIsValid(localVariableExpression)) {
            String message = "Operation " + incrementOp + " must be applied to a variable expression";
            throw new IncorrectTypeException(message);
        }
        this.localVariableExpression = localVariableExpression;
        this.incrementOp = incrementOp;
    }

    public LocalVariableExpression getLocalVariableExpression() {
        return localVariableExpression;
    }

    public IncrementOp getIncrementOp() {
        return incrementOp;
    }

    @Override
    public Type getType() {
        return localVariableExpression.getType();
    }

    private boolean typeIsValid(LocalVariableExpression localVariableExpression) {
        Type variableType = localVariableExpression.getType();
        if (!(variableType instanceof PrimitiveType))
            return false;
        return ((PrimitiveType) variableType).isIntegralType();
    }
}
