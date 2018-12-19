package comp557.a4;

import java.util.HashMap;
import java.util.Map;

public class Mesh extends Intersectable {

	/** Static map storing all meshes by name */
	public static Map<String, Mesh> meshMap = new HashMap<String, Mesh>();

	/** Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";

	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}

	/**
	 * intersecting a mesh Assumption: ALL FACES ARE TRIANGLES TODO: 1. parse the
	 * Mesh to generate a list of triangles 2. try intersecting those triangles one
	 * by one.
	 */
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		IntersectResult closest = new IntersectResult();
		for (int[] triangle : soup.faceList) {
			if (triangle.length > 3)
				throw new RuntimeException("Mesh is supposed to be triangles only");
			Triangle t = new Triangle(soup.vertexList.get(triangle[0]).toPoint(),
					soup.vertexList.get(triangle[1]).toPoint(), soup.vertexList.get(triangle[2]).toPoint());
			t.intersect(ray, closest);
		}

		if (closest.t < 1e-6 || closest.t > result.t)
			return;

		result.t = closest.t;
		result.material = this.material;

		result.p = closest.p;
		result.n = closest.n;

		if (result.material == null)
			result.material = closest.material;
	}
}
