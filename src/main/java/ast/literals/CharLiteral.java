package ast.literals;

public class CharLiteral implements ValueExpression {

    private char value;

    public CharLiteral(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }
}
