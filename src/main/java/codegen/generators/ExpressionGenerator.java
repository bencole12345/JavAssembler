package codegen.generators;

import ast.expressions.*;
import ast.literals.*;
import ast.operations.BinaryOp;
import ast.statements.Assignment;
import ast.structure.VariableScope;
import ast.types.JavaClass;
import ast.types.PrimitiveType;
import codegen.CodeEmitter;
import codegen.CodeGenUtil;
import codegen.WasmType;
import errors.IncorrectTypeException;
import util.ClassTable;
import util.FunctionTable;
import util.FunctionTableEntry;
import util.VirtualTable;

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
    private VirtualTable virtualTable;

    private ExpressionGenerator() {
        emitter = null;
        functionTable = null;
        classTable = null;
    }

    public void setCodeEmitter(CodeEmitter emitter) {
        this.emitter = emitter;
    }

    public void setTables(FunctionTable functionTable,
                          ClassTable classTable,
                          VirtualTable virtualTable) {
        this.functionTable = functionTable;
        this.classTable = classTable;
        this.virtualTable = virtualTable;
    }

    public void compileExpression(Expression expression, VariableScope scope) {
        if (expression instanceof BinaryOperatorExpression) {
            compileBopExpression((BinaryOperatorExpression) expression, scope);
        } else if (expression instanceof BinarySelectorExpression) {
            compileBinarySelectorExpression((BinarySelectorExpression) expression, scope);
        } else if (expression instanceof LocalVariableExpression) {
            compileLocalVariableNameExpression((LocalVariableExpression) expression, scope);
        } else if (expression instanceof AttributeNameExpression) {
            compileAttributeNameExpression((AttributeNameExpression) expression, scope);
        } else if (expression instanceof LiteralValue) {
            LiteralGenerator.getInstance().compileLiteralValue((LiteralValue) expression);
        } else if (expression instanceof FunctionCall) {
            compileFunctionCallExpression((FunctionCall) expression, scope);
        } else if (expression instanceof MethodCall) {
            compileMethodCallExpression((MethodCall) expression, scope);
        } else if (expression instanceof NegateExpression) {
            compileNegateExpression((NegateExpression) expression, scope);
        } else if (expression instanceof NotExpression) {
            compileNotExpression((NotExpression) expression, scope);
        } else if (expression instanceof VariableIncrementExpression) {
            compileVariableIncrementExpression((VariableIncrementExpression) expression, scope);
        } else if (expression instanceof NewObjectExpression) {
            compileNewObjectExpression((NewObjectExpression) expression, scope);
        } else if (expression instanceof NewArrayExpression) {
            compileNewArrayExpression((NewArrayExpression) expression, scope);
        } else if (expression instanceof ArrayIndexExpression) {
            compileArrayLookupExpression((ArrayIndexExpression) expression, scope);
        }
    }

    private void compileBopExpression(BinaryOperatorExpression bopExpression,
                                      VariableScope variableScope) {
        compileExpression(bopExpression.getLeft(), variableScope);
        compileExpression(bopExpression.getRight(), variableScope);
        PrimitiveType expressionType = bopExpression.getUnderlyingType();
        WasmType wasmType = CodeGenUtil.getWasmType(expressionType);
        switch (bopExpression.getOp()) {
            case ADD:
                emitter.emitLine(wasmType + ".add");
                CodeGenUtil.emitRangeRestrictionCode(expressionType, emitter);
                break;
            case SUBTRACT:
                emitter.emitLine(wasmType + ".sub");
                CodeGenUtil.emitRangeRestrictionCode(expressionType, emitter);
                break;
            case MULTIPLY:
                // TODO: This probably won't work, as multiplying can add more bits
                // eg i32 * i32 = i64
                // but it'll do for now...
                emitter.emitLine(wasmType + ".mul");
                break;
            case DIVIDE:
                emitter.emitLine(wasmType + ".div");
                break;
            case EQUAL_TO:
                emitter.emitLine(wasmType + ".eq");
                break;
            case NOT_EQUAL_TO:
                emitter.emitLine(wasmType + ".ne");
                break;
            case LESS_THAN:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".lt_s");
                } else {
                    emitter.emitLine(wasmType + ".lt");
                }
                break;
            case LESS_THAN_OR_EQUAL_TO:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".le_s");
                } else {
                    emitter.emitLine(wasmType + ".le");
                }
                break;
            case GREATER_THAN:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".gt_s");
                } else {
                    emitter.emitLine(wasmType + ".gt");
                }
                break;
            case GREATER_THAN_OR_EQUAL_TO:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".ge_s");
                } else {
                    emitter.emitLine(wasmType + ".ge");
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
        WasmType wasmType = CodeGenUtil.getWasmType(type);
        String signedSuffix = type.isIntegralType() ? "_s" : "";
        emitter.emitLine(wasmType + ".neg" + signedSuffix);
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
        // TODO: Support applying increments to attributes as well as local variables
        int registerNumber = variableScope.lookupRegisterIndexOfVariable(expression.getLocalVariableExpression().getVariableName());
        Expression varNameExpr = expression.getLocalVariableExpression();
        Expression one;
        PrimitiveType variableType = (PrimitiveType) expression.getLocalVariableExpression().getType();
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
                    assignment = new Assignment(expression.getLocalVariableExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
                    emitter.emitLine("local.get " + registerNumber);
                    break;
                case PRE_DECREMENT:
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.SUBTRACT);
                    assignment = new Assignment(expression.getLocalVariableExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
                    emitter.emitLine("local.get " + registerNumber);
                    break;
                case POST_INCREMENT:
                    emitter.emitLine("local.get " + registerNumber);
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.ADD);
                    assignment = new Assignment(expression.getLocalVariableExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
                    break;
                case POST_DECREMENT:
                    emitter.emitLine("local.get " + registerNumber);
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.SUBTRACT);
                    assignment = new Assignment(expression.getLocalVariableExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
            }
        } catch (IncorrectTypeException e) {
            e.printStackTrace();
        }
    }

    private void compileLocalVariableNameExpression(LocalVariableExpression expression,
                                                    VariableScope variableScope) {
        int index = variableScope.lookupRegisterIndexOfVariable(expression.getVariableName());
        emitter.emitLine("local.get " + index);
    }

    private void compileAttributeNameExpression(AttributeNameExpression attributeNameExpression,
                                                VariableScope scope) {
        // TODO: Implement layer of indirection for stack references to heap objects

        // Look up the local variable to leave the heap pointer on the stack
        compileLocalVariableNameExpression(attributeNameExpression.getObject(), scope);

        // Find the memory offset of the desired attribute
        int offset = attributeNameExpression.getMemoryOffset();

        // Leave it on the stack
        LiteralGenerator.getInstance().compileLiteralValue(new IntLiteral(offset));

        // Add the base and offset to find the heap address of the attribute
        emitter.emitLine("i32.add");

        // Look up this index from the heap
        WasmType wasmType = CodeGenUtil.getWasmType(attributeNameExpression.getType());
        emitter.emitLine(wasmType + ".load");
    }

    private void compileFunctionCallExpression(FunctionCall functionCall,
                                               VariableScope variableScope) {

        // Put arguments on the stack
        for (Expression expression : functionCall.getArguments()) {
            compileExpression(expression, variableScope);
        }

        // Call the function
        FunctionTableEntry tableEntry = functionCall.getFunctionTableEntry();
        String functionName = CodeGenUtil.getFunctionNameForOutput(tableEntry, functionTable);
        emitter.emitLine("call $" + functionName);
    }

    private void compileMethodCallExpression(MethodCall methodCall,
                                             VariableScope scope) {
        // Put the reference to the object on the stack; this is
        // the first argument to the function.
        compileLocalVariableNameExpression(methodCall.getLocalVariable(), scope);

        // Put the rest of the expressions on the stack
        for (Expression expression : methodCall.getArguments()) {
            compileExpression(expression, scope);
        }

        // Look up the variable
        compileLocalVariableNameExpression(methodCall.getLocalVariable(), scope);

        // Extract its vtable pointer
        emitter.emitLine("i32.load");

        // Add the offset for this particular method
        int vtableOffset = methodCall.getVirtualTableOffset();
        emitter.emitLine("i32.const " + vtableOffset);
        emitter.emitLine("i32.add");

        // Look up the type annotation for the indirect call
        String fullMethodName = CodeGenUtil.getFunctionNameForOutput(methodCall.getStaticFunctionEntry(), functionTable);
        String typeAnnotation = "(type $func_" + fullMethodName + ")";

        // Call the method
        emitter.emitLine("call_indirect " + typeAnnotation);
    }

    private void compileNewObjectExpression(NewObjectExpression newObjectExpression,
                                            VariableScope variableScope) {
        JavaClass javaClass = newObjectExpression.getType();
        int size = javaClass.getHeapSize();
        int vtableIndex = virtualTable.getVirtualTablePosition(javaClass);
        emitter.emitLine("i32.const " + size);
        emitter.emitLine("i32.const " + vtableIndex);
        emitter.emitLine("call $alloc_and_set_vtable");
        // TODO: Initialise variables
        // TODO: Invoke constructor
    }

    public void compileNewArrayExpression(NewArrayExpression newArrayExpression,
                                          VariableScope scope) {
        // TODO: Add the size for the header overhead to the argument passed
        // to alloc

        // Determine how big the array will be, by evaluating the expression
        // for its length and multiplying by element size, which will always
        // be 4 since arrays can only hold pointers.
        emitter.emitLine("i32.const 4");
        compileExpression(newArrayExpression.getLengthExpression(), scope);
        emitter.emitLine("i32.mul");

        // Now allocate the memory
        emitter.emitLine("call $alloc");
    }

    private void compileArrayLookupExpression(ArrayIndexExpression lookupExpression,
                                              VariableScope scope) {

        // Put the address of the start of the array on the stack
        Expression array = lookupExpression.getArrayExpression();
        compileExpression(array, scope);

        // Add 4 * index to this value to get the address to look up
        // (because references are all 32-bit/4-byte, and arrays can only
        // hold references)
        Expression index = lookupExpression.getIndexExpression();
        compileExpression(index, scope);
        emitter.emitLine("i32.const 4");
        emitter.emitLine("i32.mul"); // Because references are 4 bytes
        emitter.emitLine("i32.add");

        // Look up the value at this address
        WasmType type = CodeGenUtil.getWasmType(lookupExpression.getType());
        emitter.emitLine(type + ".load");
    }
}
