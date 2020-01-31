package codegen;

/**
 * Encodes the four possible WebAssembly types.
 */
public enum WasmType {
    Int32("i32", 4),
    Int64("i64", 8),
    Float32("f32", 4),
    Float64("f64", 8);

    private String representation;
    private int sizeBytes;

    WasmType(String representation, int sizeBytes) {
        this.representation = representation;
        this.sizeBytes = sizeBytes;
    }

    @Override
    public String toString() {
        return representation;
    }

    /**
     * Returns the size of the type, in bytes
     * @return the size of the type, in bytes
     */
    public int getSize() {
        return sizeBytes;
    }
}
