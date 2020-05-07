package parser;

import ast.types.*;
import errors.UnknownClassException;
import org.antlr.v4.runtime.ParserRuleContext;
import util.ClassTable;
import util.ErrorReporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private Map<String, Integer> genericTypesIndexMap;

    public TypeVisitor(ClassTable classTable) {
        this.classTable = classTable;
        mode = Mode.Validated;
        currentClass = null;
        genericTypesIndexMap = null;
    }

    public TypeVisitor() {
        this.classTable = null;
        mode = Mode.Unvalidated;
        currentClass = null;
        genericTypesIndexMap = null;
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

    public void setGenericTypesIndexMap(Map<String, Integer> genericTypesMap) {
        this.genericTypesIndexMap = genericTypesMap;
    }

    public void unsetGenericTypesMap() {
        genericTypesIndexMap = null;
    }

    @Override
    public Type visitArrayType(JavaFileParser.ArrayTypeContext ctx) {
        Type elementType = visit(ctx.type());
        return new ItemArray(elementType);
    }

    @Override
    public Type visitVoidType(JavaFileParser.VoidTypeContext ctx) {
        return new VoidType();
    }

    @Override
    public Type visitPrimitiveType(JavaFileParser.PrimitiveTypeContext ctx) {
        switch (ctx.primitiveType.getType()) {
            case JavaFileParser.INT:
                return PrimitiveType.Int;
            case JavaFileParser.SHORT:
                return PrimitiveType.Short;
            case JavaFileParser.LONG:
                return PrimitiveType.Long;
            case JavaFileParser.BYTE:
                return PrimitiveType.Byte;
            case JavaFileParser.CHAR:
                return PrimitiveType.Char;
            case JavaFileParser.BOOLEAN:
                return PrimitiveType.Boolean;
            case JavaFileParser.FLOAT:
                return PrimitiveType.Float;
            case JavaFileParser.DOUBLE:
                return PrimitiveType.Double;
            default:
                return null;
        }
    }

    @Override
    public Type visitNonPrimitive(JavaFileParser.NonPrimitiveContext ctx) {
        return visit(ctx.objectType());
    }

    @Override
    public Type visitObjectType(JavaFileParser.ObjectTypeContext ctx) {
        String identifier = ctx.IDENTIFIER().getText();
        Type objectType = lookupIdentifier(identifier, ctx);

        // Process the type arguments, if they exist
        List<HeapObjectReference> typeArguments = new ArrayList<>();
        if (ctx.objectType() != null) {
            for (JavaFileParser.ObjectTypeContext typeArgument : ctx.objectType()) {
                JavaClass type = (JavaClass) visit(typeArgument);
                typeArguments.add(type);
            }
        }

        // If we're in validated mode then make sure the correct number of type
        // arguments are included. In non-validated mode this will be handled
        // when the types are validated.
        if (mode == Mode.Validated) {
            if (objectType instanceof GenericJavaClass) {
                GenericJavaClass genericJavaClass = (GenericJavaClass) objectType;
                if (genericJavaClass.getNumberOfTypeArguments() != typeArguments.size()) {
                    String message = "Incorrect number of type arguments: "
                            + objectType + " expects " + genericJavaClass.getNumberOfTypeArguments()
                            + " type arguments but got " + typeArguments.size();
                    ErrorReporting.reportError(message, ctx, currentClass.toString());
                }
                return GenericClassInstantiationCache
                        .getInstance()
                        .getActualClass(genericJavaClass, typeArguments);
            } else {
                if (!typeArguments.isEmpty()) {
                    String message = "The class " + objectType +
                            " does not use any generic type arguments";
                    ErrorReporting.reportError(message, ctx, currentClass.toString());
                }
                return objectType;
            }
        } else {
            // If there are type arguments then include them with the unvalidated
            // class reference for validation later when it is instantiated
            if (objectType instanceof UnvalidatedJavaClassReference) {
                UnvalidatedJavaClassReference unvalidatedReference = (UnvalidatedJavaClassReference) objectType;
                unvalidatedReference.setTypeArguments(typeArguments);
            }
            return objectType;
        }
    }

    private Type lookupIdentifier(String identifier, ParserRuleContext ctx) {
        Type type = null;
        if (genericTypesIndexMap != null && genericTypesIndexMap.containsKey(identifier)) {
            // It's a generic type
            int index = genericTypesIndexMap.get(identifier);
            type = new GenericType(identifier, index);
        } else if (mode == Mode.Validated) {
            // Look up the class name from the class table
            try {
                type = classTable.lookupClass(identifier);
            } catch (UnknownClassException e) {
                ErrorReporting.reportError(e.getMessage(), ctx, currentClass.toString());
            }
        } else {
            type = new UnvalidatedJavaClassReference(identifier);
        }
        return type;
    }

}
