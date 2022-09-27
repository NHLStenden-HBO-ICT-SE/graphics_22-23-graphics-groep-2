package main.geometry;

import main.maths.Ray;
import main.maths.RayHit;
import main.maths.Vector3;

public class Sphere extends Solid implements Intersectable {

    private double radius;

    private Vector3 position;

    public Sphere(double radius, Vector3 position) {
        this.radius = radius;
        this.position = position;
    }

    public double getradius() {
        return radius;
    }

    public Vector3 getposition() {
        return position;
    }

    public void setradius(double radius) {
        this.radius = radius;
    }

    public void setposition(Vector3 position) {
        this.position = position;
    }

    @Override
    public RayHit Intersects(Ray ray) {
        return null;
    }
}
