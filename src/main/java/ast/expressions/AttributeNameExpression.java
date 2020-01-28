package ast.expressions;

import ast.types.JavaClass;
import ast.types.Type;
import errors.IllegalPrivateAccessException;
import errors.InvalidAttributeException;

public class AttributeNameExpression implements Expression {

    private VariableNameExpression object;
    private JavaClass.AllocatedClassAttribute attribute;

    public AttributeNameExpression(VariableNameExpression object,
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

    public VariableNameExpression getObject() {
        return object;
    }

    @Override
    public Type getType() {
        return attribute.getType();
    }

    public int getMemoryOffset() {
        return attribute.getMemoryOffset();
    }
}
