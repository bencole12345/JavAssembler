package util;

import errors.CircularInheritanceException;

import java.util.*;

public class TopologicalSort {

    /**
     * Finds a serial ordering of a dependency graph. If there is an edge
     * a -> b, then b is guaranteed to appear before a.
     *
     * This method is an implementation of the topological sort algorithm.
     *
     * @param nodes The set of nodes
     * @param edges The set of dependencies
     * @param <T> The type of each node
     * @return A serial list consistent with the dependency graph
     * @throws CircularInheritanceException if there is a cycle in the
     *         dependency graph
     */
    public static <T> List<T> getSerialOrder(Set<T> nodes, Map<T, Set<T>> edges)
            throws CircularInheritanceException {

        if (nodes.isEmpty())
            return new LinkedList<>();

        List<T> output = new LinkedList<>();
        Set<T> inStack = new HashSet<>();
        Set<T> done = new HashSet<>();

        for (T node : nodes) {
            if (inStack.contains(node) || done.contains(node))
                continue;
            visit(node, edges, inStack, done, output);
        }

        return output;
    }

    private static <T> void visit(T node,
                                  Map<T, Set<T>> edges,
                                  Set<T> inStack,
                                  Set<T> done,
                                  List<T> output) throws CircularInheritanceException {

        // Ignore this node if we have already visited it
        if (done.contains(node))
            return;

        // If it's currently in the stack then it means there is a cycle in the
        // inheritance graph.
        if (inStack.contains(node)) {
            String message = "Circular inheritance detected for class "
                    + node.toString();
            throw new CircularInheritanceException(message);
        }

        // Visit all descendents first
        inStack.add(node);
        for (T nextNode : edges.get(node))
            visit(nextNode, edges, inStack, done, output);
        inStack.remove(node);
        done.add(node);

        // The list now definitely contains any dependents of this node, so we
        // can safely add this node to the beginning of the list.
        output.add(0, node);
    }

}
