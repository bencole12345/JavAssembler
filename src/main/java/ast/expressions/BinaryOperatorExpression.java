package ast.expressions;

public class BinaryOperatorExpression extends Expression {

    public enum Operation {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE
    }

    private Expression left;
    private Expression right;
    private Operation op;

    public BinaryOperatorExpression(Expression left, Expression right, Operation op) {
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

    public Operation getOp() {
        return op;
    }
}
