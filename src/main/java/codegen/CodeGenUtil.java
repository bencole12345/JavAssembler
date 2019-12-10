package codegen;

import ast.literals.IntLiteral;
import ast.structure.ClassMethod;
import ast.types.PrimitiveType;

import java.util.List;

import static codegen.generators.LiteralGenerator.compileLiteralValue;

public class CodeGenUtil {

    /**
     * Builds a table of all functions/methods in the compilation unit.
     *
     * This allows functions/methods to be referenced via their index in the
     * function table during the code generation phase.
     *
     * @param methods The list of methods to include in the table
     * @return The FunctionTable object that was constructed
     */
    static FunctionTable buildFunctionTable(List<ClassMethod> methods) {
        FunctionTable functionTable = new FunctionTable();
        for (ClassMethod method : methods) {
            try {
                functionTable.registerFunction(method.getName());
            } catch (FunctionTable.DuplicateFunctionSignatureException e) {
                // TODO: Actually output this as an error message and reject
                // the input file.
                e.printStackTrace();
            }
        }
        return functionTable;
    }

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
}
