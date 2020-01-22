package ast.functions;

import ast.types.Type;
import errors.DuplicateFunctionSignatureException;
import errors.InvalidClassNameException;
import errors.UndeclaredFunctionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctionTable {

    /**
     * Contains the map from
     *      namespace -> function name -> top of the type trie
     */
    private Map<String, Map<String, FunctionLookupTreeNode>> namespaceMap;

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
        namespaceMap = new HashMap<>();
        functionsWithNameCount = new HashMap<>();
        nextIndexToAssign = 0;
    }

    /**
     * Adds a function to the function table.
     *
     * @param functionName The name of the function.
     * @param parameterTypes The types of the function's parameters
     * @return The function table entry that was created
     * @throws DuplicateFunctionSignatureException if a function with this signature has already
     *      been declared.
     */
    public FunctionTableEntry registerFunction(String namespace,
                                               String functionName,
                                               List<Type> parameterTypes,
                                               Type returnType)
            throws DuplicateFunctionSignatureException {

        FunctionLookupTreeNode node;

        // Identify which tree to walk down, creating it if this is the first
        // function entry with this name

        // First lookup the map for this namespace
        Map<String, FunctionLookupTreeNode> nameMap;
        if (namespaceMap.containsKey(namespace)) {
             nameMap = namespaceMap.get(namespace);
        } else {
            nameMap = new HashMap<>();
            namespaceMap.put(namespace, nameMap);
        }

        // Now look up the trie for the function in this namespace
        if (nameMap.containsKey(functionName)) {
            node = nameMap.get(functionName);
        } else {
            node = new FunctionLookupTreeNode();
            nameMap.put(functionName, node);
        }

        // Walk down the tree
        boolean inserting = false;
        for (Type type : parameterTypes) {
            if (inserting) {
                FunctionLookupTreeNode newNode = new FunctionLookupTreeNode();
                node.edges.put(type, newNode);
                node = newNode;
            } else {
                if (node.edges.containsKey(type)) {
                    node = node.edges.get(type);
                } else {
                    inserting = true;
                    FunctionLookupTreeNode newNode = new FunctionLookupTreeNode();
                    node.edges.put(type, newNode);
                    node = newNode;
                }
            }
        }

        if (node.value != null) {
            String message = "Duplicate functions with signature "
                    + functionSignatureToString(functionName, parameterTypes);
            throw new DuplicateFunctionSignatureException(message);
        }

        // Make a new function table entry and point the current node to it
        int assignedIndex = nextIndexToAssign++;
        FunctionTableEntry newEntry = new FunctionTableEntry(assignedIndex, namespace, functionName, returnType);
        node.value = newEntry;

        // Increment the counter for the number of functions with this name
        int countWithThisName = functionsWithNameCount.getOrDefault(functionName, 0);
        functionsWithNameCount.put(functionName, countWithThisName + 1);

        return newEntry;
    }

    /**
     * Looks up a function from the registry.
     *
     * @param name The function to look up
     * @param parameterTypes The types of the function's parameters
     * @return The index of the function in the function table
     * @throws UndeclaredFunctionException
     */
    public FunctionTableEntry lookupFunction(String namespace,
                                             String name,
                                             List<Type> parameterTypes)
            throws UndeclaredFunctionException, InvalidClassNameException {

        // First look up the map for the requested namespace
        Map<String, FunctionLookupTreeNode> nameMap = namespaceMap.getOrDefault(namespace, null);
        if (nameMap == null) {
            String message = "Invalid class name " + namespace;
            throw new InvalidClassNameException(message);
        }

        // Now look up the trie for the function within this namespace
        FunctionLookupTreeNode node = nameMap.getOrDefault(name, null);
        if (node == null) {
            String message = "No function defined with signature "
                    + functionSignatureToString(name, parameterTypes);
            throw new UndeclaredFunctionException(message);
        }

        // Now walk down the tree
        for (Type type : parameterTypes) {
            node = node.edges.getOrDefault(type, null);
            if (node == null)
                break;
        }

        // Check whether we have arrived at a node with a value
        if (node == null || node.value == null) {
            String message = "No function defined with signature "
                    + functionSignatureToString(name, parameterTypes);
            throw new UndeclaredFunctionException(message);
        } else {
            return node.value;
        }
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
