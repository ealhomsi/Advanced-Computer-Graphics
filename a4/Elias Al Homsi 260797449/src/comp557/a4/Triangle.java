package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A Triangle is defined by it's three vertecies max) 
 * corner.
 */
public class Triangle extends Intersectable {
    public Point3d[] vList;
    public double d;
    public Vector3d n;

    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0) with min -1 -1
     * -1 and max 1 1 1 The default box
     */
    public Triangle(Point3d p1, Point3d p2, Point3d p3) {
        super();
        vList = new Point3d[] { p1, p2, p3 };
        update();
    }

    public void update() {
    	n = new Vector3d();
        Point3d p0 = vList[0];
        Point3d p1 = vList[1];
        Point3d p2 = vList[2];
        Vector3d v1 = new Vector3d();
        Vector3d v2 = new Vector3d();
        v1.sub(p1, p0);
        v2.sub(p2, p1);
        n.cross(v1, v2);
        n.normalize();

        this.d = n.dot(new Vector3d(p2));
    }

    /**
     * Triangle
     */
    @Override
    public void intersect(Ray ray, IntersectResult result) {
        // step1. check if plane and ray are parallel
        if (Math.abs(n.dot(ray.viewDirection))< 1e-6)
            return;

        this.d = n.dot(new Vector3d(vList[2]));

        // Intersect with the plane
        Vector3d eyePoint = new Vector3d(ray.eyePoint);
        double t = (d - n.dot(eyePoint)) / (n.dot(ray.viewDirection));

        if (t > result.t || t < 1e-6)
            return;

        // Ensure Point is inside triangle
        Point3d point = new Point3d();
        ray.getPoint(t, point);

        Vector3d edge0 = new Vector3d();
        Vector3d edge1 = new Vector3d();
        Vector3d edge2 = new Vector3d();
        Vector3d vp0 = new Vector3d();
        Vector3d vp1 = new Vector3d();
        Vector3d vp2 = new Vector3d();
        Point3d p0 = vList[0];
        Point3d p1 = vList[1];
        Point3d p2 = vList[2];
        edge0.sub(p1, p0);
        edge1.sub(p2, p1);
        edge2.sub(p0, p2);
        vp0.sub(point, p0);
        vp1.sub(point, p1);
        vp2.sub(point, p2);

        edge0.cross(edge0, vp0);
        edge1.cross(edge1, vp1);
        edge2.cross(edge2, vp2);

        if (n.dot(edge0) < 0 || n.dot(edge1) < 0 || n.dot(edge2) < 0)
            return;

        result.t = t;
        result.material = this.material;
        this.n.normalize();
        result.n.set(n);
        result.p = point;
    }
}
