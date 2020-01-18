package ast.structure;

import ast.types.Type;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Wraps the data stored about a variable allocation.
     */
    private static class RegisterAllocation {

        private int registerIndex;
        private Type type;

        public RegisterAllocation(int registerIndex, Type type) {
            this.registerIndex = registerIndex;
            this.type = type;
        }

        public int getRegisterIndex() {
            return registerIndex;
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
     * The map from variable names to register allocations
     */
    private Map<String, RegisterAllocation> allocations;

    /**
     * Tracks which register index we can allocate next for
     * a local variable
     */
    private int nextAllocation;

    public VariableScope() {
        allocations = new HashMap<>();
        nextAllocation = 0;
        containingScope = null;
    }

    public VariableScope(VariableScope containingScope) {
        this();
        this.containingScope = containingScope;
        nextAllocation = containingScope.nextAllocation;
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
     * Assigns a register to a variable in this scope.
     *
     * @param name The variable's name
     * @param type The type of the variable
     * @return The register index that was allocated
     */
    public int registerVariable(String name, Type type) {
        if (lookupVariableType(name) == null) {
            RegisterAllocation allocation = new RegisterAllocation(nextAllocation, type);
            allocations.put(name, allocation);
        } else {
            // TODO: Report error in source file; this variable has already
            // been declared.
            throw new RuntimeException();
        }
        return nextAllocation++;
    }

    /**
     * Looks up the type of a variable from its name.
     *
     * @param name The name of the variable to look up
     * @return The Type of that variable, or null if not registered
     */
    public Type lookupVariableType(String name) {
        if (allocations.containsKey(name)) {
            return allocations.get(name).getType();
        } else if (containingScope != null) {
            return containingScope.lookupVariableType(name);
        } else {
            return null;
        }
    }

    /**
     * Looks up the register index that was assigned to a variable.
     *
     * @param name The name of the variable to look up
     * @return The index of the register that was assigned, or null if not
     *      registered
     */
    public Integer lookupRegisterIndexOfVariable(String name) {
        if (allocations.containsKey(name)) {
            return allocations.get(name).getRegisterIndex();
        } else if (containingScope != null) {
            return containingScope.lookupRegisterIndexOfVariable(name);
        } else {
            return null;
        }
    }
}
