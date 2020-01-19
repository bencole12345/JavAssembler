package errors;

/**
 * Indicates that multiple variables with the same name have been declared
 * within a scope.
 */
public class MultipleVariableDeclarationException extends JavAssemblerException {
    public MultipleVariableDeclarationException(String message) {
        super(message);
    }
}
