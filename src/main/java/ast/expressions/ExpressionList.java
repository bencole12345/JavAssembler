package ast.expressions;

import ast.types.Tuple;
import ast.types.Type;

import java.util.List;
import java.util.stream.Collectors;

public class ExpressionList implements Expression {

    private List<Expression> expressionList;

    public ExpressionList(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }

    @Override
    public Type getType() {
        List<Type> types = expressionList
                .stream()
                .map(Expression::getType)
                .collect(Collectors.toList());
        return new Tuple(types);
    }
}
