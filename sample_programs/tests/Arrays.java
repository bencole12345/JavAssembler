public class Arrays {

    public static boolean testPrimitiveArrayElementsStoredCorrectly() {
        int[] array = new int[3];
        array[0] = 1;
        array[1] = -1;
        array[2] = 10;
        int first = array[0];
        int second = array[1];
        int third = array[2];
        return first == 1
                && second == -1
                && third == 10;
    }

    public static boolean testNonPrimitiveArrayElementsStoredCorrectly() {
        Integer[] array = new Integer[3];
        array[0] = new Integer(1);
        array[1] = new Integer(-1);
        array[2] = new Integer(10);
        Integer firstElement = array[0];
        Integer secondElement = array[1];
        Integer thirdElement = array[2];
        return firstElement.value == 1
                && secondElement.value == -1
                && thirdElement.value == 10;
    }

    public static boolean testPassArrayAsParameter() {
        Integer[] array = new Integer[1];
        Integer element = new Integer();
        element.value = 10;
        array[0] = element;
        Integer returned = getFirstElement(array);
        return returned.value == 10;
    }

    public static Integer getFirstElement(Integer[] array) {
        return array[0];
    }

    public static Integer[] makeIntegerArray(int length) {
        Integer[] array = new Integer[length];
        for (int i = 0; i < length; i++) {
            array[i] = new Integer(i);
        }
        return array;
    }

    public static int readElementAtIndexInArrayOfSize(int index, int size) {
        Integer[] array = makeIntegerArray(size);
        Integer value = array[index];
        return value.value;
    }

    public static void writeElementAtIndexInArrayOfSize(int index, int size, int value) {
        Integer[] array = new Integer[size];
        Integer toInsert = new Integer(value);
        array[index] = toInsert;
    }

}
