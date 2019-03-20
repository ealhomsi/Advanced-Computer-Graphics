package comp557.a1;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

public class FreeJoint extends JointNode {
	// Euler angles
	DoubleParameter3 rotation;
		
	public FreeJoint( String name, Tuple3d center, Tuple3d rotation ) {
		super(name, center);
		if(rotation == null) {
			rotation = new Vector3d(0, 0, 0);
		}
		this.rotation = new DoubleParameter3(name, rotation, new Vector2d(-180.0d, 180.0d), new Vector2d(-180.0d, 180.0d), new Vector2d(-180.0d, 180.0d), "r");
		this.center.subscribe(dofs);
		this.rotation.subscribe(dofs);
	}

	public DoubleParameter3 getRotation() {
		return rotation;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glPushMatrix(); 
		{
			gl.glTranslated(center.x(), center.y(), center.z());
			gl.glRotated(rotation.x(), 1.0d, 0.0d, 0.0d);
			gl.glRotated(rotation.y(), 0.0d, 1.0d, 0.0d);
			gl.glRotated(rotation.z(), 0.0d, 0.0d, 1.0d);
			super.display(drawable);
		}
		gl.glPopMatrix();
	}
}
