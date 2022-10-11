package main.scene;

import main.geometry.Intersectable;
import main.geometry.Solid;
import main.maths.FullRay;
import main.maths.RayHit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class Scene {

    private ArrayList<Intersectable> geometry;

    private List<PointLight> lights = new ArrayList<PointLight>();
    ;

    private Camera camera;

    private float skyEmission;

    public Scene() {
        this.geometry = new ArrayList<Intersectable>();
    }


    //get methods
    public ArrayList<Intersectable> getGeometry() {
        return geometry;
    }

    public List<PointLight> getLights() {
        return lights;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public float getSkyEmission() {
        return 0;
    }


    public void addSolid(Solid solid) {
        this.geometry.add((Intersectable) solid);
    }

    public void addIntersectable(Intersectable intersectable) {
        this.geometry.add(intersectable);
    }

    public void clearSolid() {

    }

    public void removeSolid(Solid solid) {

    }

    public void removeSolid(int index) {

    }

    public void addLight(PointLight light) {
        this.lights.add(light);
    }

    public void clearLights() {

    }

    public void removeLight(PointLight light) {

    }

    public void removeLight(int index) {

    }

    public RayHit rayCast(FullRay fullRay) {
        return null;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void setGeometry(Intersectable[] geometry) {
        Collections.addAll(this.geometry, geometry);
    }
}
