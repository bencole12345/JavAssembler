package parser;

import ast.ASTNode;
import ast.expressions.*;
import ast.functions.FunctionTable;
import ast.functions.FunctionTableEntry;
import ast.literals.*;
import ast.operations.BinaryOp;
import ast.operations.IncrementOp;
import ast.statements.*;
import ast.structure.*;
import ast.types.*;
import errors.DuplicateFunctionSignatureException;
import errors.IncorrectTypeException;
import errors.UndeclaredFunctionException;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class ASTBuilder extends JavaFileBaseVisitor<ASTNode> {

    private Stack<VariableScope> variableScopeStack;
    private FunctionTable functionTable;
    private Type currentFunctionReturnType;

    @Override
    public CompilationUnit visitFile(JavaFileParser.FileContext ctx) {
        variableScopeStack = new Stack<>();
        Imports imports = (Imports) visit(ctx.imports());
        JavaClass javaClass = (JavaClass) visit(ctx.classDefinition());
        // TODO: Support package name
        return new CompilationUnit(imports, javaClass, functionTable);
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
        VariableScope classScope = pushNewVariableScope();


        // Split the class items into attributes and methods
        // (without visiting them any further yet)
        List<JavaFileParser.ClassMethodContext> methodASTNodes = new ArrayList<>();
        List<JavaFileParser.ClassAttributeContext> attributeASTNodes = new ArrayList<>();
        for (JavaFileParser.ClassItemContext classItem : ctx.classItem()) {
            if (classItem instanceof JavaFileParser.ClassMethodContext) {
                methodASTNodes.add((JavaFileParser.ClassMethodContext) classItem);
            } else if (classItem instanceof JavaFileParser.ClassAttributeContext) {
                attributeASTNodes.add((JavaFileParser.ClassAttributeContext) classItem);
            }
        }

        // Process the class attributes
        for (JavaFileParser.ClassAttributeContext attributeContext : attributeASTNodes) {
            ClassAttributeDeclaration declaration = (ClassAttributeDeclaration) visit(attributeContext);
            String name = declaration.getVariableName();
            Type type = declaration.getVariableType();
            classScope.registerVariable(name, type);
        }

        // Build a function table
        if (functionTable == null) {
            functionTable = new FunctionTable();
        }
        for (JavaFileParser.ClassMethodContext methodContext : methodASTNodes) {
            JavaFileParser.MethodDefinitionContext methodCtx = methodContext.methodDefinition();
            String name = methodCtx.IDENTIFIER().toString();
            Type returnType = (Type) visit(methodCtx.type());
            MethodParameterList params = (MethodParameterList) visit(methodCtx.methodParams());
            List<Type> parameterTypes = new ArrayList<>();
            for (MethodParameter parameter : params.getParameters())
                parameterTypes.add(parameter.getType());
            try {
                functionTable.registerFunction(name, parameterTypes, returnType);
            } catch (DuplicateFunctionSignatureException e) {
                reportError(e.getMessage(), methodCtx);
            }
        }

        // Now build the full AST for each method
        List<ClassMethod> methods = new ArrayList<>();
        for (JavaFileParser.ClassMethodContext methodContext : methodASTNodes) {
            ClassMethod method = (ClassMethod) visit(methodContext);
            methods.add(method);
        }

        // Pop the scope for this class from the stack
        popVariableScope();

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
        ReturnStatement returnStatement = null;
        try {
            returnStatement = new ReturnStatement(expression, currentFunctionReturnType);
        } catch (IncorrectTypeException e) {
            reportError(e.getMessage(), ctx);
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
        // This method covers +=, -=, *=, /=, and also a simple assignment, =
        // The main idea is that we can reduce any of the first 4 into a simple
        // assignment (=) by replacing the expression on the RHS with a binary
        // operation involving the variable name and the expression.
        //
        // Examples:
        //
        // x += 1;      --becomes-->  x = (x + 1);
        // y *= (y/z);  --becomes-->  y = (y * (y/z));

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

        VariableScope currentScope = variableScopeStack.peek();

        // Perform substitution if this is not a simple assignment
        if (bop != null) {
            VariableNameExpression innerVarNameExpr = new VariableNameExpression(name, currentScope);
            try {
                expression = new BinaryOperatorExpression(innerVarNameExpr, expression, bop);
            } catch (IncorrectTypeException e) {
                reportError(e.getMessage(), ctx);
            }
        }

        VariableNameExpression variableNameExpression = new VariableNameExpression(name, currentScope);
        Assignment assignment = null;
        try {
            assignment = new Assignment(variableNameExpression, expression);
        } catch (IncorrectTypeException e) {
            reportError(e.getMessage(), ctx);
        }

        return assignment;
    }

    @Override
    public DeclarationAndAssignment visitVariableDeclarationAndAssignment(
            JavaFileParser.VariableDeclarationAndAssignmentContext ctx) {
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
        NegateExpression negExpression = null;
        try {
            negExpression = new NegateExpression(expression);
        } catch (IncorrectTypeException e) {
            reportError(e.getMessage(), ctx);
        }
        return negExpression;
    }

    @Override
    public ASTNode visitIncrementExpr(JavaFileParser.IncrementExprContext ctx) {
        // TODO: Check this
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
        BinaryOperatorExpression bopExpression = null;
        try {
            bopExpression = new BinaryOperatorExpression(left, right, op);
        } catch (IncorrectTypeException e) {
            reportError(e.getMessage(), ctx);
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
        VariableNameExpression expression = new VariableNameExpression(variableName, currentScope);
        VariableIncrementExpression incrementExpression = null;
        try {
            incrementExpression = new VariableIncrementExpression(expression, op);
        } catch (IncorrectTypeException e) {
            reportError(e.getMessage(), ctx);
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
        VariableNameExpression expression = new VariableNameExpression(variableName, currentScope);
        VariableIncrementExpression incrementExpression = null;
        try {
            incrementExpression = new VariableIncrementExpression(expression, op);
        } catch (IncorrectTypeException e) {
            reportError(e.getMessage(), ctx);
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
            reportError(e.getMessage(), ctx);
        }
        return expression;
    }

    @Override
    public VariableNameExpression visitVariableNameExpr(JavaFileParser.VariableNameExprContext ctx) {
        String variableName = ctx.IDENTIFIER().toString();
        VariableScope currentScope = variableScopeStack.peek();
        return new VariableNameExpression(variableName, currentScope);
    }

    @Override
    public LiteralValue visitLiteralExpr(JavaFileParser.LiteralExprContext ctx) {
        return (LiteralValue) visit(ctx.literal());
    }

    @Override
    public FunctionCall visitFunctionCall(JavaFileParser.FunctionCallContext ctx) {
        String functionName = ctx.IDENTIFIER().toString();
        ExpressionList expressionList = (ExpressionList) visit(ctx.functionArgs());
        List<Expression> arguments = expressionList.getExpressionList();
        List<Type> argumentTypes = arguments.stream()
                .map(Expression::getType)
                .collect(Collectors.toList());
        FunctionTableEntry tableEntry = null;
        try {
            tableEntry = functionTable.lookupFunction(functionName, argumentTypes);
        } catch (UndeclaredFunctionException e) {
            reportError(e.getMessage(), ctx);
        }
        return new FunctionCall(tableEntry, arguments);
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
        Type returnType = (Type) visit(ctx.type());
        String methodName = ctx.IDENTIFIER().toString();
        MethodParameterList params = (MethodParameterList) visit(ctx.methodParams());
        List<MethodParameter> paramsList = params.getParameters();

        // Create a new variable scope object on the stack for
        // containing the list of method parameters
        VariableScope scopeForParameters = pushNewVariableScope();
        for (MethodParameter param : paramsList) {
            String name = param.getParameterName();
            Type type = param.getType();
            scopeForParameters.registerVariable(name, type);
        }

        // Now visit the body of the method
        currentFunctionReturnType = returnType;
        CodeBlock body = (CodeBlock) visit(ctx.codeBlock());

        // Pop the scope that was created to contain the parameters
        popVariableScope();
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
        VariableScope innerScope = pushNewVariableScope();

        // Recursively visit each statement in this code block
        List<Statement> statements = new ArrayList<>();
        for (JavaFileParser.StatementContext statementCtx : ctx.statement()) {
            ASTNode statementNode = visit(statementCtx);
            if (statementNode instanceof VariableDeclaration) {
                VariableDeclaration declaration = (VariableDeclaration) statementNode;
                String name = declaration.getVariableName();
                Type type = declaration.getVariableType();
                innerScope.registerVariable(name, type);
            } else if (statementNode instanceof DeclarationAndAssignment) {
                DeclarationAndAssignment combined = (DeclarationAndAssignment) statementNode;
                String name = combined.getVariableName();
                Type type = combined.getType();
                innerScope.registerVariable(name, type);
                VariableNameExpression nameExpression = new VariableNameExpression(name, innerScope);
                Assignment assignment = null;
                try {
                    assignment = new Assignment(nameExpression, combined.getExpression());
                } catch (IncorrectTypeException e) {
                    reportError(e.getMessage(), ctx);
                }
                statements.add(assignment);
            } else if (!(statementNode instanceof EmptyStatement)) {
                statements.add((Statement) statementNode);
            }
        }

        // Remove the inner scope from the stack since we are done with this block
        popVariableScope();

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
            reportError(e.getMessage(), ctx);
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

        boolean insertedExtraScope = false;
        if (initialiser instanceof DeclarationAndAssignment) {
            DeclarationAndAssignment decAndAssign = (DeclarationAndAssignment) initialiser;
            VariableScope newScope = pushNewVariableScope();
            newScope.registerVariable(decAndAssign.getVariableName(), decAndAssign.getType());
            insertedExtraScope = true;
        }

        // We need to handle the condition after the initialiser, since it may
        // refer to the variable defined in the initialiser, which is only
        // accessible from the newly created scope
        Expression condition = (ctx.forLoopCondition() != null)
                ? (Expression) visit(ctx.forLoopCondition()) : null;
        Expression updater = (ctx.forLoopUpdater() != null)
                ? (Expression) visit(ctx.forLoopUpdater()) : null;

        // Now we can safely visit the body of the loop
        CodeBlock codeBlock = (CodeBlock) visit(ctx.codeBlock());

        if (insertedExtraScope) {
            popVariableScope();
        }

        ForLoop forLoop = null;
        try {
            forLoop = new ForLoop(initialiser, condition, updater, codeBlock);
        } catch (IncorrectTypeException e) {
            reportError(e.getMessage(), ctx);
        }
        return forLoop;
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
    public LiteralValue visitLiteral(JavaFileParser.LiteralContext ctx) {
        if (ctx.BOOLEAN_LITERAL() != null) {
            boolean value;
            if (ctx.BOOLEAN_LITERAL().getSymbol().getType() == JavaFileParser.TRUE) {
                value = true;
            } else {
                value = false;
            }
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
     * @return The VariableScope object that was popped, or null
     *         if the stack is empty
     */
    private VariableScope popVariableScope() {
        return variableScopeStack.pop();
    }

    /**
     * Reports an error to the console and exits.
     *
     * @param errorMessage The error to report
     * @param ctx The ParserRuleContext at which the error occurred
     */
    private void reportError(String errorMessage, ParserRuleContext ctx) {
        int line = ctx.start.getLine();
        int col = ctx.start.getCharPositionInLine();
        String message = "Error on line " + line
                + ", column " + col
                + ": " + errorMessage;
        System.err.println(message);
        System.err.println("Exiting...");
        System.exit(0);
    }
}
