package codegen.generators;

import ast.expressions.*;
import ast.statements.*;
import ast.structure.CodeBlock;
import ast.structure.VariableScope;
import ast.types.HeapObjectReference;
import ast.types.Type;
import ast.types.VoidType;
import codegen.CodeEmitter;
import codegen.CodeGenUtil;
import codegen.Constants;
import codegen.WasmType;
import errors.IncorrectTypeException;
import util.ClassTable;
import util.FunctionTable;
import util.VirtualTable;

public class StatementGenerator {

    private static StatementGenerator INSTANCE;

    public static StatementGenerator getInstance() {
        if (INSTANCE == null)
            INSTANCE = new StatementGenerator();
        return INSTANCE;
    }

    private CodeEmitter emitter;
    private FunctionTable functionTable;
    private ClassTable classTable;
    private VirtualTable virtualTable;

    private StatementGenerator() {}

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

    public void compileStatement(Statement statement,
                                 VariableScope scope) {
        if (statement instanceof Assignment) {
            compileAssignment((Assignment) statement, scope);
        } else if (statement instanceof ReturnStatement) {
            compileReturnStatement((ReturnStatement) statement, scope);
        } else if (statement instanceof IfStatementChain) {
            compileIfStatementChain((IfStatementChain) statement, scope);
        } else if (statement instanceof WhileLoop) {
            compileWhileLoop((WhileLoop) statement, scope);
        } else if (statement instanceof ForLoop) {
            compileForLoop((ForLoop) statement);
        } else if (statement instanceof VariableIncrementExpression) {
            ExpressionGenerator.getInstance().compileExpression((VariableIncrementExpression) statement, scope);
        } else if (statement instanceof FunctionCall) {
            compileFunctionCallStatement((FunctionCall) statement, scope);
        } else if (statement instanceof MethodCall) {
            compileMethodCallStatement((MethodCall) statement, scope);
        }
    }

    private void compileReturnStatement(ReturnStatement returnStatement,
                                        VariableScope scope) {
        Type returnType = returnStatement.getExpression().getType();
        emitter.emitLine("local.get $shadow_stack_next_offset_at_entry");
        emitter.emitLine("global.set $shadow_stack_next_offset");

        if (returnType instanceof HeapObjectReference) {
            // Can't return stack values, so dereference the shadow stack
            emitter.emitLine("global.get $shadow_stack_base");
            ExpressionGenerator.getInstance().compileExpression(returnStatement.getExpression(), scope);
            emitter.emitLine("i32.sub");
            emitter.emitLine("i32.load");
        } else {
            // If it's primitive then we don't have to worry about the shadow stack
            ExpressionGenerator.getInstance().compileExpression(returnStatement.getExpression(), scope);
        }
        emitter.emitLine("return");
    }

    private void compileAssignment(Assignment assignment,
                                   VariableScope scope) {
        Expression value = assignment.getExpression();
        VariableExpression variableExpression = assignment.getVariableExpression();
        if (variableExpression instanceof LocalVariableExpression) {
            compileLocalVariableAssignment((LocalVariableExpression) variableExpression, value, scope);
        } else if (variableExpression instanceof AttributeNameExpression) {
            compileAttributeNameAssignment((AttributeNameExpression) variableExpression, value, scope);
        } else if (variableExpression instanceof ArrayIndexExpression) {
            compileArrayIndexAssignment((ArrayIndexExpression) variableExpression, value, scope);
        }
    }

    private void compileLocalVariableAssignment(LocalVariableExpression localVariable,
                                                Expression value,
                                                VariableScope scope) {
        int registerNum = scope.lookupRegisterIndexOfVariable(localVariable.getVariableName());

        ExpressionGenerator.getInstance().compileExpression(value, scope);
        emitter.emitLine("local.set " + registerNum);
    }

    private void compileAttributeNameAssignment(AttributeNameExpression attributeNameExpression,
                                                Expression value,
                                                VariableScope scope) {
        int offset = Constants.OBJECT_HEADER_LENGTH + attributeNameExpression.getMemoryOffset();
        Type attributeType = attributeNameExpression.getType();
        WasmType wasmType = CodeGenUtil.getWasmType(attributeType);

        // Find the address of the start of the object in the heap
        emitter.emitLine("global.get $shadow_stack_base");
        ExpressionGenerator.getInstance()
                .compileExpression(attributeNameExpression.getObject(), scope);
        emitter.emitLine("i32.sub");
        emitter.emitLine("i32.load");

        // Now put the value we want to store on the stack
        if (value instanceof HeapObjectReference) {
            // We need to "derefence" the shadow stack pointer and store the
            // actual heap address directly
            emitter.emitLine("global.get $shadow_stack_base");
            ExpressionGenerator.getInstance().compileExpression(value, scope);
            emitter.emitLine("i32.sub");
            emitter.emitLine("i32.load");
        } else {
            // It's a primitive type so the shadow stack is not used
            ExpressionGenerator.getInstance().compileExpression(value, scope);
        }

        // Write to the address of this attribute
        emitter.emitLine(wasmType + ".store offset=" + offset);
    }

