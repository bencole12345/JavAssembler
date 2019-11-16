package ast.expressions;

import ast.structure.Statement;

public class VariableIncrementExpression implements Expression, Statement {

    public enum IncrementType {
        PRE_INCREMENT,
        PRE_DECREMENT,
        POST_INCREMENT,
        POST_DECREMENT
    }

    private VariableNameExpression variableNameExpression;
    private IncrementType incrementType;

    public VariableIncrementExpression(VariableNameExpression variableNameExpression, IncrementType incrementType) {
        this.variableNameExpression = variableNameExpression;
        this.incrementType = incrementType;
    }

    public VariableNameExpression getVariableNameExpression() {
        return variableNameExpression;
    }

    public IncrementType getIncrementType() {
        return incrementType;
    }
}
