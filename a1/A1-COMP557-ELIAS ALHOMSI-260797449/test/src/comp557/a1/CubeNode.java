package comp557.a1;

public class CubeNode extends GeomNode {
	
	public CubeNode(String name) {
		super(name);
	}
	

	@Override
	public void drawShape() {
		DAGNode.glut.glutSolidCube( 1.0f);
	}
	
}
