package parser;

import ast.ASTNode;
import ast.expressions.*;
import ast.literals.DoubleLiteral;
import ast.literals.IntLiteral;
import ast.literals.LiteralValue;
import ast.operations.BinaryOp;
import ast.operations.IncrementOp;
import ast.statements.*;
import ast.structure.*;
import ast.types.*;

import java.util.*;

public class ASTBuilder extends JavaFileBaseVisitor<ASTNode> {

    private Stack<VariableScope> variableScopeStack;

    @Override
    public CompilationUnit visitFile(JavaFileParser.FileContext ctx) {
        variableScopeStack = new Stack<>();
        Imports imports = (Imports) visit(ctx.imports());
        JavaClass javaClass = (JavaClass) visit(ctx.classDefinition());
        // TODO: Support package name
        return new CompilationUnit(imports, javaClass);
    }

    @Override
    public Imports visitNoImports(JavaFileParser.NoImportsContext ctx) {
        return new Imports();
    }

    @Override
    public ASTNode visitSomeImports(JavaFileParser.SomeImportsContext ctx) {
        // TODO: Actually do something here!
        return new Imports();
    }

    @Override
    public JavaClass visitClassDefinition(JavaFileParser.ClassDefinitionContext ctx) {

        // Read the visibility status and class name
        AccessModifier visibility = (ctx.accessModifier() != null)
                ? (AccessModifier) visit(ctx.accessModifier())
                : AccessModifier.DEFAULT;
        String className = ctx.IDENTIFIER().toString();

        // Set up a variable scope for this class
        // (intentionally using the constructor that doesn't have a containingScope reference
        VariableScope classScope = new VariableScope();
        variableScopeStack.push(classScope);

        // Handle each method definition and attribute declaration
        List<ClassMethod> methods = new ArrayList<>();
        for (JavaFileParser.ClassItemContext classItem : ctx.classItem()) {
            ASTNode node = visit(classItem);
            if (node instanceof ClassAttributeDeclaration) {
                ClassAttributeDeclaration declaration = (ClassAttributeDeclaration) node;
                String variableName = declaration.getVariableName();
                Type type = declaration.getVariableType();
                VariableScope.Domain domain = VariableScope.Domain.StaticClassAttribute;
                classScope.registerVariable(variableName, type, domain);
            } else if (node instanceof ClassMethod) {
                // TODO: Ensure no duplicate definitions for same name/signature
                ClassMethod method = (ClassMethod) node;
                method.bindContainingVariableScope(classScope);
                methods.add(method);
            }
        }

        // Pop the scope for this class from the stack
        variableScopeStack.pop();

        return new JavaClass(visibility, className, classScope, methods);
    }

    @Override
    public ClassAttributeDeclaration visitClassAttribute(JavaFileParser.ClassAttributeContext ctx) {
        return (ClassAttributeDeclaration) visit(ctx.classAttributeDeclaration());
    }

    @Override
    public ClassAttributeDeclaration visitClassAttributeDeclaration(JavaFileParser.ClassAttributeDeclarationContext ctx) {
        AccessModifier modifier = (AccessModifier) visit(ctx.accessModifier());
        Type type = (Type) visit(ctx.type());
        String identifier = ctx.IDENTIFIER().toString();
        return new ClassAttributeDeclaration(type, identifier, modifier);
    }

    @Override
    public ClassMethod visitClassMethod(JavaFileParser.ClassMethodContext ctx) {
        return (ClassMethod) visit(ctx.methodDefinition());
    }

    @Override
    public VariableDeclaration visitDeclarationStatement(JavaFileParser.DeclarationStatementContext ctx) {
        return (VariableDeclaration) visit(ctx.variableDeclaration());
    }

    @Override
    public Assignment visitAssignmentStatement(JavaFileParser.AssignmentStatementContext ctx) {
        return (Assignment) visit(ctx.variableAssignment());
    }

    @Override
    public DeclarationAndAssignment visitDeclarationAssignmentStatement(JavaFileParser.DeclarationAssignmentStatementContext ctx) {
        return (DeclarationAndAssignment) visit(ctx.variableDeclarationAndAssignment());
    }

