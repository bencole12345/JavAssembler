package ast.functions;

import ast.types.Type;

/**
 * Contains data about a single function.
 */
public class FunctionTableEntry {

    private int index;
    private String name;
    private Type returnType;

    public FunctionTableEntry(int index, String name, Type returnType) {
        this.index = index;
        this.name = name;
        this.returnType = returnType;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }
}
