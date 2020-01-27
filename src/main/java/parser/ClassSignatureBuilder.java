package parser;

import ast.types.AccessModifier;
import ast.types.JavaClass;
import ast.types.Type;
import errors.CircularInheritanceException;
import errors.DuplicateClassDefinitionException;
import errors.DuplicateFunctionSignatureException;
import util.ClassTable;
import util.FunctionTable;
import util.TopologicalSort;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Extracts structural information from a class definition.
 *
 * The idea is that you should create an instance of this class, then use it
 * to visit every file that we are compiling. After this is complete, this class
 * can generate a function table and class table, and can also produce a list
 * containing the parse tree for every method to be compiled. In effect, the
 * purpose of this visitor is to skim over the prototypes of every class and
 * every method, in order to produce a set of data structures required for
 * the actual compilation of each method.
 */
public class ClassSignatureBuilder extends JavaFileBaseVisitor<Void> {

    /**
     * A function table, added to every time we process another method.
     */
    private FunctionTable functionTable;

    /**
     * A map from class parse tree nodes to the name of the class.
     *
     * This is needed for updating the function table, since each entry in the
     * function table has a 'namespace' attribute.
     */
    private Map<JavaFileParser.MethodDefinitionContext, String> classNameMap;

    /**
     * Helper sub-visitors to process types and access modifiers.
     */
    private TypeVisitor typeVisitor;
    private AccessModifierVisitor accessModifierVisitor;

    /**
     * State used for tracking the set of classes we have seen, and their
     * inheritance dependencies.
     */
    private String currentClassName;
    private Set<String> seenClassNames;
    private Map<String, String> classExtendsFromMap;

    /**
     * Records the parse trees for every method we have encountered.
     *
     * We also store a map from method parse trees to class names, which is
     * required because function table entries have a 'namespace' attribute.
     */
    private List<JavaFileParser.MethodDefinitionContext> methodParseTrees;
    private Map<JavaFileParser.MethodDefinitionContext, String> methodParseTreeToClassNameMap;


    public ClassSignatureBuilder() {
        super();
        this.functionTable = new FunctionTable();
        this.currentClassName = null;

        this.typeVisitor = new TypeVisitor(null);
        this.accessModifierVisitor = new AccessModifierVisitor();

        this.seenClassNames = new HashSet<>();
        this.classExtendsFromMap = new HashMap<>();
        this.methodParseTreeToClassNameMap = new HashMap<>();
        this.methodParseTrees = new ArrayList<>();
    }

    @Override
    public Void visitFile(JavaFileParser.FileContext ctx) {
        visit(ctx.classDefinition());
        return null;
    }

    @Override
    public Void visitClassDefinition(JavaFileParser.ClassDefinitionContext ctx) {
        String className = ctx.className.getText();
        currentClassName = className;

        if (seenClassNames.contains(className)) {
            String message = "Duplicate definition for class " + className;
            ParserUtil.reportError(message, ctx);
        }
        seenClassNames.add(className);

        if (ctx.parentClassName != null) {
            String parentClassName = ctx.parentClassName.getText();
            classExtendsFromMap.put(className, parentClassName);
        }

        // Now visit each item (attribute or method) in the class
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
        String functionName = ctx.IDENTIFIER().toString();
        Type returnType = typeVisitor.visit(ctx.type());
        List<Type> types = (ctx.methodParams() instanceof JavaFileParser.SomeParamsContext)
                ? visitMethodParams((JavaFileParser.SomeParamsContext) ctx.methodParams())
                : new ArrayList<>();
        AccessModifier accessModifier = accessModifierVisitor.visitAccessModifier(ctx.accessModifier());
        try {
            functionTable.registerFunction(currentClassName, functionName, types, returnType, accessModifier);
            methodParseTrees.add(ctx);
            methodParseTreeToClassNameMap.put(ctx, currentClassName);
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

    @Override
    public Void visitClassAttribute(JavaFileParser.ClassAttributeContext ctx) {
        // TODO: Implement
        return super.visitClassAttribute(ctx);
    }

    @Override
    public Void visitClassAttributeDeclaration(JavaFileParser.ClassAttributeDeclarationContext ctx) {
        // TODO: Implement
        return super.visitClassAttributeDeclaration(ctx);
    }

    /**
     * Returns a function table built from the methods seen so far.
     *
     * @return A function table built from the methods seen so far
     */
    public FunctionTable getFunctionTable() {
        return functionTable;
    }

    /**
     * Returns a class table built from all the classes seen so far.
     *
     * @return A class table built from all the classes seen so far
     */
    public ClassTable getClassTable() {
        ClassTable classTable = new ClassTable();
        Map<String, Set<String>> edges = new HashMap<>();
        for (String className : seenClassNames) {
            Set<String> dependencies = new HashSet<>();
            if (classExtendsFromMap.containsKey(className))
                dependencies.add(classExtendsFromMap.get(className));
            edges.put(className, dependencies);
        }

        List<String> ordering = null;
        try {
            ordering = TopologicalSort.getSerialOrder(seenClassNames, edges);
        } catch (CircularInheritanceException e) {
            ParserUtil.reportError(e.getMessage());
        }

        Map<String, JavaClass> classObjects = new HashMap<>();
        assert ordering != null;
        for (String className : ordering) {
            JavaClass javaClass;
            if (classExtendsFromMap.containsKey(className)) {
                String parentName = classExtendsFromMap.get(className);
                JavaClass parent = classObjects.get(parentName);
                javaClass = new JavaClass(className, parent);
            } else {
                javaClass = new JavaClass(className);
            }
            classObjects.put(className, javaClass);
            try {
                classTable.registerClass(className, javaClass);
            } catch (DuplicateClassDefinitionException e) {
                // Will never happen thanks to topological sorting
                e.printStackTrace();
            }
        }

        return classTable;
    }

    /**
     * Returns a list of all found method parse trees.
     *
     * @return A list of all found method parse trees
     */
    public List<JavaFileParser.MethodDefinitionContext> getMethodParseTrees() {
        return methodParseTrees;
    }

    /**
     * Returns a map from method parse trees to enclosing class names
     *
     * @return A map from method parse trees to enclosing class names
     */
    public Map<JavaFileParser.MethodDefinitionContext, String> getClassNameMap() {
        return methodParseTreeToClassNameMap;
    }
}
