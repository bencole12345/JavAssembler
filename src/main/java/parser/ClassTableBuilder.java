package parser;

import ast.types.AccessModifier;
import ast.types.JavaClass;
import ast.types.Type;
import errors.JavAssemblerException;
import util.ClassTable;

import java.util.ArrayList;
import java.util.List;

public class ClassTableBuilder extends JavaFileBaseVisitor<Void> {

    /**
     * The table being built
     */
    private ClassTable classTable;

    /**
     * The attributes we have seen so far for the current class
     */
    private List<JavaClass.ClassAttribute> classAttributes;

    /**
     * Additional visitor helpers used internally
     */
    private TypeVisitor typeVisitor;
    private AccessModifierVisitor accessModifierVisitor;

    public ClassTableBuilder() {
        classTable = new ClassTable();
        classAttributes = new ArrayList<>();
        typeVisitor = new TypeVisitor();
        accessModifierVisitor = new AccessModifierVisitor();
    }

    @Override
    public Void visitClassDefinition(JavaFileParser.ClassDefinitionContext ctx) {

        // Read class name
        String className = ctx.className.getText();

        // Empty the list of attributes ready for the new class
        classAttributes.clear();

        // Visit everything in this class
        ctx.classItem().forEach(this::visit);

        try {

            // Look up the parent class, or leave it null if there is none
            JavaClass parent = null;
            if (ctx.parentClassName != null) {
                parent = classTable.lookupClass(ctx.parentClassName.getText());
            }

            // Make a new JavaClass object and register it with the class table
            JavaClass javaClass = new JavaClass(className, classAttributes, parent);
            classTable.registerClass(className, javaClass);

        } catch (JavAssemblerException e) {
            ParserUtil.reportError(e.getMessage());
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

    /**
     * Returns the class table that has been constructed.
     *
     * @return The class table that has been constructed
     */
    public ClassTable getConstructedClassTable() {
        return classTable;
    }
}