    private void compileArrayIndexAssignment(ArrayIndexExpression arrayIndexExpression,
                                             Expression value,
                                             VariableScope scope) {

        Expression arrayExpression = arrayIndexExpression.getArrayExpression();
        Expression indexExpression = arrayIndexExpression.getIndexExpression();
        WasmType valueType = CodeGenUtil.getWasmType(value.getType());

        // Look up the start of the array
        emitter.emitLine("global.get $shadow_stack_base");
        ExpressionGenerator.getInstance().compileExpression(arrayExpression, scope);
        emitter.emitLine("i32.sub");
        emitter.emitLine("i32.load");

        // Move to the right element
        ExpressionGenerator.getInstance().compileExpression(indexExpression, scope);
        emitter.emitLine("i32.const 2");
        emitter.emitLine("i32.shl");
        emitter.emitLine("i32.add");

        // Put the value to store there on the stack
        ExpressionGenerator.getInstance().compileExpression(value, scope);

        // Write to memory (accounting for header)
        emitter.emitLine(valueType + ".store offset=" + Constants.ARRAY_HEADER_LENGTH);
    }

    private void compileIfStatementChain(IfStatementChain chain,
                                         VariableScope scope) {
        // We assume that type checking has already been done and that we are
        // sure that the expression is of type boolean.
        ExpressionGenerator.getInstance().compileExpression(chain.getCondition(), scope);
        emitter.emitLine("if");
        emitter.increaseIndentationLevel();
        compileCodeBlock(chain.getIfBlock());
        emitter.decreaseIndentationLevel();
        if (chain.hasNextIfStatementChain()) {
            emitter.emitLine("else");
            emitter.increaseIndentationLevel();
            compileIfStatementChain(chain.getNextInChain(), scope);
            emitter.decreaseIndentationLevel();
        } else if (chain.hasElseBlock()) {
            emitter.emitLine("else");
            emitter.increaseIndentationLevel();
            compileCodeBlock(chain.getElseBlock());
            emitter.decreaseIndentationLevel();
        }
        emitter.emitLine("end");
    }

    private void compileWhileLoop(WhileLoop whileLoop,
                                  VariableScope scope) {
        emitter.emitLine("(block");
        emitter.increaseIndentationLevel();

        emitter.emitLine("(loop");
        emitter.increaseIndentationLevel();

        // Test the condition, negate it, and jump out of the loop if the
        // negation is true
        NotExpression notExpression = null;
        try {
            notExpression = new NotExpression(whileLoop.getCondition());
        } catch (IncorrectTypeException e) {
            e.printStackTrace();
        }
        ExpressionGenerator.getInstance().compileExpression(notExpression, scope);

        // If not(condition) is true then condition is false, so exit the loop
        emitter.emitLine("br_if 1");

        // Compile the body of the loop
        compileCodeBlock(whileLoop.getCodeBlock());

        // Branch back to the start of the loop
        emitter.emitLine("br 0");

        // End the loop
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
    }

    private void compileForLoop(ForLoop forLoop) {

        VariableScope bodyScope = forLoop.getCodeBlock().getVariableScope();
        VariableScope headerScope = bodyScope.getContainingScope();

        // First run the setup code
        compileStatement(forLoop.getInitialiser(), headerScope);

        // Set up the loop
        emitter.emitLine("(block");
        emitter.increaseIndentationLevel();
        emitter.emitLine("(loop");
        emitter.increaseIndentationLevel();

        // Test the condition, negate it, and jump out of the loop if the
        // negation is true
        NotExpression notExpression = null;
        try {
            notExpression = new NotExpression(forLoop.getCondition());
        } catch (IncorrectTypeException e) {
            e.printStackTrace();
        }
        ExpressionGenerator.getInstance().compileExpression(notExpression, headerScope);
        emitter.emitLine("br_if 1");

        // Now compile the actual code block
        compileCodeBlock(forLoop.getCodeBlock());

        // Compile the updater - the part that updates the loop variable. It
        // doesn't matter if this leaves anything on the stack because we are
        // about to jump back to the start of the loop, unwinding the stack
        // anyway.
        ExpressionGenerator.getInstance().compileExpression(forLoop.getUpdater(), headerScope);

        // Branch back to the start of the loop
        emitter.emitLine("br 0");

        // End the loop
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
    }

    private void compileFunctionCallStatement(FunctionCall functionCall, VariableScope scope) {

        // Emit the function call.
        ExpressionGenerator.getInstance().compileExpression(functionCall, scope);

        // If it's not a void type then we need to remove its return value
        // from the stack.
        if (!(functionCall.getType() instanceof VoidType)) {
            emitter.emitLine("drop");
        }
    }

    private void compileMethodCallStatement(MethodCall methodCall, VariableScope scope) {

        // Emit the method call.
        ExpressionGenerator.getInstance().compileExpression(methodCall, scope);

        // Like with function calls, if it's not a void type then we need to
        // remove the return value from the stack.
        if (!(methodCall.getType() instanceof VoidType)) {
            emitter.emitLine("drop");
        }
    }

    public void compileCodeBlock(CodeBlock codeBlock) {
        for (Statement statement : codeBlock.getStatements()) {
            compileStatement(statement, codeBlock.getVariableScope());
        }
    }

}
