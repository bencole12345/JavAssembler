package codegen.generators;

import ast.expressions.*;
import ast.literals.*;
import ast.operations.BinaryOp;
import ast.statements.Assignment;
import ast.structure.VariableScope;
import ast.types.JavaClass;
import ast.types.PrimitiveType;
import ast.types.Type;
import codegen.CodeEmitter;
import codegen.CodeGenUtil;
import errors.IncorrectTypeException;
import util.ClassTable;
import util.FunctionTable;
import util.FunctionTableEntry;

import java.util.List;
import java.util.stream.Collectors;

public class ExpressionGenerator {

    private static ExpressionGenerator INSTANCE;

    public static ExpressionGenerator getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ExpressionGenerator();
        return INSTANCE;
    }

    private CodeEmitter emitter;
    private FunctionTable functionTable;
    private ClassTable classTable;

    private ExpressionGenerator() {
        emitter = null;
        functionTable = null;
        classTable = null;
    }

    public void setCodeEmitter(CodeEmitter emitter) {
        this.emitter = emitter;
    }

    public void setTables(FunctionTable functionTable, ClassTable classTable) {
        this.functionTable = functionTable;
        this.classTable = classTable;
    }

    public void compileExpression(Expression expression, VariableScope scope) {
        if (expression instanceof BinaryOperatorExpression) {
            compileBopExpression((BinaryOperatorExpression) expression, scope);
        } else if (expression instanceof BinarySelectorExpression) {
            compileBinarySelectorExpression((BinarySelectorExpression) expression, scope);
        } else if (expression instanceof VariableNameExpression) {
            compileVariableNameExpression((VariableNameExpression) expression, scope);
        } else if (expression instanceof LiteralValue) {
            LiteralGenerator.getInstance().compileLiteralValue((LiteralValue) expression);
        } else if (expression instanceof FunctionCall) {
            compileFunctionCallExpression((FunctionCall) expression, scope);
        } else if (expression instanceof NegateExpression) {
            compileNegateExpression((NegateExpression) expression, scope);
        } else if (expression instanceof NotExpression) {
            compileNotExpression((NotExpression) expression, scope);
        } else if (expression instanceof VariableIncrementExpression) {
            compileVariableIncrementExpression((VariableIncrementExpression) expression, emitter, scope, functionTable);
        }
    }

    private void compileBopExpression(BinaryOperatorExpression bopExpression,
                                      VariableScope variableScope) {
        compileExpression(bopExpression.getLeft(), variableScope);
        compileExpression(bopExpression.getRight(), variableScope);
        PrimitiveType expressionType = bopExpression.getUnderlyingType();
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
                emitter.emitLine(typeString + ".eq");
                break;
            case NOT_EQUAL_TO:
                emitter.emitLine(typeString + ".ne");
                break;
            case LESS_THAN:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(typeString + ".lt_s");
                } else {
                    emitter.emitLine(typeString + ".lt");
                }
                break;
            case LESS_THAN_OR_EQUAL_TO:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(typeString + ".le_s");
                } else {
                    emitter.emitLine(typeString + ".le");
                }
                break;
            case GREATER_THAN:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(typeString + ".gt_s");
                } else {
                    emitter.emitLine(typeString + ".gt");
                }
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(typeString + ".ge_s");
                } else {
                    emitter.emitLine(typeString + ".ge");
                }
        }
    }

    private void compileBinarySelectorExpression(BinarySelectorExpression binarySelectorExpression,
                                                 VariableScope variableScope) {
        // TODO: Use the if with type construction seen in the table at
        //  https://webassembly.org/docs/text-format/

        // EVEN BETTER: See https://webassembly.github.io/spec/core/syntax/instructions.html#parametric-instructions
    }

    private void compileNegateExpression(NegateExpression negateExpression,
                                         VariableScope scope) {
        // TODO: Check we haven't broken the range by negating
        // (you have one more negative number available than you do positive numbers)
        compileExpression(negateExpression.getExpression(), scope);
        PrimitiveType type = negateExpression.getType();
        String typeString = CodeGenUtil.getTypeForPrimitive(type);
        String signedSuffix = type.isIntegralType() ? "_s" : "";
        emitter.emitLine(typeString + ".neg" + signedSuffix);
    }

    private void compileNotExpression(NotExpression notExpression,
                                      VariableScope scope) {
        emitter.emitLine("i32.const 1");
        compileExpression(notExpression.getExpression(), scope);
        emitter.emitLine("i32.sub");
    }

    private void compileVariableIncrementExpression(VariableIncrementExpression expression,
                                                    VariableScope variableScope) {
        // TODO: Make sure range is preserved
        //       (shouldn't be able to ++ a short to get out of the 16-bit range)
        int registerNumber = variableScope.lookupRegisterIndexOfVariable(expression.getVariableNameExpression().getVariableName());
        Expression varNameExpr = expression.getVariableNameExpression();
        Expression one;
        PrimitiveType variableType = (PrimitiveType) expression.getVariableNameExpression().getType();
        // TODO: Support the rest of the primitives
        switch (variableType) {
            case Short:
                one = new ShortLiteral((short) 1);
                break;
            case Long:
                one = new LongLiteral(1);
                break;
            case Float:
                one = new FloatLiteral(1);
                break;
            case Double:
                one = new DoubleLiteral(1);
                break;
            default:
                one = new IntLiteral(1);
        }
        BinaryOperatorExpression bopExpr;
        Assignment assignment;
        try {
            switch (expression.getIncrementOp()) {
                case PRE_INCREMENT:
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.ADD);
                    assignment = new Assignment(expression.getVariableNameExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
                    emitter.emitLine("local.get " + registerNumber);
                    break;
                case PRE_DECREMENT:
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.SUBTRACT);
                    assignment = new Assignment(expression.getVariableNameExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
                    emitter.emitLine("local.get " + registerNumber);
                    break;
                case POST_INCREMENT:
                    emitter.emitLine("local.get " + registerNumber);
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.ADD);
                    assignment = new Assignment(expression.getVariableNameExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
                    break;
                case POST_DECREMENT:
                    emitter.emitLine("local.get " + registerNumber);
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.SUBTRACT);
                    assignment = new Assignment(expression.getVariableNameExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
            }
        } catch (IncorrectTypeException e) {
            e.printStackTrace();
        }
    }

    private void compileVariableNameExpression(VariableNameExpression expression,
                                               VariableScope variableScope) {
        int index = variableScope.lookupRegisterIndexOfVariable(expression.getVariableName());
        emitter.emitLine("local.get " + index);
    }

    private void compileFunctionCallExpression(FunctionCall functionCall,
                                               VariableScope variableScope) {
        for (Expression expression : functionCall.getArguments()) {
            compileExpression(expression, variableScope);
        }

        // TODO: Use function table index not function name
        FunctionTableEntry tableEntry = functionCall.getFunctionTableEntry();
        List<Type> argumentTypes = functionCall.getArguments()
                .stream()
                .map(Expression::getType)
                .collect(Collectors.toList());
        String functionName = CodeGenUtil.getFunctionNameForOutput(tableEntry, argumentTypes, functionTable);
        emitter.emitLine("call $" + functionName);
    }
}
