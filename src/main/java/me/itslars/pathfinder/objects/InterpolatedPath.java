package me.itslars.pathfinder.objects;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

public class InterpolatedPath {

    private List<Vector> preVector = new ArrayList<>();
    private Vector[] pathPoints;

    private int levelOfDetail = 10;

    private Vector origin;

    public InterpolatedPath(Vector vector, List<Vector> vectorList) {
        setOrigin(vector);
        setPreVector(vectorList);
        generatePath();
    }

    public int getPathLenght() {
        return getPreVector().size() * levelOfDetail;
    }

    public Vector getPathPosition(Double double1) {
        double percent = double1 / (getPreVector().size() * levelOfDetail);
        return interpolate(pathPoints, percent).add(getOrigin());
    }

    public void generatePath() {
        Vector[] suppliedPath = getPreVector().toArray(new Vector[getPreVector().size()]);
        Vector[] finalPath;
        finalPath = new Vector[suppliedPath.length + 2];
        copyArray(suppliedPath, 0, finalPath, 1, suppliedPath.length);
        finalPath[0] = finalPath[1].clone().add(finalPath[1].clone().subtract(finalPath[2]));
        finalPath[finalPath.length - 1] = finalPath[finalPath.length - 2].clone()
                .add(finalPath[finalPath.length - 2].clone().subtract(finalPath[finalPath.length - 3]));
        pathPoints = finalPath;
    }

    private static Vector interpolate(Vector[] pts, double t) {
        int numSections = pts.length - 3;
        int currPt = (int) Math.min(Math.floor(t * (double) numSections), numSections - 1);
        double u = t * (double) numSections - (double) currPt;

        Vector a = pts[currPt];
        Vector b = pts[currPt + 1];
        Vector c = pts[currPt + 2];
        Vector d = pts[currPt + 3];

        return a.clone().multiply(-1).add(b.clone().multiply(3f)).subtract(c.clone().multiply(3f)).add(d)
                .multiply(u * u * u)
                .add(a.clone().multiply(2f).subtract(b.clone().multiply(5f)).add(c.clone().multiply(4f)).subtract(d)
                        .multiply(u * u))
                .add(a.clone().multiply(-1).add(c).multiply(u)).add(b.clone().multiply(2f)).multiply(0.5f);
    }

    private void copyArray(Vector[] source, int a, Vector[] dest, int b, int lenght) {
        for (int i = a; i < lenght; i++) {
            dest[b + i] = source[i];
        }
    }

    public void moveTo(Vector vector) {
        setOrigin(vector);
    }

    public List<Vector> getPreVector() {
        return preVector;
    }

    public void setPreVector(List<Vector> preVector) {
        this.preVector = preVector;
    }

    public void setOrigin(Vector vector) {
        origin = vector;
    }

    public Vector getOrigin() {
        return origin;
    }
}