package ast.structure;

import ast.ASTNode;
import ast.types.Type;

public class MethodParameter implements ASTNode {

    private String parameterName;
    private Type type;

    public MethodParameter(String parameterName, Type type) {
        this.parameterName = parameterName;
        this.type = type;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Type getType() {
        return type;
    }
}
