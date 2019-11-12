package parser;

import ast.ASTNode;
import ast.expressions.*;
import ast.structure.*;
import ast.types.NonPrimitiveType;
import ast.types.PrimitiveType;
import ast.types.Type;
import ast.types.VoidType;

import java.util.*;

public class ASTBuilder extends JavaFileBaseVisitor<ASTNode> {

    @Override
    public CompilationUnit visitFile(JavaFileParser.FileContext ctx) {
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
        AccessModifier visibility = (ctx.accessModifier() != null)
                ? (AccessModifier) visit(ctx.accessModifier())
                : new AccessModifier(AccessModifier.AccessModifierType.DEFAULT);
        // TODO: check that getType() does what you think it does here
        String className = ctx.IDENTIFIER().toString();
        VariableDeclarationGroup attributeDeclarations = new VariableDeclarationGroup();
        List<ClassMethod> methods = new ArrayList<>();
        for (JavaFileParser.ClassItemContext classItem : ctx.classItem()) {
            ASTNode node = visit(classItem);
            if (node instanceof ClassAttributeDeclaration) {
                try {
                    ClassAttributeDeclaration declaration = (ClassAttributeDeclaration) node;
                    attributeDeclarations.addDeclaration(declaration);
                } catch (VariableDeclarationGroup.MultipleDeclarationsException e) {
                    // TODO: Handle multiple declarations of same variable name
                }
            } else if (node instanceof ClassMethod) {
                // TODO: Ensure no duplicate definitions for same name/signature
                ClassMethod method = (ClassMethod) node;
                methods.add(method);
            }
        }
        return new JavaClass(visibility, className, attributeDeclarations, methods);
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
//        Type type = (Type) visit(ctx.type());
//        String variableName = ctx.IDENTIFIER().toString();
//        return new VariableDeclaration(type, variableName);
    }

