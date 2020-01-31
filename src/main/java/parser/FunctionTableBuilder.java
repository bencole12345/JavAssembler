package parser;

import ast.types.AccessModifier;
import ast.types.Type;
import errors.DuplicateFunctionSignatureException;
import util.ClassTable;
import util.FunctionTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Walks through all classes to build a table of all functions.
 */
public class FunctionTableBuilder extends JavaFileBaseVisitor<Void> {

    /**
     * The function table that is being built.
     */
    private FunctionTable functionTable;

    /**
     * A list of the method parse trees encountered.
     */
    private List<JavaFileParser.MethodDefinitionContext> methodParseTrees;

    /**
     * Map of parse trees to the name of the containing class.
     */
    private Map<JavaFileParser.MethodDefinitionContext, String> classNameMap;

    /**
     * Tracks the name of the current class.
     */
    private String currentClassName;

    /**
     * Extra visitor helpers used internally.
     */
    private TypeVisitor typeVisitor;
    private AccessModifierVisitor accessModifierVisitor;

    public FunctionTableBuilder(ClassTable classTable) {
        functionTable = new FunctionTable();
        methodParseTrees = new ArrayList<>();
        classNameMap = new HashMap<>();
        typeVisitor = new TypeVisitor(classTable);
        accessModifierVisitor = new AccessModifierVisitor();
        currentClassName = null;
    }

    @Override
    public Void visitFile(JavaFileParser.FileContext ctx) {
        visit(ctx.classDefinition());
        return null;
    }

    @Override
    public Void visitClassDefinition(JavaFileParser.ClassDefinitionContext ctx) {

        // Set the current class name
        currentClassName = ctx.className.getText();

        // Visit every item in this class
        ctx.classItem().forEach(this::visit);

        return null;
    }

    @Override
    public Void visitClassMethod(JavaFileParser.ClassMethodContext ctx) {
        visit(ctx.methodDefinition());
        return null;
    }

    @Override
    public Void visitMethodDefinition(JavaFileParser.MethodDefinitionContext ctx) {
        String methodName = ctx.IDENTIFIER().toString();
        Type returnType = typeVisitor.visit(ctx.type());
        List<Type> parameterTypes = (ctx.methodParams() instanceof JavaFileParser.SomeParamsContext)
                ? visitMethodParams((JavaFileParser.SomeParamsContext) ctx.methodParams())
                : new ArrayList<>();
        AccessModifier accessModifier = accessModifierVisitor.visit(ctx.accessModifier());
        try {
            functionTable.registerFunction(currentClassName, methodName,
                    parameterTypes, returnType, accessModifier);
            methodParseTrees.add(ctx);
            classNameMap.put(ctx, currentClassName);
        } catch (DuplicateFunctionSignatureException e) {
            ParserUtil.reportError(e.getMessage(), ctx);
        }
        return null;
    }

    private List<Type> visitMethodParams(JavaFileParser.SomeParamsContext ctx) {
        return ctx.type().stream()
                .map(typeVisitor::visit)
                .collect(Collectors.toList());
    }

    public FunctionTable getConstructedFunctionTable() {
        return functionTable;
    }

    public List<JavaFileParser.MethodDefinitionContext> getMethodParseTrees() {
        return methodParseTrees;
    }

    public Map<JavaFileParser.MethodDefinitionContext, String> getClassNameMap() {
        return classNameMap;
    }
}
