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

    public static boolean testRequestingAdditionalMemory(int size) {
        boolean correct = true;
        Integer[] array = new Integer[size];
        for (int i = 0; i < size; i++) {
            Integer newInteger = new Integer(i);
            array[i] = newInteger;
        }
        for (int i = 0; i < size; i++) {
            Integer thisIndex = array[i];
            correct = correct && (thisIndex.value == i);
        }
        return correct;
    }

}
