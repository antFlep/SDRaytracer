package sdraytracer.tools;

import sdraytracer.SDRaytracer;
import sdraytracer.datatypes.RGB;
import sdraytracer.datatypes.Triangle;
import sdraytracer.datatypes.Vec3D;

import java.util.List;

public class Utilities {

    public static void debug(String s) {} // System.err.println(s); }

    public static void profileRenderImage(SDRaytracer sdr) {
        long end;
        long start;
        long time;

        sdr.renderImage(); // initialisiere Datenstrukturen, erster Lauf verf�lscht sonst Messungen

        for (int procs = 1; procs < 6; procs++) {

            sdr.maxRec = procs - 1;
            System.out.print(procs);
            for (int i = 0; i < 10; i++) {
                start = System.currentTimeMillis();

                sdr.renderImage();

                end = System.currentTimeMillis();
                time = end - start;
                System.out.print(";" + time);
            }
            System.out.println("");
        }
    }

    public static void addCube(List< Triangle > triangles, int x, int y, int z, int w, int h, int d, RGB c, float sh) {

        //front
        triangles.add(new Triangle(new Vec3D(x, y, z), new Vec3D(x + w, y, z), new Vec3D(x, y + h, z), c, sh));
        triangles.add(new Triangle(new Vec3D(x + w, y, z), new Vec3D(x + w, y + h, z), new Vec3D(x, y + h, z), c, sh));
        //left
        triangles.add(new Triangle(new Vec3D(x, y, z + d), new Vec3D(x, y, z), new Vec3D(x, y + h, z), c, sh));
        triangles.add(new Triangle(new Vec3D(x, y + h, z), new Vec3D(x, y + h, z + d), new Vec3D(x, y, z + d), c, sh));
        //right
        triangles.add(new Triangle(new Vec3D(x + w, y, z), new Vec3D(x + w, y, z + d), new Vec3D(x + w, y + h, z), c, sh));
        triangles.add(new Triangle(new Vec3D(x + w, y + h, z), new Vec3D(x + w, y, z + d), new Vec3D(x + w, y + h, z + d), c, sh));
        //top
        triangles.add(new Triangle(new Vec3D(x + w, y + h, z), new Vec3D(x + w, y + h, z + d), new Vec3D(x, y + h, z), c, sh));
        triangles.add(new Triangle(new Vec3D(x, y + h, z), new Vec3D(x + w, y + h, z + d), new Vec3D(x, y + h, z + d), c, sh));
        //bottom
        triangles.add(new Triangle(new Vec3D(x + w, y, z), new Vec3D(x, y, z), new Vec3D(x, y, z + d), c, sh));
        triangles.add(new Triangle(new Vec3D(x, y, z + d), new Vec3D(x + w, y, z + d), new Vec3D(x + w, y, z), c, sh));
        //back
        triangles.add(new Triangle(new Vec3D(x, y, z + d), new Vec3D(x, y + h, z + d), new Vec3D(x + w, y, z + d), c, sh));
        triangles.add(new Triangle(new Vec3D(x + w, y, z + d), new Vec3D(x, y + h, z + d), new Vec3D(x + w, y + h, z + d), c, sh));

    }
}
