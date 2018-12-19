package comp557.a4;

import javax.vecmath.Vector3d;

/**
 * A simple slab class. A box is defined as three slabs (1 for each two parallel faces) 
 */
public class Slab {
    /**
     * Slab normal
     */
    Vector3d n = new Vector3d();

    /**
     * slab min limit
     */
    public double a;
    
    /**
     * Slab max limit
     */
    public double b;
    
    /**
     * Constructor
     */
    public Slab(Vector3d normal, double a, double b) {
        this.n.set(normal);
        this.a = a;
        this.b = b;
    }

	
	public double[] intersectSlab(Ray ray) {
        Vector3d d = ray.viewDirection;
        Vector3d origin = new Vector3d(ray.eyePoint);
		double p1 = (a - origin.dot(n))/d.dot(n);
        double p2 = (b - origin.dot(n))/d.dot(n);
        double [] result = new double[2];
        result[0] = Math.min(p1, p2);
        result[1]= Math.max(p1, p2);
        return result;
	}	
}
