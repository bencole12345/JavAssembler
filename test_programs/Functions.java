public class Functions {

    public static int callAnotherFunction() {
        return anotherFunction();
    }

    private static int anotherFunction() {
        return 1;
    }

    public static int callPublicExternalFunction() {
        return FunctionsExternal.publicExternalFunction();
    }

    public static int callAddOneFunction(int x) {
        return addOne(x);
    }

    private static int addOne(int x) {
        return x + 1;
    }

}