public class Arrays {

    public static int testArrayElementsStoredCorrectly() {
        Integer[] array = new Integer[3];
        array[0] = new Integer(0);
        array[1] = new Integer(-1);
        array[2] = new Integer(10);
        Integer firstElement = array[0];
        Integer secondElement = array[1];
        Integer thirdElement = array[2];
        return thirdElement.value;
    }

    public static boolean testPassArrayAsParameter() {
        Integer[] array = new Integer[1];
        Integer element = new Integer();
        element.value = 10;
        array[0] = element;
        Integer returnedElement = getFirstElement(array);
        return returnedElement.value == 10;
    }

    private static Integer getFirstElement(Integer[] array) {
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

}
