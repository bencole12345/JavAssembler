package ast.statements;

import ast.expressions.Expression;
import ast.expressions.VariableExpression;
import ast.types.Type;
import errors.IncorrectTypeException;

public class Assignment implements Statement {

    private VariableExpression variableExpression;
    private Expression expression;

    public Assignment(VariableExpression variableExpression,
                      Expression expression)
            throws IncorrectTypeException {
        if (!typesAreValid(variableExpression.getType(), expression.getType())) {
            String message = "Attempted to assign an expression of type " + expression.getType()
                    + " to variable " + variableExpression
                    + " of type " + variableExpression.getType();
            throw new IncorrectTypeException(message);
        }
        this.variableExpression = variableExpression;
        this.expression = expression;
    }

    public VariableExpression getVariableExpression() {
        return variableExpression;
    }

    public Expression getExpression() {
        return expression;
    }

    private boolean typesAreValid(Type variableType, Type expressionType) {
        return expressionType.isSubtypeOf(variableType);
    }
}
