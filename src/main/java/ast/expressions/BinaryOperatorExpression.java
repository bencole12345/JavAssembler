package ast.expressions;

import ast.operations.BinaryOp;

public class BinaryOperatorExpression implements Expression {

    private Expression left;
    private Expression right;
    private BinaryOp op;

    public BinaryOperatorExpression(Expression left, Expression right, BinaryOp op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public BinaryOp getOp() {
        return op;
    }
}
