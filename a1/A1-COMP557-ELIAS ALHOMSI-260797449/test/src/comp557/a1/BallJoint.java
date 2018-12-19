package comp557.a1;

import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

public class BallJoint extends JointNode {
	//EulerAngles
	DoubleParameter3 rotation;
		
	public BallJoint ( String name, Tuple3d center, Tuple3d rotation, Tuple2d minMaxX, Tuple2d minMaxY, Tuple2d minMaxZ) {
		super(name, center);
		
		if(rotation == null) {
			rotation = new Vector3d(0, 0, 0);
		}
		if(minMaxX == null){
			minMaxX = new Vector2d(-180, 180);
		}
		if(minMaxY == null){
			minMaxY = new Vector2d(-180, 180);
		}
		if(minMaxZ == null) {
			minMaxZ = new Vector2d(-180, 180);
		}

		this.rotation = new DoubleParameter3(name, rotation, minMaxX, minMaxY, minMaxZ, "r");
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
