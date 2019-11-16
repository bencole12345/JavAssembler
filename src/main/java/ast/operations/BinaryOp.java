package ast.operations;

public enum BinaryOp {

    ADD(OpType.Combiner),
    SUBTRACT(OpType.Combiner),
    MULTIPLY(OpType.Combiner),
    DIVIDE(OpType.Combiner),
    EQUAL_TO(OpType.Boolean),
    LESS_THAN(OpType.Boolean),
    LESS_THAN_OR_EQUAL_TO(OpType.Boolean),
    GREATER_THAN(OpType.Boolean),
    GREATER_THAN_OR_EQUAL_TO(OpType.Boolean);

    private OpType opType;

    BinaryOp(OpType opType) {
        this.opType = opType;
    }

    public OpType getOpType() {
        return opType;
    }
}
