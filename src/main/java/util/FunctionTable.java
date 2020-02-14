package util;

import ast.types.AccessModifier;
import ast.types.JavaClass;
import ast.types.Type;
import errors.DuplicateFunctionSignatureException;
import errors.InvalidClassNameException;
import errors.UndeclaredFunctionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionTable {

    /**
     * The list of functions.
     */
    private List<FunctionTableEntry> functions;

    /**
     * Contains the map from
     *      class -> function name ->
     *      (parameter types list -> function table entry) trie
     */
    private Map<JavaClass, Map<String, LookupTrie<FunctionTableEntry, Type>>> classesToFunctionsMap;

    /**
     * Tracks the next index in the table that we are free to assign.
     */
    private int nextIndexToAssign;

    /**
     * Counts how many functions have been registered with a given name.
     *
     * This is used to determine whether or not to perform name mangling.
     */
    private Map<String, Integer> functionsWithNameCount;

    public FunctionTable() {
        functions = new ArrayList<>();
        classesToFunctionsMap = new HashMap<>();
        functionsWithNameCount = new HashMap<>();
        nextIndexToAssign = 0;
    }

    /**
     * Looks up the entry at a given index.
     *
     * @param index The index to look up
     * @return The function table entry at that index
     */
    public FunctionTableEntry getEntry(int index) {
        if (index < functions.size())
            return functions.get(index);
        else
            return null;
    }

    /**
     * Adds a function to the function table.
     *
     * @param containingClass The class in which this function is defined
     * @param functionName The name of the function.
     * @param parameterTypes The types of the function's parameters
     * @param returnType The return type of the function
     * @param isStatic Whether this is a static function
     * @param accessModifier The access modifier applied to this function
     * @return The function table entry that was created
     * @throws DuplicateFunctionSignatureException if a function with this signature has already
     *      been declared.
     */
    public FunctionTableEntry registerFunction(JavaClass containingClass,
                                               String functionName,
                                               List<Type> parameterTypes,
                                               Type returnType,
                                               boolean isStatic,
                                               AccessModifier accessModifier)
            throws DuplicateFunctionSignatureException {

        // Create a function table entry for the new function
        FunctionTableEntry functionTableEntry = new FunctionTableEntry(
                nextIndexToAssign++,
                containingClass,
                functionName,
                returnType,
                isStatic,
                parameterTypes,
                accessModifier);

        // Look up or generate an entry for this class
        Map<String, LookupTrie<FunctionTableEntry, Type>> functionNameMap;
        if (classesToFunctionsMap.containsKey(containingClass)) {
            functionNameMap = classesToFunctionsMap.get(containingClass);
        } else {
            functionNameMap = new HashMap<>();
            classesToFunctionsMap.put(containingClass, functionNameMap);
        }

        // Look up or generate a trie for this function name
        LookupTrie<FunctionTableEntry, Type> trie;
        if (functionNameMap.containsKey(functionName)) {
            trie = functionNameMap.get(functionName);
        } else {
            trie = new LookupTrie<>();
            functionNameMap.put(functionName, trie);
        }

        // Attempt to insert the function into the trie
        boolean success = trie.insert(parameterTypes, functionTableEntry);

        // If the insertion was successful then we need to increment the
        // count of functions with this name. If it was unsuccessful then
        // we need to throw an exception to indicate that there are multiple
        // entries with the same function signature.
        if (success) {
            int countWithName = functionsWithNameCount.getOrDefault(functionName, 0);
            functionsWithNameCount.put(functionName, countWithName + 1);
        } else {
            String message = "Duplicate functions with signature "
                    + functionSignatureToString(functionName, parameterTypes);
            throw new DuplicateFunctionSignatureException(message);
        }

        // If the method is static then we also want to register it with the
        // relevant class so that it can be included in that class's virtual
        // table.
        if (!isStatic)
            containingClass.registerNewMethod(functionTableEntry);

        return functionTableEntry;
    }

    /**
     * Looks up a function from the registry.
     *
     * @param functionName The function to look up
     * @param parameterTypes The types of the function's parameters
     * @return The index of the function in the function table
     * @throws UndeclaredFunctionException
     */
    public FunctionTableEntry lookupFunction(JavaClass containingClass,
                                             String functionName,
                                             List<Type> parameterTypes)
            throws UndeclaredFunctionException, InvalidClassNameException {

        // Look up the map for the correct class
        Map<String, LookupTrie<FunctionTableEntry, Type>> functionNameMap =
                classesToFunctionsMap.getOrDefault(containingClass, null);
        if (functionNameMap == null) {
            String message = "Invalid class name " + containingClass;
            throw new InvalidClassNameException(message);
        }

        // Look up the trie for the name within that class
        LookupTrie<FunctionTableEntry, Type> trie =
                functionNameMap.getOrDefault(functionName, null);

        // Throw an exception if there's no entry with that function name
        String errorMessage = "No function defined in class " + containingClass
                + " with signature " + functionSignatureToString(functionName, parameterTypes);
        if (trie == null)
            throw new UndeclaredFunctionException(errorMessage);

        // Call the lookup method on that trie
        FunctionTableEntry entry = trie.lookup(parameterTypes);

        // Throw an exception if there's no entry with those types as parameters
        if (entry == null)
            throw new UndeclaredFunctionException(errorMessage);

        return entry;
    }

    /**
     * Returns the number of functions registered with a given name.
     *
     * @param name The name to look up
     * @return The number of functions registered with that name
     */
    public int getNumberOfFunctionsWithName(String name) {
        return functionsWithNameCount.getOrDefault(name, 0);
    }

    private String functionSignatureToString(String name, List<Type> parameterTypes) {
        List<String> typeNames = parameterTypes.stream()
                .map(Object::toString)
                .collect(Collectors.toList());
        return name + "(" + String.join(", ", typeNames) + ")";
    }

    /**
     * Represents a node in the tree of functions.
     *
     * The purpose of this is to perform method resolution when there are
     * overloaded functions. Example:
     *
     * Functions: f(), f(int), f(int, int), f(int, float)
     *
     *                  root [0]
     *                   | int
     *                   . [1]
     *              int / \ float
     *             [2] .   . [3]
     *
     */
    private static class FunctionLookupTreeNode {

        public FunctionTableEntry value;
        public Map<Type, FunctionLookupTreeNode> edges;

        public FunctionLookupTreeNode() {
            value = null;
            edges = new HashMap<>();
        }
    }
}
