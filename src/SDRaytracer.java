import datatypes.*;
import scenes.Scene;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static scenes.Scene.createScene;

/* Implementation of a very simple Raytracer
   Stephan Diehl, Universit�t Trier, 2010-2016
*/

public class SDRaytracer extends JFrame {

    private static final long serialVersionUID = 1L;
    boolean profiling = false;
    int width = 1000;
    int height = 1000;

    Future[] futureList = new Future[width];
    int nrOfProcessors = Runtime.getRuntime().availableProcessors();
    ExecutorService eservice = Executors.newFixedThreadPool(nrOfProcessors);

    int maxRec = 3;
    int rayPerPixel = 1;
    int startX;
    int startY;
    int startZ;

    Light mainLight = new Light(new Vec3D(0, 100, 0), new RGB(0.1f, 0.1f, 0.1f));

    Light lights[] = new Light[] {
            mainLight,
            new Light(new Vec3D(100, 200, 300), new RGB(0.5f, 0, 0.0f)),
            new Light(new Vec3D(-100, 200, 300), new RGB(0.0f, 0, 0.5f))
            //,new Light(new Vec3D(-100,0,0), new RGB(0.0f,0.8f,0.0f))
    };

    RGB[][] image = new RGB[width][height];

    float fovx = (float) 0.628;
    float fovy = (float) 0.628;
    RGB ambient_color = new RGB(0.01f, 0.01f, 0.01f);
    RGB background_color = new RGB(0.05f, 0.05f, 0.05f);
    RGB black = new RGB(0.0f, 0.0f, 0.0f);
    int y_angle_factor = 4;
    int x_angle_factor = -4;

    public static void main(String argv[]) {
        long start = System.currentTimeMillis();
        SDRaytracer sdr = new SDRaytracer();
        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println("time: " + time + " ms");
        System.out.println("nrprocs=" + sdr.nrOfProcessors);
    }

    void profileRenderImage() {
        long end;
        long start;
        long time;

        renderImage(); // initialisiere Datenstrukturen, erster Lauf verf�lscht sonst Messungen

        for (int procs = 1; procs < 6; procs++) {

            maxRec = procs - 1;
            System.out.print(procs);
            for (int i = 0; i < 10; i++) {
                start = System.currentTimeMillis();

                renderImage();

                end = System.currentTimeMillis();
                time = end - start;
                System.out.print(";" + time);
            }
            System.out.println("");
        }
    }

