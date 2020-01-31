package util;

import ast.types.AccessModifier;
import ast.types.Type;

/**
 * Contains data about a single function.
 */
public class FunctionTableEntry {

    private int index;
    private String namespace;
    private String functionName;
    private Type returnType;
    private AccessModifier accessModifier;

    public FunctionTableEntry(int index,
                              String namespace,
                              String functionName,
                              Type returnType,
                              AccessModifier accessModifier) {
        this.index = index;
        this.namespace = namespace;
        this.functionName = functionName;
        this.returnType = returnType;
        this.accessModifier = accessModifier;
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

    /**
     * Determines whether the method referenced by this table entry can be
     * executed from the given context.
     *
     * If the method is public then this will always return true. For private
     * methods, it will return true only if the function is called from the
     * same class.
     *
     * @param context The name of the class from which the method is called
     * @return true if the method can legally be executed; false otherwise
     */
    public boolean canBeCalledFrom(String context) {
        if (accessModifier.equals(AccessModifier.PUBLIC))
            return true;
        else
            return context.equals(namespace);
    }
}
