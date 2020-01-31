package ast.expressions;

import ast.types.Type;
import util.FunctionTableEntry;

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

    // TODO: Consider finding a way to also make this a statement

    private FunctionTableEntry tableEntry;
    private List<Expression> arguments;

    public FunctionCall(FunctionTableEntry functionTableEntry, List<Expression> arguments) {
        this.tableEntry = functionTableEntry;
        this.arguments = arguments;
    }

    public FunctionTableEntry getFunctionTableEntry() {
        return tableEntry;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public Type getType() {
        return tableEntry.getReturnType();
    }
}
