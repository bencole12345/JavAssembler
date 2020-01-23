package parser;

import ast.functions.FunctionTable;
import ast.types.AccessModifier;
import ast.types.Type;
import errors.DuplicateFunctionSignatureException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassSignatureVisitor extends JavaFileBaseVisitor<Void> {

    private FunctionTable functionTable;
    private List<JavaFileParser.MethodDefinitionContext> methodsList;
    private Map<JavaFileParser.MethodDefinitionContext, String> classNameMap;

    private TypeVisitor typeVisitor;
    private AccessModifierVisitor accessModifierVisitor;

    public ClassSignatureVisitor(FunctionTable functionTable,
                                 List<JavaFileParser.MethodDefinitionContext> methods,
                                 Map<JavaFileParser.MethodDefinitionContext, String> classNameMap) {
        super();
        this.functionTable = functionTable;
        this.methodsList = methods;
        this.classNameMap = classNameMap;

        this.typeVisitor = new TypeVisitor();
        this.accessModifierVisitor = new AccessModifierVisitor();
    }

    @Override
    public Void visitFile(JavaFileParser.FileContext ctx) {

        String className = ctx.classDefinition().IDENTIFIER().toString();

        ctx.classDefinition().classItem()
                .stream()

                // Just look at class methods, no class attributes
                .filter(item -> item instanceof JavaFileParser.ClassMethodContext)

                // Extract inner method definition
                .map(method -> ((JavaFileParser.ClassMethodContext) method).methodDefinition())

                // Visit method definition
                .forEach(methodDefinition -> visitMethodDefinition(
                        methodDefinition,
                        className,
                        functionTable)
                );

        return null;
    }

    private void visitMethodDefinition(JavaFileParser.MethodDefinitionContext ctx,
                                       String className,
                                       FunctionTable table) {

        String functionName = ctx.IDENTIFIER().toString();
        Type returnType = typeVisitor.visit(ctx.type());
        List<Type> types = (ctx.methodParams() instanceof JavaFileParser.SomeParamsContext)
                ? visitMethodParams((JavaFileParser.SomeParamsContext) ctx.methodParams())
                : new ArrayList<>();
        AccessModifier accessModifier = accessModifierVisitor.visitAccessModifier(ctx.accessModifier());
        try {
            table.registerFunction(className, functionName, types, returnType, accessModifier);
            methodsList.add(ctx);
            classNameMap.put(ctx, className);
        } catch (DuplicateFunctionSignatureException e) {
            ParserUtil.reportError(e.getMessage(), ctx);
        }
    }

    private List<Type> visitMethodParams(JavaFileParser.SomeParamsContext ctx) {
        return ctx.type().stream()
                .map(typeVisitor::visit)
                .collect(Collectors.toList());
    }

}
