package comp557.a1;

public class SphereNode extends GeomNode {
	
	public SphereNode(String name) {
		super(name);
	}
	
	@Override
	public void drawShape() {
		DAGNode.glut.glutSolidSphere(1.0, SLICES, STACKS);
	}
	
}

