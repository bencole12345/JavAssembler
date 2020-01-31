package codegen;

import ast.literals.IntLiteral;
import ast.structure.ClassMethod;
import ast.structure.MethodParameter;
import ast.types.PrimitiveType;
import ast.types.Type;
import codegen.generators.LiteralGenerator;
import errors.InvalidClassNameException;
import errors.UndeclaredFunctionException;
import util.FunctionTable;
import util.FunctionTableEntry;

import java.util.List;
import java.util.stream.Collectors;

public class CodeGenUtil {

    /**
     * Returns the wasm type mapping for a given Java primitive type.
     *
     * @param type The type to use
     * @return The wasm type it is represented by
     */
    public static WasmType getWasmType(Type type) {
        if (!(type instanceof PrimitiveType))
            return WasmType.Int32;
        switch ((PrimitiveType) type) {
            case Int:
            case Short:
            case Char:
            case Byte:
            case Boolean:
                return WasmType.Int32;
            case Long:
                return WasmType.Int64;
            case Float:
                return WasmType.Float32;
            case Double:
                return WasmType.Float64;
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
        if (type.getSize() != 4 && type.getSize() != 8) {
            int mask = (1 << (type.getSize() * 8)) - 1;
            IntLiteral literal = new IntLiteral(mask);
            LiteralGenerator.getInstance().compileLiteralValue(literal);
            codeEmitter.emitLine("i32.and");
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
    public static String getFunctionNameForOutput(ClassMethod method,
                                                  FunctionTable functionTable) {
        List<Type> parameterTypes = method.getParams().stream()
                .map(MethodParameter::getType)
                .collect(Collectors.toList());
        FunctionTableEntry functionTableEntry = null;
        try {
            functionTableEntry = functionTable.lookupFunction(
                    method.getContainingClassName(), method.getName(), parameterTypes);
        } catch (InvalidClassNameException | UndeclaredFunctionException e) {
            e.printStackTrace();
        }
        return getFunctionNameForOutput(functionTableEntry, parameterTypes, functionTable);
    }

    /**
     * Determines the function name to be emitted.
     *
     * If this is the only method with this name, then just the method name will
     * be used. If there are multiple overloaded methods with this name, then
     * name mangling with the types will be applied.
     *
     * @param entry The function table entry for the method under compilation
     * @param parameterTypes The type of each parameter
     * @param functionTable The function table
     * @return The name to be emitted
     */
    public static String getFunctionNameForOutput(FunctionTableEntry entry,
                                                  List<Type> parameterTypes,
                                                  FunctionTable functionTable) {
        String className = entry.getNamespace();
        String functionName = entry.getFunctionName();
        String namespacedName = className + "__" + functionName;
        if (functionTable.getNumberOfFunctionsWithName(functionName) == 1) {
            return namespacedName;
        } else {
            List<String> typeNames = parameterTypes.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            return namespacedName + "__" + String.join("_", typeNames);
        }
    }
}
