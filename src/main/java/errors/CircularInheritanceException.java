package errors;

public class CircularInheritanceException extends JavAssemblerException {
    public CircularInheritanceException(String message) {
        super(message);
    }
}