    @Override
    public ReturnStatement visitReturnStatement(JavaFileParser.ReturnStatementContext ctx) {
        Expression expression = (Expression) visit(ctx.expr());
        return new ReturnStatement(expression);
    }

    @Override
    public IfStatementChain visitIfStatementWrap(JavaFileParser.IfStatementWrapContext ctx) {
        return (IfStatementChain) visit(ctx.ifStatement());
    }

    @Override
    public WhileLoop visitWhileLoopWrap(JavaFileParser.WhileLoopWrapContext ctx) {
        return (WhileLoop) visit(ctx.whileLoop());
    }

    @Override
    public ForLoop visitForLoopWrap(JavaFileParser.ForLoopWrapContext ctx) {
        return (ForLoop) visit(ctx.forLoop());
    }

    @Override
    public FunctionCall visitFunctionCallStatement(JavaFileParser.FunctionCallStatementContext ctx) {
        return (FunctionCall) visit(ctx.functionCall());
    }

    @Override
    public VariableDeclaration visitVariableDeclaration(JavaFileParser.VariableDeclarationContext ctx) {
        Type type = (Type) visit(ctx.type());
        String name = ctx.IDENTIFIER().toString();
        return new VariableDeclaration(type, name);
    }

    @Override
    public Assignment visitVariableAssignment(JavaFileParser.VariableAssignmentContext ctx) {
        String name = ctx.IDENTIFIER().toString();
        Expression expression = (Expression) visit(ctx.expr());
        BinaryOp bop = null;
        switch (ctx.op.getType()) {
            case JavaFileParser.PLUS_EQUALS:
                bop = BinaryOp.ADD;
                break;
            case JavaFileParser.MINUS_EQUALS:
                bop = BinaryOp.SUBTRACT;
                break;
            case JavaFileParser.MULTIPLY_EQUALS:
                bop = BinaryOp.MULTIPLY;
                break;
            case JavaFileParser.DIVIDE_EQUALS:
                bop = BinaryOp.DIVIDE;
            // No case for JavaFileParser.EQUALS
        }
        if (bop != null) {
            VariableNameExpression varNameExpr = new VariableNameExpression(name);
            expression = new BinaryOperatorExpression(varNameExpr, expression, bop);
        }
        return new Assignment(name, expression);
    }

    @Override
    public DeclarationAndAssignment visitVariableDeclarationAndAssignment(JavaFileParser.VariableDeclarationAndAssignmentContext ctx) {
        Type type = (Type) visit(ctx.type());
        String name = ctx.IDENTIFIER().toString();
        Expression expression = (Expression) visit(ctx.expr());
        return new DeclarationAndAssignment(type, name, expression);
    }

    @Override
    public EmptyStatement visitEmptyStatement(JavaFileParser.EmptyStatementContext ctx) {
        return new EmptyStatement();
    }

    @Override
    public FunctionCall visitFunctionCallExpr(JavaFileParser.FunctionCallExprContext ctx) {
        return (FunctionCall) visit(ctx.functionCall());
    }

    @Override
    public Expression visitParenthesesExpr(JavaFileParser.ParenthesesExprContext ctx) {
        return (Expression) visit(ctx.expr());
    }

    @Override
    public NegateExpression visitNegateExpr(JavaFileParser.NegateExprContext ctx) {
        Expression expression = (Expression) visit(ctx.expr());
        return new NegateExpression(expression);
    }

    @Override
    public ASTNode visitIncrementExpr(JavaFileParser.IncrementExprContext ctx) {
        return super.visitIncrementExpr(ctx);
    }

