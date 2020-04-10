package codegen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Contains static methods for reading hand-written .wat files so that they
 * can be included in the generated source code.
 */
public class WasmLibReader {

    public static Stream<String> getGlobalsCode() {
        return readCleanedWasmCode("globals.wat");
    }

    public static Stream<String> getAllocationCode() {
        return readCleanedWasmCode("alloc.wat");
    }

    public static Stream<String> getGarbageCollectionCode() {
        return readCleanedWasmCode("gc.wat");
    }

    public static Stream<String> getDebugCode() {
        return readCleanedWasmCode("debug.wat");
    }

    /**
     * Cleans the WebAssembly code for output by removing empty lines and
     * comments.
     *
     * @param fileName The name of the file to read
     * @return A filtered stream with the unneeded lines removed
     */
    private static Stream<String> readCleanedWasmCode(String fileName) {
        return readLinesFromFile(fileName)
                .filter(line -> line.trim().length() > 0)        // Remove empty lines
                .filter(line -> !line.trim().startsWith(";;"));  // Remove comments
    }

    /**
     * Reads a file and outputs the lines in a stream.
     *
     * @param fileName The name of the file to read
     * @return A stream containing the lines from the file
     */
    private static Stream<String> readLinesFromFile(String fileName) {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = Paths.get(currentPath.toString(), "src", "main", "wasm-lib", fileName);
        File file = new File(filePath.toString());
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert reader != null;
        return reader.lines();
    }

}