    SDRaytracer() {
        createScene(x_angle_factor, y_angle_factor);

        if (!profiling) renderImage();
        else profileRenderImage();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        JPanel area = new JPanel() {
            public void paint(Graphics g) {
                System.out.println("fovx=" + fovx + ", fovy=" + fovy + ", xangle=" + x_angle_factor + ", yangle=" + y_angle_factor);
                if (image == null) return;
                for (int i = 0; i < width; i++)
                    for (int j = 0; j < height; j++) {
                        g.setColor(image[i][j].color());
                        // zeichne einzelnen Pixel
                        g.drawLine(i, height - j, i, height - j);
                    }
            }
        };

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                boolean redraw = false;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    x_angle_factor--;
                    //mainLight.position.y-=10;
                    //fovx=fovx+0.1f;
                    //fovy=fovx;
                    //maxRec--; if (maxRec<0) maxRec=0;
                    redraw = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    x_angle_factor++;
                    //mainLight.position.y+=10;
                    //fovx=fovx-0.1f;
                    //fovy=fovx;
                    //maxRec++;if (maxRec>10) return;
                    redraw = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    y_angle_factor--;
                    //mainLight.position.x-=10;
                    //startX-=10;
                    //fovx=fovx+0.1f;
                    //fovy=fovx;
                    redraw = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    y_angle_factor++;
                    //mainLight.position.x+=10;
                    //startX+=10;
                    //fovx=fovx-0.1f;
                    //fovy=fovx;
                    redraw = true;
                }
                if (redraw) {
                    createScene(x_angle_factor, y_angle_factor);
                    renderImage();
                    repaint();
                }
            }
        });

        area.setPreferredSize(new Dimension(width, height));
        contentPane.add(area);
        this.pack();
        this.setVisible(true);
    }

    Ray eye_ray = new Ray();
    double tan_fovx;
    double tan_fovy;

    void renderImage() {
        tan_fovx = Math.tan(fovx);
        tan_fovy = Math.tan(fovy);
        for (int i = 0; i < width; i++) {
            futureList[i] = (Future) eservice.submit(new RaytraceTask(this, i));
        }

        for (int i = 0; i < width; i++) {
            try {
                RGB[] col = (RGB[]) futureList[i].get();
                for (int j = 0; j < height; j++)
                    image[i][j] = col[j];
            } catch (InterruptedException e) {} catch (ExecutionException e) {}
        }
    }

    RGB rayTrace(Ray ray, int rec) {
        if (rec > maxRec) return black;
        IPoint ip = Ray.hitObject(ray); // (ray, p, n, triangle);
        if (ip.dist > IPoint.epsilon)
            return lighting(ray, ip, rec);
        else
            return black;
    }



    RGB lighting(Ray ray, IPoint ip, int rec) {
        Vec3D point = ip.ipoint;
        Triangle triangle = ip.triangle;
        RGB color = RGB.addColors(triangle.color, ambient_color, 1);
        Ray shadow_ray = new Ray();

        for (Light light: lights) {
            shadow_ray.start = point;
            shadow_ray.dir = light.position.minus(point).mult(-1);
            shadow_ray.dir.normalize();
            IPoint ip2 = Ray.hitObject(shadow_ray);
            if (ip2.dist < IPoint.epsilon) {
                float ratio = Math.max(0, shadow_ray.dir.dot(triangle.normal));
                color = RGB.addColors(color, light.color, ratio);
            }
        }

        Ray reflection = new Ray();
        //R = 2N(N*L)-L)    L ausgehender Vektor
        Vec3D L = ray.dir.mult(-1);
        reflection.start = point;
        reflection.dir = triangle.normal.mult(2 * triangle.normal.dot(L)).minus(L);
        reflection.dir.normalize();
        RGB rcolor = rayTrace(reflection, rec + 1);
        float ratio = (float) Math.pow(Math.max(0, reflection.dir.dot(L)), triangle.shininess);
        color = RGB.addColors(color, rcolor, ratio);
        return (color);
    }

}

class RaytraceTask implements Callable {

    SDRaytracer tracer;
    int i;

    public RaytraceTask(SDRaytracer t, int ii) {
        tracer = t;
        i = ii;
    }

    public RGB[] call() {
        RGB[] col = new RGB[tracer.height];

        for (int j = 0; j < tracer.height; j++) {
            tracer.image[i][j] = new RGB(0, 0, 0);

            for (int k = 0; k < tracer.rayPerPixel; k++) {
                double di = i + (Math.random() / 2 - 0.25);
                double dj = j + (Math.random() / 2 - 0.25);
                if (tracer.rayPerPixel == 1) {
                    di = i;
                    dj = j;
                }

                Ray eye_ray = new Ray();
                eye_ray.setStart(tracer.startX, tracer.startY, tracer.startZ); // ro
                eye_ray.setDir(
                        (float)(((0.5 + di) * tracer.tan_fovx * 2.0) / tracer.width - tracer.tan_fovx),
                        (float)(((0.5 + dj) * tracer.tan_fovy * 2.0) / tracer.height - tracer.tan_fovy),
                        (float) 1f); // rd
                eye_ray.normalize();
                col[j] = RGB.addColors(tracer.image[i][j], tracer.rayTrace(eye_ray, 0), 1.0f / tracer.rayPerPixel);
            }
        }
        return col;
    }
}
