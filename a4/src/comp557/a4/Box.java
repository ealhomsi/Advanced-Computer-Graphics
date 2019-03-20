package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see
 * max) corner.
 */
public class Box extends SlabCollection {
	private Point3d max;
	private Point3d min;

	/**
	 * Default constructor. Creates a 2x2x2 box centered at (0,0,0) with min -1 -1
	 * -1 and max 1 1 1 The default box
	 */
	public Box() {
		super(new Vector3d[] { new Vector3d(1.0d, 0.0d, 0.0d), new Vector3d(0.0d, 1.0d, 0.0d),
				new Vector3d(0.0d, 0.0d, 1.0d) }, new double[] { -1, -1, -1 }, new double[] { 1, 1, 1 });
		max = new Point3d(1, 1, 1);
		min = new Point3d(-1, -1, -1);
	}

	public Point3d getMax() {
		return max;
	}

	public void setMax(Point3d max) {
		this.max = max;
		updateSlabs();
	}

	public Point3d getMin() {
		return min;
	}

	public void setMin(Point3d min) {
		this.min = min;
		updateSlabs();
	}

	private void updateSlabs() {
		this.slabs[0].a = this.min.x;
		this.slabs[1].a = this.min.y;
		this.slabs[2].a = this.min.z;
		this.slabs[0].b = this.max.x;
		this.slabs[1].b = this.max.y;
		this.slabs[2].b = this.max.z;
	}
}
