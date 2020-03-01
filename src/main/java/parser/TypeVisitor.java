package parser;

import ast.types.*;
import errors.UnknownClassException;
import util.ClassTable;
import util.ErrorReporting;

/**
 * Builds a Type object from an AST node.
 */
public class TypeVisitor extends JavaFileBaseVisitor<Type> {

    /**
     * Indicates which mode the visitor should be in.
     */
    private enum Mode {

        /**
         * Class references will be checked against the class table. Invalid
         * class names will cause an error to be displayed.
         */
        Validated,

        /**
         * Class references will not be checked against the class table. We may
         * end up accepting references to invalid classes.
         */
        Unvalidated
    }

    private ClassTable classTable;
    private Mode mode;
    private JavaClass currentClass;

    public TypeVisitor(ClassTable classTable) {
        this.classTable = classTable;
        mode = Mode.Validated;
        currentClass = null;
    }

    public TypeVisitor() {
        this.classTable = null;
        mode = Mode.Unvalidated;
        currentClass = null;
    }

    /**
     * Sets the class that we are currently parsing.
     *
     * This is only used to give more informative error messages.
     *
     * @param currentClass The class currently being processed
     */
    public void setCurrentClass(JavaClass currentClass) {
        this.currentClass = currentClass;
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
    public HeapObjectReference visitNonPrimitiveType(JavaFileParser.NonPrimitiveTypeContext ctx) {
        String className = ctx.IDENTIFIER().getText();
        HeapObjectReference reference = null;
        if (mode == Mode.Validated) {
            try {
                reference = classTable.lookupClass(className);
            } catch (UnknownClassException e) {
                ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
            }
        } else if (mode == Mode.Unvalidated) {
            reference = new UnvalidatedJavaClassReference(className);
        }
        return reference;
    }

    @Override
    public Type visitArrayType(JavaFileParser.ArrayTypeContext ctx) {
        Type elementType = visit(ctx.type());
        return new ObjectArray(elementType);
    }
}
