package parser;

import ast.structure.CompilationUnit;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;

/**
 * Encapsulates invoking the parser
 */
public class ParserWrapper {

    /**
     * Loads a file and parses it into an AST.
     * @param filename The name of the file to parse
     * @return The AST that was parsed
     * @throws IOException If there was an error accessing the file
     */
    public static CompilationUnit parseFile(String filename) throws IOException {
        ParseTree parseTree = parse(filename);
        ASTBuilder astBuilder = new ASTBuilder();
        return (CompilationUnit) astBuilder.visit(parseTree);
    }

    /**
     * Parses the given file
     * @param filename The name of the file to parse
     * @return The ParseTree that was generated
     * @throws IOException If there is an error accessing the file
     */
    private static ParseTree parse(String filename) throws IOException {
        // TODO: Find a not-deprecated way to do this
        ANTLRFileStream fileStream = new ANTLRFileStream(filename);
        JavaFileLexer lexer = new JavaFileLexer(fileStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        JavaFileParser parser = new JavaFileParser(tokenStream);
        return parser.file();
    }

}
