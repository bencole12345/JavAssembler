public class Demo {

    public static void main() {
        int x;
        x = 12;
        int y;
        y = x + 1;
        int z = addTwelve(y);
    }

    private static int addTwelve(int x) {
        return x + 12;
    }

    public static double doSomething(int a, double b) {
        double start = 1.0;
        double soFar = start;
        for (int i = 0; i < a; i++) {
            soFar += b * 12.0;
        }
        return soFar;
    }

    public static int testIfAndWhile(boolean conditionA, boolean conditionB) {
        int a;
        if (conditionA) {
            a = 1;
        } else if (conditionB) {
            a = 2;
        } else {
            a = 0;
            int i = 0;
            while (i < 10) {
                a *= 10;
                i++;
            }
        }
        return a;
    }
}
