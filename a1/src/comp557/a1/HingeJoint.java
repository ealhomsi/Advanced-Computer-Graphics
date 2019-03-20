package comp557.a1;

import javax.vecmath.Tuple2d;
import javax.vecmath.Tuple3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import mintools.parameters.DoubleParameter;

public class HingeJoint extends JointNode {
	//Axis
	Tuple3d axis;

	DoubleParameter theta;

	public HingeJoint(String name, Tuple3d center, Tuple3d axis, double theta, Tuple2d minMax) {
		super(name, center);
		if(axis == null || minMax == null) {
			throw new RuntimeException("Hinge joint must have axis and minMax specified");
		}
		this.axis = axis;
		dofs.add(this.theta = new DoubleParameter(name + " theta", theta, minMax.x, minMax.y));
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glPushMatrix();
		{
			gl.glTranslated(center.x(), center.y(), center.z());
			gl.glRotated(theta.getValue(), axis.x, axis.y, axis.z);
			super.display(drawable);
		}
		gl.glPopMatrix();
	}

}
