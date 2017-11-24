package datatypes;

public class Triangle {

    Vec3D p1;
    Vec3D p2;
    Vec3D p3;
    public RGB color;
    public Vec3D normal;
    public float shininess;

    Triangle(Vec3D pp1, Vec3D pp2, Vec3D pp3, RGB col, float sh) {
        p1 = pp1;
        p2 = pp2;
        p3 = pp3;

        color = col;
        shininess = sh;

        Vec3D e1 = p2.minus(p1);
        Vec3D e2 = p3.minus(p1);
        normal = e1.cross(e2);
        normal.normalize();
    }
}