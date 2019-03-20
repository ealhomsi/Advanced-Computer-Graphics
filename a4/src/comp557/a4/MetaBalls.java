package comp557.a4;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple MetaBalls class. 
 */
public class MetaBalls extends Intersectable {
    /**
     * The marching speed
     */
    public static double marching = 0.5;

    /** Radius of the MetaBalls. */
    public double radius = 1;

    /** Location of the centers. */
    public ArrayList<Point3d> centers = new ArrayList<Point3d>();

    /**
     * Default constructor
     */
    public MetaBalls() {
        super();
    }
    		
    public MetaBalls(double radius, ArrayList<Point3d> centers) {
		super();
		this.radius = radius;
		this.centers = centers;
	}
    
    private double evaluate(Point3d x) {
        double total = 0.0d;
        
        for(Point3d p: centers) {
            Vector3d v = new Vector3d();
            v.sub(p, x);
            total += 1.0d / v.lengthSquared();
        }

        return total;
    }

    private boolean inside(Point3d x) {
        return evaluate(x) > this.radius;
    }

    
    private Vector3d getNormal(Point3d x) {
        Vector3d normal = new Vector3d();
        for(Point3d center: centers) {
            Vector3d v = new Vector3d();
            v.sub(x, center);
            v.scale(1.0d / (v.lengthSquared() * v.lengthSquared()));
            v.scale(-2.0d);
            normal.add(v);
        }
        normal.normalize();
        return normal;
    }

    private double[] minMax(Ray ray) {
        Vector3d v = new Vector3d();
        v.sub(centers.get(0), ray.eyePoint);
        double low = v.lengthSquared();
        double high = low;

        for(int i=1; i<centers.size(); i++) {
            v.sub(centers.get(i), ray.eyePoint);
            double current = v.lengthSquared();
            if(current > high){
                high = current;
            }
            if(current < low) {
                low = current;
            }
        }

        return new double[] {Math.max(0, low-radius), Math.max(0, high + radius)};
    }

    /**
     * this method is for intersecting ray with a metaballs 
     * using ray marching technique binary search
     */
    @Override
    public void intersect(Ray ray, IntersectResult result) {
        double t = 0;
        //find min and max t for intersection
        double [] limits = minMax(ray);
        t = limits[0];
        while(!inside(ray.getPoint(t)) && t < limits[1]) {
            t += marching;
        }
        
        if( t < 1e-6 || !inside(ray.getPoint(t)))
        	return;
        
        //do binary search between t and t - marching
        double a = t-marching;
        double b = t;
        while( Math.abs(a - b) > 1e-5) {
            double mid = (a + b) /2.0d;
            if(inside(ray.getPoint(mid)))
                b = mid;
            else
                a = mid;
        }

        //found the point of intersection
        t = (a + b) /2.0d;
        if(t < 1e-6 || t > result.t)
            return;
     
        result.t = t;
        result.material = this.material;
        Point3d point = new Point3d();
        ray.getPoint(t, point);
        result.p.set(point);
        result.n = getNormal(point);
    }

}
