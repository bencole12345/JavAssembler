package codegen.generators;

import ast.expressions.*;
import ast.functions.FunctionTable;
import ast.literals.IntLiteral;
import ast.literals.LiteralValue;
import ast.operations.BinaryOp;
import ast.statements.Assignment;
import ast.structure.VariableScope;
import ast.types.PrimitiveType;
import codegen.CodeEmitter;
import codegen.CodeGenUtil;
import errors.IncorrectTypeException;

public class ExpressionGenerator {

    public static void compileExpression(Expression expression,
                                         CodeEmitter emitter,
                                         VariableScope scope,
                                         FunctionTable functionTable) {
        if (expression instanceof BinaryOperatorExpression) {
            compileBopExpression((BinaryOperatorExpression) expression, emitter, scope, functionTable);
        } else if (expression instanceof BinarySelectorExpression) {
            compileBinarySelectorExpression((BinarySelectorExpression) expression, emitter, scope, functionTable);
        } else if (expression instanceof VariableNameExpression) {
            compileVariableNameExpression((VariableNameExpression) expression, emitter, scope);
        } else if (expression instanceof LiteralValue) {
            LiteralGenerator.compileLiteralValue((LiteralValue) expression, emitter);
        } else if (expression instanceof FunctionCall) {
            compileFunctionCallExpression((FunctionCall) expression, emitter, scope, functionTable);
        } else if (expression instanceof NegateExpression) {
            compileNegateExpression((NegateExpression) expression, emitter, scope, functionTable);
        } else if (expression instanceof VariableIncrementExpression) {
            compileVariableIncrementExpression((VariableIncrementExpression) expression, emitter, scope, functionTable);
        }
    }

    private static void compileBopExpression(BinaryOperatorExpression bopExpression,
                                             CodeEmitter emitter,
                                             VariableScope variableScope,
                                             FunctionTable functionTable) {
        compileExpression(bopExpression.getLeft(), emitter, variableScope, functionTable);
        compileExpression(bopExpression.getRight(), emitter, variableScope, functionTable);
        // TODO: Actually use the type!
        PrimitiveType expressionType = PrimitiveType.Int;
        String typeString = CodeGenUtil.getTypeForPrimitive(expressionType);
        switch (bopExpression.getOp()) {
            case ADD:
                emitter.emitLine(typeString + ".add");
                CodeGenUtil.emitRangeRestrictionCode(expressionType, emitter);
                break;
            case SUBTRACT:
                emitter.emitLine(typeString + ".sub");
                CodeGenUtil.emitRangeRestrictionCode(expressionType, emitter);
                break;
            case MULTIPLY:
                // TODO: This probably won't work, as multiplying can add more bits
                // eg i32 * i32 = i64
                // but it'll do for now...
                emitter.emitLine(typeString + ".mul");
                break;
            case DIVIDE:
                emitter.emitLine(typeString + ".div");
                break;
            case EQUAL_TO:
                emitter.emitLine("eq");
                break;
            case NOT_EQUAL_TO:
                emitter.emitLine("ne");
                break;
            case LESS_THAN:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine("lt_s");
                } else {
                    emitter.emitLine("lt");
                }
                break;
            case LESS_THAN_OR_EQUAL_TO:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine("le_s");
                } else {
                    emitter.emitLine("le");
                }
                break;
            case GREATER_THAN:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine("gt_s");
                } else {
                    emitter.emitLine("gt");
                }
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine("ge_s");
                } else {
                    emitter.emitLine("ge");
                }
        }
    }

    private static void compileBinarySelectorExpression(BinarySelectorExpression binarySelectorExpression,
                                                        CodeEmitter emitter,
                                                        VariableScope scope,
                                                        FunctionTable functionTable) {
        // TODO: Use the if with type construction seen in the table at
        //  https://webassembly.org/docs/text-format/

        // EVEN BETTER: See https://webassembly.github.io/spec/core/syntax/instructions.html#parametric-instructions
    }

    private static void compileNegateExpression(NegateExpression negateExpression,
                                                CodeEmitter emitter,
                                                VariableScope scope,
                                                FunctionTable functionTable) {
        // TODO: Check we haven't broken the range by negating
        // (you have one more negative number available than you do positive numbers)
        emitter.emitLine("i32.const 0");
        compileExpression(negateExpression.getExpression(), emitter, scope, functionTable);
        emitter.emitLine("sub");
    }

    private static void compileVariableIncrementExpression(VariableIncrementExpression expression,
                                                           CodeEmitter emitter,
                                                           VariableScope variableScope,
                                                           FunctionTable functionTable) {
        // TODO: Check the type of the thing we are incrementing/decrementing
        //       (this affects whether we use i32.const or i64.const)
        // TODO: Make sure range is preserved
        //       (shouldn't be able to ++ a short to get out of the 16-bit range)
        int registerNumber = variableScope.lookupRegisterIndexOfVariable(expression.getVariableNameExpression().getVariableName());
        Expression varNameExpr = expression.getVariableNameExpression();
        Expression one = new IntLiteral(1);
        BinaryOperatorExpression bopExpr;
        Assignment assignment;
        try {
            switch (expression.getIncrementOp()) {
                case PRE_INCREMENT:
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.ADD);
                    assignment = new Assignment(expression.getVariableNameExpression(), bopExpr);
                    StatementGenerator.compileStatement(assignment, emitter, variableScope, functionTable);
                    emitter.emitLine("local.get " + registerNumber);
                    break;
                case PRE_DECREMENT:
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.SUBTRACT);
                    assignment = new Assignment(expression.getVariableNameExpression(), bopExpr);
                    StatementGenerator.compileStatement(assignment, emitter, variableScope, functionTable);
                    emitter.emitLine("local.get " + registerNumber);
                    break;
                case POST_INCREMENT:
                    emitter.emitLine("local.get " + registerNumber);
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.ADD);
                    assignment = new Assignment(expression.getVariableNameExpression(), bopExpr);
                    StatementGenerator.compileStatement(assignment, emitter, variableScope, functionTable);
                    break;
                case POST_DECREMENT:
                    emitter.emitLine("local.get " + registerNumber);
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.SUBTRACT);
                    assignment = new Assignment(expression.getVariableNameExpression(), bopExpr);
                    StatementGenerator.compileStatement(assignment, emitter, variableScope, functionTable);
            }
        } catch (IncorrectTypeException e) {
            e.printStackTrace();
        }
    }

    private static void compileVariableNameExpression(VariableNameExpression expression, CodeEmitter emitter, VariableScope variableScope) {
        int index = variableScope.lookupRegisterIndexOfVariable(expression.getVariableName());
        emitter.emitLine("local.get " + index);
    }

    private static void compileFunctionCallExpression(FunctionCall functionCall,
                                                      CodeEmitter emitter,
                                                      VariableScope variableScope,
                                                      FunctionTable functionTable) {
        for (Expression expression : functionCall.getArguments()) {
            compileExpression(expression, emitter, variableScope, functionTable);
        }

        // TODO: Use function table index not function name
        emitter.emitLine("call $" + functionCall.getFunctionTableEntry().getName());
    }
}
