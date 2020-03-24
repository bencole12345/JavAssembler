public class ParentClass {
    public int x;
    public double y;
    public float z;

    public ParentClass(int x, double y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getNumber() {
        return 10;
    }

    public void incrementX() {
        this.x = this.x + 1;
    }
}
