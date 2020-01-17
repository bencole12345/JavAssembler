import ast.functions.FunctionTable;
import ast.structure.CompilationUnit;
import codegen.CodeEmitter;
import codegen.WasmGenerator;
import org.apache.commons.cli.*;
import parser.ParserWrapper;

import java.io.IOException;

public class JavAssembler {

    public static void main(String[] args) throws IOException {

        Options commandLineOptions = getCommandLineOptions();
        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = null;
        try {
            commandLine = commandLineParser.parse(commandLineOptions, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("JavAssembler", commandLineOptions);
            System.exit(1);
        }

        String inputFile = commandLine.getOptionValue("input");
        String outputFile = commandLine.getOptionValue("output");

        compileFile(inputFile, outputFile);
    }

    private static void compileFile(String inputFile, String outputFile) throws IOException {
        CompilationUnit compilationUnit = ParserWrapper.parseFile(inputFile);
        if (compilationUnit == null) {
            return;
            // An error message should already have been displayed
        }
        FunctionTable functionTable = compilationUnit.getFunctionTable();
        CodeEmitter codeEmitter = new CodeEmitter(outputFile);
        WasmGenerator.compile(compilationUnit, codeEmitter, functionTable);
    }

    private static Options getCommandLineOptions() {
        Options options = new Options();
        Option input = new Option("i", "input", true, "The Java file to read from");
        input.setRequired(true);
        options.addOption(input);
        Option output = new Option("o", "output", true, "The wasm file to write to");
        output.setRequired(true);
        options.addOption(output);
        return options;
    }

}
