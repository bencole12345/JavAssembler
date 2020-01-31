package ast.types;

public abstract class JavaClassReference implements Type {
    @Override
    public int getSize() {
        // References are always 32-bits
        return 32;
    }
}
