public class Debugging {
    public static Integer makeInteger(int value) {
        return new Integer(value);
    }
    public static Integer[] makeArray() {
        Integer[] array = new Integer[3];
        return array;
    }
    public static Integer[] makeAndSetUpArray() {
        Integer[] array = new Integer[3];
        array[0] = new Integer(100);
        array[1] = new Integer(101);
        array[2] = new Integer(102);
        return array;
    }
    public static void setTwoLocals() {
        Integer one = new Integer(1);
        Integer two = new Integer(2);
    }
    public static void setLocalAndCallSetTwoLocals() {
        Integer one = new Integer(1);
        setTwoLocals();
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
