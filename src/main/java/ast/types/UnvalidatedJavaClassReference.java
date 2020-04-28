package ast.types;

import java.util.List;

/**
 * Used to indicate a reference to a class that has not yet been checked.
 *
 * The class will be referred to symbolically - we haven't yet checked that the
 * class actually exists. Any instance of this should be replaced with a
 * JavaClass instance later, or an error will be raised if there is no such
 * class.
 */
public class UnvalidatedJavaClassReference extends HeapObjectReference {

    /**
     * The name of the class
     */
    private String className;

    /**
     * The type arguments that were supplied
     */
    private List<HeapObjectReference> typeArguments;

    public UnvalidatedJavaClassReference(String className) {
        this.className = className;
        typeArguments = null;
    }

    public void setTypeArguments(List<HeapObjectReference> typeArguments) {
        this.typeArguments = typeArguments;
    }

    public List<HeapObjectReference> getTypeArguments() {
        return typeArguments;
    }

    public boolean hasTypeArguments() {
        return typeArguments != null && !typeArguments.isEmpty();
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        // Should never be called
        assert false;
        return false;
    }

    @Override
    public int getStackSize() {
        // All references are 32-bit
        return 4;
    }

    /**
     * Returns the class name referenced.
     *
     * This class name may or may not correspond to a real class.
     *
     * @return The class name referenced
     */
    public String getClassName() {
        return className;
    }
}
