public class Arrays {

    public static Integer[] makeArray(int size) {
        Integer[] array = new Integer[size];
        for (int i = 0; i < size; i++) {
            Integer element = new Integer();
            element.value = 0;
            array[i] = element;
        }
        return array;
    }

    public static void setElement(Integer[] array, int index, int value) {
        Integer element = array[index];
        element.value = value;
    }

    public static int getElement(Integer[] array, int index) {
        Integer element = array[index];
        return element.value;
    }

    public static boolean passArrayAsParameter() {
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

}
