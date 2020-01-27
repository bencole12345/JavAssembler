package parser;

import ast.types.JavaClass;
import ast.types.PrimitiveType;
import ast.types.Type;
import ast.types.VoidType;
import errors.UnknownClassException;
import util.ClassTable;

/**
 * Builds a Type object from an AST node.
 */
public class TypeVisitor extends JavaFileBaseVisitor<Type> {

    private ClassTable classTable;

    public TypeVisitor(ClassTable classTable) {
        this.classTable = classTable;
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
    public JavaClass visitNonPrimitiveType(JavaFileParser.NonPrimitiveTypeContext ctx) {
        String className = ctx.IDENTIFIER().getText();
        JavaClass javaClass = null;
        try {
            javaClass = classTable.lookupClass(className);
        } catch (UnknownClassException e) {
            ParserUtil.reportError(e.getMessage(), ctx);
        }
        return javaClass;
    }
}
