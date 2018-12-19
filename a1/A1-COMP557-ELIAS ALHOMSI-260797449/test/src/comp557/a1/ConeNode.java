package comp557.a1;

public class ConeNode extends GeomNode {
	
	public ConeNode(String name) {
		super(name);
	}
	
	@Override
	public void drawShape() {
		DAGNode.glut.glutSolidCone( 1.0f, 1.0f, GeomNode.SLICES, GeomNode.STACKS);
	}
	
}

