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
     */
    static void reportError(String errorMessage, ParserRuleContext ctx) {
        // TODO: Report which file
        int line = ctx.start.getLine();
        int col = ctx.start.getCharPositionInLine();
        String message = "Error on line " + line
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
