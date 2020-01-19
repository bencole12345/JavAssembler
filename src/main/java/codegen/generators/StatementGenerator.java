package codegen.generators;

import ast.expressions.NotExpression;
import ast.expressions.VariableIncrementExpression;
import ast.functions.FunctionTable;
import ast.statements.*;
import ast.structure.CodeBlock;
import ast.structure.VariableScope;
import codegen.CodeEmitter;
import errors.IncorrectTypeException;

import static codegen.generators.ExpressionGenerator.compileExpression;

public class StatementGenerator {

    public static void compileStatement(Statement statement,
                                        CodeEmitter emitter,
                                        VariableScope scope,
                                        FunctionTable functionTable) {
        if (statement instanceof Assignment) {
            compileAssignment((Assignment) statement, emitter, scope, functionTable);
        } else if (statement instanceof ReturnStatement) {
            compileReturnStatement((ReturnStatement) statement, emitter, scope, functionTable);
        } else if (statement instanceof IfStatementChain) {
            compileIfStatementChain((IfStatementChain) statement, emitter, scope, functionTable);
        } else if (statement instanceof WhileLoop) {
            compileWhileLoop((WhileLoop) statement, emitter, scope, functionTable);
        } else if (statement instanceof ForLoop) {
            compileForLoop((ForLoop) statement, emitter, scope, functionTable);
        } else if (statement instanceof VariableIncrementExpression) {
            ExpressionGenerator.compileExpression((VariableIncrementExpression) statement, emitter, scope, functionTable);
        }
    }

    private static void compileReturnStatement(ReturnStatement returnStatement,
                                               CodeEmitter emitter,
                                               VariableScope scope,
                                               FunctionTable functionTable) {
        compileExpression(returnStatement.getExpression(), emitter, scope, functionTable);
        emitter.emitLine("return");
    }

    private static void compileAssignment(Assignment assignment,
                                          CodeEmitter emitter,
                                          VariableScope scope,
                                          FunctionTable functionTable) {
        compileExpression(assignment.getExpression(), emitter, scope, functionTable);
        int registerNum = scope.lookupRegisterIndexOfVariable(assignment.getVariableNameExpression().getVariableName());
        emitter.emitLine("local.set " + registerNum);
    }

    private static void compileIfStatementChain(IfStatementChain chain,
                                                CodeEmitter emitter,
                                                VariableScope scope,
                                                FunctionTable functionTable) {
        // We assume that type checking has already been done and that we are
        // sure that the expression is of type boolean.
        ExpressionGenerator.compileExpression(chain.getCondition(), emitter, scope, functionTable);
        emitter.emitLine("if");
        emitter.increaseIndentationLevel();
        compileCodeBlock(chain.getIfBlock(), emitter, functionTable);
        emitter.decreaseIndentationLevel();
        if (chain.hasNextIfStatementChain()) {
            emitter.emitLine("else");
            emitter.increaseIndentationLevel();
            compileIfStatementChain(chain.getNextInChain(), emitter, scope, functionTable);
            emitter.decreaseIndentationLevel();
        } else if (chain.hasElseBlock()) {
            emitter.emitLine("else");
            emitter.increaseIndentationLevel();
            compileCodeBlock(chain.getElseBlock(), emitter, functionTable);
            emitter.decreaseIndentationLevel();
        }
        emitter.emitLine("end");
    }

    private static void compileWhileLoop(WhileLoop whileLoop,
                                         CodeEmitter emitter,
                                         VariableScope scope,
                                         FunctionTable functionTable) {
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
        ExpressionGenerator.compileExpression(notExpression, emitter, scope, functionTable);

        // If not(condition) is true then condition is false, so exit the loop
        emitter.emitLine("br_if 1");

        // Compile the body of the loop
        compileCodeBlock(whileLoop.getCodeBlock(), emitter, functionTable);

        // Branch back to the start of the loop
        emitter.emitLine("br 0");

        // End the loop
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
    }

    private static void compileForLoop(ForLoop forLoop,
                                       CodeEmitter emitter,
                                       VariableScope scope,
                                       FunctionTable functionTable) {

        VariableScope bodyScope = forLoop.getCodeBlock().getVariableScope();
        VariableScope headerScope = bodyScope.getContainingScope();

        // First run the setup code
        compileStatement(forLoop.getInitialiser(), emitter, headerScope, functionTable);

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
        ExpressionGenerator.compileExpression(notExpression, emitter, headerScope, functionTable);
        emitter.emitLine("br_if 1");

        compileCodeBlock(forLoop.getCodeBlock(), emitter, functionTable);

        // We have a problem: we need to compile the updater, eg i++
        // BUT: if you do this the standard way, it leaves the old value of i
        // on the stack
        // And it would be messy to do a pop as we'd have to check that there
        // is indeed an updater. It's also valid to have an empty expression!
        // One idea: could we return "how much stuff did we put on the stack"
        // whenever you compile an expression?

        // TODO: Stop dumping result on the stack
        ExpressionGenerator.compileExpression(forLoop.getUpdater(), emitter, headerScope, functionTable);

        // Branch back to the start of the loop
        emitter.emitLine("br 0");

        // End the loop
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
    }

    public static void compileCodeBlock(CodeBlock codeBlock,
                                        CodeEmitter emitter,
                                        FunctionTable functionTable) {
        for (Statement statement : codeBlock.getStatements()) {
            StatementGenerator.compileStatement(statement, emitter, codeBlock.getVariableScope(), functionTable);
        }
    }

}
