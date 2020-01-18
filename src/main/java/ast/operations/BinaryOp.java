package ast.operations;

public enum BinaryOp {

    ADD(OpType.Combiner),
    SUBTRACT(OpType.Combiner),
    MULTIPLY(OpType.Combiner),
    DIVIDE(OpType.Combiner),
    EQUAL_TO(OpType.Comparison),
    NOT_EQUAL_TO(OpType.Comparison),
    LESS_THAN(OpType.Comparison),
    LESS_THAN_OR_EQUAL_TO(OpType.Comparison),
    GREATER_THAN(OpType.Comparison),
    GREATER_THAN_OR_EQUAL_TO(OpType.Comparison);

    private OpType opType;

    BinaryOp(OpType opType) {
        this.opType = opType;
    }

    public OpType getOpType() {
        return opType;
    }
}
