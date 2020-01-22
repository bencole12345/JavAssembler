package parser;

import ast.types.NonPrimitiveType;
import ast.types.PrimitiveType;
import ast.types.Type;
import ast.types.VoidType;

/**
 * Builds a Type object from an AST node.
 */
public class TypeVisitor extends JavaFileBaseVisitor<Type> {

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
    public NonPrimitiveType visitNonPrimitiveType(JavaFileParser.NonPrimitiveTypeContext ctx) {
        String className = ctx.IDENTIFIER().toString();
        return new NonPrimitiveType(className);
    }
}
