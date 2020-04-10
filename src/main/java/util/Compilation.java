package util;

import ast.structure.ClassMethod;
import ast.types.JavaClass;
import codegen.CodeEmitter;
import codegen.WasmGenerator;
import errors.SyntaxErrorException;
import parser.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Compilation {

    public static void compileFiles(String[] fileNames, String outputFileName, boolean debug) throws IOException {

        // First use ANTLR to generate a parse tree for every file.
        List<JavaFileParser.FileContext> parseTrees = new ArrayList<>();
        for (String fileName : fileNames) {
            try {
                parseTrees.add(ParserWrapper.parse(fileName));
            } catch (SyntaxErrorException e) {
                ErrorReporting.reportError(e.getMessage());
            } catch (IOException e) {
                ErrorReporting.reportError("Unable to read file " + fileName);
            }
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
        FunctionAndClassTableBuilder functionAndClassTableBuilder = new FunctionAndClassTableBuilder();
        for (JavaFileParser.ClassDefinitionContext classDefinitionContext : classes) {
            functionAndClassTableBuilder.visit(classDefinitionContext);
        }
        ClassTable classTable = functionAndClassTableBuilder.getClassTable();

        // Now that we have built a memory representation of all types in the
        // program, we can update any class attributes that have not yet been
        // checked to ensure that they reference a type that actually exists.
        classTable.validateAllTypes();

        // Now build a function table
        FunctionTable functionTable = functionAndClassTableBuilder.getFunctionTable();
        functionTable.validateAllTypes(classTable);

        // Convert the parse tree of each method into an AST
        ASTBuilder astBuilder = new ASTBuilder(functionTable, classTable);
        List<SubroutineToCompile> subroutines = functionAndClassTableBuilder.getSubroutines();
        List<JavaClass> containingClasses = functionAndClassTableBuilder.getContainingClasses();
        List<ClassMethod> methodASTs = new ArrayList<>();

        // Parse each method, also passing in the containing class
        for (int i = 0; i < subroutines.size(); i++) {
            SubroutineToCompile subroutine = subroutines.get(i);
            JavaClass containingClass = containingClasses.get(i);
            ClassMethod methodAST = astBuilder.visitSubroutine(subroutine, containingClass);
            methodASTs.add(methodAST);
        }

        // Build a virtual table now that all classes have been seen
        VirtualTable virtualTable = classTable.buildCombinedVirtualTable();

        // Finally, compile each AST into WebAssembly
        CodeEmitter emitter = new CodeEmitter(outputFileName);
        WasmGenerator.compile(methodASTs, emitter, functionTable, classTable, virtualTable, debug);

    }
}
