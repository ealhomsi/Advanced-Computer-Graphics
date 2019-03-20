package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple sphere class. 
 */
public class Sphere extends Intersectable {

    /** Radius of the sphere. */
    public double radius = 1;

    /** Location of the sphere center. */
    public Point3d center = new Point3d(0, 0, 0);

    /**
     * Default constructor
     */
    public Sphere() {
        super();
    }

    /**
     * Creates a sphere with the request radius and center.
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere(double radius, Point3d center, Material material) {
        super();
        this.radius = radius;
        this.center = center;
        this.material = material;
    }

    /**
     * this method is for intersecting ray with a sphere 
     */
    @Override
    public void intersect(Ray ray, IntersectResult result) {
        double a, b, c, delta;
        Vector3d O = new Vector3d(ray.eyePoint);
        O.sub(center);

        Vector3d D = ray.viewDirection;

        // A is D^2
        a = D.dot(D);

        // B is 2.O.D
        b = 2 * O.dot(D);

        // C is O^2 - R^2
        c = O.dot(O) - Math.pow(radius, 2);
        delta = Math.pow(b, 2) - (4 * a * c);

        if (delta < 0)
            return;

        double t = Math.min((-b + Math.sqrt(delta)) / (2 * a), (-b - Math.sqrt(delta)) / (2 * a));
        if(t < 1e-6 || t > result.t)
            return;
     
        result.t = t;
        result.material = this.material;
        Point3d point = new Point3d();
        ray.getPoint(t, point);
        result.p.set(point);

        Vector3d normal = new Vector3d(point);
        normal.sub(center);
        normal.normalize();
        result.n.set(normal);
    }

}
