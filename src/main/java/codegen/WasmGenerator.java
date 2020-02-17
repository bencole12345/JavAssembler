package codegen;

import ast.structure.ClassMethod;
import ast.structure.MethodParameter;
import ast.structure.VariableScope;
import ast.types.AccessModifier;
import ast.types.Type;
import ast.types.VoidType;
import codegen.generators.ExpressionGenerator;
import codegen.generators.LiteralGenerator;
import codegen.generators.StatementGenerator;
import util.ClassTable;
import util.FunctionTable;
import util.FunctionTableEntry;
import util.VirtualTable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class WasmGenerator {

    public static void compile(List<ClassMethod> methods,
                               CodeEmitter emitter,
                               FunctionTable functionTable,
                               ClassTable classTable,
                               VirtualTable virtualTable) {

        // Notify generators of required state
        ExpressionGenerator.getInstance().setCodeEmitter(emitter);
        ExpressionGenerator.getInstance().setTables(functionTable, classTable, virtualTable);
        StatementGenerator.getInstance().setCodeEmitter(emitter);
        StatementGenerator.getInstance().setTables(functionTable, classTable, virtualTable);
        LiteralGenerator.getInstance().setCodeEmitter(emitter);

        // Emit start of module
        emitter.emitLine("(module");
        emitter.increaseIndentationLevel();

        // Emit the list of function types
        emitFunctionTypes(emitter, functionTable);

        // Emit virtual tables
        emitVirtualTables(emitter, functionTable, virtualTable);

        // Emit hand-coded WebAssembly functions
        WasmLibReader.getGlobalsCode().forEach(emitter::emitLine);
        WasmLibReader.getAllocationCode().forEach(emitter::emitLine);

        // Now compile each method
        for (ClassMethod method : methods) {
            compileMethod(method, functionTable, emitter);
        }

        // End the module
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
        emitter.close();
    }

    /**
     * Emits the types of all non-static methods.
     *
     * These are required so that indirect function calls can still be
     * type-checked.
     *
     * @param emitter The code emitter
     * @param functionTable The function table
     */
    private static void emitFunctionTypes(CodeEmitter emitter,
                                          FunctionTable functionTable) {

        for (FunctionTableEntry entry : functionTable.getFunctions()) {

            // No need to emit types for static methods since they will never
            // be called indirectly.
            if (entry.getIsStatic()) continue;

            // Build up a string for the type for this function.
            String typeString = "(type $func_"
                    + CodeGenUtil.getFunctionNameForOutput(entry, functionTable)
                    + " (func ";

            // Add parameter for the reference to the object
            typeString += "(param i32)";

            // Add each parameter to the function
            String parameters = entry.getParameterTypes()
                    .stream()
                    .map(CodeGenUtil::getWasmType)
                    .filter(Objects::nonNull)
                    .map(WasmType::toString)
                    .map(wasmType -> "(param " + wasmType + ")")
                    .collect(Collectors.joining(" "));
            if (parameters.length() > 0) {
                typeString += " " + parameters;
            }

            // Return type
            if (!entry.getReturnType().equals(new VoidType())) {
                typeString += " (result " + CodeGenUtil.getWasmType(entry.getReturnType()) + ")";
            }

            // End the line
            typeString += "))";
            emitter.emitLine(typeString);
        }
    }

    /**
     * Emits the concatenation of all virtual tables for all classes.
     *
     * @param emitter The code emitter
     * @param functionTable The function table
     * @param virtualTable The virtual table to emit
     */
    private static void emitVirtualTables(CodeEmitter emitter,
                                          FunctionTable functionTable,
                                          VirtualTable virtualTable) {

        int numEntries = virtualTable.getEntries().size();
        emitter.emitLine("(table " + numEntries + " anyfunc)");
        emitter.increaseIndentationLevel();
        emitter.emitLine("(elem (i32.const 0)");
        emitter.increaseIndentationLevel();
        for (String functionName : virtualTable.getEntriesSymbolic(functionTable)) {
            emitter.emitLine("$" + functionName);
        }
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
        emitter.decreaseIndentationLevel();
    }

    private static void compileMethod(ClassMethod method,
                                      FunctionTable functionTable,
                                      CodeEmitter emitter) {

        // Emit the function declaration
        String functionName = CodeGenUtil.getFunctionNameForOutput(method, functionTable);
        emitter.emitLine("(func $" + functionName);
        emitter.increaseIndentationLevel();

        // If it's not a static method then pass a reference to the class as the
        // first parameter.
        if (!method.isStatic()) {
            emitter.emitLine("(param $this i32)");
        }

        // Emit the rest of the parameters
        for (MethodParameter param : method.getParams()) {
            WasmType paramType = CodeGenUtil.getWasmType(param.getType());
            emitter.emitLine("(param " + paramType + ")");
        }

        // Emit return type, unless it's a void return
        Type returnType = method.getReturnType();
        if (!(returnType instanceof VoidType)) {
            emitter.emitLine("(result " + CodeGenUtil.getWasmType(returnType) + ")");
        }

        // Declare all local variables
        VariableScope bodyScope = method.getBody().getVariableScope();
        for (Type type : bodyScope.getAllKnownAllocatedTypes()) {
            WasmType wasmType = CodeGenUtil.getWasmType(type);
            emitter.emitLine("(local " + wasmType + ")");
        }

        // Now compile the body of the function
        StatementGenerator.getInstance().compileCodeBlock(method.getBody());

        // End the body
        emitter.emitLine(")");
        emitter.decreaseIndentationLevel();

        // Export the function if it's declared public
        if (method.getAccessModifier() == AccessModifier.PUBLIC) {
            emitter.emitLine("(export \"" + functionName + "\" (func $" + functionName + "))");
        }
    }

}
