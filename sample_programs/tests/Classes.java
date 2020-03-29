public class Classes {

    public static ExampleClass createInstanceWithoutConstructor() {
        return new ExampleClass();
    }

    public static ExampleClass createInstanceUsingConstructor(int x) {
        return new ExampleClass(x);
    }

    public static void callSetX(ExampleClass exampleClass, int x) {
        exampleClass.setX(x);
    }

    public static int callGetX(ExampleClass exampleClass) {
        return exampleClass.getX();
    }

    public static void setXAttributeDirectly(ExampleClass exampleClass, int x) {
        exampleClass.x = x;
    }

    public static int lookupXAttributeDirectly(ExampleClass exampleClass) {
        return exampleClass.x;
    }

    public static void callSetY(ExampleClass exampleClass, int y) {
        exampleClass.setY(y);
    }

    public static int callGetY(ExampleClass exampleClass) {
        return exampleClass.getY();
    }

    public static void callIncrementX(ExampleClass exampleClass) {
        exampleClass.incrementX();
    }

    public static void callIncrementY(ExampleClass exampleClass) {
        exampleClass.incrementY();
    }

    public static ClassWith33Attributes createClassWith33Attributes() {
        return new ClassWith33Attributes();
    }

    public static void setX1(ClassWith33Attributes object, int value) {
        object.x1 = value;
    }

    public static void setX32(ClassWith33Attributes object, int value) {
        object.x32 = value;
    }

    public static void setX33(ClassWith33Attributes object, int value) {
        object.x33 = value;
    }

    public static int getX1(ClassWith33Attributes object) {
        return object.x1;
    }

    public static int getX32(ClassWith33Attributes object) {
        return object.x32;
    }

    public static int getX33(ClassWith33Attributes object) {
        return object.x33;
    }
}
