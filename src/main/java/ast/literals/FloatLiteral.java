package ast.literals;

public class FloatLiteral implements LiteralValue {

    private float value;

    public FloatLiteral(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }
}
