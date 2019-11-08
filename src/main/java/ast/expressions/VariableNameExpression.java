package ast.expressions;

/**
 * This is one of the 'base case' expressions, denoting a variable name.
 */
public class VariableNameExpression extends Expression {

    private String variableName;

    public VariableNameExpression(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }
}
