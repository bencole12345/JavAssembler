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

}