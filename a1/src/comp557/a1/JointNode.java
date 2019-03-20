package comp557.a1;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

/**
 * This is the parent class of all joints
 */
public abstract class JointNode extends DAGNode {
	protected DoubleParameter3 center;
	public static final Vector2d xLimits = new Vector2d(-5, 5);
	public static final Vector2d yLimits = new Vector2d(-5, 5);
	public static final Vector2d zLimits = new Vector2d(-5, 5);

	public JointNode( String name, Tuple3d center) {
		super(name);
		if(center == null) {
			center = new Vector3d(0, 0, 0);
		}
		this.center = new DoubleParameter3(name, center, xLimits,yLimits,zLimits, "t");
	}

	public DoubleParameter3 getCenter() {
		return center;
	}
}
