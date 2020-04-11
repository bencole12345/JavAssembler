public class TypeRanges {

    public static byte addBytes(byte a, byte b) {
        return a + b;
    }

    public static byte subtractBytes(byte a, byte b) {
        return a - b;
    }

    public static char addChars(char a, char b) {
        return a + b;
    }

    public static char subtractChars(char a, char b) {
        return a - b;
    }

    public static short addShorts(short a, short b) {
        return a + b;
    }

    public static short subtractShorts(short a, short b) {
        return a - b;
    }

    public static int addInts(int a, int b) {
        return a + b;
    }

    public static int subtractInts(int a, int b) {
        return a - b;
    }

    public static boolean testLongOverflow() {
        long sum = 9223372036854775807l + 1l;
        return sum == -9223372036854775808l;
    }

    public static boolean testLongUnderflow() {
        long difference = -9223372036854775808l - 1l;
        return difference == 9223372036854775807l;
    }

}
