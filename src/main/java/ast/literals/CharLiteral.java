package ast.literals;

public class CharLiteral implements LiteralValue {

    private char value;

    public CharLiteral(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }
}
