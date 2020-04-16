public class JVMRecursionBenchmark {

    public static void recurse(int depth) {
        if (depth > 0) {
            recurse(depth - 1);
        }
    }

    public static void main(String[] args) {
        int depth = Integer.parseInt(args[0]);
        recurse(depth);
    }

}
