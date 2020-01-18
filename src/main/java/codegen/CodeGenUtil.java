package codegen;

import ast.functions.FunctionTable;
import ast.literals.IntLiteral;
import ast.structure.ClassMethod;
import ast.structure.MethodParameter;
import ast.types.PrimitiveType;

import java.util.List;
import java.util.stream.Collectors;

import static codegen.generators.LiteralGenerator.compileLiteralValue;

public class CodeGenUtil {

    /**
     * Returns the wasm type mapping for a given Java primitive type.
     *
     * @param type The primitive type to use
     * @return The wasm type it is represented by
     */
    public static String getTypeForPrimitive(PrimitiveType type) {
        switch (type) {
            case Int:
            case Short:
            case Char:
            case Byte:
            case Boolean:
                return "i32";
            case Long:
                return "i64";
            case Float:
                return "f32";
            case Double:
                return "f64";
        }
        return null;
    }

    /**
     * Emits code to ensure that the range of a type is preserved after an
     * arithmetic operation.
     *
     * For example, if two shorts are added together, we
     * should AND the result with a mask to ensure that the result remains
     * within the range of values that a Java short can take.
     *
     * @param type The type of the value to range-restrict
     * @param codeEmitter The CodeEmitter to use
     */
    public static void emitRangeRestrictionCode(PrimitiveType type, CodeEmitter codeEmitter) {
        if (type.getSize() != 32 && type.getSize() != 64) {
            int mask = (1 << type.getSize()) - 1;
            IntLiteral literal = new IntLiteral(mask);
            compileLiteralValue(literal, codeEmitter);
            codeEmitter.emitLine("and");
        }
    }

    /**
     * Determines the function name to be emitted.
     *
     * If this is the only method with this name, then just the method name will
     * be used. If there are multiple overloaded methods with this name, then
     * name mangling with the types will be applied.
     *
     * @param method The method under compilation
     * @param functionTable The function table
     * @return The name to be emitted
     */
    public static String getFunctionNameForOutput(ClassMethod method, FunctionTable functionTable) {
        String name = method.getName();
        if (functionTable.getNumberOfFunctionsWithName(name) == 1) {
            return name;
        } else {
            List<String> typeNames = method.getParams()
                    .stream()
                    .map(MethodParameter::getType)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            return name + "__" + String.join("_", typeNames);
        }
    }
}
