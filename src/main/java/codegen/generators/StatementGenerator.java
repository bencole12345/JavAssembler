package codegen.generators;

import ast.expressions.*;
import ast.statements.*;
import ast.structure.CodeBlock;
import ast.structure.VariableScope;
import ast.types.Type;
import ast.types.VoidType;
import codegen.CodeEmitter;
import codegen.CodeGenUtil;
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
        ExpressionGenerator.getInstance().compileExpression(returnStatement.getExpression(), scope);
        emitter.emitLine("return");
    }

    private void compileAssignment(Assignment assignment,
                                   VariableScope scope) {
        Expression expression = assignment.getExpression();
        VariableExpression variableExpression = assignment.getVariableExpression();
        if (variableExpression instanceof LocalVariableExpression) {
            ExpressionGenerator.getInstance().compileExpression(expression, scope);
            LocalVariableExpression localVariable = (LocalVariableExpression) variableExpression;
            int registerNum = scope.lookupRegisterIndexOfVariable(localVariable.getVariableName());
            emitter.emitLine("local.set " + registerNum);
        } else if (variableExpression instanceof AttributeNameExpression) {
            AttributeNameExpression attributeNameExpression = (AttributeNameExpression) variableExpression;
            LocalVariableExpression localVariable = attributeNameExpression.getObject();
            int registerNum = scope.lookupRegisterIndexOfVariable(localVariable.getVariableName());
            emitter.emitLine("local.get " + registerNum);
            int offset = attributeNameExpression.getMemoryOffset();
            emitter.emitLine("i32.const " + offset);
            emitter.emitLine("i32.add");
            ExpressionGenerator.getInstance().compileExpression(expression, scope);
            Type attributeType = attributeNameExpression.getType();
            WasmType wasmType = CodeGenUtil.getWasmType(attributeType);
            emitter.emitLine(wasmType + ".store");
        }
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

        compileCodeBlock(forLoop.getCodeBlock());

        // We have a problem: we need to compile the updater, eg i++
        // BUT: if you do this the standard way, it leaves the old value of i
        // on the stack
        // And it would be messy to do a pop as we'd have to check that there
        // is indeed an updater. It's also valid to have an empty expression!
        // One idea: could we return "how much stuff did we put on the stack"
        // whenever you compile an expression?

        // TODO: Stop dumping result on the stack
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
