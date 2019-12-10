package codegen;

import java.io.*;

/**
 * Encapsulates the writing of lines of assembly to a file.
 *
 * This class handles indention levels and newline characters automatically.
 * The aim is that you only have to interact with the emitLine() method.
 */
public class CodeEmitter {

    private static String DEFAULT_INDENTION_STRING = "  ";

    private BufferedWriter writer;
    private int indentationLevel;
    private String indentationString;

    public CodeEmitter(String outputFilePath) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFilePath, false);
        writer = new BufferedWriter(fileWriter);
        indentationLevel = 0;
        indentationString = DEFAULT_INDENTION_STRING;
    }

    /**
     * Emits a line of code, handling indention and appending a newline
     * character.
     *
     * @param line The line of code to emit
     */
    public void emitLine(String line) {
        for (int i = 0; i < indentationLevel; i++) {
            emit(indentationString);
        }
        emit(line + "\n");
    }

    /**
     * Emits text to the output buffer.
     *
     * @param output The text to output
     */
    private void emit(String output) {
        try {
            writer.write(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Attempts to close the file.
     */
    public void close() {
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Increases the indentation level.
     *
     * @return The new indentation level
     */
    public int increaseIndentationLevel() {
        return ++indentationLevel;
    }

    /**
     * Decreases the indentation level.
     *
     * If the level is already 0 then this method will do nothing.
     *
     * @return The new indentation level
     */
    public int decreaseIndentationLevel() {
        if (indentationLevel > 0)
            indentationLevel--;
        return indentationLevel;
    }

    /**
     * Sets a new string to use for indentation.
     *
     * This string will be emitted once per indentation level.
     *
     * @param indentationString The new string to use for indentation
     */
    public void setIndentationString(String indentationString) {
        this.indentationString = indentationString;
    }
}
