package codegen;

import ast.statements.*;
import ast.structure.*;
import ast.types.AccessModifier;
import ast.types.PrimitiveType;
import ast.types.Type;
import codegen.generators.StatementGenerator;

import java.util.List;


public class WasmGenerator {

    // Note: I think .wat is the human-readable form and .wasm is the assembled version

    public static void compile(CompilationUnit compilationUnit, CodeEmitter emitter) {
        emitter.emitLine("(module");
        emitter.increaseIndentationLevel();

        JavaClass classToCompile = compilationUnit.getJavaClass();

        // Pass over all the methods to add them to the function table, so that they can be referred
        // to via their index during the code generation phase.
        List<ClassMethod> methods = classToCompile.getMethods();
        FunctionTable functionTable = CodeGenUtil.buildFunctionTable(methods);

        // Compile each method separately
        for (ClassMethod method : methods) {
            compileStaticMethod(method, emitter);
        }

        emitter.decreaseIndentationLevel();
        emitter.emitLine(")");
        emitter.close();
    }

    private static void compileStaticMethod(ClassMethod method, CodeEmitter emitter) {
        // Emit the function declaration
//        emitter.startLine();
        StringBuilder line = new StringBuilder();
        line.append("(func $");
        line.append(method.getName());
        for (MethodParameter param : method.getParams()) {
            line.append(" (param $");
            line.append(param.getParameterName());
            line.append(" ");
            Type paramType = param.getType();
            if (paramType instanceof PrimitiveType) {
                String representationType = CodeGenUtil.getTypeForPrimitive((PrimitiveType) paramType);
//                emitter.emit(representationType + ")");
                line.append(representationType);
                line.append(")");
            } else {
                // TODO: Implement non-primitive types
            }
        }

        // TODO: Return type

//        emitter.finishLine();
        emitter.emitLine(line.toString());

        // Now compile the body of the function
        emitter.increaseIndentationLevel();
        compileCodeBlock(method.getBody(), emitter);
        // TODO: Find way to get this on the same line as the last instruction from above
        emitter.emitLine(")");

        emitter.decreaseIndentationLevel();

        // Possibly export the function
        if (method.getAccessModifier() == AccessModifier.PUBLIC) {
            emitter.emitLine("(export \"" + method.getName() + "\" (func $" + method.getName() + "))");
        }
    }

    // TODO: Probably shouldn't have this in here
    public static void compileCodeBlock(CodeBlock codeBlock, CodeEmitter emitter) {
        for (Statement statement : codeBlock.getStatements()) {
            StatementGenerator.compileStatement(statement, emitter, codeBlock.getVariableScope());
        }
    }

}
