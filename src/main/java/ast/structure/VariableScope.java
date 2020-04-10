package ast.structure;

import ast.types.HeapObjectReference;
import ast.types.PrimitiveType;
import ast.types.Type;
import errors.MultipleVariableDeclarationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Captures a particular level of variable scoping.
 *
 * This class handles mappings from variable names to types and to their
 * register allocations. The main features are the lookup functions for type
 * and register allocation index. If the variable was declared in a containing
 * scope then the lookup functions will recursively call the corresponding
 * function in the containing scope.
 */
public class VariableScope {

    public static abstract class Allocation {

        public abstract Type getType();

    }

    /**
     * Wraps the data stored about a local variable allocation.
     *
     * This must only be used for primitive types. Non-primitive types
     * must be stored in the shadow stack.
     */
    public static class LocalVariableAllocation extends Allocation {

        private int localVariableIndex;
        private PrimitiveType type;

        public LocalVariableAllocation(int localVariableIndex, PrimitiveType type) {
            this.localVariableIndex = localVariableIndex;
            this.type = type;
        }

        public int getLocalVariableIndex() {
            return localVariableIndex;
        }

        public Type getType() {
            return type;
        }
    }

    /**
     * Wraps the data stored about a stack allocation.
     *
     * This must only be used for offsets into the shadow stack, where
     * the actual pointer into the heap will be stored. Primitive types
     * should be stored using a local variable.
     */
    public static class StackOffsetAllocation extends Allocation {

        private int offset;
        private HeapObjectReference type;

        public StackOffsetAllocation(int offset, HeapObjectReference type) {
            this.offset = offset;
            this.type = type;
        }

        public int getStackFrameOffset() {
            return offset;
        }

        public Type getType() {
            return type;
        }
    }

    /**
     * Reference to the containing scope
     *
     * This is needed in case a variable is requested that
     * was declared in a higher scope.
     */
    private VariableScope containingScope;

    /**
     * Contains the allocations for all variables that have been registered.
     */
    private Map<String, Allocation> variableAllocations;

    /**
     * An ordered list of all known allocations at this level or below
     *
     * This is used to generate the list of types used within a function, which
     * is required at the start of a WebAssembly function.
     */
    private List<LocalVariableAllocation> localVariableAllocationsList;

    /**
     * Tracks which register index we can allocate next for
     * a local variable
     */
    private int nextLocalVariableIndexToAllocate;

    /**
     * Tracks which stack offset to use next to allocate a value in the
     * shadow stack
     */
    private int nextStackOffsetToAllocate;

    public VariableScope() {
        variableAllocations = new HashMap<>();
        localVariableAllocationsList = new ArrayList<>();
        nextLocalVariableIndexToAllocate = 0;
        nextStackOffsetToAllocate = 0;
        containingScope = null;
    }

    public VariableScope(VariableScope containingScope) {
        this();
        this.containingScope = containingScope;
        nextLocalVariableIndexToAllocate = containingScope.nextLocalVariableIndexToAllocate;
        nextStackOffsetToAllocate = containingScope.nextStackOffsetToAllocate;
    }

    /**
     * Sets the containing scope.
     *
     * @param containingScope The new ContainingScope
     */
    public void bindContainingScope(VariableScope containingScope) {
        this.containingScope = containingScope;
    }

    /**
     * Returns the containing scope.
     *
     * @return The containing scope, or none if there is no containing scope
     */
    public VariableScope getContainingScope() {
        return containingScope;
    }

    /**
     * Registers a variable with this scope.
     *
     * @param name The name of the variable being registered
     * @param type The type of the variable
     * @throws MultipleVariableDeclarationException if a variable with this
     *         name has already been declared in this scope
     */
    public void registerVariable(String name, Type type) throws MultipleVariableDeclarationException {
        if (hasMappingFor(name)) {
            String message = "Variable " + name + " already has a declaration in this scope";
            throw new MultipleVariableDeclarationException(message);
        }
        Allocation allocation;
        if (type instanceof PrimitiveType) {
            PrimitiveType primitiveType = (PrimitiveType) type;
            allocation = new LocalVariableAllocation(nextLocalVariableIndexToAllocate, primitiveType);
            localVariableAllocationsList.add((LocalVariableAllocation) allocation);
            nextLocalVariableIndexToAllocate++;
        } else {
            HeapObjectReference nonPrimitiveType = (HeapObjectReference) type;
            allocation = new StackOffsetAllocation(nextStackOffsetToAllocate, nonPrimitiveType);
            nextStackOffsetToAllocate += 4;
        }
        variableAllocations.put(name, allocation);
    }

    /**
     * Registers the parameters of a method just like normal variables
     *
     * @param parameters The parameters to register
     */
    public void registerParameters(List<MethodParameter> parameters) throws MultipleVariableDeclarationException {
        for (MethodParameter parameter : parameters) {
            String name = parameter.getParameterName();
            Type type = parameter.getType();
            registerVariable(name, type);
        }
    }

    /**
     * Determines whether this scope has a mapping for a given variable name.
     *
     * @param name The name to look up
     * @return true if this scope has a mapping; false otherwise
     */
    public boolean hasMappingFor(String name) {
        if (variableAllocations.containsKey(name)) return true;
        if (containingScope != null) return containingScope.hasMappingFor(name);
        return false;
    }

    /**
     * Looks up an allocated variable from its name.
     *
     * @param name The variable name to look up
     * @return The Allocation of that variable
     */
    public Allocation getVariableWithName(String name) {
        if (variableAllocations.containsKey(name)) {
            return variableAllocations.get(name);
        } else if (containingScope != null) {
            return containingScope.getVariableWithName(name);
        } else {
            return null;
        }
    }

    /**
     * Notifies this scope that it has been popped from the scope stack, so that
     * it can update its containing scope about the allocations that have been
     * made.
     */
    public void notifyPopped() {
        if (containingScope != null) {
            containingScope.nextLocalVariableIndexToAllocate = this.nextLocalVariableIndexToAllocate;
            containingScope.nextStackOffsetToAllocate = this.nextStackOffsetToAllocate;
            containingScope.localVariableAllocationsList.addAll(this.localVariableAllocationsList);
        }
    }

    /**
     * Returns a list of all known allocated types.
     *
     * This includes allocations made by child scopes.
     *
     * @return A list of all known allocated types
     */
    public List<Type> getPrimitiveLocalVariableTypes() {
        return localVariableAllocationsList
                .stream()
                .map(LocalVariableAllocation::getType)
                .collect(Collectors.toList());
    }
}
