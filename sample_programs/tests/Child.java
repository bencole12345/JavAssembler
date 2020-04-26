public class Child extends Parent {

    public boolean isParent() {
        return false;
    }

    public void setX(int value) {
        this.x = value;
    }

    public int getX() {
        return this.x;
    }

    public void setY(int value) {
        this.y = value;
    }

    public int getY() {
        return this.y;
    }

    public static boolean testAccessingInheritedPublicAttribute() {
        Child child = new Child();
        child.setX(10);
        return child.getX() == 10;
    }

    public static boolean testAccessingInheritedPrivateAttribute() {
        Child child = new Child();
        child.setY(20);
        return child.getY() == 20;
    }

}