package ast.expressions;

import ast.statements.Statement;
import ast.types.Type;
import util.FunctionTableEntry;

import java.util.List;

public class FunctionCall implements Expression, Statement {

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
