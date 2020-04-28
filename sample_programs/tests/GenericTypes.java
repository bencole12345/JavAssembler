public class GenericTypes {

    public static boolean testGenericContainerWithInteger() {
        Integer value = new Integer(10);
        GenericContainer<Integer> container = new GenericContainer<Integer>(value);
        Integer obtained = container.getValue();
        return obtained.value == 10;
    }

    public static boolean testGenericContainerWithExampleClass() {
        ExampleClass exampleClass = new ExampleClass(1);
        exampleClass.setY(2);
        GenericContainer<ExampleClass> container = new GenericContainer<ExampleClass>(exampleClass);
        ExampleClass extracted = container.getValue();
        return extracted.getX() == 1
                && extracted.getY() == 2;
    }

}
