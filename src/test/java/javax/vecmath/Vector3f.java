package javax.vecmath;

public class Vector3f {
    public float x;
    public float y;
    public float z;

    public Vector3f() {
        this(0.0F, 0.0F, 0.0F);
    }

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
