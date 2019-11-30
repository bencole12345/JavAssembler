package ast.literals;

public class FloatLiteral implements ValueExpression {

    private float value;

    public FloatLiteral(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }
}
