package parser;

import ast.types.AccessModifier;
import ast.types.JavaClass;
import ast.types.Type;
import ast.types.VoidType;
import errors.DuplicateClassAttributeException;
import errors.DuplicateClassDefinitionException;
import errors.UnknownClassException;
import util.*;

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
     * The name of the class currently being processed
     */
    private String currentClassName;

    /**
     * The attributes we have seen so far for the current class
     */
    private List<JavaClass.ClassAttribute> classAttributes;

    /**
     * Lists of methods and constructors seen in the current class
     */
    private List<MethodOrConstructorSignature> methodsAndConstructorsInCurrentClass;

    /**
     * A list of the method parse trees encountered.
     *
     * We also keep a list of the containing class of each method.
     */
//    private List<JavaFileParser.MethodDefinitionContext> methodParseTrees;
    private List<SubroutineToCompile> subroutines;
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
        methodsAndConstructorsInCurrentClass = new ArrayList<>();
        typeVisitor = new TypeVisitor();
        accessModifierVisitor = new AccessModifierVisitor();
        subroutines = new ArrayList<>();
        containingClasses = new ArrayList<>();
    }

    @Override
    public Void visitClassDefinition(JavaFileParser.ClassDefinitionContext ctx) {

        // Read class name
        currentClassName = ctx.className.getText();

        // Empty the lists of attributes and methods ready for the new class
        classAttributes.clear();
        methodsAndConstructorsInCurrentClass.clear();

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
            currentClass = new JavaClass(currentClassName, classAttributes, parent);
        } catch (DuplicateClassAttributeException e) {
            ErrorReporting.reportError(e.getMessage());
        }

        assert currentClass != null;
        for (MethodOrConstructorSignature signature : methodsAndConstructorsInCurrentClass) {
            if (signature.isMethod()) {
                MethodSignature methodSignature = signature.getMethodSignature();
                FunctionTableEntry entry = functionTable.registerFunction(
                        currentClass,
                        methodSignature.methodName,
                        methodSignature.parameterTypes,
                        methodSignature.returnType,
                        methodSignature.isStatic,
                        methodSignature.accessModifier
                );

                // If it's a non-static method then we want to register it with the
                // class that owns it.
                if (!methodSignature.isStatic)
                    currentClass.registerNewMethod(entry);

            } else {
                ConstructorSignature constructorSignature = signature.getConstructorSignature();
                boolean isStatic = false;
                FunctionTableEntry entry = functionTable.registerFunction(
                        currentClass,
                        "constructor",
                        constructorSignature.parameterTypes,
                        new VoidType(),
                        isStatic,
                        AccessModifier.PUBLIC
                );
                currentClass.registerNewConstructor(entry);
            }

            // Add an entry to the list of containing classes
            containingClasses.add(currentClass);
        }

        // Finally register this class with the class table
        try {
            classTable.registerClass(currentClassName, currentClass);
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
    public Void visitConstructor(JavaFileParser.ConstructorContext ctx) {
        visit(ctx.constructorDefinition());
        return null;
    }

    @Override
    public Void visitMethodDefinition(JavaFileParser.MethodDefinitionContext ctx) {
        MethodSignature signature = new MethodSignature();
        signature.methodName = ctx.IDENTIFIER().toString();
        signature.accessModifier = accessModifierVisitor.visit(ctx.accessModifier());
        signature.returnType = typeVisitor.visit(ctx.type());
        signature.parameterTypes = visitMethodParams(ctx.methodParams());
        signature.isStatic = ctx.STATIC() != null;
        MethodOrConstructorSignature wrapper = new MethodOrConstructorSignature(signature);
        methodsAndConstructorsInCurrentClass.add(wrapper);
        SubroutineToCompile subroutine = new SubroutineToCompile(ctx);
        subroutines.add(subroutine);
        return null;
    }

    @Override
    public Void visitConstructorDefinition(JavaFileParser.ConstructorDefinitionContext ctx) {
        if (!ctx.IDENTIFIER().toString().equals(currentClassName)) {
            String message = "Missing return type in method definition.";
            ErrorReporting.reportError(message, ctx, currentClassName+".java");
        }
        ConstructorSignature signature = new ConstructorSignature();
        signature.parameterTypes = visitMethodParams(ctx.methodParams());
        MethodOrConstructorSignature wrapper = new MethodOrConstructorSignature(signature);
        methodsAndConstructorsInCurrentClass.add(wrapper);
        SubroutineToCompile subroutine = new SubroutineToCompile(ctx);
        subroutines.add(subroutine);
        return null;
    }

    private List<Type> visitMethodParams(JavaFileParser.MethodParamsContext ctx) {
        return (ctx instanceof JavaFileParser.SomeParamsContext)
                ? ((JavaFileParser.SomeParamsContext) ctx)
                    .type()
                    .stream()
                    .map(typeVisitor::visit)
                    .collect(Collectors.toList())
                : new ArrayList<>();
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

    public List<SubroutineToCompile> getSubroutines() {
        return subroutines;
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

    private static class ConstructorSignature {
        public List<Type> parameterTypes;
    }

    private static class MethodOrConstructorSignature {

        private MethodSignature methodSignature;
        private ConstructorSignature constructorSignature;
        private boolean isConstructor;

        public MethodOrConstructorSignature(MethodSignature methodSignature) {
            this.methodSignature = methodSignature;
            this.constructorSignature = null;
            this.isConstructor = false;
        }

        public MethodOrConstructorSignature(ConstructorSignature constructorSignature) {
            this.constructorSignature = constructorSignature;
            this.methodSignature = null;
            this.isConstructor = true;
        }

        public boolean isMethod() {
            return !isConstructor;
        }

        public boolean isConstructor() {
            return isConstructor;
        }

        public MethodSignature getMethodSignature() {
            return methodSignature;
        }

        public ConstructorSignature getConstructorSignature() {
            return constructorSignature;
        }
    }
}
