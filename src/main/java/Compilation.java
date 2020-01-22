import ast.functions.FunctionTable;
import ast.structure.ClassMethod;
import codegen.CodeEmitter;
import codegen.WasmGenerator;
import parser.ASTBuilder;
import parser.ClassSignatureVisitor;
import parser.JavaFileParser;
import parser.ParserWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compilation {

    public static void compileFiles(List<String> fileNames, String outputFileName) throws IOException {

        FunctionTable functionTable = new FunctionTable();
        List<JavaFileParser.FileContext> parseTrees = new ArrayList<>();

        // Parse every file
        for (String fileName : fileNames) {
            parseTrees.add(ParserWrapper.parse(fileName));
        }

        // In the first parse we just extract all function signatures and load
        // them into the function table. We use the map to track the class name
        // for each method.
        // TODO: Also pull out class attributes
        List<JavaFileParser.MethodDefinitionContext> methods = new ArrayList<>();
        Map<JavaFileParser.MethodDefinitionContext, String> classNameMap = new HashMap<>();
        ClassSignatureVisitor visitor = new ClassSignatureVisitor(functionTable, methods, classNameMap);
        for (JavaFileParser.FileContext parseTree : parseTrees) {
            visitor.visit(parseTree);
        }

        // Now that the function table has been built, we can properly construct
        // an AST for each method.
        List<ClassMethod> methodASTs = new ArrayList<>();
        ASTBuilder astBuilder = new ASTBuilder(functionTable);
        for (JavaFileParser.MethodDefinitionContext methodCtx : methods) {
            String className = classNameMap.get(methodCtx);
            ClassMethod methodAST = astBuilder.visitMethod(methodCtx, className);
            methodASTs.add(methodAST);
        }

        // Now that we have an AST for each method, and a complete function
        // table, we can start generating code for each method. We do this
        // in the same order as the entries in the function
        CodeEmitter codeEmitter = new CodeEmitter(outputFileName);
        WasmGenerator.compileMethods(methodASTs, codeEmitter, functionTable);

    }
}
