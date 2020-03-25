package parser;

import ast.expressions.*;
import ast.literals.*;
import ast.operations.BinaryOp;
import ast.operations.IncrementOp;
import ast.structure.VariableScope;
import ast.types.JavaClass;
import ast.types.ObjectArray;
import ast.types.Type;
import errors.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import util.ClassTable;
import util.ErrorReporting;
import util.FunctionTable;
import util.FunctionTableEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds expressions from parse trees.
 */
public class ExpressionVisitor extends JavaFileBaseVisitor<Expression> {

    /**
     * Tables used to resolve function and method calls
     */
    private FunctionTable functionTable;
    private ClassTable classTable;

    /**
     * Tracks the class currently being visited
     */
    private JavaClass currentClass;

    /**
     * Tracks the current scope
     */
    private VariableScope currentScope;

    /**
     * Helper visitors
     */
    private BopVisitor bopVisitor;

    public ExpressionVisitor(FunctionTable functionTable, ClassTable classTable) {
        super();
        this.functionTable = functionTable;
        this.classTable = classTable;
        currentScope = null;
        bopVisitor = new BopVisitor();
    }

    /**
     * Visits a given parse tree, using the provided variable scope.
     *
     * @param ctx The parse tree to visit
     * @param scope The scope in which to process it
     * @return The Expression that was built
     */
    public Expression visit(RuleContext ctx, VariableScope scope) {
        currentScope = scope;
        return visit(ctx);
    }

    /**
     * Sets the current class.
     *
     * @param currentClass The class currently being processed
     */
    public void setCurrentClass(JavaClass currentClass) {
        this.currentClass = currentClass;
    }

