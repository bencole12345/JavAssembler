package ast.types;

public class ObjectArrayType extends HeapObjectReference {

    /**
     * The type of each element.
     */
    private Type elementType;

    public ObjectArrayType(Type elementType) {
        this.elementType = elementType;
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        // TODO: Implement correct subtyping behaviour (look up whether should be covariant vs contravariant)
        if (!(other instanceof ObjectArrayType))
            return false;
        ObjectArrayType otherArray = (ObjectArrayType) other;
        return elementType.equals(otherArray.elementType);
    }

    @Override
    public String toString() {
        return elementType + "[]";
    }
}
