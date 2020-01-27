package ast.expressions;

import ast.types.JavaClass;

import java.util.List;

public class NewObjectExpression implements Expression {

    private JavaClass javaClass;
    private List<Expression> arguments;

    public NewObjectExpression(JavaClass javaClass, List<Expression> arguments) {
        this.javaClass = javaClass;
        this.arguments = arguments;
    }

    @Override
    public JavaClass getType() {
        return javaClass;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}
