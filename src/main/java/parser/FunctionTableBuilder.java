package parser;

import ast.types.AccessModifier;
import ast.types.JavaClass;
import ast.types.Type;
import errors.DuplicateFunctionSignatureException;
import errors.UnknownClassException;
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
     * The class table that was built previously.
     */
    private ClassTable classTable;
    /**
     * A list of the method parse trees encountered.
     */
    private List<JavaFileParser.MethodDefinitionContext> methodParseTrees;

    /**
     * Map of parse trees to the name of the containing class.
     */
    private Map<JavaFileParser.MethodDefinitionContext, JavaClass> methodParseTreeToContainingClassMap;

    /**
     * Tracks the class that we are currently processing.
     */
    private JavaClass currentClass;

    /**
     * Extra visitor helpers used internally.
     */
    private TypeVisitor typeVisitor;
    private AccessModifierVisitor accessModifierVisitor;

    public FunctionTableBuilder(ClassTable classTable) {
        this.classTable = classTable;
        functionTable = new FunctionTable();
        methodParseTrees = new ArrayList<>();
        methodParseTreeToContainingClassMap = new HashMap<>();
        typeVisitor = new TypeVisitor(classTable);
        accessModifierVisitor = new AccessModifierVisitor();
        currentClass = null;
    }

    @Override
    public Void visitFile(JavaFileParser.FileContext ctx) {
        visit(ctx.classDefinition());
        return null;
    }

    @Override
    public Void visitClassDefinition(JavaFileParser.ClassDefinitionContext ctx) {

        // Set the current class
        try {
            currentClass = classTable.lookupClass(ctx.className.getText());
        } catch (UnknownClassException e) {
            e.printStackTrace();
        }

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
        boolean isStatic = ctx.STATIC() != null;
        List<Type> parameterTypes = (ctx.methodParams() instanceof JavaFileParser.SomeParamsContext)
                ? visitMethodParams((JavaFileParser.SomeParamsContext) ctx.methodParams())
                : new ArrayList<>();
        AccessModifier accessModifier = accessModifierVisitor.visit(ctx.accessModifier());
        try {
            functionTable.registerFunction(
                    currentClass,
                    methodName,
                    parameterTypes,
                    returnType,
                    isStatic,
                    accessModifier);
            methodParseTrees.add(ctx);
            methodParseTreeToContainingClassMap.put(ctx, currentClass);
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

    public Map<JavaFileParser.MethodDefinitionContext, JavaClass> getMethodParseTreeToContainingClassMap() {
        return methodParseTreeToContainingClassMap;
    }
}
