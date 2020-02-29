package ast.types;

public abstract class HeapObjectReference implements Type {
    @Override
    public int getStackSize() {
        // References are always 32-bits
        return 4;
    }
}
