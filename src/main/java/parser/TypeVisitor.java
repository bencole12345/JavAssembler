package parser;

import ast.types.*;
import errors.UnknownClassException;
import util.ClassTable;

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

    public TypeVisitor(ClassTable classTable) {
        this.classTable = classTable;
        mode = Mode.Validated;
    }

    public TypeVisitor() {
        this.classTable = null;
        mode = Mode.Unvalidated;
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
    public JavaClassReference visitNonPrimitiveType(JavaFileParser.NonPrimitiveTypeContext ctx) {
        String className = ctx.IDENTIFIER().getText();
        JavaClassReference reference = null;
        if (mode == Mode.Validated) {
            try {
                reference = classTable.lookupClass(className);
            } catch (UnknownClassException e) {
                ParserUtil.reportError(e.getMessage(), ctx);
            }
        } else if (mode == Mode.Unvalidated) {
            reference = new UnvalidatedJavaClassReference(className);
        }
        return reference;
    }
}