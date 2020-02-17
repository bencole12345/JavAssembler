package parser;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Contains miscellaneous helper functions used for parsing.
 */
public class ParserUtil {

    /**
     * Reports an error to the console and exits.
     *
     * @param errorMessage The error to report
     * @param ctx The ParserRuleContext at which the error occurred
     * @param location The file in which the error occurred
     */
    static void reportError(String errorMessage, ParserRuleContext ctx, String location) {
        // TODO: Fix error reporting when it's a local variable declaration
        // (the problem is that we process it at the start of the method so
        // the error is reported for the line of the method declaration rather
        // than actually the line of the variable declaration)
        int line = ctx.start.getLine();
        int col = ctx.start.getCharPositionInLine();
        String message = "Error in " + location + ".java"
                + " on line " + line
                + ", column " + col
                + ": " + errorMessage;
        System.err.println(message);
        System.err.println("Exiting...");
        System.exit(0);
    }

    /**
     * Reports an error to the console and exits.
     *
     * @param errorMessage The error to report
     */
    public static void reportError(String errorMessage) {
        System.err.println(errorMessage);
        System.err.println("Exiting...");
        System.exit(0);
    }
}