    @Override
    public BinaryOperatorExpression visitInfixExpr(JavaFileParser.InfixExprContext ctx) {
        Expression left = (Expression) visit(ctx.expr(0));
        Expression right = (Expression) visit(ctx.expr(1));
        BinaryOp op = null;
        switch (ctx.op.getType()) {
            case JavaFileParser.MULTIPLY:
                op = BinaryOp.MULTIPLY;
                break;
            case JavaFileParser.DIVIDE:
                op = BinaryOp.DIVIDE;
                break;
            case JavaFileParser.PLUS:
                op = BinaryOp.ADD;
                break;
            case JavaFileParser.MINUS:
                op = BinaryOp.SUBTRACT;
                break;
            case JavaFileParser.EQUAL_TO:
                op = BinaryOp.EQUAL_TO;
                break;
            case JavaFileParser.NOT_EQUAL_TO:
                op = BinaryOp.NOT_EQUAL_TO;
                break;
            case JavaFileParser.LESS_THAN:
                op = BinaryOp.LESS_THAN;
                break;
            case JavaFileParser.LESS_THAN_EQUAL_TO:
                op = BinaryOp.LESS_THAN_OR_EQUAL_TO;
                break;
            case JavaFileParser.GREATER_THAN:
                op = BinaryOp.GREATER_THAN;
                break;
            case JavaFileParser.GREATER_THAN_EQUAL_TO:
                op = BinaryOp.GREATER_THAN_OR_EQUAL_TO;
        }
        return new BinaryOperatorExpression(left, right, op);
    }

    @Override
    public VariableIncrementExpression visitPreIncrementExpr(JavaFileParser.PreIncrementExprContext ctx) {
        String variableName = ctx.IDENTIFIER().toString();
        IncrementOp op = null;
        switch (ctx.op.getType()) {
            case JavaFileParser.INCREMENT:
                op = IncrementOp.PRE_INCREMENT;
                break;
            case JavaFileParser.DECREMENT:
                op = IncrementOp.PRE_DECREMENT;
        }
        VariableNameExpression expression = new VariableNameExpression(variableName);
        return new VariableIncrementExpression(expression, op);
    }

    @Override
    public VariableIncrementExpression visitPostIncrementExpr(JavaFileParser.PostIncrementExprContext ctx) {
        String variableName = ctx.IDENTIFIER().toString();
        IncrementOp op = null;
        switch (ctx.op.getType()) {
            case JavaFileParser.INCREMENT:
                op = IncrementOp.POST_INCREMENT;
                break;
            case JavaFileParser.DECREMENT:
                op = IncrementOp.POST_DECREMENT;
        }
        VariableNameExpression expression = new VariableNameExpression(variableName);
        return new VariableIncrementExpression(expression, op);
    }

    @Override
    public BinarySelectorExpression visitBinarySelectorExpr(JavaFileParser.BinarySelectorExprContext ctx) {
        Expression condition = (Expression) visit(ctx.expr(0));
        Expression trueExpression = (Expression) visit(ctx.expr(1));
        Expression falseExpression = (Expression) visit(ctx.expr(2));
        return new BinarySelectorExpression(condition, trueExpression, falseExpression);
    }

    @Override
    public VariableNameExpression visitVariableNameExpr(JavaFileParser.VariableNameExprContext ctx) {
        String variableName = ctx.IDENTIFIER().toString();
        return new VariableNameExpression(variableName);
    }

    @Override
    public LiteralValue visitValueExpr(JavaFileParser.ValueExprContext ctx) {
        return (LiteralValue) visit(ctx.value());
    }

    @Override
    public FunctionCall visitFunctionCall(JavaFileParser.FunctionCallContext ctx) {
        String functionName = ctx.IDENTIFIER().toString();
        ExpressionList functionArgs = (ExpressionList) visit(ctx.functionArgs());
        return new FunctionCall(functionName, functionArgs.getExpressionList());
    }

    @Override
    public ExpressionList visitNoArgs(JavaFileParser.NoArgsContext ctx) {
        List<Expression> emptyExpressionList = new ArrayList<>();
        return new ExpressionList(emptyExpressionList);
    }

    @Override
    public ExpressionList visitSomeArgs(JavaFileParser.SomeArgsContext ctx) {
        List<Expression> expressions = new ArrayList<>();
        for (JavaFileParser.ExprContext expressionCtx : ctx.expr()) {
            Expression expression = (Expression) visit(expressionCtx);
            expressions.add(expression);
        }
        return new ExpressionList(expressions);
    }

