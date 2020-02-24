package ast.types;

public class ObjectArray extends HeapObjectReference {

    /**
     * The type of each element.
     */
    private Type elementType;

    public ObjectArray(Type elementType) {
        this.elementType = elementType;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        // TODO: Implement correct subtyping behaviour (look up whether should be covariant vs contravariant)
        if (!(other instanceof ObjectArray))
            return false;
        ObjectArray otherArray = (ObjectArray) other;
        return elementType.equals(otherArray.elementType);
    }

    @Override
    public String toString() {
        return elementType + "[]";
    }
}
