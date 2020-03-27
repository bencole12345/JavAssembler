public class ExampleClass {

    public int x;
    private int y;

    public ExampleClass(int x) {
        this.x = x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void incrementX() {
        this.x = this.x + 1;
    }

    public void incrementY() {
        this.y = this.y + 1;
    }
}
