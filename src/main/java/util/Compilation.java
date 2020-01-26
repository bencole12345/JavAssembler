package util;

import ast.structure.ClassMethod;
import codegen.CodeEmitter;
import codegen.WasmGenerator;
import parser.ASTBuilder;
import parser.ClassSignatureBuilder;
import parser.JavaFileParser;
import parser.ParserWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Compilation {

    public static void compileFiles(String[] fileNames, String outputFileName) throws IOException {

        // First use ANTLR to generate a parse tree for every file.
        List<JavaFileParser.FileContext> parseTrees = new ArrayList<>();
        for (String fileName : fileNames) {
            parseTrees.add(ParserWrapper.parse(fileName));
        }

        // In the first pass we just extract all function signatures and load
        // them into the function table. We use the map to track the class name
        // for each method.
        ClassSignatureBuilder signatureBuilder = new ClassSignatureBuilder();
        for (JavaFileParser.FileContext parseTree : parseTrees) {
            signatureBuilder.visit(parseTree);
        }

        // Now that the first pass has completed, we can extract the generated
        // function table and class table.
        FunctionTable functionTable = signatureBuilder.getFunctionTable();
        ClassTable classTable = signatureBuilder.getClassTable();

        // Also extract the list of methods to be compiled.
        List<JavaFileParser.MethodDefinitionContext> methodParseTrees =
                signatureBuilder.getMethodParseTrees();
        Map<JavaFileParser.MethodDefinitionContext, String> classNameMap =
                signatureBuilder.getClassNameMap();

        // Now that the function table has been built, we can properly construct
        // an AST for each method.
        List<ClassMethod> methodASTs = new ArrayList<>();
        ASTBuilder astBuilder = new ASTBuilder(functionTable, classTable);
        for (JavaFileParser.MethodDefinitionContext methodCtx : methodParseTrees) {
            String className = classNameMap.get(methodCtx);
            ClassMethod methodAST = astBuilder.visitMethod(methodCtx, className);
            methodASTs.add(methodAST);
        }

        // Now that we have built the AST for every method, we can compile each
        // method into WebAssembly.
        CodeEmitter codeEmitter = new CodeEmitter(outputFileName);
        WasmGenerator.compileMethods(methodASTs, codeEmitter, functionTable);

    }
}
