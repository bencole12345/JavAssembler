package codegen.generators;

import ast.expressions.*;
import ast.literals.*;
import ast.operations.BinaryOp;
import ast.statements.Assignment;
import ast.structure.VariableScope;
import ast.types.*;
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
        Type expressionType = bopExpression.getUnderlyingType();
        PrimitiveType primitiveType = null;
        if (expressionType instanceof PrimitiveType) {
            primitiveType = (PrimitiveType) expressionType;
        }
        WasmType wasmType = CodeGenUtil.getWasmType(expressionType);
        switch (bopExpression.getOp()) {
            case Add:
                emitter.emitLine(wasmType + ".add");
                break;
            case Subtract:
                emitter.emitLine(wasmType + ".sub");
                break;
            case Multiply:
                emitter.emitLine(wasmType + ".mul");
                break;
            case Divide:
                if (primitiveType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".div_s");
                } else {
                    emitter.emitLine(wasmType + ".div");
                }
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
                if (primitiveType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".lt_s");
                } else {
                    emitter.emitLine(wasmType + ".lt");
                }
                break;
            case LessThanOrEqualTo:
                if (primitiveType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".le_s");
                } else {
                    emitter.emitLine(wasmType + ".le");
                }
                break;
            case GreaterThan:
                if (primitiveType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".gt_s");
                } else {
                    emitter.emitLine(wasmType + ".gt");
                }
                break;
            case GreaterThanOrEqualTo:
                if (primitiveType.isIntegralType()) {
                    emitter.emitLine(wasmType + ".ge_s");
                } else {
                    emitter.emitLine(wasmType + ".ge");
                }
        }
        CodeGenUtil.emitRangeRestrictionCode(expressionType, emitter);
    }

    private void compileBinarySelectorExpression(BinarySelectorExpression binarySelectorExpression,
                                                 VariableScope variableScope) {
        // TODO: Use the if with type construction seen in the table at
        //  https://webassembly.org/docs/text-format/

        // EVEN BETTER: See https://webassembly.github.io/spec/core/syntax/instructions.html#parametric-instructions
    }

    private void compileNegateExpression(NegateExpression negateExpression,
                                         VariableScope scope) {
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
                                                    VariableScope scope) {
        // TODO: Support applying increments to attributes as well as local variables
        String variableName = expression.getLocalVariableExpression().getVariableName();
        VariableScope.LocalVariableAllocation allocation = (VariableScope.LocalVariableAllocation) scope.getVariableWithName(variableName);
        int registerNumber = allocation.getLocalVariableIndex();
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
                    StatementGenerator.getInstance().compileStatement(assignment, scope);
                    emitter.emitLine("local.get " + registerNumber);
                    break;
                case PRE_DECREMENT:
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.Subtract);
                    assignment = new Assignment(expression.getLocalVariableExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, scope);
                    emitter.emitLine("local.get " + registerNumber);
                    break;
                case POST_INCREMENT:
                    emitter.emitLine("local.get " + registerNumber);
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.Add);
                    assignment = new Assignment(expression.getLocalVariableExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, scope);
                    break;
                case POST_DECREMENT:
                    emitter.emitLine("local.get " + registerNumber);
                    bopExpr = new BinaryOperatorExpression(varNameExpr, one, BinaryOp.Subtract);
                    assignment = new Assignment(expression.getLocalVariableExpression(), bopExpr);
                    StatementGenerator.getInstance().compileStatement(assignment, scope);
            }
        } catch (IncorrectTypeException e) {
            e.printStackTrace();
        }
    }

    private void compileLocalVariableNameExpression(LocalVariableExpression expression,
                                                    VariableScope variableScope) {
        VariableScope.Allocation allocation = variableScope.getVariableWithName(expression.getVariableName());
        if (allocation instanceof VariableScope.LocalVariableAllocation) {
            // It's a WebAssembly local variable
            VariableScope.LocalVariableAllocation localAllocation = (VariableScope.LocalVariableAllocation) allocation;
            int index = localAllocation.getLocalVariableIndex();
            emitter.emitLine("local.get " + index);
        } else {
            // It's a stack variable
            VariableScope.StackOffsetAllocation stackAllocation = (VariableScope.StackOffsetAllocation) allocation;
            int stackFrameOffset = stackAllocation.getStackFrameOffset();
            emitter.emitLine("global.get $stack_base");
            emitter.emitLine("global.get $stack_frame_start");
            emitter.emitLine("i32.add");
            emitter.emitLine("i32.load offset=" + stackFrameOffset);
        }
    }

    private void compileAttributeNameExpression(AttributeNameExpression attributeNameExpression,
                                                VariableScope scope) {
        int attributeOffset = Constants.OBJECT_HEADER_LENGTH + attributeNameExpression.getMemoryOffset();
        WasmType wasmType = CodeGenUtil.getWasmType(attributeNameExpression.getType());

        // Put the address of the object on the stack
        compileExpression(attributeNameExpression.getObject(), scope);

        // Check that it's not null
        emitter.emitLine("global.set $temp_heap_address");
        emitter.emitLine("global.get $temp_heap_address");
        emitter.emitLine("i32.const 0");
        emitter.emitLine("i32.eq");
        emitter.emitLine("if");
        emitter.increaseIndentationLevel();
        emitter.emitLine("unreachable");
        emitter.decreaseIndentationLevel();
        emitter.emitLine("end");
        emitter.emitLine("global.get $temp_heap_address");

        // Look up the value at the offset for the requested attribute
        emitter.emitLine(wasmType + ".load offset=" + attributeOffset);
    }

    private void compileFunctionCallExpression(FunctionCall functionCall,
                                               VariableScope scope) {
        List<Expression> arguments = functionCall.getArguments();
        FunctionTableEntry tableEntry = functionCall.getFunctionTableEntry();
        String functionName = CodeGenUtil.getFunctionNameForOutput(tableEntry, functionTable);
        String functionCallString = "call $" + functionName;
        saveStateAndCallFunction(functionCallString, arguments, scope, null, null, false);
    }

    private void compileMethodCallExpression(MethodCall methodCall,
                                             VariableScope scope) {

        // Extract arguments
        List<Expression> arguments = methodCall.getArguments();

        // Include the 'this' object as the final parameter
        arguments.add(methodCall.getLocalVariable());

        // Calculate the method to call
        int vtableOffset = methodCall.getVirtualTableOffset();
        String fullMethodName = CodeGenUtil.getFunctionNameForOutput(methodCall.getStaticFunctionEntry(), functionTable);
        String typeAnnotation = "(type $func_" + fullMethodName + ")";
        String functionCallString = "call_indirect " + typeAnnotation;

        // Make the call
        saveStateAndCallFunction(functionCallString, arguments, scope, methodCall.getLocalVariable(), vtableOffset, false);
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

        // Save object reference
        emitter.emitLine("global.set $temp_heap_address");

        // Write pointer information
        int currentPosition = pointerInfoStart;
        for (int pointerInfoWord : pointerInformation) {
            emitter.emitLine("global.get $temp_heap_address");
            emitter.emitLine("i32.const " + pointerInfoWord);
            emitter.emitLine("i32.store offset=" + currentPosition + " align=2");
            currentPosition += 4;
        }

        // If a constructor is used, put its arguments on the stack and call the constructor
        if (newObjectExpression.usesConstructor()) {
            List<Expression> arguments = newObjectExpression.getArguments();
            FunctionTableEntry entry = newObjectExpression.getConstructor();
            String functionName = CodeGenUtil.getFunctionNameForOutput(entry, functionTable);
            String functionCallString = "call $" + functionName;
            saveStateAndCallFunction(functionCallString, arguments, scope, null, null, true);
        }

        // Leave a reference to the object on the stack
        emitter.emitLine("global.get $temp_heap_address");
    }

    private void saveStateAndCallFunction(String functionCallString,
                                          List<Expression> arguments,
                                          VariableScope scope,
                                          Expression objectForVtable,
                                          Integer vtableOffset,
                                          boolean includeThisArgument) {

        // Set up all arguments
        int offset = 0;
        for (Expression expression : arguments) {
            if (expression.getType() instanceof PrimitiveType) {
                // Primitive arguments go on the WebAssembly operand stack
                compileExpression(expression, scope);
            } else {
                // Heap pointers go on the manually managed stack
                emitter.emitLine("global.get $stack_base");
                emitter.emitLine("global.get $stack_pointer");
                emitter.emitLine("i32.add");
                compileExpression(expression, scope);
                emitter.emitLine("i32.store offset=" + offset + " align=2");
                offset += 4;
            }
        }

        if (includeThisArgument) {
            // Assume that the pointer has been saved to $temp_heap_address
            // Note that method calls DON'T use this because they instead
            // pass the this argument by appending it to the normal list
            // of arguments. This is only used for constructors, where there
            // is no local variable to treat as an argument.
            emitter.emitLine("global.get $stack_base");
            emitter.emitLine("global.get $stack_pointer");
            emitter.emitLine("i32.add");
            emitter.emitLine("global.get $temp_heap_address");
            emitter.emitLine("i32.store offset=" + offset + " align=2");
            offset += 4;
        }

        // If this is a method call, put the vtable index on the stack
        if (objectForVtable != null) {
            compileExpression(objectForVtable, scope);
            emitter.emitLine("i32.load offset=" + Constants.VTABLE_POINTER_POS + " align=2");
            emitter.emitLine("i32.const " + vtableOffset);
            emitter.emitLine("i32.add");
        }

        // Save the current stack frame start
        emitter.emitLine("global.get $stack_frame_start");
        emitter.emitLine("local.set $saved_stack_frame_start");

        // Move the start of the new stack frame ready for the new function
        emitter.emitLine("global.get $stack_pointer");
        emitter.emitLine("global.set $stack_frame_start");

        // Bump up the stack pointer
        emitter.emitLine("global.get $stack_pointer");
        emitter.emitLine("i32.const " + offset);
        emitter.emitLine("i32.add");
        emitter.emitLine("global.set $stack_pointer");

        // Make the function call
        emitter.emitLine(functionCallString);

        // Restore the previous stack pointer
        emitter.emitLine("global.get $stack_frame_start");
        emitter.emitLine("global.set $stack_pointer");

        // Restore the previous start of call frame
        emitter.emitLine("local.get $saved_stack_frame_start");
        emitter.emitLine("global.set $stack_frame_start");
    }

    public void compileNewArrayExpression(NewArrayExpression newArrayExpression,
                                          VariableScope scope) {
        Expression lengthExpression = newArrayExpression.getLengthExpression();
        Type elementType = newArrayExpression.getElementType();
        int elementSize = CodeGenUtil.getWasmType(elementType).getSize();
        int containsPointersBit = elementType instanceof HeapObjectReference ? 1 : 0;

        // First evaluate the expression for how long the array should be
        compileExpression(lengthExpression, scope);

        // Multiply by element size to get the actual size
        emitter.emitLine("i32.const " + elementSize);
        emitter.emitLine("i32.mul");

        // Pass in bit for whether the array contains pointers
        emitter.emitLine("i32.const " + containsPointersBit);

        // Now allocate the memory, leaving the address on the stack
        emitter.emitLine("call $alloc_array");
    }

    private void compileArrayLookupExpression(ArrayIndexExpression lookupExpression,
                                              VariableScope scope) {
        Expression array = lookupExpression.getArrayExpression();
        Expression index = lookupExpression.getIndexExpression();
        ObjectArray arrayType = (ObjectArray) lookupExpression.getArrayExpression().getType();
        Type elementType = arrayType.getElementType();
        WasmType wasmType = CodeGenUtil.getWasmType(elementType);

//        compileExpression(array, scope);
//        compileExpression(index, scope);
//        emitter.emitLine("call $array_read_" + wasmType);
        compileExpression(array, scope);
        compileExpression(index, scope);
        emitter.emitLine("i32.const 2");
        emitter.emitLine("i32.shl");
        emitter.emitLine("i32.add");
        emitter.emitLine("i32.load offset=8 align=2");
    }
}
