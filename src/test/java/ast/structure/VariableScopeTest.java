package ast.structure;

import ast.types.JavaClass;
import ast.types.PrimitiveType;
import errors.MultipleVariableDeclarationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class VariableScopeTest {

    @Test
    void localVariableAllocation() throws MultipleVariableDeclarationException {
        VariableScope variableScope = new VariableScope();
        variableScope.registerVariable("localTest", PrimitiveType.Int);
        VariableScope.Allocation allocation = variableScope.getVariableWithName("localTest");
        assertTrue(allocation instanceof VariableScope.LocalVariableAllocation);
        assertEquals(PrimitiveType.Int, allocation.getType());
    }

    @Test
    void stackVariableAllocation() throws MultipleVariableDeclarationException {
        VariableScope variableScope = new VariableScope();
        JavaClass javaClass = Mockito.mock(JavaClass.class);
        variableScope.registerVariable("stackTest", javaClass);
        VariableScope.Allocation allocation = variableScope.getVariableWithName("stackTest");
        assertTrue(allocation instanceof VariableScope.StackOffsetAllocation);
        assertEquals(javaClass, allocation.getType());
    }

    @Test
    void variableAllocationInParent() throws MultipleVariableDeclarationException {
        VariableScope parent = new VariableScope();
        parent.registerVariable("x", PrimitiveType.Int);
        VariableScope child = new VariableScope(parent);
        VariableScope.Allocation allocation = child.getVariableWithName("x");
        assertTrue(allocation instanceof VariableScope.LocalVariableAllocation);
        assertEquals(PrimitiveType.Int, allocation.getType());
    }

    @Test
    void invalidVariableRequested() {
        VariableScope parent = Mockito.mock(VariableScope.class);
        VariableScope child = new VariableScope(parent);
        VariableScope.Allocation allocation = child.getVariableWithName("x");
        Mockito.verify(parent).getVariableWithName("x");
        assertNull(allocation);
    }

    @Test
    void multipleIdenticalDeclarations() {
        VariableScope scope = new VariableScope();
        assertThrows(MultipleVariableDeclarationException.class, () -> {
            scope.registerVariable("x", PrimitiveType.Int);
            scope.registerVariable("x", PrimitiveType.Int);
        });
    }

    @Test
    void multipleDifferentDeclarationsSameLevel() {
        VariableScope scope = new VariableScope();
        assertThrows(MultipleVariableDeclarationException.class, () -> {
            scope.registerVariable("x", PrimitiveType.Int);
            JavaClass javaClass = Mockito.mock(JavaClass.class);
            scope.registerVariable("x", javaClass);
        });
    }

    @Test
    void multipleDeclarationsDifferentLevels() throws MultipleVariableDeclarationException {
        VariableScope parent = new VariableScope();
        parent.registerVariable("x", PrimitiveType.Int);
        assertThrows(MultipleVariableDeclarationException.class, () -> {
            VariableScope child = new VariableScope(parent);
            JavaClass javaClass = Mockito.mock(JavaClass.class);
            child.registerVariable("x", javaClass);
        });
    }

}