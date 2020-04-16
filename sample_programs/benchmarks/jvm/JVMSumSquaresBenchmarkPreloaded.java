
public class JVMSumSquaresBenchmarkPreloaded {

    public static long sumSquares(long n) {
        long sum = 0;
        for (long i = 1; i <= n; i++) {
            sum = sum + i * i;
        }
        return sum;
    }
    public static void main(String[] args) {
        sumSquares(1000000);
    }

}