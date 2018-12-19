package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/** 
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see
 * max) corner.
 */
public class SlabCollection extends Intersectable {
    public Slab[] slabs;

    public SlabCollection(Vector3d[] normals, double[] a, double[] b) {
        super();
        this.slabs = new Slab[normals.length];
        for (int i = 0; i < normals.length; i++) {
        	normals[i].normalize();
            this.slabs[i] = new Slab(normals[i], a[i], b[i]);
        }
    }

    /**
     * SlabCollection ray intersection using slab
     */
    @Override
    public void intersect(Ray ray, IntersectResult result) {
        double[][] slabIntersections = new double[this.slabs.length][2];
        Vector3d n = new Vector3d();

        // find the latest entry point and the first exit point
        int maxIndex = 0;
        int minIndex = 0;

        for (int i = 0; i < slabs.length; i++) {
            slabIntersections[i] = slabs[i].intersectSlab(ray);
            if (slabIntersections[maxIndex][0] < slabIntersections[i][0]) {
                maxIndex = i;
            }
            if (slabIntersections[minIndex][1] > slabIntersections[i][1]) {
                minIndex = i;
            }
        }

        // set the normal and flip if necessary
        n.set(slabs[maxIndex].n);
        
        if (n.dot(ray.viewDirection) > 0)
            n.negate();

         if (slabIntersections[maxIndex][0] < 0 || slabIntersections[maxIndex][0] > result.t ||
         slabIntersections[maxIndex][0] >  slabIntersections[minIndex][1]|| slabIntersections[maxIndex][0] < 1e-6)
            return;

        Point3d point = new Point3d();
        ray.getPoint(slabIntersections[maxIndex][0], point);
        result.p.set(point);
        result.n.set(n);
		result.t = slabIntersections[maxIndex][0];
		result.material = this.material;
    }
}
