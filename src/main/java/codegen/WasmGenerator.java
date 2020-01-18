package codegen;

import ast.functions.FunctionTable;
import ast.structure.ClassMethod;
import ast.structure.CompilationUnit;
import ast.structure.JavaClass;
import ast.structure.MethodParameter;
import ast.types.AccessModifier;
import ast.types.PrimitiveType;
import ast.types.Type;
import ast.types.VoidType;
import codegen.generators.StatementGenerator;


public class WasmGenerator {

    public static void compile(CompilationUnit compilationUnit, CodeEmitter emitter, FunctionTable functionTable) {
        emitter.emitLine("(module");
        emitter.increaseIndentationLevel();

        JavaClass classToCompile = compilationUnit.getJavaClass();

        // Compile each method separately
        for (ClassMethod method : classToCompile.getMethods()) {
            compileStaticMethod(method, functionTable, emitter);
        }

        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
        emitter.close();
    }

    private static void compileStaticMethod(ClassMethod method, FunctionTable functionTable, CodeEmitter emitter) {

        // Emit the function declaration
        StringBuilder line = new StringBuilder();
        line.append("(func $");
        String functionName = CodeGenUtil.getFunctionNameForOutput(method, functionTable);
        line.append(functionName);

        // Emit return type, unless it's a void return
        Type returnType = method.getReturnType();
        if (!(returnType instanceof VoidType)) {
            line.append(" (return ");
            // TODO: This will break for non-primitive types
            line.append(CodeGenUtil.getTypeForPrimitive((PrimitiveType) returnType));
            line.append(")");
        }

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

        emitter.emitLine(line.toString());

        // Now compile the body of the function
        emitter.increaseIndentationLevel();
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
