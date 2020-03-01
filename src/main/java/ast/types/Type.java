package ast.types;

import ast.ASTNode;

public interface Type extends ASTNode {

    /**
     * Determines whether this type is a subtype of another type.
     *
     * It is acceptable to assign a variable of type T to a value of type U
     * iff U is a subtype of T.
     *
     * @param other The other Type
     * @return true if this is a subtype of other; false otherwise
     */
    boolean isSubtypeOf(Type other);

    /**
     * Reports the total memory size of the type, in bytes.
     *
     * For heap objects, this should be the size of the reference to the object,
     * not the size of the object of itself.
     *
     * @return The memory size of the type, in bytes
     */
    int getStackSize();

}
