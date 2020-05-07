package ast.types;

import java.util.Objects;

public class ItemArray extends HeapObjectReference {

    /**
     * The type of each element.
     */
    private Type elementType;

    public ItemArray(Type elementType) {
        this.elementType = elementType;
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        if (!(other instanceof ItemArray))
            return false;
        ItemArray otherArray = (ItemArray) other;
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
        ItemArray that = (ItemArray) o;
        return elementType.equals(that.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType);
    }
}
