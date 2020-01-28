package codegen;

import ast.structure.ClassMethod;
import ast.structure.MethodParameter;
import ast.structure.VariableScope;
import ast.types.AccessModifier;
import ast.types.PrimitiveType;
import ast.types.Type;
import ast.types.VoidType;
import codegen.generators.ExpressionGenerator;
import codegen.generators.LiteralGenerator;
import codegen.generators.StatementGenerator;
import util.ClassTable;
import util.FunctionTable;

import java.util.List;


public class WasmGenerator {

    public static void compile(List<ClassMethod> methods,
                               CodeEmitter emitter,
                               FunctionTable functionTable,
                               ClassTable classTable) {

        // Notify generators of required state
        ExpressionGenerator.getInstance().setCodeEmitter(emitter);
        ExpressionGenerator.getInstance().setTables(functionTable, classTable);
        StatementGenerator.getInstance().setCodeEmitter(emitter);
        StatementGenerator.getInstance().setTables(functionTable, classTable);
        LiteralGenerator.getInstance().setCodeEmitter(emitter);

        // Emit start of module
        emitter.emitLine("(module");
        emitter.increaseIndentationLevel();

        // Emit hand-coded WebAssembly functions
        WasmLibReader.getGlobalsCode().forEach(emitter::emitLine);
        WasmLibReader.getAllocationCode().forEach(emitter::emitLine);

        // Now compile each method
        for (ClassMethod method : methods) {
            // TODO: Pass in classTable
            compileMethod(method, functionTable, emitter);
        }

        // End the module
        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
        emitter.close();
    }

    private static void compileMethod(ClassMethod method, FunctionTable functionTable, CodeEmitter emitter) {

        // Emit the function declaration
        StringBuilder line = new StringBuilder();
        line.append("(func $");
        String functionName = CodeGenUtil.getFunctionNameForOutput(method, functionTable);
        line.append(functionName);

        // List the parameters
        for (MethodParameter param : method.getParams()) {
            line.append(" (param $");
            line.append(param.getParameterName());
            line.append(" ");
            Type paramType = param.getType();
            if (paramType instanceof PrimitiveType) {
                String representationType = CodeGenUtil.getTypeForPrimitive((PrimitiveType) paramType);
                line.append(representationType);
                line.append(")");
            } else {
                // TODO: Implement non-primitive types
            }
        }

        // Emit return type, unless it's a void return
        Type returnType = method.getReturnType();
        if (!(returnType instanceof VoidType)) {
            line.append(" (result ");
            // TODO: This will break for non-primitive types
            line.append(CodeGenUtil.getTypeForPrimitive((PrimitiveType) returnType));
            line.append(")");
        }

        emitter.emitLine(line.toString());
        emitter.increaseIndentationLevel();

        VariableScope bodyScope = method.getBody().getVariableScope();
        for (Type type : bodyScope.getAllKnownAllocatedTypes()) {
            if (type instanceof PrimitiveType) {
                String typeString = CodeGenUtil.getTypeForPrimitive((PrimitiveType) type);
                emitter.emitLine("(local " + typeString + ")");
            } else {
                // TODO: Implement for non-primitive types
            }
        }

        // Now compile the body of the function
        StatementGenerator.getInstance().compileCodeBlock(method.getBody());
        // TODO: Find way to get this on the same line as the last instruction from above
        emitter.emitLine(")");

        emitter.decreaseIndentationLevel();

        // Export the function if it's declared public
        if (method.getAccessModifier() == AccessModifier.PUBLIC) {
            emitter.emitLine("(export \"" + functionName + "\" (func $" + functionName + "))");
        }
    }

}
