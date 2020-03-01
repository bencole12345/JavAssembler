package ast.types;

/**
 * Denotes a void type.
 *
 * This is only ever used for function return types. A variable should never
 * be marked as void type.
 */
public class VoidType implements Type {

    @Override
    public boolean isSubtypeOf(Type other) {
        return other instanceof VoidType;
    }

    @Override
    public int getStackSize() {
        return 0;
    }
}
