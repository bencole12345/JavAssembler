package ast.types;

import java.util.Objects;

/**
 * Represents a generic type that will be replaced once the generic class is
 * instantiated.
 *
 * For example, in a class with type parameter T, any occurrence of T would be
 * an instance of a GenericType.
 */
public class GenericType extends HeapObjectReference {

    private String name;
    private int positionInArguments;

    public GenericType(String name, int positionInArguments) {
        this.name = name;
        this.positionInArguments = positionInArguments;
    }

    public String getName() {
        return name;
    }

    public int getPositionInArguments() {
        return positionInArguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericType that = (GenericType) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        return other.equals(this);
    }

}