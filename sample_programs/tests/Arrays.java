public class Arrays {

    public static boolean testArrayElementsStoredCorrectly() {
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

    public static Integer makeInteger(int value) {
        return new Integer(value);
    }

    public static Integer[] makeIntegerArray(int length) {
        Integer[] array = new Integer[length];
        for (int i = 1; i <= length; i++) {
            array[i] = new Integer(i);
        }
        return array;
    }

    public static int readElementAtIndexInArrayOfSize(int index, int size) {
        Integer[] array = makeIntegerArray(size);
        Integer value = array[index];
        return value.value;
    }

}
