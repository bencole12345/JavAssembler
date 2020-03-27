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

    public static void linkedListInsertTraverse(int numValues) {
        LinkedList list = new LinkedList();
        for (int i = 0; i < numValues; i++) {
            list.append(i);
        }
        for (int i = 0; i < numValues; i++) {
            int result = list.popFirstElement();
        }
    }
}
