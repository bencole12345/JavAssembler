package codegen;

import ast.structure.ClassMethod;
import ast.structure.MethodParameter;
import ast.structure.VariableScope;
import ast.types.AccessModifier;
import ast.types.PrimitiveType;
import ast.types.Type;
import ast.types.VoidType;
import codegen.generators.StatementGenerator;
import util.FunctionTable;

import java.util.List;


public class WasmGenerator {

    public static void compileMethods(List<ClassMethod> methods, CodeEmitter emitter, FunctionTable functionTable) {
        emitter.emitLine("(module");
        emitter.increaseIndentationLevel();

        // Compile each method separately
        for (ClassMethod method : methods) {
            compileMethod(method, functionTable, emitter);
        }

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
        StatementGenerator.compileCodeBlock(method.getBody(), emitter, functionTable);
        // TODO: Find way to get this on the same line as the last instruction from above
        emitter.emitLine(")");

        emitter.decreaseIndentationLevel();

        // Export the function if it's declared public
        if (method.getAccessModifier() == AccessModifier.PUBLIC) {
            emitter.emitLine("(export \"" + functionName + "\" (func $" + functionName + "))");
        }
    }

}
