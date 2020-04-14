public class Arrays {

    public static boolean testIntegerArrayElementsStoredCorrectly() {
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

    public static boolean testShortArrayElementsStoredCorrectly() {
        short[] array = new short[3];
        array[0] = 1s;
        array[1] = -1s;
        array[2] = 10s;
        short first = array[0];
        short second = array[1];
        short third = array[2];
        return first == 1s
                && second == -1s
                && third == 10s;
    }

    public static boolean testFloatArrayElementsStoredCorrectly() {
        float[] array = new float[3];
        array[0] = 1.0f;
        array[1] = -1.0f;
        array[2] = 10.0f;
        float first = array[0];
        float second = array[1];
        float third = array[2];
        return first == 1.0f
                && second == -1.0f
                && third == 10.0f;
    }

    public static boolean testDoubleArrayElementsStoredCorrectly() {
        double[] array = new double[3];
        array[0] = 1.0;
        array[1] = -1.0;
        array[2] = 10.0;
        double first = array[0];
        double second = array[1];
        double third = array[2];
        return first == 1.0
                && second == -1.0
                && third == 10.0;
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
