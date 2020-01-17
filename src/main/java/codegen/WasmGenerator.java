package codegen;

import ast.functions.FunctionTable;
import ast.structure.ClassMethod;
import ast.structure.CompilationUnit;
import ast.structure.JavaClass;
import ast.structure.MethodParameter;
import ast.types.AccessModifier;
import ast.types.PrimitiveType;
import ast.types.Type;
import codegen.generators.StatementGenerator;

import java.util.List;


public class WasmGenerator {

    public static void compile(CompilationUnit compilationUnit, CodeEmitter emitter, FunctionTable functionTable) {
        emitter.emitLine("(module");
        emitter.increaseIndentationLevel();

        JavaClass classToCompile = compilationUnit.getJavaClass();

        // Pass over all the methods to add them to the function table, so that they can be referred
        // to via their index during the code generation phase.
        List<ClassMethod> methods = classToCompile.getMethods();

        // Compile each method separately
        for (ClassMethod method : methods) {
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
        line.append(method.getName());

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

        // TODO: Return type

        emitter.emitLine(line.toString());

        // Now compile the body of the function
        emitter.increaseIndentationLevel();
        StatementGenerator.compileCodeBlock(method.getBody(), emitter, functionTable);
        // TODO: Find way to get this on the same line as the last instruction from above
        emitter.emitLine(")");

        emitter.decreaseIndentationLevel();

        // Export the function if it's declared public
        if (method.getAccessModifier() == AccessModifier.PUBLIC) {
            emitter.emitLine("(export \"" + method.getName() + "\" (func $" + method.getName() + "))");
        }
    }

}
