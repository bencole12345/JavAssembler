package util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LookupTreeTest {

    @Test
    void emptyLookupTree() {
        LookupTree<Void, Void> lookupTree = new LookupTree<>();
        assertEquals(0, lookupTree.getCount());
    }

    @Test
    void lookupTreeZeroPathLength() {
        LookupTree<Integer, Integer> lookupTree = new LookupTree<>();
        lookupTree.insert(Collections.emptyList(), 10);
        assertEquals(1, lookupTree.getCount());
        int value = lookupTree.lookup(Collections.emptyList());
        assertEquals(10, value);
    }

    @Test
    void lookupTreeOnePathLength() {
        LookupTree<Integer, Integer> lookupTree = new LookupTree<>();
        lookupTree.insert(Collections.singletonList(1), 10);
        assertEquals(1, lookupTree.getCount());
        int value = lookupTree.lookup(Collections.singletonList(1));
        assertEquals(10, value);
    }

    @Test
    void lookupTreeSeveralPathLength() {
        LookupTree<Integer, Integer> lookupTree = new LookupTree<>();
        lookupTree.insert(Arrays.asList(1, 2, 3), 10);
        assertEquals(1, lookupTree.getCount());
        int value = lookupTree.lookup(Arrays.asList(1, 2, 3));
        assertEquals(10, value);
    }

    @Test
    void lookupTreeBadPath() {
        LookupTree<Integer, Integer> lookupTree = new LookupTree<>();
        lookupTree.insert(Collections.singletonList(1), 10);
        assertEquals(1, lookupTree.getCount());
        Integer value = lookupTree.lookup(Collections.singletonList(2));
        assertNull(value);
    }

}