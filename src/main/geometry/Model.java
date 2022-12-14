package main.geometry;

import main.maths.*;

import java.util.ArrayList;

public class Model implements Intersectable {

    private ArrayList<Triangle> triangles; //also called faces

    private Vector3 position; //the over al position of the object
    private double size;

    private Orientation orientation;

    //constructor with general position at 0,0,0
    public Model(ArrayList<Triangle> triangles) {
        this.triangles = triangles;
        this.position = new Vector3();
        orientation = new Orientation();
    }

    //constructor with  position
    public Model(ArrayList<Triangle> triangles, Vector3 position, double size) {
        this.triangles = triangles;
        this.orientation = new Orientation();
        this.position = position;
        this.size = size;
        move();
    }

    public Vector3 getPosition() {
        return position;
    }

    public ArrayList<Triangle> getTriangles() {
        return triangles;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        move();
    }

    public void setSize(double size) {
        this.size = size;
        move();
    }

    public void setTriangles(ArrayList<Triangle> triangles) {
        this.triangles = triangles; //if triangles change the vertice need to change and be moved to correct position
        move();
    }

    public void lookAt(Vector3 point) {
        orientation.LookAt(position, point);
        move();
    }

    //if the position of the object changes all the vectors need to change
    private void move() {
        for (Triangle triangle : triangles) {
            Vector3[] vertices = triangle.getVertices();
            for (int i = 0; i < triangle.getVertices().length; i++) {
                triangle.setVertex(i, triangle.getVertexOrigin(i).rotateByQuaternion(orientation.getQuaternion()));
                triangle.setVertex(i, (triangle.getVertex(i).multi(size))); //first the size is calculated before position because size needs to be calculated out of the origin before position is added
                triangle.setVertex(i, (triangle.getVertex(i).add(position))); // if object moves to the plus side it will be added since plus + plus = plus while moving to minus side will also be added since plus + minus = plus-plus
            }
            triangle.setSurfaceNormal(triangle.getSurfaceNormal().rotateByQuaternion(orientation.getQuaternion()));
        }
    }

    @Override
    public RayHit intersects(FullRay fullRay) {
        return fullRay.castRay(triangles);
    }

    @Override
    public boolean intersectsFast(ShadowRay shadowRay) {
        return shadowRay.castRay(triangles);
    }
}
