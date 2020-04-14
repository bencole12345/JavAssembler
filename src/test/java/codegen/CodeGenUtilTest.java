package codegen;

import ast.types.JavaClass;
import ast.types.ObjectArray;
import ast.types.PrimitiveType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeGenUtilTest {

    @Test
    void wasmTypeBoolean() {
        WasmType wasmType = CodeGenUtil.getWasmType(PrimitiveType.Boolean);
        assertEquals(WasmType.Int32, wasmType);
    }

    @Test
    void wasmTypeByte() {
        WasmType wasmType = CodeGenUtil.getWasmType(PrimitiveType.Byte);
        assertEquals(WasmType.Int32, wasmType);
    }

    @Test
    void wasmTypeChar() {
        WasmType wasmType = CodeGenUtil.getWasmType(PrimitiveType.Char);
        assertEquals(WasmType.Int32, wasmType);
    }

    @Test
    void wasmTypeShort() {
        WasmType wasmType = CodeGenUtil.getWasmType(PrimitiveType.Short);
        assertEquals(WasmType.Int32, wasmType);
    }

    @Test
    void wasmTypeInt() {
        WasmType wasmType = CodeGenUtil.getWasmType(PrimitiveType.Int);
        assertEquals(WasmType.Int32, wasmType);
    }

    @Test
    void wasmTypeLong() {
        WasmType wasmType = CodeGenUtil.getWasmType(PrimitiveType.Long);
        assertEquals(WasmType.Int64, wasmType);
    }

    @Test
    void wasmTypeFloat() {
        WasmType wasmType = CodeGenUtil.getWasmType(PrimitiveType.Float);
        assertEquals(WasmType.Float32, wasmType);
    }

    @Test
    void wasmTypeDouble() {
        WasmType wasmType = CodeGenUtil.getWasmType(PrimitiveType.Double);
        assertEquals(WasmType.Float64, wasmType);
    }

    @Test
    void wasmTypeObject() {
        JavaClass mocked = Mockito.mock(JavaClass.class);
        WasmType wasmType = CodeGenUtil.getWasmType(mocked);
        assertEquals(WasmType.Int32, wasmType);
    }

    @Test
    void wasmTypeArray() {
        ObjectArray mocked = Mockito.mock(ObjectArray.class);
        WasmType wasmType = CodeGenUtil.getWasmType(mocked);
        assertEquals(WasmType.Int32, wasmType);
    }

}