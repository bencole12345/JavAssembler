public class JVMArrayBenchmark {

    public static void traverseArray(int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        for (int i = 0; i < size; i++) {
            int temp = array[i];
        }
    }

    public static void main(String[] args) {
        int size = Integer.parseInt(args[0]);
        traverseArray(size);
    }

}