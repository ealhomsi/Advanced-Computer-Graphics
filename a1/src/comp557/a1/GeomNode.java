package comp557.a1;

import javax.vecmath.Tuple3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

public abstract class GeomNode extends DAGNode {
	public static final int SLICES = 20;
	public static final int STACKS = 20;
	
	Tuple3d center;
	Tuple3d rotation;
	Tuple3d color;
	Tuple3d scale;
	
	public GeomNode( String name) {
		super(name);
	}
	
	public GeomNode( String name, Tuple3d center, Tuple3d rotation, Tuple3d color, Tuple3d scale ) {
		super(name);
		this.center = center;
		this.color = color;
		this.rotation = rotation;
		this.scale = scale;
	}
	
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glPushMatrix(); 
		{
			gl.glTranslated(center.x, center.y, center.z);
			gl.glScaled(scale.x, scale.y, scale.z);
			gl.glRotated(rotation.x, 1.0d, 0.0d, 0.0d);
			gl.glRotated(rotation.y, 0.0d,  1.0d, 0.0d);
			gl.glRotated(rotation.z, 0.0d, 0.0d,  1.0d);
			gl.glColor3d(color.x, color.y, color.z);
			this.drawShape();
		}
		gl.glPopMatrix();
	}
	
	

	public Tuple3d getCenter() {
		return center;
	}

	public void setCenter(Tuple3d center) {
		this.center = center;
	}

	public Tuple3d getRotation() {
		return rotation;
	}

	public void setRotation(Tuple3d rotation) {
		this.rotation = rotation;
	}

	public Tuple3d getColor() {
		return color;
	}

	public void setColor(Tuple3d color) {
		this.color = color;
	}

	public Tuple3d getScale() {
		return scale;
	}

	public void setScale(Tuple3d scale) {
		this.scale = scale;
	}

	public abstract void drawShape();

	
}
