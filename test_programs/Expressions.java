public class Expressions {

    public static int add(int a, int b) {
        return a + b;
    }

    public static int subtract(int a, int b) {
        return a - b;
    }

    public static int multiply(int a, int b) {
        return a * b;
    }

    public static int divide(int a, int b) {
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
