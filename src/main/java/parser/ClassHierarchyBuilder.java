package parser;


import errors.CircularInheritanceException;
import util.TopologicalSort;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Visits each class in order to infer the class inheritance hierarchy and
 * deduce a serial order in which to visit them.
 */
public class ClassHierarchyBuilder extends JavaFileBaseVisitor<Void> {

    /**
     * Records the set of all classes seen so far.
     */
    private Map<String, JavaFileParser.ClassDefinitionContext> seenClasses;

    /**
     * Records the class hierarchy observed so far.
     */
    private Map<String, String> classExtendsFromMap;

    public ClassHierarchyBuilder() {
        seenClasses = new HashMap<>();
        classExtendsFromMap = new HashMap<>();
    }

    @Override
    public Void visitFile(JavaFileParser.FileContext ctx) {
        visit(ctx.classDefinition());
        return null;
    }

    @Override
    public Void visitClassDefinition(JavaFileParser.ClassDefinitionContext ctx) {

        // Record the name of this class
        String className = ctx.className.getText();
        if (seenClasses.containsKey(className)) {
            String message = "Duplicate definition for class " + className;
            ParserUtil.reportError(message, ctx);
        }
        seenClasses.put(className, ctx);

        // Record its parent, if it has one
        if (ctx.parentClassName != null) {
            String parentClassName = ctx.parentClassName.getText();
            classExtendsFromMap.put(className, parentClassName);
        }

        return null;
    }

    /**
     * Returns a serial order in which to safely visit every class.
     *
     * @return A list of class parse trees in a safe order for visiting
     */
    public List<JavaFileParser.ClassDefinitionContext> getSerialClassOrdering() {
        Set<String> vertices = seenClasses.keySet();
        Map<String, Set<String>> edges = new HashMap<>();
        for (String className : vertices) {
            edges.put(className, new HashSet<>());
        }
        for (String className : vertices) {
            if (classExtendsFromMap.containsKey(className)) {
                String parentClassName = classExtendsFromMap.get(className);
                Set<String> dependants;
                dependants = edges.get(parentClassName);
                dependants.add(className);
            }
        }
        List<String> ordering = null;
        try {
            ordering = TopologicalSort.getSerialOrder(vertices, edges);
        } catch (CircularInheritanceException e) {
            ParserUtil.reportError(e.getMessage());
        }
        assert ordering != null;
        return ordering
                .stream()
                .map(seenClasses::get)
                .collect(Collectors.toList());
    }
}
