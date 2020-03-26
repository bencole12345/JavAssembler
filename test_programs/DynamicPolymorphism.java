public class DynamicPolymorphism {

    public static Parent createParentInstance() {
        return new Parent();
    }

    public static Child createChildInstance() {
        return new Child();
    }

    public static boolean callIsParentFromParentContext(Parent parent) {
        return parent.isParent();
    }

    public static boolean callIsParentFromChildContext(Child child) {
        return child.isParent();
    }

}