    @Override
    public Assignment visitAssignmentStatement(JavaFileParser.AssignmentStatementContext ctx) {
//        String variableName = ctx.IDENTIFIER().toString();
//        Expression expression = (Expression) visit(ctx.expr());
//        return new Assignment(variableName, expression);
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
    public UnaryOperatorExpression visitNegateExpr(JavaFileParser.NegateExprContext ctx) {
        Expression expression = (Expression) visit(ctx.expr());
        return new UnaryOperatorExpression(expression, UnaryOperatorExpression.Operation.NEGATE);
    }

    @Override
    public BinaryOperatorExpression visitInfixExpr(JavaFileParser.InfixExprContext ctx) {
        Expression left = (Expression) visit(ctx.expr(0));
        Expression right = (Expression) visit(ctx.expr(1));
        BinaryOperatorExpression.Operation op = null;
        switch (ctx.op.getType()) {
            case JavaFileParser.MULTIPLY:
                op = BinaryOperatorExpression.Operation.MULTIPLY;
                break;
            case JavaFileParser.DIVIDE:
                op = BinaryOperatorExpression.Operation.DIVIDE;
                break;
            case JavaFileParser.PLUS:
                op = BinaryOperatorExpression.Operation.ADD;
                break;
            case JavaFileParser.MINUS:
                op = BinaryOperatorExpression.Operation.SUBTRACT;
        }
        return new BinaryOperatorExpression(left, right, op);
    }

    @Override
    public UnaryOperatorExpression visitPostfixExpr(JavaFileParser.PostfixExprContext ctx) {
        UnaryOperatorExpression.Operation op = null;
        switch (ctx.op.getType()) {
            case JavaFileParser.INCREMENT:
                op = UnaryOperatorExpression.Operation.INCREMENT;
                break;
            case JavaFileParser.DECREMENT:
                op = UnaryOperatorExpression.Operation.DECREMENT;
        }
        Expression expression = (Expression) visit(ctx.expr());
        return new UnaryOperatorExpression(expression, op);
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
    public ValueExpression visitValueExpr(JavaFileParser.ValueExprContext ctx) {
        return (ValueExpression) visit(ctx.value());
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
        // TODO: Check that the thing is indeed null if it's not present for an optional term
        AccessModifier modifier = new AccessModifier(AccessModifier.AccessModifierType.DEFAULT);
        if (ctx.accessModifier() != null) {
            modifier = (AccessModifier) visit(ctx.accessModifier());
        }
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
        Map<String, Type> declarations = new HashMap<>();
        List<Statement> statements = new ArrayList<>();
        for (JavaFileParser.StatementContext statementCtx : ctx.statement()) {
            ASTNode statementNode = visit(statementCtx);
            if (statementNode instanceof VariableDeclaration) {
                VariableDeclaration declaration = (VariableDeclaration) statementNode;
                if (declarations.containsKey(declaration.getVariableName())) {
                    // TODO: We already have a declaration for this variable in
                    // this scope, so the compiler should reject the file with a
                    // semantic error.
                    // Possibly throw an exception?
                    // Throwing exceptions in a visitor class looks like it could
                    // be a nightmare because the method we are overriding doesn't
                    // throw any exceptions. Hmmmm
                }
                String name = declaration.getVariableName();
                Type type = declaration.getVariableType();
                declarations.put(name, type);
            } else if (statementNode instanceof DeclarationAndAssignment) {
                DeclarationAndAssignment combined = (DeclarationAndAssignment) statementNode;
                declarations.put(combined.getVariableName(), combined.getType());
                Assignment assignment = new Assignment(combined.getVariableName(), combined.getExpression());
                statements.add(assignment);
            } else if (!(statementNode instanceof EmptyStatement)) {
                statements.add((Statement) statementNode);
            }
        }
        return new CodeBlock(declarations, statements);
    }

    @Override
    public IfStatementChain visitIfElseIf(JavaFileParser.IfElseIfContext ctx) {
        Expression condition = (Expression) visit(ctx.expr());
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        IfStatementChain nextStatement = (IfStatementChain) visit(ctx.ifStatement());
        nextStatement.prependBlock(condition, codeBlock);
        return nextStatement;
    }

    @Override
    public IfStatementChain visitIfElse(JavaFileParser.IfElseContext ctx) {
        Expression condition = (Expression) visit(ctx.expr());
        CodeBlock ifBlock = (CodeBlock) visit(ctx.codeBlock(0));
        CodeBlock elseBlock = (CodeBlock) visit(ctx.codeBlock(1));
        List<Expression> conditions = new ArrayList<>();
        conditions.add(condition);
        List<CodeBlock> codeBlocks = new ArrayList<>();
        codeBlocks.add(ifBlock);
        return new IfStatementChain(conditions, codeBlocks, elseBlock);
    }

    @Override
    public IfStatementChain visitIf(JavaFileParser.IfContext ctx) {
        Expression condition = (Expression) visit(ctx.expr());
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        List<Expression> conditions = new ArrayList<>();
        conditions.add(condition);
        List<CodeBlock> codeBlocks = new ArrayList<>();
        codeBlocks.add(codeBlock);
        return new IfStatementChain(conditions, codeBlocks);
    }

    @Override
    public WhileLoop visitWhileLoop(JavaFileParser.WhileLoopContext ctx) {
        Expression condition = (Expression) visit(ctx.expr());
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        return new WhileLoop(condition, codeBlock);
    }

    @Override
    public ForLoop visitForLoop(JavaFileParser.ForLoopContext ctx) {
        Statement initialiser = (Statement) visit(ctx.statement(0));
        Expression condition = (Expression) visit(ctx.expr());
        Statement updater = (Statement) visit(ctx.statement(1));
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());
        return new ForLoop(initialiser, condition, updater, codeBlock);
    }

    @Override
    public AccessModifier visitAccessModifier(JavaFileParser.AccessModifierContext ctx) {
        AccessModifier.AccessModifierType modifierType = AccessModifier.AccessModifierType.DEFAULT;
        switch (ctx.modifier.getType()) {
            case JavaFileParser.PUBLIC:
                modifierType = AccessModifier.AccessModifierType.PUBLIC;
                break;
            case JavaFileParser.PRIVATE:
                modifierType = AccessModifier.AccessModifierType.PRIVATE;
                break;
            case JavaFileParser.PROTECTED:
                modifierType = AccessModifier.AccessModifierType.PROTECTED;
        }
        return new AccessModifier(modifierType);
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
    public IntegerLiteral visitSignedIntegerValue(JavaFileParser.SignedIntegerValueContext ctx) {
        int value = Integer.parseInt(ctx.SIGNED_INTEGER().toString());
        return new IntegerLiteral(value);
    }

    @Override
    public ASTNode visitDecimalValue(JavaFileParser.DecimalValueContext ctx) {
        double value = Double.parseDouble(ctx.DECIMAL().toString());
        return new DecimalLiteral(value);
    }
}