    @Override
    public Expression visitParenthesesExpr(JavaFileParser.ParenthesesExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitNegateExpr(JavaFileParser.NegateExprContext ctx) {
        Expression expression = visit(ctx.expr());
        NegateExpression negExpression = null;
        try {
            negExpression = new NegateExpression(expression);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return negExpression;
    }

    @Override
    public Expression visitNotExpr(JavaFileParser.NotExprContext ctx) {
        Expression expression = visit(ctx.expr());
        NotExpression notExpression = null;
        try {
            notExpression = new NotExpression(expression);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return notExpression;
    }

    @Override
    public Expression visitNewObjectExpr(JavaFileParser.NewObjectExprContext ctx) {
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
    public Expression visitNewArrayExpr(JavaFileParser.NewArrayExprContext ctx) {

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
            Expression lengthExpression = visit(ctx.expr(i));
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
    public Expression visitIncrementExpr(JavaFileParser.IncrementExprContext ctx) {
        return super.visitIncrementExpr(ctx);
    }

    @Override
    public Expression visitMultiplicativeBopExpr(JavaFileParser.MultiplicativeBopExprContext ctx) {
        BinaryOp op = bopVisitor.visit(ctx.multiplicativeBop());
        return makeBopExpression(ctx.expr(), op, ctx);
    }

    @Override
    public Expression visitAdditiveBopExpr(JavaFileParser.AdditiveBopExprContext ctx) {
        BinaryOp op = bopVisitor.visit(ctx.additiveBop());
        return makeBopExpression(ctx.expr(), op, ctx);
    }

    @Override
    public Expression visitComparisonBopExpr(JavaFileParser.ComparisonBopExprContext ctx) {
        BinaryOp op = bopVisitor.visit(ctx.comparisonBop());
        return makeBopExpression(ctx.expr(), op, ctx);
    }

    @Override
    public Expression visitLogicalAndBopExpr(JavaFileParser.LogicalAndBopExprContext ctx) {
        BinaryOp op = bopVisitor.visit(ctx.logicalAndBop());
        return makeBopExpression(ctx.expr(), op, ctx);
    }

    @Override
    public Expression visitLogicalOrBopExpr(JavaFileParser.LogicalOrBopExprContext ctx) {
        BinaryOp op = bopVisitor.visit(ctx.logicalOrBop());
        return makeBopExpression(ctx.expr(), op, ctx);
    }

    private Expression makeBopExpression(List<JavaFileParser.ExprContext> expressions,
                                         BinaryOp bop,
                                         ParserRuleContext ctx) {
        Expression left = visit(expressions.get(0));
        Expression right = visit(expressions.get(1));
        BinaryOperatorExpression bopExpression = null;
        try {
            bopExpression = new BinaryOperatorExpression(left, right, bop);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return bopExpression;
    }

    @Override
    public Expression visitPreIncrementExpr(JavaFileParser.PreIncrementExprContext ctx) {
        String variableName = ctx.IDENTIFIER().toString();
        IncrementOp op = null;
        switch (ctx.op.getType()) {
            case JavaFileParser.INCREMENT:
                op = IncrementOp.PRE_INCREMENT;
                break;
            case JavaFileParser.DECREMENT:
                op = IncrementOp.PRE_DECREMENT;
        }
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
    public Expression visitPostIncrementExpr(JavaFileParser.PostIncrementExprContext ctx) {
        String variableName = ctx.IDENTIFIER().toString();
        IncrementOp op = null;
        switch (ctx.op.getType()) {
            case JavaFileParser.INCREMENT:
                op = IncrementOp.POST_INCREMENT;
                break;
            case JavaFileParser.DECREMENT:
                op = IncrementOp.POST_DECREMENT;
        }
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
    public Expression visitBinarySelectorExpr(JavaFileParser.BinarySelectorExprContext ctx) {
        Expression condition = visit(ctx.expr(0));
        Expression trueExpression = visit(ctx.expr(1));
        Expression falseExpression = visit(ctx.expr(2));
        BinarySelectorExpression expression = null;
        try {
            expression = new BinarySelectorExpression(condition, trueExpression, falseExpression);
        } catch (IncorrectTypeException e) {
            ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
        }
        return expression;
    }

    @Override
    public Expression visitArrayLookupExpr(JavaFileParser.ArrayLookupExprContext ctx) {

        // Start with the array itself
        Expression lookupExpression = visit(ctx.expr(0));

        // Recurse over the indices
        // For example, for 'a[1][2]' this loop will run twice
        // We also get a guarantee from the grammar that it will run at least
        // once.
        for (int i = 1; i < ctx.expr().size(); i++) {
            Expression indexExpression = visit(ctx.expr(i));
            try {
                lookupExpression = new ArrayIndexExpression(lookupExpression, indexExpression);
            } catch (IncorrectTypeException e) {
                ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
            }
        }

        return lookupExpression;
    }

    @Override
    public Expression visitVariableNameExpr(JavaFileParser.VariableNameExprContext ctx) {
        return visit(ctx.variableName());
    }

    @Override
    public Expression visitAttributeLookupExpr(JavaFileParser.AttributeLookupExprContext ctx) {
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
    public Expression visitVariableReference(JavaFileParser.VariableReferenceContext ctx) {
        String variableName = ctx.IDENTIFIER().toString();
        return new LocalVariableExpression(variableName, currentScope);
    }

    @Override
    public Expression visitThisReference(JavaFileParser.ThisReferenceContext ctx) {
        return new LocalVariableExpression("this", currentScope);
    }

    @Override
    public Expression visitLiteralExpr(JavaFileParser.LiteralExprContext ctx) {
        return visit(ctx.literal());
    }

    @Override
    public Expression visitQualifiedFunctionCall(JavaFileParser.QualifiedFunctionCallContext ctx) {
        String qualifier = ctx.IDENTIFIER(0).toString();
        String functionName = ctx.IDENTIFIER(1).toString();
        ExpressionList expressionList = (ExpressionList) visit(ctx.functionArgs());
        List<Expression> arguments = expressionList.getExpressionList();
        if (currentScope.hasMappingFor(qualifier)) {
            return buildMethodCall(qualifier, functionName, arguments, ctx);
        } else {
            return visitFunctionCall(qualifier, functionName, arguments, ctx);
        }
    }

    private Expression buildMethodCall(String localVariableName,
                                       String methodName,
                                       List<Expression> argumentsList,
                                       JavaFileParser.QualifiedFunctionCallContext ctx) {
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
    public Expression visitUnqualifiedFunctionCall(JavaFileParser.UnqualifiedFunctionCallContext ctx) {
        String functionName = ctx.IDENTIFIER().toString();
        ExpressionList expressionList = (ExpressionList) visit(ctx.functionArgs());
        List<Expression> arguments = expressionList.getExpressionList();
        return visitFunctionCall(currentClass.toString(), functionName, arguments, ctx);
    }

    private Expression visitFunctionCall(String className,
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
    public Expression visitFunctionCallExpr(JavaFileParser.FunctionCallExprContext ctx) {
        return visit(ctx.functionCall());
    }

    @Override
    public Expression visitNoArgs(JavaFileParser.NoArgsContext ctx) {
        List<Expression> emptyExpressionList = new ArrayList<>();
        return new ExpressionList(emptyExpressionList);
    }

    @Override
    public Expression visitSomeArgs(JavaFileParser.SomeArgsContext ctx) {
        List<Expression> expressions = new ArrayList<>();
        for (JavaFileParser.ExprContext expressionCtx : ctx.expr()) {
            Expression expression = visit(expressionCtx);
            expressions.add(expression);
        }
        return new ExpressionList(expressions);
    }

    @Override
    public LiteralValue visitLiteral(JavaFileParser.LiteralContext ctx) {
        // TODO: Implement the rest of these
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
}
