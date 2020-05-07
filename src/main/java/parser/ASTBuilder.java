package parser;

import ast.ASTNode;
import ast.expressions.*;
import ast.literals.BooleanLiteral;
import ast.operations.BinaryOp;
import ast.statements.*;
import ast.structure.*;
import ast.types.*;
import errors.IncorrectTypeException;
import errors.JavAssemblerException;
import errors.MultipleVariableDeclarationException;
import org.antlr.v4.runtime.Token;
import util.ClassTable;
import util.ErrorReporting;
import util.FunctionTable;
import util.SubroutineToCompile;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ASTBuilder extends JavaFileBaseVisitor<ASTNode> {

    private Stack<VariableScope> variableScopeStack;
    private Type currentFunctionReturnType;
    private JavaClass currentClass;

    private ExpressionVisitor expressionVisitor;
    private TypeVisitor typeVisitor;
    private AccessModifierVisitor accessModifierVisitor;

    public ASTBuilder(FunctionTable functionTable, ClassTable classTable) {
        currentClass = null;
        variableScopeStack = new Stack<>();

        typeVisitor = new TypeVisitor(classTable);
        expressionVisitor = new ExpressionVisitor(functionTable, classTable, typeVisitor);
        accessModifierVisitor = new AccessModifierVisitor();
    }

    public ClassMethod visitSubroutine(SubroutineToCompile subroutine, JavaClass containingClass) {
        this.currentClass = containingClass;
        expressionVisitor.setCurrentClass(currentClass);
        typeVisitor.setCurrentClass(containingClass);
        if (containingClass instanceof GenericJavaClass) {
            GenericJavaClass genericTypedClass = (GenericJavaClass) containingClass;
            typeVisitor.setGenericTypesIndexMap(genericTypedClass.getGenericTypesIndexMap());
        } else {
            typeVisitor.unsetGenericTypesMap();
        }
        if (subroutine.isMethod()) {
            return visitMethodDefinition(subroutine.getMethodDefinition());
        } else {
            return visitConstructorDefinition(subroutine.getConstructorDefinition());
        }
    }

    @Override
    public ClassMethod visitMethodDefinition(JavaFileParser.MethodDefinitionContext ctx) {
        AccessModifier modifier = accessModifierVisitor.visitAccessModifier(ctx.accessModifier());
        boolean isStatic = ctx.STATIC() != null;
        Type returnType = typeVisitor.visit(ctx.type());
        String methodName = ctx.IDENTIFIER().toString();
        MethodParameterList params = (MethodParameterList) visit(ctx.methodParams());
        List<MethodParameter> paramsList = params.getParameters();

        // Create a new variable scope stack and push a variable scope object
        // to contain the method's parameters.
        variableScopeStack.clear();
        VariableScope scopeForParameters = pushNewVariableScope();

        // Register the parameters
        try {
            scopeForParameters.registerParameters(paramsList);
            if (!isStatic) {
                scopeForParameters.registerVariable("this", currentClass);
            }
        } catch (MultipleVariableDeclarationException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }

        // Now visit the body of the method
        currentFunctionReturnType = returnType;
        CodeBlock body = (CodeBlock) visit(ctx.codeBlock());

        // Pop the scope that was created to contain the parameters
        popVariableScope(false);

        // Return the method that was created
        return new ClassMethod(modifier, isStatic, returnType, methodName, paramsList, body, currentClass);
    }

    @Override
    public ClassMethod visitConstructorDefinition(JavaFileParser.ConstructorDefinitionContext ctx) {
        AccessModifier modifier = AccessModifier.PUBLIC;
        boolean isStatic = false;
        Type returnType = new VoidType();
        String methodName = "constructor";
        MethodParameterList params = (MethodParameterList) visit(ctx.methodParams());
        List<MethodParameter> paramsList = params.getParameters();

        // Create a new variable scope stack and push a variable scope object
        // to contain the method's parameters.
        variableScopeStack.clear();
        VariableScope scopeForParameters = pushNewVariableScope();

        // Register the parameters
        try {
            scopeForParameters.registerParameters(paramsList);
            scopeForParameters.registerVariable("this", currentClass);
        } catch (MultipleVariableDeclarationException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }

        // Visit the body of the method
        currentFunctionReturnType = returnType;
        CodeBlock body = (CodeBlock) visit(ctx.codeBlock());

        // Pop the variable scope of the parameters from the stack
        popVariableScope(false);

        // Return the method that was created
        return new ClassMethod(modifier, isStatic, returnType, methodName,
                paramsList, body, currentClass);
    }

    @Override
    public ASTNode visitDeclarationStatement(JavaFileParser.DeclarationStatementContext ctx) {
        return visit(ctx.variableDeclaration());
    }

    @Override
    public ASTNode visitAssignmentStatement(JavaFileParser.AssignmentStatementContext ctx) {
        return visit(ctx.assignment());
    }

    @Override
    public ASTNode visitDeclarationAssignmentStatement(JavaFileParser.DeclarationAssignmentStatementContext ctx) {
        return visit(ctx.variableDeclarationAndAssignment());
    }

    @Override
    public ASTNode visitReturnStatement(JavaFileParser.ReturnStatementContext ctx) {
        Expression expression = expressionVisitor.visit(ctx.expr(), variableScopeStack.peek());
        ReturnStatement returnStatement = null;
        try {
            returnStatement = new ReturnStatement(expression, currentFunctionReturnType);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return returnStatement;
    }

    @Override
    public ASTNode visitIfStatementWrap(JavaFileParser.IfStatementWrapContext ctx) {
        return visit(ctx.ifStatement());
    }

    @Override
    public ASTNode visitWhileLoopWrap(JavaFileParser.WhileLoopWrapContext ctx) {
        return visit(ctx.whileLoop());
    }

    @Override
    public ASTNode visitForLoopWrap(JavaFileParser.ForLoopWrapContext ctx) {
        return visit(ctx.forLoop());
    }

    @Override
    public ASTNode visitFunctionCallStatement(JavaFileParser.FunctionCallStatementContext ctx) {
        return expressionVisitor.visit(ctx.functionCall(), variableScopeStack.peek());
    }

    @Override
    public ASTNode visitVariableDeclaration(JavaFileParser.VariableDeclarationContext ctx) {
        Type type = typeVisitor.visit(ctx.type());
        String name = ctx.IDENTIFIER().toString();
        return new VariableDeclaration(type, name);
    }

    @Override
    public ASTNode visitVariableAssignment(JavaFileParser.VariableAssignmentContext ctx) {
        String variableName = ctx.variableName().getText();
        VariableScope scope = variableScopeStack.peek();
        VariableExpression variableExpression = new LocalVariableExpression(variableName, scope);
        Token op = ctx.op;
        Expression rhs = expressionVisitor.visit(ctx.expr(), variableScopeStack.peek());
        return buildAssignment(variableExpression, op, rhs, ctx);
    }

    @Override
    public ASTNode visitAttributeAssignment(JavaFileParser.AttributeAssignmentContext ctx) {
        String localVarName = ctx.variableName().getText();
        LocalVariableExpression object =
                new LocalVariableExpression(localVarName, variableScopeStack.peek());
        String attributeName = ctx.IDENTIFIER().getText();
        Token op = ctx.op;
        Expression rhs = expressionVisitor.visit(ctx.expr(), variableScopeStack.peek());
        AttributeNameExpression attributeNameExpression = null;
        try {
            attributeNameExpression = new AttributeNameExpression(object, attributeName);
        } catch (JavAssemblerException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return buildAssignment(attributeNameExpression, op, rhs, ctx);
    }

    @Override
    public ASTNode visitArrayIndexAssignment(JavaFileParser.ArrayIndexAssignmentContext ctx) {
        Expression arrayExpression = expressionVisitor.visit(ctx.expr(0), variableScopeStack.peek());
        Expression indexExpression = expressionVisitor.visit(ctx.expr(1), variableScopeStack.peek());
        Expression rhs = expressionVisitor.visit(ctx.expr(2), variableScopeStack.peek());
        Token op = ctx.op;

        ArrayIndexExpression lhs = null;
        try {
            lhs = new ArrayIndexExpression(arrayExpression, indexExpression);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }

        return buildAssignment(lhs, op, rhs, ctx);
    }

    private ASTNode buildAssignment(VariableExpression variableExpression,
                                    Token op,
                                    Expression rhs,
                                    JavaFileParser.AssignmentContext ctx) {

        // This method covers +=, -=, *=, /=, and also a simple assignment, =
        // The main idea is that we can reduce any of the first 4 into a simple
        // assignment (=) by replacing the expression on the RHS with a binary
        // operation involving the variable name and the expression.
        //
        // Examples:
        //
        // x += 1;      --becomes-->  x = (x + 1);
        // y *= (y/z);  --becomes-->  y = (y * (y/z));

        BinaryOp bop = null;
        switch (op.getType()) {
            case JavaFileParser.PLUS_EQUALS:
                bop = BinaryOp.Add;
                break;
            case JavaFileParser.MINUS_EQUALS:
                bop = BinaryOp.Subtract;
                break;
            case JavaFileParser.MULTIPLY_EQUALS:
                bop = BinaryOp.Multiply;
                break;
            case JavaFileParser.DIVIDE_EQUALS:
                bop = BinaryOp.Divide;
            // No case for JavaFileParser.EQUALS
        }

        // Perform substitution if this is not a simple assignment
        if (bop != null) {
            try {
                rhs = new BinaryOperatorExpression(variableExpression, rhs, bop);
            } catch (IncorrectTypeException e) {
                ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
            }
        }

        Assignment assignment = null;
        try {
            assignment = new Assignment(variableExpression, rhs);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }

        return assignment;
    }

    @Override
    public ASTNode visitVariableDeclarationAndAssignment(
            JavaFileParser.VariableDeclarationAndAssignmentContext ctx) {
        Type type = typeVisitor.visit(ctx.type());
        String name = ctx.IDENTIFIER().toString();
        Expression expression = expressionVisitor.visit(ctx.expr(), variableScopeStack.peek());
        return new DeclarationAndAssignment(type, name, expression);
    }

    @Override
    public ASTNode visitVariableIncrementStatement(JavaFileParser.VariableIncrementStatementContext ctx) {
        return expressionVisitor.visit(ctx.variableIncrementExpr(), variableScopeStack.peek());
    }

    @Override
    public ASTNode visitEmptyStatement(JavaFileParser.EmptyStatementContext ctx) {
        return new EmptyStatement();
    }

    @Override
    public ASTNode visitNoParams(JavaFileParser.NoParamsContext ctx) {
        List<MethodParameter> emptyList = new ArrayList<>();
        return new MethodParameterList(emptyList);
    }

    @Override
    public ASTNode visitSomeParams(JavaFileParser.SomeParamsContext ctx) {
        List<MethodParameter> paramList = new ArrayList<>();
        for (int i = 0; i < ctx.IDENTIFIER().size(); i++) {
            String parameterName = ctx.IDENTIFIER(i).toString();
            Type type = typeVisitor.visit(ctx.type(i));
            MethodParameter param = new MethodParameter(parameterName, type);
            paramList.add(param);
        }
        return new MethodParameterList(paramList);
    }

    @Override
    public ASTNode visitStatementList(JavaFileParser.StatementListContext ctx) {

        // Create a new scope for this code block and push it to the stack
        VariableScope innerScope = pushNewVariableScope();

        // Recursively visit each statement in this code block
        List<Statement> statements = new ArrayList<>();
        for (JavaFileParser.StatementContext statementCtx : ctx.statement()) {
            ASTNode statementNode = visit(statementCtx);
            if (statementNode instanceof VariableDeclaration) {
                VariableDeclaration declaration = (VariableDeclaration) statementNode;
                String name = declaration.getVariableName();
                Type type = declaration.getVariableType();
                try {
                    innerScope.registerVariable(name, type);
                } catch (MultipleVariableDeclarationException e) {
                    ErrorReporting.reportError(e.getMessage(), statementCtx, currentClass.toString());
                }
            } else if (statementNode instanceof DeclarationAndAssignment) {
                DeclarationAndAssignment combined = (DeclarationAndAssignment) statementNode;
                String name = combined.getVariableName();
                Type type = combined.getType();
                try {
                    innerScope.registerVariable(name, type);
                } catch (MultipleVariableDeclarationException e) {
                    ErrorReporting.reportError(e.getMessage(), statementCtx, currentClass.toString());
                }
                LocalVariableExpression nameExpression = new LocalVariableExpression(name, innerScope);
                Assignment assignment = null;
                try {
                    assignment = new Assignment(nameExpression, combined.getExpression());
                } catch (IncorrectTypeException e) {
                    ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
                }
                statements.add(assignment);
            } else if (!(statementNode instanceof EmptyStatement)) {
                statements.add((Statement) statementNode);
            }
        }

        // Remove the inner scope from the stack since we are done with this block
        popVariableScope(true);

        return new CodeBlock(innerScope, statements);
    }

    @Override
    public ASTNode visitIfElseIf(JavaFileParser.IfElseIfContext ctx) {
        Expression condition = expressionVisitor.visit(ctx.expr(), variableScopeStack.peek());
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        IfStatementChain nextStatement = (IfStatementChain) visit(ctx.ifStatement());
        return new IfStatementChain(condition, codeBlock, nextStatement);
    }

    @Override
    public ASTNode visitIfElse(JavaFileParser.IfElseContext ctx) {
        Expression condition = expressionVisitor.visit(ctx.expr(), variableScopeStack.peek());
        CodeBlock ifBlock = (CodeBlock) visit(ctx.codeBlock(0));
        CodeBlock elseBlock = (CodeBlock) visit(ctx.codeBlock(1));
        return new IfStatementChain(condition, ifBlock, elseBlock);
    }

    @Override
    public ASTNode visitIf(JavaFileParser.IfContext ctx) {
        Expression condition = expressionVisitor.visit(ctx.expr(), variableScopeStack.peek());
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        return new IfStatementChain(condition, codeBlock);
    }

    @Override
    public ASTNode visitWhileLoop(JavaFileParser.WhileLoopContext ctx) {
        Expression condition = expressionVisitor.visit(ctx.expr(), variableScopeStack.peek());
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        WhileLoop whileLoop = null;
        try {
            whileLoop = new WhileLoop(condition, codeBlock);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return whileLoop;
    }

    @Override
    public ASTNode visitForLoop(JavaFileParser.ForLoopContext ctx) {

        Statement initialiser = (ctx.forLoopInitialiser() != null)
                ? (Statement) visit(ctx.forLoopInitialiser()) : null;

        // Explanation: if a new variable is introduced in the
        // initialisation section of the for loop, then we need to
        // insert an intermediate VariableScope object to make the
        // declared variable accessible within the body of the for
        // loop but inaccessible once the for loop has finished.
        //
        // Examples:
        //
        // Extra scope required:
        // for (int i = 0; i < 10; i++) {}
        //
        // Extra scope not required:
        // int i;
        // for (i = 0; i < 10; i++) {}
        //
        // We need to compute this before visiting the CodeBlock of
        // this for loop, because doing that will cause a VariableScope
        // object to be created for that scope, and its parent needs
        // to be set correctly, using variableScopeStack.

        VariableScope newScope = pushNewVariableScope();
        if (initialiser instanceof DeclarationAndAssignment) {
            DeclarationAndAssignment decAndAssign = (DeclarationAndAssignment) initialiser;
            try {
                newScope.registerVariable(decAndAssign.getVariableName(), decAndAssign.getType());
            } catch (MultipleVariableDeclarationException e) {
                ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
            }
        }

        // We need to handle the condition after the initialiser, since it may
        // refer to the variable defined in the initialiser, which is only
        // accessible from the newly created scope
        Expression condition = (ctx.forLoopCondition() != null)
                ? expressionVisitor.visit(ctx.forLoopCondition(), variableScopeStack.peek())
                : new BooleanLiteral(true);
        Expression updater = (ctx.forLoopUpdater() != null)
                ? expressionVisitor.visit(ctx.forLoopUpdater(), variableScopeStack.peek()) : null;

        // Now we can safely visit the body of the loop
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());

        // Pop the header's scope
        popVariableScope(true);

        ForLoop forLoop = null;
        try {
            forLoop = new ForLoop(initialiser, condition, updater, codeBlock);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return forLoop;
    }

    @Override
    public ASTNode visitForLoopDeclareAndAssign(JavaFileParser.ForLoopDeclareAndAssignContext ctx) {
        return visit(ctx.variableDeclarationAndAssignment());
    }

    @Override
    public ASTNode visitForLoopAssignOnly(JavaFileParser.ForLoopAssignOnlyContext ctx) {
        return visit(ctx.assignment());
    }

    @Override
    public ASTNode visitForLoopCondition(JavaFileParser.ForLoopConditionContext ctx) {
        return expressionVisitor.visit(ctx.expr(), variableScopeStack.peek());
    }

    @Override
    public ASTNode visitForLoopUpdater(JavaFileParser.ForLoopUpdaterContext ctx) {
        return expressionVisitor.visit(ctx.expr(), variableScopeStack.peek());
    }

    /**
     * Pushes a new VariableScope object to the stack
     *
     * This method automatically handles setting the containing
     * scope attribute of the scope that is created.
     *
     * @return The VariableScope object that was created
     */
    private VariableScope pushNewVariableScope() {
        VariableScope newScope;
        if (!variableScopeStack.isEmpty()) {
            newScope = new VariableScope(variableScopeStack.peek());
        } else {
            newScope = new VariableScope();
        }
        variableScopeStack.push(newScope);
        return newScope;
    }

    /**
     * Pops the top VariableScope object from the stack
     *
     * This also informs the new top scope how many allocations its child
     * made, to ensure that different variables are never bound to the
     * same register index.
     *
     * @return The VariableScope object that was popped, or null
     *         if the stack is empty
     */
    private VariableScope popVariableScope(boolean updateParent) {
        VariableScope top = variableScopeStack.pop();
        if (updateParent) {
            top.notifyPopped();
        }
        return top;
    }

}
