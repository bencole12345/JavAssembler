public class NullTest {

    public int lookupValueOnNullObject() {
        Integer object = null;
        return object.value;
    }

    public int passNullObjectAsArgument() {
        Integer object = null;
        return lookupValueOnInteger(object);
    }

    public int lookupValueOnInteger(Integer input) {
        return input.value;
    }

    public boolean testNullObjectIsNull() {
        Integer integer = null;
        return integer == null;
    }

    public boolean testNonNullObjectIsNull() {
        Integer integer = new Integer(10);
        return integer == null;
    }

    public boolean testNullOnNullArgument() {
        Integer argument = null;
        return inputIsNull(argument);
    }

    public boolean testNullOnNonNullArgument() {
        Integer argument = new Integer(0);
        return inputIsNull(argument);
    }

    public boolean inputIsNull(Integer input) {
        return input == null;
    }

}
