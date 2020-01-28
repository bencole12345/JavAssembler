package ast.expressions;

import ast.types.JavaClass;
import ast.types.Type;
import errors.IllegalPrivateAccessException;
import errors.InvalidAttributeException;

public class AttributeNameExpression implements VariableExpression {

    private LocalVariableExpression object;
    private JavaClass.AllocatedClassAttribute attribute;

    public AttributeNameExpression(LocalVariableExpression object,
                                   String attributeName)
            throws InvalidAttributeException, IllegalPrivateAccessException {

        if (!(object.getType() instanceof JavaClass)) {
            String message = "Unable to look up attribute " + attributeName
                    + " from type " + object.getType();
            throw new InvalidAttributeException(message);
        }
        JavaClass javaClass = (JavaClass) object.getType();

        // TODO: Test whether it's okay to access a private method from here
        this.attribute = javaClass.lookupAttribute(attributeName, false);
        this.object = object;
    }

    public LocalVariableExpression getObject() {
        return object;
    }

    @Override
    public Type getType() {
        return attribute.getType();
    }

    public int getMemoryOffset() {
        return attribute.getMemoryOffset();
    }

    @Override
    public String toString() {
        return object.toString() + "." + attribute.getName();
    }
}
