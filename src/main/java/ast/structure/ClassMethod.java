package ast.structure;

import ast.ASTNode;
import ast.types.AccessModifier;
import ast.types.Type;

import java.util.List;

public class ClassMethod implements ASTNode {

    private AccessModifier accessModifier;
    private boolean isStatic;
    private Type returnType;
    private String name;
    private List<MethodParameter> params;
    private CodeBlock body;

    public ClassMethod(AccessModifier accessModifier, boolean isStatic, Type returnType, String name, List<MethodParameter> params, CodeBlock body) {
        this.accessModifier = accessModifier;
        this.isStatic = isStatic;
        this.returnType = returnType;
        this.name = name;
        this.params = params;
        this.body = body;

        for (MethodParameter param : params) {
            String paramName = param.getParameterName();
            Type type = param.getType();
            body.getVariableScope().registerVariable(paramName, type, VariableScope.Domain.Parameter);
        }
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public Type getReturnType() {
        return returnType;
    }

    public String getName() {
        return name;
    }

    public List<MethodParameter> getParams() {
        return params;
    }

    public CodeBlock getBody() {
        return body;
    }

    /**
     * Sets the VariableScope that contains this code block.
     *
     * @param containingScope The new VariableScope to contain this code block
     */
    public void bindContainingVariableScope(VariableScope containingScope) {
        body.bindContainingVariableScope(containingScope);
    }
}
