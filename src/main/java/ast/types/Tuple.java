package ast.types;

import java.util.List;
import java.util.Optional;

public class Tuple implements Type {

    /**
     * The list of types contained in this tuple
     */
    private List<Type> types;

    public Tuple(List<Type> types) {
        this.types = types;
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        return other instanceof Tuple
                && this.types.equals(((Tuple) other).types);
    }

    @Override
    public int getStackSize() {
        Optional<Integer> size = types
                .stream()
                .map(Type::getStackSize)
                .reduce(Integer::sum);
        return size.isPresent() ? size.get() : 0;
    }

    @Override
    public boolean isPointer() {
        return false;
    }
}
