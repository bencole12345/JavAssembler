package parser;

/**
 * Used by the ASTBuilder class to report semantic errors detected during parsing.
 */
public class ErrorReporter {

    /**
     * Tracks whether any errors have been reported
     */
    private boolean errorHasHappened;

    public ErrorReporter() {
        errorHasHappened = false;
    }

    /**
     * Reports an error to System.err
     *
     * @param message The error message to report
     */
    public void reportError(String message) {
        System.err.println(message);
        errorHasHappened = true;
    }

    /**
     * Reports an error to System.err, including line and column numbers
     *
     * @param message The error message to report
     * @param line The line in the source file at which the error occurred
     * @param col The column in the source file at which the error occurred
     */
    public void reportError(String message, int line, int col) {
        System.err.println("An error occurred at line " + line
                + ", column " + col + ", message: " + message);
        errorHasHappened = true;
    }

    /**
     * Returns whether an error has happened
     * @return true if an error has been reported; false otherwise
     */
    public boolean getErrorHasHappened() {
        return errorHasHappened;
    }
}
