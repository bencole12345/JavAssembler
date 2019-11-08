package ast.types;

import java.util.Objects;

public class NonPrimitiveType implements Type {

    // TODO: Think about whether we should be referring to the name of the class here,
    // or whether instead we should actually hold a reference to a JavaClass object.
    //
    // As this is for the AST, I guess the best option is to go for the name only, and
    // let linking to an actual class be handled in the linking stage.
    private String className;

    public NonPrimitiveType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NonPrimitiveType that = (NonPrimitiveType) o;
        return className.equals(that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }
}