    @Override
    public ClassMethod visitMethodDefinition(JavaFileParser.MethodDefinitionContext ctx) {
        AccessModifier modifier = (ctx.accessModifier() != null)
                ? (AccessModifier) visit(ctx.accessModifier())
                : AccessModifier.DEFAULT;
        boolean isStatic = ctx.STATIC() != null;
        // TODO: Investigate this, sometimes it's null (eg for void)
        Type returnType = (Type) visit(ctx.type());
        String methodName = ctx.IDENTIFIER().toString();
        MethodParameterList params = (MethodParameterList) visit(ctx.methodParams());
        List<MethodParameter> paramsList = params.getParameters();
        CodeBlock body = (CodeBlock) visit(ctx.codeBlock());
        return new ClassMethod(modifier, isStatic, returnType, methodName, paramsList, body);
    }

    @Override
    public MethodParameterList visitNoParams(JavaFileParser.NoParamsContext ctx) {
        List<MethodParameter> emptyList = new ArrayList<>();
        return new MethodParameterList(emptyList);
    }

    @Override
    public MethodParameterList visitSomeParams(JavaFileParser.SomeParamsContext ctx) {
        List<MethodParameter> paramList = new ArrayList<>();
        for (int i = 0; i < ctx.IDENTIFIER().size(); i++) {
            String parameterName = ctx.IDENTIFIER(i).toString();
            Type type = (Type) visit(ctx.type(i));
            MethodParameter param = new MethodParameter(parameterName, type);
            paramList.add(param);
        }
        return new MethodParameterList(paramList);
    }

    @Override
    public CodeBlock visitStatementList(JavaFileParser.StatementListContext ctx) {

        // Create a new scope for this code block and push it to the stack
        VariableScope containingScope = variableScopeStack.peek();
        VariableScope variableScope = new VariableScope(containingScope);
        variableScopeStack.push(variableScope);

        // Recursively visit each statement in this code block
        List<Statement> statements = new ArrayList<>();
        for (JavaFileParser.StatementContext statementCtx : ctx.statement()) {
            ASTNode statementNode = visit(statementCtx);
            if (statementNode instanceof VariableDeclaration) {
                VariableDeclaration declaration = (VariableDeclaration) statementNode;
                String name = declaration.getVariableName();
                Type type = declaration.getVariableType();
                VariableScope.Domain domain = VariableScope.Domain.Local;
                variableScope.registerVariable(name, type, domain);
            } else if (statementNode instanceof DeclarationAndAssignment) {
                DeclarationAndAssignment combined = (DeclarationAndAssignment) statementNode;
                String name = combined.getVariableName();
                Type type = combined.getType();
                VariableScope.Domain domain = VariableScope.Domain.Local;
                variableScope.registerVariable(name, type, domain);
                Assignment assignment = new Assignment(name, combined.getExpression());
                statements.add(assignment);
            } else if (!(statementNode instanceof EmptyStatement)) {
                statements.add((Statement) statementNode);
            }
        }

        // Remove the scope from the stack since we are done with this block
        variableScopeStack.pop();

        return new CodeBlock(variableScope, statements);
    }

    @Override
    public IfStatementChain visitIfElseIf(JavaFileParser.IfElseIfContext ctx) {
        Expression condition = (Expression) visit(ctx.expr());
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        IfStatementChain nextStatement = (IfStatementChain) visit(ctx.ifStatement());
        return new IfStatementChain(condition, codeBlock, nextStatement);
    }

    @Override
    public IfStatementChain visitIfElse(JavaFileParser.IfElseContext ctx) {
        Expression condition = (Expression) visit(ctx.expr());
        CodeBlock ifBlock = (CodeBlock) visit(ctx.codeBlock(0));
        CodeBlock elseBlock = (CodeBlock) visit(ctx.codeBlock(1));
        return new IfStatementChain(condition, ifBlock, elseBlock);
    }

    @Override
    public IfStatementChain visitIf(JavaFileParser.IfContext ctx) {
        Expression condition = (Expression) visit(ctx.expr());
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        return new IfStatementChain(condition, codeBlock);
    }

