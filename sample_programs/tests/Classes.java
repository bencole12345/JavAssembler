public class Classes {

    public static int testSetGetPublicAttributeDirectly(int value) {
        ExampleClass example = new ExampleClass();
        example.x = value;
        return example.x;
    }

    public static int testUseSetterGetterForPublicAttribute(int value) {
        ExampleClass example = new ExampleClass();
        example.setX(value);
        return example.getX();
    }

    public static int testUseSetterGetterForPrivateAttribute(int value) {
        ExampleClass example = new ExampleClass();
        example.setY(value);
        return example.getY();
    }

    public static int testSetPublicAttributeUsingConstructor(int value) {
        ExampleClass example = new ExampleClass(value);
        return example.x;
    }

    public static int testMutatePublicAttribute(int value) {
        ExampleClass example = new ExampleClass(value);
        example.incrementX();
        return example.x;
    }

    public static int testMutatePrivateAttribute(int value) {
        ExampleClass example = new ExampleClass();
        example.setY(value);
        example.incrementY();
        return example.getY();
    }

    public static boolean testMoreThan32Attributes() {
        ClassWith33Attributes bigClass = new ClassWith33Attributes();
        bigClass.x1 = 1;
        bigClass.x2 = 2;
        bigClass.x31 = 31;
        bigClass.x32 = 32;
        bigClass.x33 = 33;
        return bigClass.x1 == 1
                && bigClass.x2 == 2
                && bigClass.x31 == 31
                && bigClass.x32 == 32
                && bigClass.x33 == 33;
    }

    private static int getX(ExampleClass example) {
        return example.x;
    }

    public static boolean testPassAnonymousObjectAsArgument() {
        int result = getX(new ExampleClass(10));
        return result == 10;
    }
}
