package ast.expressions;

import java.util.List;

public class FunctionCall implements Expression {

    /**
     * Don't forget that a function call can occur on its own:
     *      doSomething();
     * or alternatively in an assignment:
     *      x = calculateSomething();
     * So need to think about how to integrate this type with structure.Statement
     *
     * We don't really want to allow arbitrary expressions at the top level, eg
     *      1+2;
     * should not be a possible statement.
     */

    // TODO: Consider find a way to also make this a statement

    private String functionName;
    private List<Expression> arguments;

    public FunctionCall(String functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}
