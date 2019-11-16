package ast.expressions;

import ast.operations.IncrementOp;
import ast.statements.Statement;

public class VariableIncrementExpression implements Expression, Statement {

    private VariableNameExpression variableNameExpression;
    private IncrementOp incrementOp;

    public VariableIncrementExpression(VariableNameExpression variableNameExpression, IncrementOp incrementOp) {
        this.variableNameExpression = variableNameExpression;
        this.incrementOp = incrementOp;
    }

    public VariableNameExpression getVariableNameExpression() {
        return variableNameExpression;
    }

    public IncrementOp getIncrementOp() {
        return incrementOp;
    }
}
