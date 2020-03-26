public class Benchmarks {

    public static int sumSquares(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum = sum + i*i;
        }
        return sum;
    }

    public static void recurse(int depth) {
        if (depth > 0) {
            recurse(depth - 1);
        }
    }
}