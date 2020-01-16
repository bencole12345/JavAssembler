package ast.expressions;

import ast.operations.IncrementOp;
import ast.statements.Statement;
import ast.types.PrimitiveType;
import ast.types.Type;
import errors.IncorrectTypeException;

public class VariableIncrementExpression implements Expression, Statement {

    private VariableNameExpression variableNameExpression;
    private IncrementOp incrementOp;

    public VariableIncrementExpression(VariableNameExpression variableNameExpression, IncrementOp incrementOp) throws IncorrectTypeException {
        if (!typeIsValid(variableNameExpression)) {
            String message = "Operation " + incrementOp + " must be applied to a variable expression";
            throw new IncorrectTypeException(message);
        }
        this.variableNameExpression = variableNameExpression;
        this.incrementOp = incrementOp;
    }

    public VariableNameExpression getVariableNameExpression() {
        return variableNameExpression;
    }

    public IncrementOp getIncrementOp() {
        return incrementOp;
    }

    @Override
    public Type getType() {
        return variableNameExpression.getType();
    }

    private boolean typeIsValid(VariableNameExpression variableNameExpression) {
        Type variableType = variableNameExpression.getType();
        if (!(variableType instanceof PrimitiveType))
            return false;
        return ((PrimitiveType) variableType).isIntegralType();
    }
}
