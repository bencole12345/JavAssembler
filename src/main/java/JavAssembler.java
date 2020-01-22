import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        // TODO: Support multiple input files
        String inputFile = commandLine.getOptionValue("input");
        List<String> inputFiles = new ArrayList<>();
        inputFiles.add(inputFile);
        String outputFile = commandLine.getOptionValue("output");

        Compilation.compileFiles(inputFiles, outputFile);
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
