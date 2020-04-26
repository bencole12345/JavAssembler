public class Expressions {

    public static int integerAdd(int a, int b) {
        return a + b;
    }

    public static int integerSubtract(int a, int b) {
        return a - b;
    }

    public static int integerMultiply(int a, int b) {
        return a * b;
    }

    public static int integerDivide(int a, int b) {
        return a / b;
    }

    public static float floatingPointAdd(float a, float b) {
        return a + b;
    }

    public static float floatingPointSubtract(float a, float b) {
        return a - b;
    }

    public static float floatingPointMultiply(float a, float b) {
        return a * b;
    }

    public static float floatingPointDivide(float a, float b) {
        return a / b;
    }

    public static boolean and(boolean a, boolean b) {
        return a && b;
    }

    public static boolean or(boolean a, boolean b) {
        return a || b;
    }

    public static boolean not(boolean a) {
        return !a;
    }

    public static int arithmeticParsingOrder() {
        return 1 + 2 * 3;
    }

    public static boolean booleanLogicParsingOrder1() {
        return true && false || false && true;
    }

    public static boolean booleanLogicParsingOrder2() {
        return true && true || false && false;
    }

}
