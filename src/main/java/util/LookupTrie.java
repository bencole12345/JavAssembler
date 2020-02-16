package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides fast lookup of functions through a trie where each edge represents
 * a type in the function's arguments list.
 *
 * @param <ValueType> The type associated with each node
 * @param <EdgeType> The type associated with each edge
 */
public class LookupTrie<ValueType, EdgeType> {

    /**
     * The number of entries in the trie.
     */
    private int numEntries;

    /**
     * The root of the trie.
     */
    private TrieNode root;

    public LookupTrie() {
        numEntries = 0;
        root = new TrieNode();
    }

    /**
     * Inserts a value into the trie, in the position specified by the passed
     * list of edges.
     *
     * @param edges The sequence of edges to follow
     * @param value The value to insert
     * @return true if the insertion was successful; false if there is already
     *      an entry at this position
     */
    public boolean insert(List<EdgeType> edges, ValueType value) {

        // Start at the root
        TrieNode currentNode = root;

        // Walk down the tree
        boolean inserting = false;
        for (EdgeType edge : edges) {
            if (inserting) {
                TrieNode newNode = new TrieNode();
                currentNode.outwardEdges.put(edge, newNode);
                currentNode = newNode;
            } else {
                if (currentNode.outwardEdges.containsKey(edge)) {
                    currentNode = currentNode.outwardEdges.get(edge);
                } else {
                    inserting = true;
                    // TODO: Figure out whether the next 3 lines can be deleted
                    TrieNode newNode = new TrieNode();
                    currentNode.outwardEdges.put(edge, newNode);
                    currentNode = newNode;
                }
            }
        }

        // We have now arrived at the node in the correct place. If there's no
        // other entry here then insert the requested value and return true to
        // indicate that the insertion was successful. If there is something
        // else there then don't remove it, just return false to indicate that
        // insertion was unsuccessful.
        if (currentNode.value == null) {
            currentNode.value = value;
            numEntries++;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Looks up the value at a particular position in the trie.
     *
     * @param edges The path to follow
     * @return The value that was found, or null if no such value exists
     */
    public ValueType lookup(List<EdgeType> edges) {

        // Start at the root
        TrieNode currentNode = root;

        // Walk down the tree
        for (EdgeType edge : edges) {
            if (currentNode == null)
                break;
            currentNode = currentNode.outwardEdges.getOrDefault(edge, null);
        }

        // Return the value found, or null if it doesn't exist
        if (currentNode != null && currentNode.value != null)
            return currentNode.value;
        else
            return null;
    }

    public LookupTrie<ValueType, EdgeType> merge(LookupTrie<ValueType, EdgeType> other) {
        // TODO: Implement
        return null;
    }

    public List<ValueType> getSerialList() {
        // TODO: Return a tree traversal in any order
        return null;
    }

    /**
     * Returns the number of entries in the trie.
     *
     * @return The number of entries in the trie
     */
    public int getCount() {
        return numEntries;
    }

    /**
     * Represents a single node in the trie.
     *
     * The node is a leaf iff value == null.
     */
    private class TrieNode {

        ValueType value;
        Map<EdgeType, TrieNode> outwardEdges;

        public TrieNode() {
            value = null;
            outwardEdges = new HashMap<>();
        }

    }

}