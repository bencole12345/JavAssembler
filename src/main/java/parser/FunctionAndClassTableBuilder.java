package parser;

import ast.types.AccessModifier;
import ast.types.JavaClass;
import ast.types.Type;
import errors.DuplicateClassAttributeException;
import errors.DuplicateClassDefinitionException;
import errors.UnknownClassException;
import util.ClassTable;
import util.ErrorReporting;
import util.FunctionTable;
import util.FunctionTableEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionAndClassTableBuilder extends JavaFileBaseVisitor<Void> {

    /**
     * The class table being built
     */
    private ClassTable classTable;

    /**
     * The function table being built
     */
    private FunctionTable functionTable;

    /**
     * The attributes we have seen so far for the current class
     */
    private List<JavaClass.ClassAttribute> classAttributes;

    /**
     * A list of methods seen in the current class
     */
    private List<MethodSignature> methodsInCurrentClass;

    /**
     * A list of the method parse trees encountered.
     *
     * We also keep a list of the containing class of each method.
     */
    private List<JavaFileParser.MethodDefinitionContext> methodParseTrees;
    private List<JavaClass> containingClasses;

    /**
     * Additional visitor helpers used internally
     */
    private TypeVisitor typeVisitor;
    private AccessModifierVisitor accessModifierVisitor;

    public FunctionAndClassTableBuilder() {
        classTable = new ClassTable();
        functionTable = new FunctionTable();
        classAttributes = new ArrayList<>();
        methodsInCurrentClass = new ArrayList<>();
        typeVisitor = new TypeVisitor();
        accessModifierVisitor = new AccessModifierVisitor();
        methodParseTrees = new ArrayList<>();
        containingClasses = new ArrayList<>();
    }

    @Override
    public Void visitClassDefinition(JavaFileParser.ClassDefinitionContext ctx) {

        // Read class name
        String className = ctx.className.getText();

        // Empty the lists of attributes and methods ready for the new class
        classAttributes.clear();
        methodsInCurrentClass.clear();

        // Visit every attribute in this class
        ctx.classItem().forEach(this::visit);

        // Look up the parent class, or leave it null if there is none
        JavaClass parent = null;
        if (ctx.parentClassName != null) {
            try {
                parent = classTable.lookupClass(ctx.parentClassName.getText());
            } catch (UnknownClassException e) {
                ErrorReporting.reportError(e.getMessage());
            }
        }

        JavaClass currentClass = null;
        try {
            currentClass = new JavaClass(className, classAttributes, parent);
        } catch (DuplicateClassAttributeException e) {
            ErrorReporting.reportError(e.getMessage());
        }

        // Now add all the methods
        for (MethodSignature signature : methodsInCurrentClass) {
            assert currentClass != null;
            FunctionTableEntry entry = functionTable.registerFunction(
                    currentClass,
                    signature.methodName,
                    signature.parameterTypes,
                    signature.returnType,
                    signature.isStatic,
                    signature.accessModifier
            );

            // If it's a non-static method then we want to register it with the
            // class that owns it.
            if (!signature.isStatic)
                currentClass.registerNewMethod(entry);

            // Add an entry to the list of containing classes
            containingClasses.add(currentClass);
        }

        // Finally register this class with the class table
        try {
            classTable.registerClass(className, currentClass);
        } catch (DuplicateClassDefinitionException e) {
            ErrorReporting.reportError(e.getMessage());
        }

        return null;
    }

    @Override
    public Void visitClassAttributeDeclaration(JavaFileParser.ClassAttributeDeclarationContext ctx) {
        AccessModifier accessModifier = accessModifierVisitor.visit(ctx.accessModifier());
        Type type = typeVisitor.visit(ctx.type());
        String name = ctx.IDENTIFIER().getText();
        JavaClass.ClassAttribute attribute = new JavaClass.ClassAttribute(name, type, accessModifier);
        classAttributes.add(attribute);
        return null;
    }

    @Override
    public Void visitClassMethod(JavaFileParser.ClassMethodContext ctx) {
        visit(ctx.methodDefinition());
        return null;
    }

    @Override
    public Void visitMethodDefinition(JavaFileParser.MethodDefinitionContext ctx) {
        MethodSignature signature = new MethodSignature();
        signature.methodName = ctx.IDENTIFIER().toString();;
        signature.accessModifier = accessModifierVisitor.visit(ctx.accessModifier());
        signature.returnType = typeVisitor.visit(ctx.type());
        signature.parameterTypes = (ctx.methodParams() instanceof JavaFileParser.SomeParamsContext)
                ? visitMethodParams((JavaFileParser.SomeParamsContext) ctx.methodParams())
                : new ArrayList<>();
        signature.isStatic = ctx.STATIC() != null;
        methodsInCurrentClass.add(signature);
        methodParseTrees.add(ctx);
        return null;
    }

    private List<Type> visitMethodParams(JavaFileParser.SomeParamsContext ctx) {
        return ctx.type().stream()
                .map(typeVisitor::visit)
                .collect(Collectors.toList());
    }

    /**
     * @return The class table that has been constructed
     */
    public ClassTable getClassTable() {
        return classTable;
    }

    /**
     * @return The function table that has been constructed
     */
    public FunctionTable getFunctionTable() {
        return functionTable;
    }

    public List<JavaFileParser.MethodDefinitionContext> getMethodParseTrees() {
        return methodParseTrees;
    }

    public List<JavaClass> getContainingClasses() {
        return containingClasses;
    }

    private static class MethodSignature {
        public String methodName;
        public List<Type> parameterTypes;
        public Type returnType;
        public boolean isStatic;
        public AccessModifier accessModifier;
    }
}
