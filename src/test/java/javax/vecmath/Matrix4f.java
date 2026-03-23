package javax.vecmath;

public class Matrix4f {
    private float m00;
    private float m01;
    private float m02;
    private float m03;
    private float m10;
    private float m11;
    private float m12;
    private float m13;
    private float m20;
    private float m21;
    private float m22;
    private float m23;

    public Matrix4f() {
        setIdentity();
    }

    public void setIdentity() {
        m00 = 1.0F;
        m01 = 0.0F;
        m02 = 0.0F;
        m03 = 0.0F;
        m10 = 0.0F;
        m11 = 1.0F;
        m12 = 0.0F;
        m13 = 0.0F;
        m20 = 0.0F;
        m21 = 0.0F;
        m22 = 1.0F;
        m23 = 0.0F;
    }

    public void transform(Vector3f vector) {
        float x = vector.x;
        float y = vector.y;
        float z = vector.z;
        vector.x = (m00 * x) + (m01 * y) + (m02 * z) + m03;
        vector.y = (m10 * x) + (m11 * y) + (m12 * z) + m13;
        vector.z = (m20 * x) + (m21 * y) + (m22 * z) + m23;
    }
}
