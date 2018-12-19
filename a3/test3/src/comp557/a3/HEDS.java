package comp557.a3;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * Half edge data structure. Maintains a list of faces (i.e., one half edge of
 * each) to allow for easy display of geometry. Elias Al Homsi 260797449
 */
public class HEDS {

	/** List of faces */
	Set<Face> faces = new HashSet<Face>();

	/**
	 * You might want to use this to match up half edges...
	 */
	Map<String, HalfEdge> halfEdges = new TreeMap<String, HalfEdge>();

	/**
	 * Need to know both verts before the collapse, but this information is actually
	 * already stored within the excized portion of the half edge data structure.
	 * Thus, we only need to have a half edge (the collapsed half edge) to undo
	 */
	LinkedList<HalfEdge> undoList = new LinkedList<>();
	/**
	 * To redo an undone collapse, we must know which edge to collapse. We should
	 * likewise reuse the Vertex that was created for the collapse.
	 */
	LinkedList<HalfEdge> redoListHalfEdge = new LinkedList<>();
	LinkedList<Vertex> redoListVertex = new LinkedList<>();

	/**
	 * Priority Queue of edges
	 */
	PriorityQueue<Edge> pq = new PriorityQueue<Edge>();

	/**
	 * Constructs an empty mesh (used when building a mesh with subdivision)
	 */
	public HEDS() {
		// do nothing
	}

	/**
	 * helper method to get faster indecies
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private String getIndex(int i, int j) {
		return String.format("%d,%d", i, j).toString();
	}

	/**
	 * Builds a half edge data structure from the polygon soup
	 * 
	 * @param soup
	 */
	public HEDS(PolygonSoup soup, double lambda) {
		// step 0 init
		halfEdges.clear();
		faces.clear();

		// step 1 build the half edge for all faces
		HalfEdge he = null;
		for (int[] f : soup.faceList) {
			HalfEdge start = new HalfEdge();
			start.head = soup.vertexList.get(f[0]);
			HalfEdge prev = start;

			int faceCount = f.length;
			for (int i = 1; i < faceCount; i++) {
				he = new HalfEdge();
				he.head = soup.vertexList.get(f[i]);
				prev.next = he;
				halfEdges.put(getIndex(f[(i - 2 + faceCount) % faceCount], f[(i - 1 + faceCount) % faceCount]), prev);
				prev = he;
			}
			he.next = start;
			halfEdges.put(getIndex(f[faceCount - 2], f[faceCount - 1]), he);
		}

		// step 2 build twins and add a he to the list of faces
		for (int[] f : soup.faceList) {
			for (int i = 0; i < f.length - 1; i++) {
				he = halfEdges.get(getIndex(f[i], f[i + 1]));
				he.twin = halfEdges.get(getIndex(f[i + 1], f[i]));
				halfEdges.put(getIndex(f[i], f[i + 1]), he);
			}
			he = halfEdges.get(getIndex(f[f.length - 1], f[0]));
			he.twin = halfEdges.get(getIndex(f[0], f[f.length - 1]));
			halfEdges.put(getIndex(f[f.length - 1], f[0]), he);
			faces.add(new Face(he));
		}

		Edge myedge;
		// TODO: Objective 5: fill your priority queue on load
		for (int[] f : soup.faceList) {
			for (int i = 0; i < f.length; i++) {
				// for each twin put a new edge
				he = halfEdges.get(getIndex(f[i], f[(i + 1) % f.length]));
				if (he.e != null)
					continue;
				myedge = new Edge();
				updateQ(he);
				myedge.he = he;
				myedge.Q.setZero();
				myedge.Q.add(he.head.Q);
				myedge.Q.add(he.twin.head.Q);
				myedge.v = convert4d(getMinError(he, lambda));
				myedge.error = computeError(myedge.v, myedge.Q);
				he.e = myedge;
				he.twin.e = myedge;
				pq.add(myedge);
			}
		}
	}

	// TODO: Objective 2, 3, 4, 5: write methods to help with collapse, and for7

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

