package ast.types;

import java.util.Objects;

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
        // TODO: Implement correct covariant array subtyping
        // and check types on write
        if (!(other instanceof ObjectArray))
            return false;
        ObjectArray otherArray = (ObjectArray) other;
        return elementType.equals(otherArray.elementType);
    }

    @Override
    public String toString() {
        return elementType + "[]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectArray that = (ObjectArray) o;
        return elementType.equals(that.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType);
    }
}