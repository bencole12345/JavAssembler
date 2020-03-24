package parser;

import ast.operations.BinaryOp;

public class BopVisitor extends JavaFileBaseVisitor<BinaryOp> {

    @Override
    public BinaryOp visitMultiplicativeBop(JavaFileParser.MultiplicativeBopContext ctx) {
        switch (ctx.op.getType()) {
            case JavaFileParser.MULTIPLY:
                return BinaryOp.Multiply;
            case JavaFileParser.DIVIDE:
                return BinaryOp.Divide;
            default:
                return null;
        }
    }

    @Override
    public BinaryOp visitAdditiveBop(JavaFileParser.AdditiveBopContext ctx) {
        switch (ctx.op.getType()) {
            case JavaFileParser.PLUS:
                return BinaryOp.Add;
            case JavaFileParser.MINUS:
                return BinaryOp.Subtract;
            default:
                return null;
        }
    }

    @Override
    public BinaryOp visitComparisonBop(JavaFileParser.ComparisonBopContext ctx) {
        switch (ctx.op.getType()) {
            case JavaFileParser.EQUAL_TO:
                return BinaryOp.EqualTo;
            case JavaFileParser.NOT_EQUAL_TO:
                return BinaryOp.NotEqualTo;
            case JavaFileParser.LESS_THAN:
                return BinaryOp.LessThan;
            case JavaFileParser.LESS_THAN_EQUAL_TO:
                return BinaryOp.LessThanOrEqualTo;
            case JavaFileParser.GREATER_THAN:
                return BinaryOp.GreaterThan;
            case JavaFileParser.GREATER_THAN_EQUAL_TO:
                return BinaryOp.GreaterThanOrEqualTo;
            default:
                return null;
        }
    }

    @Override
    public BinaryOp visitLogicalAndBop(JavaFileParser.LogicalAndBopContext ctx) {
        return BinaryOp.LogicalAnd;
    }

    @Override
    public BinaryOp visitLogicalOrBop(JavaFileParser.LogicalOrBopContext ctx) {
        return BinaryOp.LogicalOr;
    }
}
