package util;

import ast.structure.ClassMethod;
import ast.types.JavaClass;
import codegen.CodeEmitter;
import codegen.WasmGenerator;
import parser.*;

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

        // First determine the class hierarchy so that we can derive an order
        // for visiting each class.
        ClassHierarchyBuilder hierarchyBuilder = new ClassHierarchyBuilder();
        for (JavaFileParser.FileContext parseTree : parseTrees) {
            hierarchyBuilder.visit(parseTree);
        }
        List<JavaFileParser.ClassDefinitionContext> classes =
                hierarchyBuilder.getSerialClassOrdering();

        // Now that we have a serial ordering, build up a memory representation
        // of each class.
        ClassTableBuilder classTableBuilder = new ClassTableBuilder();
        for (JavaFileParser.ClassDefinitionContext classDefinitionContext : classes) {
            classTableBuilder.visit(classDefinitionContext);
        }
        ClassTable classTable = classTableBuilder.getConstructedClassTable();

        // Now that we have built a memory representation of all types in the
        // program, we can update any class attributes that have not yet been
        // checked to ensure that they reference a type that actually exists.
        classTable.validateAllClassReferences();

        // Now build a function table
        FunctionTableBuilder functionTableBuilder = new FunctionTableBuilder(classTable);
        for (JavaFileParser.FileContext parseTree : parseTrees) {
            functionTableBuilder.visit(parseTree);
        }
        FunctionTable functionTable = functionTableBuilder.getConstructedFunctionTable();

        // Convert the parse tree of each method into an AST
        ASTBuilder astBuilder = new ASTBuilder(functionTable, classTable);
        List<JavaFileParser.MethodDefinitionContext> methodParseTrees =
                functionTableBuilder.getMethodParseTrees();
        Map<JavaFileParser.MethodDefinitionContext, JavaClass> classMap =
                functionTableBuilder.getMethodParseTreeToContainingClassMap();
        List<ClassMethod> methodASTs = new ArrayList<>();
        for (JavaFileParser.MethodDefinitionContext methodParseTree : methodParseTrees) {
            JavaClass javaClass = classMap.get(methodParseTree);
            ClassMethod methodAST = astBuilder.visitMethod(methodParseTree, javaClass);
            methodASTs.add(methodAST);
        }

        // Finally, compile each AST into WebAssembly
        CodeEmitter emitter = new CodeEmitter(outputFileName);
        WasmGenerator.compile(methodASTs, emitter, functionTable, classTable);

    }
}
