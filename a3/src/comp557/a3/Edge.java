package comp557.a3;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

/**
 * A class to store information concerning mesh simplificaiton that is common to
 * a pair of half edges. Speicifically, the error metric, optimal vertex
 * location on collapse, and the error.
 * 
 * @author kry Elias Al Homsi 260797449
 */
public class Edge implements Comparable<Edge> {

	/** One of the two half edges */
	HalfEdge he;

	/** Optimal vertex location on collapse */
	Vector4d v = new Vector4d();

	/** Error metric for this edge */
	Matrix4d Q = new Matrix4d();

	/** The error involved in performing the collapse of this edge */
	double error;

	@Override
	public int compareTo(Edge o) {
		if (error < o.error)
			return -1;
		if (error > o.error)
			return 1;
		return 0;
	}
	
	private Vector4d convert4d(Vertex head) {
		return new Vector4d(head.p.x, head.p.y, head.p.z, 1.0d);
	}
	
	/**
	 * this basically does V^T Q V v being a 4d vector with w = 1
	 */
	private double computeError(Vector4d v, Matrix4d Q) {
		// do V^T Q V
		// get columns
		Vector4d col1 = new Vector4d();
		Vector4d col2 = new Vector4d();
		Vector4d col3 = new Vector4d();
		Vector4d col4 = new Vector4d();

		Q.getColumn(0, col1);
		Q.getColumn(1, col2);
		Q.getColumn(2, col3);
		Q.getColumn(3, col4);

		Vector4d res = new Vector4d(col1.dot(v), col2.dot(v), col3.dot(v), col4.dot(v));
		return res.dot(v);
	}

	
	public void updateEdge(HalfEdge he, double lambda) {
		this.he = he;
		this.Q.setZero();
		this.Q.add(he.head.Q);
		this.Q.add(he.twin.head.Q);
		this.v = convert4d(getMinError(he, lambda));
		this.error = computeError(this.v, this.Q);
		he.e = this;
		he.twin.e = this;
	}
	
	private Vertex getMinError(HalfEdge he, double lambda) {
		//updateQ(he);
		Matrix4d sum = new Matrix4d();
		sum.setZero();
		sum.add(he.head.Q);
		sum.add(he.twin.head.Q);

		Matrix3d A = new Matrix3d();
		sum.getRotationScale(A);

		Vector3d B = new Vector3d();
		sum.get(B);

		Vector3d m = convert(getMidPoint(he.head, he.twin.head));

		// add lambda I to A
		Matrix3d AA = new Matrix3d();
		AA.setIdentity();
		AA.mul(lambda);
		AA.add(A);
		try {
			AA.invert();	
		}catch(javax.vecmath.SingularMatrixException e) {
			
		}

		// fix b and lambda m
		m.scale(lambda);
		B.scale(-1.0d);
		B.add(m);

		// the result is AA * B have to be done manually
		Vector3d result = mymult(AA, B);

		Vertex vres = new Vertex();
		vres.p.set(result);
		return vres;
	}
	
	private Vertex getMidPoint(final Vertex v1, final Vertex v2) {
		Vertex v = new Vertex();
		v.p.add(v1.p);
		v.p.add(v2.p);
		v.p.scale(0.5);
		return v;
	}
	
	public void updateQ(HalfEdge he) {
		updateQ(he, he.head);
		updateQ(he.twin, he.twin.head);
	}

	/**
	 * this method updates only the Q of the head
	 * 
	 * @param he
	 * @param head
	 */
	public void updateQ(HalfEdge he, Vertex head) {
		if (he.head != head)
			throw new RuntimeException("precondition is not met for updateQ");

		// find all adjacent faces of vertex head
		head.Q.setZero();

		HalfEdge pointer = he;

		do {
			pointer.leftFace.recomputeNormal();
			head.Q.add(pointer.leftFace.K); // am I adding twice?
			pointer = pointer.next.twin;
		} while (pointer != he);
	}
	
	public Vector3d mymult(Matrix3d AA, Vector3d B) {
		Vector3d row1 = new Vector3d();
		Vector3d row2 = new Vector3d();
		Vector3d row3 = new Vector3d();
		AA.getRow(0, row1);
		AA.getRow(1, row2);
		AA.getRow(2, row3);

		return new Vector3d(row1.dot(B), row2.dot(B), row3.dot(B));
	}
	
	private Vector3d convert(Vertex head) {
		return new Vector3d(head.p.x, head.p.y, head.p.z);
	}	
}
