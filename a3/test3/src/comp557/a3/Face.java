package comp557.a3;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

/**
 * Simple face class Elias Al Homsi 260797449
 */
public class Face {

	/** sure, why not keep a normal for flat shading? */
	public Vector3d n = new Vector3d();

	/** Plane equation */
	Vector4d p = new Vector4d();

	/** Quadratic function for the plane equation */
	public Matrix4d K = new Matrix4d();

	/** Some half edge on the face */
	HalfEdge he;

	/**
	 * Constructs a face from a half edge, and computes the flat normal
	 * 
	 * @param he
	 */
	public Face(HalfEdge he) {
		this.he = he;
		HalfEdge loop = he;
		do {
			loop.leftFace = this;
			loop = loop.next;
		} while (loop != he);
		recomputeNormal();
	}

	@Override
	public String toString() {
		String res = "";
		HalfEdge loop = he;
		do {
			res += String.format("%f,%f,%f", he.head.p.x, he.head.p.y, he.head.p.z).toString();
			loop = loop.next;
		} while (loop != he);
		return res;
	}

	public void recomputeNormal() {
		Point3d p0 = he.head.p;
		Point3d p1 = he.next.head.p;
		Point3d p2 = he.next.next.head.p;
		Vector3d v1 = new Vector3d();
		Vector3d v2 = new Vector3d();
		v1.sub(p1, p0);
		v2.sub(p2, p1);
		n.cross(v1, v2);
		n.normalize();
		// TODO: Objective 4: you might compute the plane and matrix K for the quadric
		// error metric here (or you could do it elsewhere)

		// step 1 find plane equation
		p.x = n.x;
		p.y = n.y;
		p.z = n.z;
		p.w = -n.dot(new Vector3d(he.head.p.x, he.head.p.y, he.head.p.z));
		p.normalize();

		// step 2 compute K
		double[] inner = new double[16];
		inner[0] = p.x * p.x;
		inner[1] = p.x * p.y;
		inner[2] = p.x * p.z;
		inner[3] = p.x * p.w;

		inner[4] = p.y * p.x;
		inner[5] = p.y * p.y;
		inner[6] = p.y * p.z;
		inner[7] = p.y * p.w;

		inner[8] = p.z * p.x;
		inner[9] = p.z * p.y;
		inner[10] = p.z * p.z;
		inner[11] = p.z * p.w;

		inner[12] = p.w * p.x;
		inner[13] = p.w * p.y;
		inner[14] = p.w * p.z;
		inner[15] = p.w * p.w;
		K.set(inner);
	}

}