    @Override
    public WhileLoop visitWhileLoop(JavaFileParser.WhileLoopContext ctx) {
        Expression condition = (Expression) visit(ctx.expr());
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        return new WhileLoop(condition, codeBlock);
    }

    @Override
    public ForLoop visitForLoop(JavaFileParser.ForLoopContext ctx) {
        Statement initialiser = (ctx.forLoopInitialiser() != null)
                ? (Statement) visit(ctx.forLoopInitialiser()) : null;
        Expression condition = (ctx.forLoopCondition() != null)
                ? (Expression) visit(ctx.forLoopCondition()) : null;
        Expression updater = (ctx.forLoopUpdater() != null)
                ? (Expression) visit(ctx.forLoopUpdater()) : null;
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        return new ForLoop(initialiser, condition, updater, codeBlock);
    }

    // TODO: Think about how to handle this
    // Remember that, normally, a DeclarationAndAssignment is split into two statements.
    // Really, we want this declaration to be added to the code block belonging to that for loop.
    @Override
    public DeclarationAndAssignment visitForLoopDeclareAndAssign(JavaFileParser.ForLoopDeclareAndAssignContext ctx) {
        return (DeclarationAndAssignment) visit(ctx.variableDeclarationAndAssignment());
    }

    @Override
    public Assignment visitForLoopAssignOnly(JavaFileParser.ForLoopAssignOnlyContext ctx) {
        return (Assignment) visit(ctx.variableAssignment());
    }

    @Override
    public Expression visitForLoopCondition(JavaFileParser.ForLoopConditionContext ctx) {
        return (Expression) visit(ctx.expr());
    }

    @Override
    public Expression visitForLoopUpdater(JavaFileParser.ForLoopUpdaterContext ctx) {
        return (Expression) visit(ctx.expr());
    }

    @Override
    public AccessModifier visitAccessModifier(JavaFileParser.AccessModifierContext ctx) {
        AccessModifier modifier = AccessModifier.DEFAULT;
        switch (ctx.modifier.getType()) {
            case JavaFileParser.PUBLIC:
                modifier = AccessModifier.PUBLIC;
                break;
            case JavaFileParser.PRIVATE:
                modifier = AccessModifier.PRIVATE;
                break;
            case JavaFileParser.PROTECTED:
                modifier = AccessModifier.PROTECTED;
        }
        return modifier;
    }

    @Override
    public VoidType visitVoidType(JavaFileParser.VoidTypeContext ctx) {
        return new VoidType();
    }

    @Override
    public PrimitiveType visitPrimitiveType(JavaFileParser.PrimitiveTypeContext ctx) {
        PrimitiveType type = null;
        switch (ctx.primitiveType.getType()) {
            case JavaFileParser.INT:
                type = PrimitiveType.Int;
                break;
            case JavaFileParser.SHORT:
                type = PrimitiveType.Short;
                break;
            case JavaFileParser.LONG:
                type = PrimitiveType.Long;
                break;
            case JavaFileParser.BYTE:
                type = PrimitiveType.Byte;
                break;
            case JavaFileParser.CHAR:
                type = PrimitiveType.Char;
                break;
            case JavaFileParser.BOOLEAN:
                type = PrimitiveType.Boolean;
                break;
            case JavaFileParser.FLOAT:
                type = PrimitiveType.Float;
                break;
            case JavaFileParser.DOUBLE:
                type = PrimitiveType.Double;
        }
        return type;
    }

    @Override
    public NonPrimitiveType visitNonPrimitiveType(JavaFileParser.NonPrimitiveTypeContext ctx) {
        String className = ctx.IDENTIFIER().toString();
        return new NonPrimitiveType(className);
    }

    @Override
    public IntLiteral visitSignedIntegerValue(JavaFileParser.SignedIntegerValueContext ctx) {
        int value = Integer.parseInt(ctx.SIGNED_INTEGER().toString());
        return new IntLiteral(value);
    }

    @Override
    public ASTNode visitDecimalValue(JavaFileParser.DecimalValueContext ctx) {
        double value = Double.parseDouble(ctx.DECIMAL().toString());
        return new DoubleLiteral(value);
    }
}
