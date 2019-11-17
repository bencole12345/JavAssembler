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

    // TODO: Generalise or extend to also cover class attributes

    private VariableScope containingScope;
    private Map<String, Type> variableTypes;
    private Map<String, Integer> registerIndices;
    private int nextRegisterToAllocate;

    public VariableScope() {
        variableTypes = new HashMap<>();
        registerIndices = new HashMap<>();
        nextRegisterToAllocate = 0;
    }

    public VariableScope(VariableScope containingScope) {
        this();
        this.containingScope = containingScope;
        nextRegisterToAllocate = containingScope.nextRegisterToAllocate;
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
            variableTypes.put(name, type);
            registerIndices.put(name, nextRegisterToAllocate);
        } else {
            // TODO: Report error in source file; this variable has already
            // been declared.
            throw new RuntimeException();
        }
        return ++nextRegisterToAllocate;
    }

    /**
     * Looks up the type of a variable from its name.
     *
     * @param name The name of the variable to look up
     * @return The Type of that variable, or null if not registered
     */
    public Type lookupVariableType(String name) {
        if (variableTypes.containsKey(name)) {
            return variableTypes.get(name);
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
        if (registerIndices.containsKey(name)) {
            return registerIndices.get(name);
        } else if (containingScope != null) {
            return containingScope.lookupRegisterIndexOfVariable(name);
        } else {
            return null;
        }
    }
}
