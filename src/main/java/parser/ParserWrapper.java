package parser;

import errors.SyntaxErrorException;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

/**
 * Encapsulates invoking the parser
 */
public class ParserWrapper {

    /**
     * Parses the given file
     * @param filename The name of the file to parse
     * @return The ParseTree that was generated
     * @throws IOException If there is an error accessing the file
     */
    public static JavaFileParser.FileContext parse(String filename)
            throws IOException, SyntaxErrorException {
        // TODO: Find a not-deprecated way to do this
        ANTLRFileStream fileStream = new ANTLRFileStream(filename);
        JavaFileLexer lexer = new JavaFileLexer(fileStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        JavaFileParser parser = new JavaFileParser(tokenStream);
        JavaFileParser.FileContext file = parser.file();
        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new SyntaxErrorException("Syntax error in file " + filename);
        }
        return file;
    }

}
