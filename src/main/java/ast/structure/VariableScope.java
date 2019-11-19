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

    /**
     * Denotes the nature of the variable.
     */
    public enum Domain {

        /**
         * The variable is a local variable.
         */
        Local,

        /**
         * The variable is a parameter to a method.
         */
        Parameter,

        /**
         * The variable is a static class attribute.
         */
        StaticClassAttribute
    }

    private VariableScope containingScope;
    private Map<String, Type> variableTypes;
    private Map<String, Integer> registerIndices;
    private Map<String, Domain> variableDomains;
    private int nextRegisterToAllocate;
    private int methodParamCount;

    public VariableScope() {
        variableTypes = new HashMap<>();
        registerIndices = new HashMap<>();
        variableDomains = new HashMap<>();
        nextRegisterToAllocate = 0;
        methodParamCount = 0;
    }

    public VariableScope(VariableScope containingScope) {
        this();
        this.containingScope = containingScope;
        nextRegisterToAllocate = containingScope.nextRegisterToAllocate;
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
     * Assigns a register to a variable in this scope.
     *
     * @param name The variable's name
     * @param type The type of the variable
     * @return The register index that was allocated
     */
    public int registerVariable(String name, Type type, Domain domain) {
        if (lookupVariableType(name) == null) {
            variableTypes.put(name, type);
            registerIndices.put(name, nextRegisterToAllocate);
            variableDomains.put(name, domain);
            if (domain == Domain.Parameter) {
                methodParamCount++;
            }
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
            Domain domain = lookupVariableDomain(name);
            int offset = (domain == Domain.Local) ? methodParamCount : 0;
            return registerIndices.get(name) + offset;
        } else if (containingScope != null) {
            return containingScope.lookupRegisterIndexOfVariable(name);
        } else {
            return null;
        }
    }

    /**
     * Looks up the domain of a variable.
     *
     * @param name The name of the variable to look up
     * @return The domain of the variable
     */
    public Domain lookupVariableDomain(String name) {
        if (variableDomains.containsKey(name)) {
            return variableDomains.get(name);
        } else if (containingScope != null) {
            return containingScope.lookupVariableDomain(name);
        } else {
            return null;
        }
    }
}
