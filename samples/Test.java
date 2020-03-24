public class Test {

    public static int sumSquaresWhile(int n) {
        int sum = 0;
        int i = 1;
        while (i <= n) {
            sum += i*i;
            i++;
        }
        return sum;
    }

    public static int sumSquaresForLoop(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum += i*i;
        }
        return sum;
    }

    public static boolean isAbove100IfStatement(int x) {
        boolean result;
        if (x > 100) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    public static boolean isAbove100BooleanExpression(int x) {
        return (x > 100);
    }

    public static boolean testExpressionParsing() {
        return 1+2*3 == 7;
    }

    public static float callOtherFunction() {
        return aPrivateFunction();
    }
    private static float aPrivateFunction() {
        return 10.0f;
    }

    public static int callFunctionFromOtherClass() {
        return AnotherClass.get42FromAnotherClass();
    }

    public static int dynamicallyAllocateMemory() {
        ChildClass child = new ChildClass();
        child.w = 12;
        return readW(child);
    }

    public static int readW(ChildClass child) {
        return child.w;
    }

    public static int callMethod() {
        ParentClass childClass = new ChildClass();
        return childClass.getNumber();
    }

    public static int testThis() {
        ParentClass parent = new ParentClass();
        parent.x = 1;
        parent.incrementX();
        return parent.x;
    }

    public static boolean testArrays() {
        Integer[] array = new Integer[5];
        Integer firstElement = new Integer();
        firstElement.value = 5;
        array[1] = firstElement;
        Integer readFromArray = array[1];
        return readFromArray.value == 5;
    }

    public static boolean testNestedArrays() {
        Integer[][] nestedList = new Integer[2][2];
        Integer element = new Integer();
        element.value = 5;
        nestedList[1][0] = element;
        Integer extracted = nestedList[1][0];
        return extracted.value == 5;
    }

    public static boolean testPassArrayReference() {
        Integer[] myList = new Integer[5];
        Integer first = new Integer();
        first.value = 10;
        myList[0] = first;
        Integer fromFunction = getFirstElement(myList);
        return fromFunction.value == 10;
    }

    private static Integer getFirstElement(Integer[] list) {
        return list[0];
    }

    public boolean testConstructor() {
        ParentClass parent = new ParentClass(1, 2.0, 3.0f);
        if (parent.x == 1) {
            if (parent.y == 2.0) {
                if (parent.z == 3.0f) {
                    return true;
                }
            }
        }
        return false;
    }

}
