package parser;

import ast.ASTNode;
import ast.expressions.*;
import ast.literals.*;
import ast.operations.BinaryOp;
import ast.operations.IncrementOp;
import ast.statements.*;
import ast.structure.*;
import ast.types.*;
import errors.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class ASTBuilder extends JavaFileBaseVisitor<ASTNode> {

    private Stack<VariableScope> variableScopeStack;
    private FunctionTable functionTable;
    private ClassTable classTable;
    private Type currentFunctionReturnType;
    private JavaClass currentClass;

    private BopVisitor bopVisitor;
    private TypeVisitor typeVisitor;
    private AccessModifierVisitor accessModifierVisitor;

    public ASTBuilder(FunctionTable functionTable, ClassTable classTable) {
        this.functionTable = functionTable;
        this.classTable = classTable;
        currentClass = null;
        variableScopeStack = new Stack<>();

        bopVisitor = new BopVisitor();
        typeVisitor = new TypeVisitor(classTable);
        accessModifierVisitor = new AccessModifierVisitor();
    }

    public ClassMethod visitSubroutine(SubroutineToCompile subroutine, JavaClass containingClass) {
        this.currentClass = containingClass;
        typeVisitor.setCurrentClass(containingClass);
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

        // If it's a non-static method, insert a reference to the object as the
        // first parameter.
        if (!isStatic) {
            try {
                scopeForParameters.registerVariable("this", currentClass);
            } catch (MultipleVariableDeclarationException e) {
                ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
            }
        }

        // Add all method parameters to the stack
        for (MethodParameter param : paramsList) {
            String name = param.getParameterName();
            Type type = param.getType();
            try {
                scopeForParameters.registerVariable(name, type);
            } catch (MultipleVariableDeclarationException e) {
                ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
            }
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

        // Pass 'this' as extra parameter
        try {
            scopeForParameters.registerVariable("this", currentClass);
        } catch (MultipleVariableDeclarationException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }

        // Put all the constructor parameters on the stack
        for (MethodParameter param : paramsList) {
            String name = param.getParameterName();
            Type type = param.getType();
            try {
                scopeForParameters.registerVariable(name, type);
            } catch (MultipleVariableDeclarationException e) {
                ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
            }
        }

        // Visit the body of the method
        currentFunctionReturnType = returnType;
        CodeBlock body = (CodeBlock) visit(ctx.codeBlock());

        // Pop the variable scope of the parameters from the stack
        popVariableScope(false);

        // Return the method that was created
        return new ClassMethod(modifier, isStatic, returnType, methodName, paramsList, body, currentClass);
    }

    @Override
    public VariableDeclaration visitDeclarationStatement(JavaFileParser.DeclarationStatementContext ctx) {
        return (VariableDeclaration) visit(ctx.variableDeclaration());
    }

    @Override
    public Assignment visitAssignmentStatement(JavaFileParser.AssignmentStatementContext ctx) {
        return (Assignment) visit(ctx.assignment());
    }

    @Override
    public DeclarationAndAssignment visitDeclarationAssignmentStatement(JavaFileParser.DeclarationAssignmentStatementContext ctx) {
        return (DeclarationAndAssignment) visit(ctx.variableDeclarationAndAssignment());
    }

    @Override
    public ReturnStatement visitReturnStatement(JavaFileParser.ReturnStatementContext ctx) {
        Expression expression = (Expression) visit(ctx.expr());
        ReturnStatement returnStatement = null;
        try {
            returnStatement = new ReturnStatement(expression, currentFunctionReturnType);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return returnStatement;
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
    public Expression visitFunctionCallStatement(JavaFileParser.FunctionCallStatementContext ctx) {
        return (Expression) visit(ctx.functionCall());
    }

    @Override
    public VariableDeclaration visitVariableDeclaration(JavaFileParser.VariableDeclarationContext ctx) {
        Type type = typeVisitor.visit(ctx.type());
        String name = ctx.IDENTIFIER().toString();
        return new VariableDeclaration(type, name);
    }

    @Override
    public Assignment visitVariableAssignment(JavaFileParser.VariableAssignmentContext ctx) {
        String variableName = ctx.variableName().getText();
        VariableScope scope = variableScopeStack.peek();
        VariableExpression variableExpression = new LocalVariableExpression(variableName, scope);
        Token op = ctx.op;
        Expression rhs = (Expression) visit(ctx.expr());
        return buildAssignment(variableExpression, op, rhs, ctx);
    }

    @Override
    public Assignment visitAttributeAssignment(JavaFileParser.AttributeAssignmentContext ctx) {
        String localVarName = ctx.variableName().getText();
        LocalVariableExpression object =
                new LocalVariableExpression(localVarName, variableScopeStack.peek());
        String attributeName = ctx.IDENTIFIER().getText();
        Token op = ctx.op;
        Expression rhs = (Expression) visit(ctx.expr());
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
        Expression arrayExpression = (Expression) visit(ctx.expr(0));
        Expression indexExpression = (Expression) visit(ctx.expr(1));
        Expression rhs = (Expression) visit(ctx.expr(2));
        Token op = ctx.op;

        ArrayIndexExpression lhs = null;
        try {
            lhs = new ArrayIndexExpression(arrayExpression, indexExpression);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }

        return buildAssignment(lhs, op, rhs, ctx);
    }

    private Assignment buildAssignment(VariableExpression variableExpression,
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

        VariableScope currentScope = variableScopeStack.peek();

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
    public DeclarationAndAssignment visitVariableDeclarationAndAssignment(
            JavaFileParser.VariableDeclarationAndAssignmentContext ctx) {
        Type type = typeVisitor.visit(ctx.type());
        String name = ctx.IDENTIFIER().toString();
        Expression expression = (Expression) visit(ctx.expr());
        return new DeclarationAndAssignment(type, name, expression);
    }

    @Override
    public EmptyStatement visitEmptyStatement(JavaFileParser.EmptyStatementContext ctx) {
        return new EmptyStatement();
    }

    @Override
    public Expression visitFunctionCallExpr(JavaFileParser.FunctionCallExprContext ctx) {
        return (Expression) visit(ctx.functionCall());
    }

    @Override
    public Expression visitParenthesesExpr(JavaFileParser.ParenthesesExprContext ctx) {
        return (Expression) visit(ctx.expr());
    }

    @Override
    public NegateExpression visitNegateExpr(JavaFileParser.NegateExprContext ctx) {
        Expression expression = (Expression) visit(ctx.expr());
        NegateExpression negExpression = null;
        try {
            negExpression = new NegateExpression(expression);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return negExpression;
    }

    @Override
    public NotExpression visitNotExpr(JavaFileParser.NotExprContext ctx) {
        Expression expression = (Expression) visit(ctx.expr());
        NotExpression notExpression = null;
        try {
            notExpression = new NotExpression(expression);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return notExpression;
    }

    @Override
    public NewObjectExpression visitNewObjectExpr(JavaFileParser.NewObjectExprContext ctx) {
        String className = ctx.IDENTIFIER().toString();
        JavaClass javaClass = null;
        try {
            javaClass = classTable.lookupClass(className);
        } catch (UnknownClassException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        ExpressionList expressionList = (ExpressionList) visit(ctx.functionArgs());
        List<Expression> arguments = expressionList.getExpressionList();

        assert javaClass != null;
        if (arguments.size() > 0 || javaClass.hasNoArgumentConstructor()) {
            List<Type> argumentTypes = arguments
                    .stream()
                    .map(Expression::getType)
                    .collect(Collectors.toList());
            FunctionTableEntry entry = javaClass.lookupConstructor(argumentTypes);
            return new NewObjectExpression(javaClass, arguments, entry);
        } else {
            return new NewObjectExpression(javaClass);
        }
    }

    @Override
    public NewArrayExpression visitNewArrayExpr(JavaFileParser.NewArrayExprContext ctx) {

        // Identify the base element type
        String identifier = ctx.IDENTIFIER().getText();
        Type currentType = null;
        try {
            currentType = classTable.lookupClass(identifier);
        } catch (UnknownClassException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }

        // Iterate over each axis of the array
        NewArrayExpression result = null;
        for (int i = 0; i < ctx.expr().size(); i++) {
            Expression lengthExpression = (Expression) visit(ctx.expr(i));
            try {
                result = new NewArrayExpression(currentType, lengthExpression);
            } catch (IncorrectTypeException e) {
                ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
            }
            currentType = new ObjectArray(currentType);
        }

        return result;
    }

    @Override
    public ASTNode visitIncrementExpr(JavaFileParser.IncrementExprContext ctx) {
        return super.visitIncrementExpr(ctx);
    }

    @Override
    public ASTNode visitMultiplicativeBopExpr(JavaFileParser.MultiplicativeBopExprContext ctx) {
        BinaryOp op = bopVisitor.visit(ctx.multiplicativeBop());
        return makeBopExpression(ctx.expr(), op, ctx);
    }

    @Override
    public ASTNode visitAdditiveBopExpr(JavaFileParser.AdditiveBopExprContext ctx) {
        BinaryOp op = bopVisitor.visit(ctx.additiveBop());
        return makeBopExpression(ctx.expr(), op, ctx);
    }

    @Override
    public ASTNode visitComparisonBopExpr(JavaFileParser.ComparisonBopExprContext ctx) {
        BinaryOp op = bopVisitor.visit(ctx.comparisonBop());
        return makeBopExpression(ctx.expr(), op, ctx);
    }

    @Override
    public ASTNode visitLogicalAndBopExpr(JavaFileParser.LogicalAndBopExprContext ctx) {
        BinaryOp op = bopVisitor.visit(ctx.logicalAndBop());
        return makeBopExpression(ctx.expr(), op, ctx);
    }

    @Override
    public ASTNode visitLogicalOrBopExpr(JavaFileParser.LogicalOrBopExprContext ctx) {
        BinaryOp op = bopVisitor.visit(ctx.logicalOrBop());
        return makeBopExpression(ctx.expr(), op, ctx);
    }

    private BinaryOperatorExpression makeBopExpression(List<JavaFileParser.ExprContext> expressions,
                                                       BinaryOp bop,
                                                       ParserRuleContext ctx) {
        Expression left = (Expression) visit(expressions.get(0));
        Expression right = (Expression) visit(expressions.get(1));
        BinaryOperatorExpression bopExpression = null;
        try {
            bopExpression = new BinaryOperatorExpression(left, right, bop);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return bopExpression;
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
        VariableScope currentScope = variableScopeStack.peek();
        LocalVariableExpression expression = new LocalVariableExpression(variableName, currentScope);
        VariableIncrementExpression incrementExpression = null;
        try {
            incrementExpression = new VariableIncrementExpression(expression, op);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return incrementExpression;
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
        VariableScope currentScope = variableScopeStack.peek();
        LocalVariableExpression expression = new LocalVariableExpression(variableName, currentScope);
        VariableIncrementExpression incrementExpression = null;
        try {
            incrementExpression = new VariableIncrementExpression(expression, op);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return incrementExpression;
    }

    @Override
    public BinarySelectorExpression visitBinarySelectorExpr(JavaFileParser.BinarySelectorExprContext ctx) {
        Expression condition = (Expression) visit(ctx.expr(0));
        Expression trueExpression = (Expression) visit(ctx.expr(1));
        Expression falseExpression = (Expression) visit(ctx.expr(2));
        BinarySelectorExpression expression = null;
        try {
            expression = new BinarySelectorExpression(condition, trueExpression, falseExpression);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return expression;
    }

    @Override
    public ASTNode visitArrayLookupExpr(JavaFileParser.ArrayLookupExprContext ctx) {

        // Start with the array itself
        Expression lookupExpression = (Expression) visit(ctx.expr(0));

        // Recurse over the indices
        // For example, for 'a[1][2]' this loop will run twice
        // We also get a guarantee from the grammar that it will run at least
        // once.
        for (int i = 1; i < ctx.expr().size(); i++) {
            Expression indexExpression = (Expression) visit(ctx.expr(i));
            try {
                lookupExpression = new ArrayIndexExpression(lookupExpression, indexExpression);
            } catch (IncorrectTypeException e) {
                ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
            }
        }

        return lookupExpression;
    }

    @Override
    public LocalVariableExpression visitVariableNameExpr(JavaFileParser.VariableNameExprContext ctx) {
        return (LocalVariableExpression) visit(ctx.variableName());
    }

    @Override
    public AttributeNameExpression visitAttributeLookupExpr(JavaFileParser.AttributeLookupExprContext ctx) {
        LocalVariableExpression localVariableExpression = (LocalVariableExpression) visit(ctx.variableName(0));
        String attributeName = ctx.variableName(1).getText();
        AttributeNameExpression result = null;
        try {
            result = new AttributeNameExpression(localVariableExpression, attributeName);
        } catch (JavAssemblerException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return result;
    }

    @Override
    public LocalVariableExpression visitVariableReference(JavaFileParser.VariableReferenceContext ctx) {
        String variableName = ctx.IDENTIFIER().toString();
        VariableScope currentScope = variableScopeStack.peek();
        return new LocalVariableExpression(variableName, currentScope);
    }

    @Override
    public LocalVariableExpression visitThisReference(JavaFileParser.ThisReferenceContext ctx) {
        return new LocalVariableExpression("this", variableScopeStack.peek());
    }

    @Override
    public LiteralValue visitLiteralExpr(JavaFileParser.LiteralExprContext ctx) {
        return (LiteralValue) visit(ctx.literal());
    }

    @Override
    public Expression visitQualifiedFunctionCall(JavaFileParser.QualifiedFunctionCallContext ctx) {
        String qualifier = ctx.IDENTIFIER(0).toString();
        String functionName = ctx.IDENTIFIER(1).toString();
        ExpressionList expressionList = (ExpressionList) visit(ctx.functionArgs());
        List<Expression> arguments = expressionList.getExpressionList();
        VariableScope currentScope = variableScopeStack.peek();
        if (currentScope.hasMappingFor(qualifier)) {
            return buildMethodCall(qualifier, functionName, arguments, ctx);
        } else {
            return visitFunctionCall(qualifier, functionName, arguments, ctx);
        }
    }

    private MethodCall buildMethodCall(String localVariableName,
                                       String methodName,
                                       List<Expression> argumentsList,
                                       JavaFileParser.QualifiedFunctionCallContext ctx) {
        VariableScope currentScope = variableScopeStack.peek();
        LocalVariableExpression localVariable = new LocalVariableExpression(localVariableName, currentScope);
        Type variableType = localVariable.getType();
        List<String> variableTypeStrings = argumentsList
                .stream()
                .map(Expression::getType)
                .map(Type::toString)
                .collect(Collectors.toList());
        String errorMessageIfNotFound = "Method " + methodName + "("
                + String.join(", ", variableTypeStrings) + ")"
                + " is not defined in static context " + variableType;
        if (!(variableType instanceof JavaClass)) {
            ErrorReporting.reportError(errorMessageIfNotFound, ctx, currentClass.toString());
        }
        JavaClass javaClass = (JavaClass) variableType;
        List<Type> argumentTypes = argumentsList
                .stream()
                .map(Expression::getType)
                .collect(Collectors.toList());
        Integer vtableIndex = javaClass.getVirtualTableIndex(methodName, argumentTypes);
        if (vtableIndex == null) {
            ErrorReporting.reportError(errorMessageIfNotFound, ctx, currentClass.toString());
        }
        Type returnType = javaClass.getReturnTypeOfMethodAtIndex(vtableIndex);

        // Look up static method signature
        FunctionTableEntry tableEntry = javaClass.lookupMethod(methodName, argumentTypes);

        return new MethodCall(localVariable, argumentsList, returnType, vtableIndex, tableEntry);
    }

    @Override
    public FunctionCall visitUnqualifiedFunctionCall(JavaFileParser.UnqualifiedFunctionCallContext ctx) {
        String functionName = ctx.IDENTIFIER().toString();
        ExpressionList expressionList = (ExpressionList) visit(ctx.functionArgs());
        List<Expression> arguments = expressionList.getExpressionList();
        return visitFunctionCall(currentClass.toString(), functionName, arguments, ctx);
    }

    private FunctionCall visitFunctionCall(String className,
                                           String functionName,
                                           List<Expression> arguments,
                                           ParserRuleContext ctx) {

        // Look up the class from the name
        JavaClass javaClass = null;
        try {
            javaClass = classTable.lookupClass(className);
        } catch (UnknownClassException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }

        // Get a list of argument types to identify which function to call
        List<Type> argumentTypes = arguments
                .stream()
                .map(Expression::getType)
                .collect(Collectors.toList());

        // Look up the function table entry from the function's signature
        FunctionTableEntry tableEntry = null;
        try {
            tableEntry = functionTable.lookupFunction(javaClass, functionName, argumentTypes);
        } catch (InvalidClassNameException | UndeclaredFunctionException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }

        assert tableEntry != null;
        if (tableEntry.canBeCalledFrom(currentClass)) {
            return new FunctionCall(tableEntry, arguments);
        } else {
            String message = "Illegal call to a private method: method "
                    + functionName + " is declared private in " + javaClass + ".";
            ErrorReporting.reportError(message, ctx, currentClass.toString());
            return null;
        }
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
    public MethodParameterList visitNoParams(JavaFileParser.NoParamsContext ctx) {
        List<MethodParameter> emptyList = new ArrayList<>();
        return new MethodParameterList(emptyList);
    }

    @Override
    public MethodParameterList visitSomeParams(JavaFileParser.SomeParamsContext ctx) {
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
    public CodeBlock visitStatementList(JavaFileParser.StatementListContext ctx) {

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
        WhileLoop whileLoop = null;
        try {
            whileLoop = new WhileLoop(condition, codeBlock);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return whileLoop;
    }

    @Override
    public ForLoop visitForLoop(JavaFileParser.ForLoopContext ctx) {

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
                ? (Expression) visit(ctx.forLoopCondition())
                : new BooleanLiteral(true);
        Expression updater = (ctx.forLoopUpdater() != null)
                ? (Expression) visit(ctx.forLoopUpdater()) : null;

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
    public DeclarationAndAssignment visitForLoopDeclareAndAssign(JavaFileParser.ForLoopDeclareAndAssignContext ctx) {
        return (DeclarationAndAssignment) visit(ctx.variableDeclarationAndAssignment());
    }

    @Override
    public Assignment visitForLoopAssignOnly(JavaFileParser.ForLoopAssignOnlyContext ctx) {
        return (Assignment) visit(ctx.assignment());
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
    public LiteralValue visitLiteral(JavaFileParser.LiteralContext ctx) {
        if (ctx.BOOLEAN_LITERAL() != null) {
            boolean value = Boolean.parseBoolean(ctx.BOOLEAN_LITERAL().toString());
            return new BooleanLiteral(value);
        } else if (ctx.SHORT_LITERAL() != null) {
            String toParse = ctx.SHORT_LITERAL().toString();
            // Need to remove the 's' from the end
            short value = Short.parseShort(toParse.substring(0, toParse.length()-1));
            return new ShortLiteral(value);
        } else if (ctx.INT_LITERAL() != null) {
            int value = Integer.parseInt(ctx.INT_LITERAL().toString());
            return new IntLiteral(value);
        } else if (ctx.LONG_LITERAL() != null) {
            String toParse = ctx.LONG_LITERAL().toString();
            long value = Long.parseLong(toParse.substring(0, toParse.length()-1));
            return new LongLiteral(value);
        } else if (ctx.DOUBLE_LITERAL() != null) {
            double value = Double.parseDouble(ctx.DOUBLE_LITERAL().toString());
            return new DoubleLiteral(value);
        } else if (ctx.FLOAT_LITERAL() != null) {
            String toParse = ctx.FLOAT_LITERAL().toString();
            float value = Float.parseFloat(toParse.substring(0, toParse.length()-1));
            return new FloatLiteral(value);
        } else {
            return null;
        }
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
