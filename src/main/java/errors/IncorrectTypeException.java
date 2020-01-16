package errors;

/**
 * Indicates that an operation was applied to two mismatching types
 */
public class IncorrectTypeException extends JavAssemblerException {

    public IncorrectTypeException(String message) {
        super(message);
    }

}
