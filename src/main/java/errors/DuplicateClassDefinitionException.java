package errors;

public class DuplicateClassDefinitionException extends JavAssemblerException {
    public DuplicateClassDefinitionException(String message) {
        super(message);
    }
}
