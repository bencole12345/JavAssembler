package ast.expressions;

import ast.types.Type;

import java.util.List;

/**
 * Represents a call to a method on an object.
 */
public class MethodCall implements Expression {

    private LocalVariableExpression localVariable;
    private List<Expression> arguments;
    private Type returnType;
    private int virtualTableIndex;

    public MethodCall(LocalVariableExpression localVariable,
                      List<Expression> arguments,
                      Type returnType,
                      int virtualTableIndex) {
        this.localVariable = localVariable;
        this.arguments = arguments;
        this.returnType = returnType;
        this.virtualTableIndex = virtualTableIndex;
    }

    public LocalVariableExpression getLocalVariable() {
        return localVariable;
    }

    public int getVirtualTableIndex() {
        return virtualTableIndex;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public Type getType() {
        return returnType;
    }
}