	// checking topological problems
	public boolean topologicalProblem(HalfEdge he) {
		// check if 4 faces
		if (this.faces.size() <= 4)
			return true;

		// check if the ring have more than 2 points in common
		Set<Point3d> s1 = adjacentVertecies(he);
		Set<Point3d> s2 = adjacentVertecies(he.twin);
		s1.retainAll(s2);

		if (s1.size() > 2)
			return true;

		return false;
	}

	private Set<Point3d> adjacentVertecies(HalfEdge he) {
		Set<Point3d> myset = new HashSet<Point3d>();
		HalfEdge pointer = he.next;
		do {
			myset.add(pointer.head.p);
			pointer = pointer.twin.next;
		} while (pointer != he.next);

		return myset;
	}

	/**
	 * returns an edge to continue from there
	 * 
	 * @param he
	 * @return
	 */
	public HalfEdge normalcollapse(final HalfEdge he, double lambda) {
		if (topologicalProblem(he)) {
			System.out.println("error colapsing");
			return he;
		}

		// step 0. if a user starts a collapse the redo list should be initialized
		while(!redoListHalfEdge.isEmpty()) {
			redoCollapse();
		}

		// step 1. create the new vertex which is the combination of the two other
		// points
		Vertex midPoint = getMinError(he, lambda);

		// step 2. edit the heads of all other half edges to point to the new head while
		// keeping the old he intact
		pointToNewHead(midPoint, he, he.twin.prev());
		pointToNewHead(midPoint, he.twin, he.prev());

		// step 2.1. edit the next and twin pointers if necessary
		HalfEdge A = he.next.twin;
		HalfEdge B = he.prev().twin;
		HalfEdge C = he.twin.next.twin;
		HalfEdge D = he.twin.prev().twin;

		A.twin = B;
		B.twin = A;
		C.twin = D;
		D.twin = C;
		
		//lets keep edges B and D
		A.e = B.e;
		B.e.he = B;
		C.e = D.e;
		D.e.he = D;

		// step 3. delete the two faces
		faces.remove(he.leftFace);
		faces.remove(he.twin.leftFace);

		// step 4. fix the undo list
		undoList.add(he); // keep the points pointer intact in he

		// return an edge
		return A;
	}

	/**
	 * get best candidate
	 * 
	 * @param he
	 * @param lambda
	 * @return
	 */
	public HalfEdge getBestCandidate() {
		if (pq.isEmpty()) {
			System.out.println("pq is empty");
			return null; // ignore the request
		}
		return pq.peek().he;
	}

	/**
	 * returns an edge to continue from there This is auto collapse (choses the best
	 * edge to minimize from the pq)
	 * 
	 * @param he
	 * @return
	 */
	public HalfEdge collapse(double lambda) {
		if(faces.size() <= 4)
			return null;
		
		if (pq.isEmpty()) {
			System.out.println("pq is empty");
			return null; // ignore the request
		}

		// get the best halfedge without errors
		Edge e = null;
		LinkedList<Edge> getThemBack = new LinkedList<Edge>();
		HalfEdge he = null;
		do {
			e = pq.poll();
			getThemBack.push(e);
			he = e.he;
		} while (topologicalProblem(he) && !pq.isEmpty());

		// get the linked list back to pq
		getThemBack.pop();
		pq.addAll(getThemBack);
		
		if (pq.isEmpty()) {
			System.out.println("pq is empty");
			return null; // ignore the request
		}

		// remove other edges from the pq and preform a normal collapse
		Edge e1 = he.next.e;
		Edge e2 = he.prev().e;
		Edge e3 = he.twin.next.e;
		Edge e4 = he.twin.prev().e;
		
		pq.remove(e1);
		pq.remove(e3);
		
		// not the best way inefficient but still works :)
		e.v = convert4d(getMinError(he, lambda));
		
		//update all edges in 1 ring loop
		HalfEdge A = normalcollapse(he, lambda);
		HalfEdge point = A;
		updateQ(point, point.head);
		do {
			if(pq.remove(point.e)) {
				point.e.updateEdge(point, lambda);
				pq.add(point.e);		
			}
			point = point.next.twin;
		}while(point != A);
	
		// put the edges back once we updated them.
		return getBestCandidate();
	}

	private void pointToNewHead(final Vertex head, final HalfEdge start, final HalfEdge end) {
		HalfEdge loop = start.next.twin;
		while (loop != end) {
			loop.head = head;
			loop = loop.next.twin;
		}
	}

