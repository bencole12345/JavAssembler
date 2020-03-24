package ast.operations;

public enum BinaryOp {

    Add(OpType.Combiner),
    Subtract(OpType.Combiner),
    Multiply(OpType.Combiner),
    Divide(OpType.Combiner),
    EqualTo(OpType.Comparison),
    NotEqualTo(OpType.Comparison),
    LessThan(OpType.Comparison),
    LessThanOrEqualTo(OpType.Comparison),
    GreaterThan(OpType.Comparison),
    GreaterThanOrEqualTo(OpType.Comparison),
    LogicalAnd(OpType.Logical),
    LogicalOr(OpType.Logical);

    private OpType opType;

    BinaryOp(OpType opType) {
        this.opType = opType;
    }

    public OpType getOpType() {
        return opType;
    }
}
