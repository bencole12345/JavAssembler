package codegen.generators;

import ast.expressions.*;
import ast.literals.*;
import ast.operations.BinaryOp;
import ast.statements.Assignment;
import ast.structure.VariableScope;
import ast.types.HeapObjectReference;
import ast.types.JavaClass;
import ast.types.PrimitiveType;
import ast.types.Type;
import codegen.CodeEmitter;
import codegen.CodeGenUtil;
import codegen.Constants;
import codegen.WasmType;
import errors.IncorrectTypeException;
import util.ClassTable;
import util.FunctionTable;
import util.FunctionTableEntry;
import util.VirtualTable;

import java.util.List;

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
            case Add:
                emitter.emitLine(wasmType + ".add");
                CodeGenUtil.emitRangeRestrictionCode(expressionType, emitter);
                break;
            case Subtract:
                emitter.emitLine(wasmType + ".sub");
                CodeGenUtil.emitRangeRestrictionCode(expressionType, emitter);
                break;
            case Multiply:
                // TODO: This probably won't work, as multiplying can add more bits
                // eg i32 * i32 = i64
                // but it'll do for now...
                emitter.emitLine(wasmType + ".mul");
                break;
            case Divide:
                emitter.emitLine(wasmType + ".div_s");
                break;
            case LogicalAnd:
                emitter.emitLine("i32.and");
                break;
            case LogicalOr:
                emitter.emitLine("i32.or");
                break;
            case EqualTo:
                emitter.emitLine(wasmType + ".eq");
                break;
            case NotEqualTo:
                emitter.emitLine(wasmType + ".ne");
                break;
            case LessThan:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".lt_s");
                } else {
                    emitter.emitLine(wasmType + ".lt");
                }
                break;
            case LessThanOrEqualTo:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".le_s");
                } else {
                    emitter.emitLine(wasmType + ".le");
                }
                break;
            case GreaterThan:
                if (expressionType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".gt_s");
                } else {
                    emitter.emitLine(wasmType + ".gt");
                }
                break;
            case GreaterThanOrEqualTo:
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
        emitter.emitLine("i32.xor");
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
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.Add);
                    assignment = new Assignment(expression.getLocalVariableExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
                    emitter.emitLine("local.get " + registerNumber);
                    break;
                case PRE_DECREMENT:
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.Subtract);
                    assignment = new Assignment(expression.getLocalVariableExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
                    emitter.emitLine("local.get " + registerNumber);
                    break;
                case POST_INCREMENT:
                    emitter.emitLine("local.get " + registerNumber);
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.Add);
                    assignment = new Assignment(expression.getLocalVariableExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, variableScope);
                    break;
                case POST_DECREMENT:
                    emitter.emitLine("local.get " + registerNumber);
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.Subtract);
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
        int headerSize = 9;
        int offset = headerSize + attributeNameExpression.getMemoryOffset();

        // Locate the start of the object in the heap
        emitter.emitLine("global.get $shadow_stack_base");
        compileExpression(attributeNameExpression.getObject(), scope);
        emitter.emitLine("i32.sub");
        emitter.emitLine("i32.load");

        // Look up the value at this address
        WasmType wasmType = CodeGenUtil.getWasmType(attributeNameExpression.getType());
        emitter.emitLine(wasmType + ".load offset=" + offset);

        // If it's a heap object then add a shadow stack entry
        if (attributeNameExpression.getType() instanceof HeapObjectReference) {
            emitter.emitLine("call $push_to_shadow_stack");
        }
    }

    private void compileFunctionCallExpression(FunctionCall functionCall,
                                               VariableScope scope) {

        // Put the arguments on the stack
        for (Expression expression : functionCall.getArguments()) {
            compileExpression(expression, scope);
        }

        // Call the function
        FunctionTableEntry tableEntry = functionCall.getFunctionTableEntry();
        String functionName = CodeGenUtil.getFunctionNameForOutput(tableEntry, functionTable);
        emitter.emitLine("call $" + functionName);

        // If it's a heap object then add a shadow stack entry
        if (functionCall.getType() instanceof HeapObjectReference) {
            emitter.emitLine("call $push_to_shadow_stack");
        }
    }

    private void compileMethodCallExpression(MethodCall methodCall,
                                             VariableScope scope) {
        int vtableOffset = methodCall.getVirtualTableOffset();
        String fullMethodName = CodeGenUtil.getFunctionNameForOutput(methodCall.getStaticFunctionEntry(), functionTable);
        String typeAnnotation = "(type $func_" + fullMethodName + ")";

        // Put the arguments on the stack
        for (Expression expression : methodCall.getArguments()) {
            compileExpression(expression, scope);
        }

        // Pass the object as the last parameter, saving a reference to it
        compileLocalVariableNameExpression(methodCall.getLocalVariable(), scope);
        emitter.emitLine("global.set $temp_ref_shadow_stack_offset");
        emitter.emitLine("global.get $temp_ref_shadow_stack_offset");

        // Look up the object's heap address
        emitter.emitLine("global.get $shadow_stack_base");
        emitter.emitLine("global.get $temp_ref_shadow_stack_offset");
        emitter.emitLine("i32.sub");
        emitter.emitLine("i32.load");

    // Extract the vtable pointer
    emitter.emitLine("i32.load offset=" + Constants.VTABLE_POINTER_POS);

        // Add the offset for this particular method
        emitter.emitLine("i32.const " + vtableOffset);
        emitter.emitLine("i32.add");

        // Call the method
        emitter.emitLine("call_indirect " + typeAnnotation);

        // If it's a heap object then add a shadow stack entry
        if (methodCall.getType() instanceof HeapObjectReference) {
            emitter.emitLine("call $push_to_shadow_stack");
        }
    }

    private void compileNewObjectExpression(NewObjectExpression newObjectExpression,
                                            VariableScope scope) {
        JavaClass javaClass = newObjectExpression.getType();
        int numAttributeBytes = javaClass.getNumAttributeBytes();
        int totalSize = javaClass.getHeapSize();
        int vtablePointer = virtualTable.getVirtualTablePosition(javaClass);
        List<Integer> pointerInformation = javaClass.getEncodedPointersDescription();
        int pointerInfoStart = javaClass.getPointerInfoStartOffset();

        // Allocate the memory
        emitter.emitLine("i32.const " + totalSize);
        emitter.emitLine("i32.const " + numAttributeBytes);
        emitter.emitLine("i32.const " + vtablePointer);
        emitter.emitLine("call $alloc_object");

        // Save the allocated address
        emitter.emitLine("global.set $temp_ref_heap_address");

        // Add a shadow stack entry
        emitter.emitLine("global.get $temp_ref_heap_address");
        emitter.emitLine("call $push_to_shadow_stack");
        emitter.emitLine("global.set $temp_ref_shadow_stack_offset");

        // Write pointer information
        int currentPosition = pointerInfoStart;
        for (int pointerInfoWord : pointerInformation) {
            emitter.emitLine("global.get $temp_ref_heap_address");
            emitter.emitLine("i32.const " + pointerInfoWord);
            emitter.emitLine("i32.store offset=" + currentPosition);
            currentPosition += 4;
        }

        // TODO: Set every attribute to 0 (null)

        // If a constructor is used, put its arguments on the stack and call the constructor
        if (newObjectExpression.usesConstructor()) {
            FunctionTableEntry entry = newObjectExpression.getConstructor();
            String functionName = CodeGenUtil.getFunctionNameForOutput(entry, functionTable);

            // Put all the arguments on the stack and call the constructor
            for (Expression expression : newObjectExpression.getArguments()) {
                compileExpression(expression, scope);
            }
            emitter.emitLine("global.get $temp_ref_shadow_stack_offset");
            emitter.emitLine("call $" + functionName);
        }

        // Leave a reference to the object on the stack
        emitter.emitLine("global.get $temp_ref_shadow_stack_offset");
    }

    public void compileNewArrayExpression(NewArrayExpression newArrayExpression,
                                          VariableScope scope) {
        Expression lengthExpression = newArrayExpression.getLengthExpression();

        // First evaluate the expression for how long the array should be
        compileExpression(lengthExpression, scope);

        // Now allocate the memory
        emitter.emitLine("call $alloc_array");

        // Allocate a shadow stack entry
        emitter.emitLine("call $push_to_shadow_stack");
    }

    private void compileArrayLookupExpression(ArrayIndexExpression lookupExpression,
                                              VariableScope scope) {
        Expression array = lookupExpression.getArrayExpression();
        Expression index = lookupExpression.getIndexExpression();
        Type elementType = lookupExpression.getArrayExpression().getType();
        int elementSize = elementType.getStackSize();

        // Find the start of the array in the heap
        emitter.emitLine("global.get $shadow_stack_base");
        compileExpression(array, scope);
        emitter.emitLine("i32.sub");
        emitter.emitLine("i32.load");

        // Compute the address of the requested element
        compileExpression(index, scope);
        emitter.emitLine("i32.const " + elementSize);
        emitter.emitLine("i32.mul");
        emitter.emitLine("i32.add");

        // Look up the value at this address (accounting for header)
        WasmType type = CodeGenUtil.getWasmType(lookupExpression.getType());
        emitter.emitLine(type + ".load offset=" + Constants.ARRAY_HEADER_LENGTH);
    }
}
