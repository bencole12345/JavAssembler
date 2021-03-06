import org.apache.commons.cli.*;
import util.Compilation;

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

        String[] inputFiles = commandLine.getOptionValues("inputs");
        String outputFile = commandLine.getOptionValue("output");
        boolean debug = commandLine.hasOption("debug");

        Compilation.compileFiles(inputFiles, outputFile, debug);
    }

    private static Options getCommandLineOptions() {
        Options options = new Options();
        Option input = new Option("i", "inputs", true, "The Java files to read from");
        input.setRequired(true);
        input.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(input);
        Option output = new Option("o", "output", true, "The wasm file to write to");
        output.setRequired(true);
        options.addOption(output);
        Option debug = new Option("d", "debug", false, "Include additional debugging functions in the output");
        options.addOption(debug);
        return options;
    }

}
