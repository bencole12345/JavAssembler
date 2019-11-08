package ast.structure;

import ast.ASTNode;

import java.util.List;

public class MethodParameterList implements ASTNode {

    private List<MethodParameter> parameters;

    public MethodParameterList(List<MethodParameter> parameters) {
        this.parameters = parameters;
    }

    public List<MethodParameter> getParameters() {
        return parameters;
    }

}
