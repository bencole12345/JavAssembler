package ast.types;

public enum PrimitiveType implements Type {
    Int(true, false, 32, "int"),
    Short(true, false, 16, "short"),
    Long(true, false, 64, "long"),
    Char(true, false, 16, "char"),
    Byte(true, false, 8, "byte"),
    Boolean(false, false, 1, "boolean"),
    Float(false, true, 32, "float"),
    Double(false, true, 64, "double");

    private boolean isIntegralType;
    private boolean isDecimalType;
    private int size;
    private String representation;

    PrimitiveType(boolean isIntegralType, boolean isDecimalType, int size, String representation) {
        this.isIntegralType = isIntegralType;
        this.isDecimalType = isDecimalType;
        this.size = size;
        this.representation = representation;
    }

    /**
     * Returns whether or not the type is an integral (integer-like) type.
     *
     * @return true for integral types, false otherwise
     */
    public boolean isIntegralType() {
        return isIntegralType;
    }

    /**
     * Returns whether the type is a decimal type (float or double).
     *
     * @return true for decimal types, false otherwise
     */
    public boolean isDecimalType() {
        return isDecimalType;
    }

    /**
     * Returns whether the type is a numeric type.
     *
     * @return true for numeric types, false otherwise
     */
    public boolean isNumericType() {
        return isIntegralType || isDecimalType;
    }

    /**
     * Returns the size of the type, in bits.
     *
     * @return The size of the type, in bits
     */
    public int getSize() {
        return size;
    }

    @Override
    public boolean isSubtypeOf(Type other) {
        // TODO: Consider supporting type coercion
        return this.equals(other);
    }

    @Override
    public String toString() {
        return representation;
    }
}
