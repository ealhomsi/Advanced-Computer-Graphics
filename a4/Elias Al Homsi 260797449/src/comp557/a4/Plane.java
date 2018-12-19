package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials. If both are defined, a 1x1 tile checker
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {

    /**
     * The second material, if non-null is used to produce a checker board pattern.
     */
    Material material2;

    /** The plane normal is the y direction */
    public static final Vector3d n = new Vector3d(0, 1, 0);

    /**
     * Default constructor
     */
    public Plane() {
        super();
    }

    /**
     * ray plane intersection
     */
    @Override
    public void intersect(Ray ray, IntersectResult result) {
        if (n.dot(ray.viewDirection) == 0)
            return; // parallel

        Vector3d eyePoint = new Vector3d(ray.eyePoint);
        double t = -eyePoint.dot(n) / ray.viewDirection.dot(n);
        if (t < 1e-6 || t > result.t)
            return;

        Point3d point = new Point3d();
        ray.getPoint(t, point);

        result.p.set(point);
        result.t = t;
        result.n.set(n);
        result.material = this.material;

        int positiveX = Math.abs(((int) Math.ceil(point.x)) % 2);
        int positiveZ = Math.abs(((int) Math.ceil(point.z)) % 2);
        // check if material 2 is required
        if (material2 != null && (positiveX != positiveZ))
            result.material = this.material2;
    }
}
