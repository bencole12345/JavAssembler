public class GarbageCollection {

    private static Integer allocateMemory(int value) {
        return new Integer(value);
    }

    public static boolean testGarbageCollection(int iterations) {
        boolean correct = true;
        for (int i = 0; i < iterations; i++) {
            Integer allocated = allocateMemory(i);
            correct = correct && allocated.value == i;
        }
        return correct;
    }

}
