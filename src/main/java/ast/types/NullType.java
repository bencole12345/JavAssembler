package ast.types;

public class NullType extends HeapObjectReference {
    @Override
    public boolean isSubtypeOf(Type other) {
        return other instanceof HeapObjectReference;
    }

    @Override
    public String toString() {
        return "null";
    }
}
