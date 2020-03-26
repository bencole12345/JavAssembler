public class LanguageConstructs {

    public static boolean ifThen(boolean condition) {
        boolean wasTaken = false;
        if (condition) {
            wasTaken = true;
        }
        return wasTaken;
    }

    public static boolean ifThenElse(boolean condition) {
        boolean wasTaken;
        if (condition) {
            wasTaken = true;
        } else {
            wasTaken = false;
        }
        return wasTaken;
    }

    public static int chainedIfStatements(boolean condition1, boolean condition2) {
        int pathTaken;
        if (condition1) {
            pathTaken = 1;
        } else if (condition2) {
            pathTaken = 2;
        } else {
            pathTaken = 3;
        }
        return pathTaken;
    }

    public static int sumToWhile(int n) {
        int sum = 0;
        int i = 1;
        while (i <= n) {
            sum = sum + i;
            i++;
        }
        return sum;
    }

    public static int sumToFor(int n) {
        int sum = 0;
        for (int i = 1; i <= n; i++) {
            sum = sum + i;
        }
        return sum;
    }

    public static int countForIterations(int n) {
        int iterations = 0;
        for (int i = 0; i < n; i++) {
            iterations++;
        }
        return iterations;
    }

}