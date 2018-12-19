package comp557.a4;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Matrix4d;

import comp557.a4.IntersectResult;
import comp557.a4.Intersectable;
import comp557.a4.Ray;

/**
 * The scene is constructed from a hierarchy of nodes, where each node
 * contains a transform, a material definition, some amount of geometry, 
 * and some number of children nodes.  Each node has a unique name so that
 * it can be instanced elsewhere in the hierarchy (provided it does not 
 * make loops. 
 * 
 * Note that if the material (inherited from Intersectable) for a scene 
 * node is non-null, it should override the material of any child.
 * 
 */
public class SceneNode extends Intersectable {
	
	/** Static map for accessing scene nodes by name, to perform instancing */
	public static Map<String,SceneNode> nodeMap = new HashMap<String,SceneNode>(); 
	
    public String name;
   
    /** Matrix transform for this node */
    public Matrix4d M;
    
    /** Inverse matrix transform for this node */
    public Matrix4d Minv;
    
    /** Child nodes */
    public List<Intersectable> children;
    
    /**
     * Default constructor.
     * Note that all nodes must have a unique name, so that they can used as an instance later on.
     */
    public SceneNode() {
    	super();
    	this.name = "";
    	this.M = new Matrix4d();
    	this.Minv = new Matrix4d();
    	this.children = new LinkedList<Intersectable>();
    }
           
    @Override
    public void intersect(Ray ray, IntersectResult result) {
        Ray transformedRay = new Ray(ray.eyePoint, ray.viewDirection);
        transformedRay.transform(Minv);

        IntersectResult closest = new IntersectResult();

        for (Intersectable s : children)
            s.intersect(transformedRay, closest);

        if (closest.t < 1e-6 || closest.t > result.t)
            return;
            
        closest.transform(M);

        result.n.set(closest.n);
        result.p.set(closest.p);
        result.t = closest.t;
        result.material = this.material;
        if (result.material == null)
            result.material = closest.material;
    }
    
}
