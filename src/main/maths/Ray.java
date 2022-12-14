package main.maths;

public abstract class Ray {
    private Vector3 origin;

    private Vector3 direction;

    public Ray(Vector3 direction, Vector3 origin) {
        this.direction = direction.normalise();
        this.origin = origin;
    }

    //some overloaded constructors to reduce boilerplate code
    public Ray(double dirX, double dirY, double dirZ, double originX, double originY, double originZ) {
        this.direction = new Vector3(dirX, dirY, dirZ).normalise();
        this.origin = new Vector3(originX, originY, originZ);
    }

    public Vector3 getOrigin() {
        return origin;
    }

    public Vector3 getDirection() {
        return direction;
    }

    //returns a point that's the specified distance away from the origin and lies on the ray.g
    public Vector3 getPointAlongRay(double distance) {
        return origin.add(direction.multi(distance));
    }
}
