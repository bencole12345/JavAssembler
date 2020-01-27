package ast.types;

import java.util.Objects;

/**
 * Represents a known Java class.
 */
public class JavaClass implements Type {

    private String name;
    private JavaClass parent;

    public JavaClass(String name) {
        this.name = name;
        this.parent = null;
    }

    public JavaClass(String name, JavaClass parent) {
        this(name);
        this.parent = parent;
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        return (this.equals(other))
                || (parent != null && parent.isSubtypeOf(other));
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JavaClass)
                && ((JavaClass) obj).name.equals(this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
