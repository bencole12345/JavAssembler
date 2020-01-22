package ast.functions;

import ast.types.Type;

/**
 * Contains data about a single function.
 */
public class FunctionTableEntry {

    private int index;
    private String namespace;
    private String functionName;
    private Type returnType;

    public FunctionTableEntry(int index,
                              String namespace,
                              String functionName,
                              Type returnType) {
        this.index = index;
        this.namespace = namespace;
        this.functionName = functionName;
        this.returnType = returnType;
    }

    public int getIndex() {
        return index;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getFunctionName() {
        return functionName;
    }

    public Type getReturnType() {
        return returnType;
    }
}
