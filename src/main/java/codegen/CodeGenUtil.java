package codegen;

import ast.structure.ClassMethod;
import ast.structure.MethodParameter;
import ast.types.HeapObjectReference;
import ast.types.PrimitiveType;
import ast.types.Type;
import errors.InvalidClassNameException;
import errors.UndeclaredFunctionException;
import util.ErrorReporting;
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
        if (type instanceof HeapObjectReference)
            return WasmType.Int32;
        switch ((PrimitiveType) type) {
            case Float:
                return WasmType.Float32;
            case Double:
                return WasmType.Float64;
            case Long:
                return WasmType.Int64;
            default:
                return WasmType.Int32;
        }
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
     * @param emitter The CodeEmitter to use
     */
    public static void emitRangeRestrictionCode(Type type, CodeEmitter emitter) {
        if (type instanceof PrimitiveType) {
            switch ((PrimitiveType) type) {
                case Short:
                    emitter.emitLine("i32.const 16");
                    emitter.emitLine("i32.shl");
                    emitter.emitLine("i32.const 16");
                    emitter.emitLine("i32.shr_s");
                    break;
                case Byte:
                    emitter.emitLine("i32.const 24");
                    emitter.emitLine("i32.shl");
                    emitter.emitLine("i32.const 24");
                    emitter.emitLine("i32.shr_s");
                    break;
                case Char:
                    emitter.emitLine("i32.const 0x0000ffff");
                    emitter.emitLine("i32.and");
            }
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
                    method.getContainingClass(), method.getName(), parameterTypes);
        } catch (InvalidClassNameException | UndeclaredFunctionException e) {
            ErrorReporting.reportError(e.getMessage());
        }
        assert functionTableEntry != null;
        return getFunctionNameForOutput(functionTableEntry, functionTable);
    }

    /**
     * Determines the function name to be emitted.
     *
     * If this is the only method with this name, then just the method name will
     * be used. If there are multiple overloaded methods with this name, then
     * name mangling with the types will be applied.
     *
     * @param entry The function table entry for the method under compilation
     * @param functionTable The function table
     * @return The name to be emitted
     */
    public static String getFunctionNameForOutput(FunctionTableEntry entry,
                                                  FunctionTable functionTable) {
        String delimiter = "_";
        String className;
        if (entry.getContainingClass().getGenericClass() != null) {
            className = entry.getContainingClass().getGenericClass().toString();
        } else {
            className = entry.getContainingClass().toString();
        }
        String functionName = entry.getFunctionName();
        String namespacedName = className + delimiter + functionName;
        List<Type> parameterTypes = entry.getParameterTypes();
        if (functionTable.getNumberOfFunctionsWithName(functionName) <= 1) {
            return namespacedName;
        } else {
            List<String> typeNames = parameterTypes.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            return namespacedName + delimiter + String.join(delimiter, typeNames);
        }
    }
}
