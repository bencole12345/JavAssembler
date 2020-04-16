package sample_programs_compiled;

public class JVMSumSquaresBenchmark {
    
    public static int sumSquares(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum = sum + i * i;
        }
        return sum;
    }

    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]);
        System.out.println(sumSquares(n));
    }

}
