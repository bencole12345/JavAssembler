package ast.expressions;

import java.util.List;

public class ExpressionList implements Expression {

    private List<Expression> expressionList;

    public ExpressionList(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }
}