	private Vertex getMidPoint(HalfEdge he) {
		return getMidPoint(he.head, he.twin.head);
	}

	/**
	 * this method calculates Q for both end points of this halfedge
	 * 
	 * @param he
	 */
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

	private Vertex getMidPoint(final Vertex v1, final Vertex v2) {
		Vertex v = new Vertex();
		v.p.add(v1.p);
		v.p.add(v2.p);
		v.p.scale(0.5);
		return v;
	}

	private Vector3d convert(Vertex head) {
		return new Vector3d(head.p.x, head.p.y, head.p.z);
	}

	private Vector4d convert4d(Vertex head) {
		return new Vector4d(head.p.x, head.p.y, head.p.z, 1.0d);
	}

	private Vertex getMinError(HalfEdge he, double lambda) {
		updateQ(he);
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

	public Vector3d mymult(Matrix3d AA, Vector3d B) {
		Vector3d row1 = new Vector3d();
		Vector3d row2 = new Vector3d();
		Vector3d row3 = new Vector3d();
		AA.getRow(0, row1);
		AA.getRow(1, row2);
		AA.getRow(2, row3);

		return new Vector3d(row1.dot(B), row2.dot(B), row3.dot(B));
	}

	void undoCollapse() {
		if (undoList.isEmpty())
			return; // ignore the request

		HalfEdge he = undoList.removeLast();

		// TODO: Objective 6: undo the last collapse
		// be sure to put the information on the redo list so you can redo the collapse
		// too!

		// step 0. fix the twin pointers that we made wrong up
		// step 1. add two new faces
		// step 2. point towards the old points.
		// step3 add to the redo list

		// add to the redo list
		redoListHalfEdge.add(he);
		redoListVertex.add(he.next.twin.head);

		// fix twins
		he.next.twin.twin = he.next;
		he.prev().twin.twin = he.prev();
		he.twin.next.twin.twin = he.twin.next;
		he.twin.prev().twin.twin = he.twin.prev();

		// fix the heads
		pointToNewHead(he.head, he, he);
		pointToNewHead(he.twin.head, he.twin, he.twin);

		// add two new faces
		faces.add(new Face(he));
		faces.add(new Face(he.twin));

	}

	void redoCollapse() {
		if (redoListHalfEdge.isEmpty())
			return; // ignore the request

		HalfEdge he = redoListHalfEdge.removeLast();
		Vertex midPoint = redoListVertex.removeLast();

		// TODO: Objective 7: undo the edge collapse!
		// step 2. edit the heads of all other half edges to point to the new head while
		// keeping the old he intact
		pointToNewHead(midPoint, he, he.twin.prev());
		pointToNewHead(midPoint, he.twin, he.prev());

		// step 2.1. edit the next and twin pointers if necessary
		HalfEdge A = he.next.twin;
		HalfEdge B = he.prev().twin;
		HalfEdge C = he.twin.next.twin;
		HalfEdge D = he.twin.prev().twin;

		A.twin = B;
		B.twin = A;
		C.twin = D;
		D.twin = C;

		// step 3. delete the two faces
		faces.remove(he.leftFace);
		faces.remove(he.twin.leftFace);

		// step 4. fix the undo list
		undoList.add(he); // keep the points pointer intact in he
	}

	/**
	 * Draws the half edge data structure by drawing each of its faces. Per vertex
	 * normals are used to draw the smooth surface when available, otherwise a face
	 * normal is computed.
	 * 
	 * @param drawable
	 */
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		// we do not assume triangular faces here
		Point3d p;
		Vector3d n;
		for (Face face : faces) {
			face.recomputeNormal(); // just in case
			HalfEdge he = face.he;
			gl.glBegin(GL2.GL_POLYGON);
			if (he.leftFace != face)
				throw new RuntimeException("pointer mismatch");
			n = he.leftFace.n;
			gl.glNormal3d(n.x, n.y, n.z);
			HalfEdge e = he;
			do {
				p = e.head.p;
				gl.glVertex3d(p.x, p.y, p.z);
				e = e.next;
			} while (e != he);
			gl.glEnd();
		}
	}

}