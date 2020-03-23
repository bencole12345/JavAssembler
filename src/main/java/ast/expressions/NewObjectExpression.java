package ast.expressions;

import ast.types.JavaClass;
import util.FunctionTableEntry;

import java.util.List;

public class NewObjectExpression implements Expression {

    private JavaClass javaClass;
    private List<Expression> arguments;
    private FunctionTableEntry constructor;

    public NewObjectExpression(
            JavaClass javaClass,
            List<Expression> arguments,
            FunctionTableEntry constructor) {

        this.javaClass = javaClass;
        this.arguments = arguments;
        this.constructor = constructor;
    }

    public NewObjectExpression(JavaClass javaClass) {
        this.javaClass = javaClass;
        this.arguments = null;
        this.constructor = null;
    }

    @Override
    public JavaClass getType() {
        return javaClass;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public boolean usesConstructor() {
        return constructor != null;
    }

    public FunctionTableEntry getConstructor() {
        return constructor;
    }
}
