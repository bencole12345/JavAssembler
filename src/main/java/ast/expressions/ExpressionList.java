package ast.expressions;

import ast.ASTNode;

import java.util.List;

public class ExpressionList implements ASTNode {

    private List<Expression> expressionList;

    public ExpressionList(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }
}
