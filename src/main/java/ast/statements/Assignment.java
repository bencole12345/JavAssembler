package ast.statements;

import ast.expressions.Expression;
import ast.expressions.VariableNameExpression;
import ast.types.Type;
import errors.IncorrectTypeException;

public class Assignment implements Statement {

    private VariableNameExpression variableNameExpression;
    private Expression expression;

    public Assignment(VariableNameExpression variableNameExpression, Expression expression) throws IncorrectTypeException {
        if (!typesAreValid(variableNameExpression.getType(), expression.getType())) {
            String message = "Attempted to assign an expression of type " + expression.getType()
                    + " to variable " + variableNameExpression.getVariableName()
                    + " of type " + variableNameExpression.getType();
            throw new IncorrectTypeException(message);
        }
        this.variableNameExpression = variableNameExpression;
        this.expression = expression;
    }

    public VariableNameExpression getVariableNameExpression() {
        return variableNameExpression;
    }

    public Expression getExpression() {
        return expression;
    }

    private boolean typesAreValid(Type variableType, Type expressionType) {
        return expressionType.isSubtypeOf(variableType);
    }
}
