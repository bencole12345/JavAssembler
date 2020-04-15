package util;


import errors.CircularInheritanceException;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TopologicalSortTest {

    @Test
    void topologicalSortEmptyGraph() throws CircularInheritanceException {
        Set<Integer> nodes = new HashSet<>();
        Map<Integer, Set<Integer>> edges = new HashMap<>();
        List<Integer> sorted = TopologicalSort.getSerialOrder(nodes, edges);
        assertEquals(Collections.emptyList(), sorted);
    }

    @Test
    void topologicalSortSingleNode() throws CircularInheritanceException {
        Set<Integer> nodes = new HashSet<>();
        nodes.add(0);
        Map<Integer, Set<Integer>> edges = new HashMap<>();
        List<Integer> sorted = TopologicalSort.getSerialOrder(nodes, edges);
        assertEquals(Collections.singletonList(0), sorted);
    }

    @Test
    void topologicalSortTwoNodes() throws CircularInheritanceException {
        Set<Integer> nodes = new HashSet<>(Arrays.asList(0, 1));
        Map<Integer, Set<Integer>> edges = new HashMap<>();
        edges.put(0, new HashSet<>(Collections.singletonList(1)));
        List<Integer> sorted = TopologicalSort.getSerialOrder(nodes, edges);
        assertEquals(Arrays.asList(0, 1), sorted);
    }

    @Test
    void topologicalSortThreeNodes() throws CircularInheritanceException {
        Set<Integer> nodes = new HashSet<>(Arrays.asList(0, 1, 2));
        Map<Integer, Set<Integer>> edges = new HashMap<>();
        edges.put(0, new HashSet<>(Collections.singletonList(1)));
        edges.put(2, new HashSet<>(Collections.singletonList(0)));
        List<Integer> sorted = TopologicalSort.getSerialOrder(nodes, edges);
        assertEquals(Arrays.asList(2, 0, 1), sorted);
    }

    @Test
    void topologicalSortDirectCircularDependency() {
        Set<Integer> nodes = new HashSet<>(Collections.singletonList(0));
        Map<Integer, Set<Integer>> edges = new HashMap<>();
        edges.put(0, new HashSet<>(Collections.singletonList(0)));
        assertThrows(CircularInheritanceException.class, () -> {
            TopologicalSort.getSerialOrder(nodes, edges);
        });
    }

    @Test
    void topologicalSortIndirectCircularDependency() {
        Set<Integer> nodes = new HashSet<>(Arrays.asList(0, 1));
        Map<Integer, Set<Integer>> edges = new HashMap<>();
        edges.put(0, new HashSet<>(Collections.singletonList(1)));
        edges.put(1, new HashSet<>(Collections.singletonList(0)));
        assertThrows(CircularInheritanceException.class, () -> {
            TopologicalSort.getSerialOrder(nodes, edges);
        });
    }
}