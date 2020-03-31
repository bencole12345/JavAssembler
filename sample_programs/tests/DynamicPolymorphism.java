public class DynamicPolymorphism {

    public static boolean callIsParentFromParentContext(Parent parent) {
        return parent.isParent();
    }

    public static boolean callIsParentFromChildContext(Child child) {
        return child.isParent();
    }

    public static boolean testParentInParentContext() {
        Parent parent = new Parent();
        return callIsParentFromParentContext(parent);
    }

    public static boolean testChildInChildContext() {
        Child child = new Child();
        return callIsParentFromChildContext(child);
    }

    public static boolean testChildInParentContext() {
        Parent child = new Child();
        return callIsParentFromParentContext(child);
    }

}
