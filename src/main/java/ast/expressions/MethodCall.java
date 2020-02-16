package ast.expressions;

import ast.types.Type;
import util.FunctionTableEntry;

import java.util.List;

/**
 * Represents a call to a method on an object.
 */
public class MethodCall implements Expression {

    private LocalVariableExpression localVariable;
    private List<Expression> arguments;
    private Type returnType;
    private int virtualTableOffset;
    private FunctionTableEntry staticFunctionEntry;

    public MethodCall(LocalVariableExpression localVariable,
                      List<Expression> arguments,
                      Type returnType,
                      int virtualTableOffset,
                      FunctionTableEntry staticFunctionEntry) {
        // TODO: Validate types
        // In particular, check that the local variable is a JavaClass, and
        // maybe check that the parameters are correct for that method?
        this.localVariable = localVariable;
        this.arguments = arguments;
        this.returnType = returnType;
        this.virtualTableOffset = virtualTableOffset;
        this.staticFunctionEntry = staticFunctionEntry;
    }

    public LocalVariableExpression getLocalVariable() {
        return localVariable;
    }

    public int getVirtualTableOffset() {
        return virtualTableOffset;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    /**
     * Returns the function that would be used were we using static
     * polymorphism.
     *
     * This is needed for the code generation stage, in which we need to use a
     * type annotation whenever making an indirect call.
     *
     * @return The function table entry corresponding to the function that would
     *      be called were we using static polymorphism
     */
    public FunctionTableEntry getStaticFunctionEntry() {
        return staticFunctionEntry;
    }

    @Override
    public Type getType() {
        return returnType;
    }
}
