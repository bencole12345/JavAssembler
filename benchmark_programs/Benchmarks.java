public class Benchmarks {

    public static int sumSquares(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum = sum + i*i;
        }
        return sum;
    }

}