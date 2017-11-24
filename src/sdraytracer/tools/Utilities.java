package sdraytracer.tools;

import sdraytracer.SDRaytracer;

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
}
