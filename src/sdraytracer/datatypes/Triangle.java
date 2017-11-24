package sdraytracer.datatypes;

import sdraytracer.math.Vec3D;

public class Triangle {

    public Vec3D p1;
    public Vec3D p2;
    public Vec3D p3;
    public RGB color;
    public Vec3D normal;
    public float shininess;

    public Triangle(Vec3D pp1, Vec3D pp2, Vec3D pp3, RGB col, float sh) {
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