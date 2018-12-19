package comp557.a4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import mintools.parameters.IntParameter;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.EasyViewer;
import mintools.viewer.SceneGraphNode;

/** 
 * Creates poison disk radius r samples within a [-1,1]^2 domain.
 * The get method further allows a variable number of generated samples to be 
 * accessed as if they were in a ball of radius 1 at the origin.
 * See http://www.cs.ubc.ca/~rbridson/docs/bridson-siggraph07-poissondisk.pdf
 * Note this implementation is inefficient as it skips the spatial grid acceleration.
 * Likewise, while this techniqeu is useful for higher dimensional point distributions, 
 * this 2D point generation problem is quite simple!
 * @author kry
 */

public class FastPoissonDisk {

	/** Radius of samples to generate in [-1,1]^2 domain */
	double r = 0.05;
	
	/** 
	 * List of samples, the number of samples is determined by the radius, 
	 * but the actual number depends on the random creation process
	 */
	public ArrayList<Point2d> samples = new ArrayList<Point2d>();
	
	/**
	 * Tests the sample generation code, and displays samples in an OpenGL window.
	 * @param args
	 */
	static public void main(String[] args) {
		final FastPoissonDisk fpd = new FastPoissonDisk();
		new EasyViewer("FPD Test", new SceneGraphNode() {
			@Override
			public void init(GLAutoDrawable drawable) {
				// do nothing
			}			
			final IntParameter n = new IntParameter("samples", 1, 1, 300 );
			@Override
			public JPanel getControls() {
				VerticalFlowPanel vfp = new VerticalFlowPanel();
				vfp.add( n.getSliderControls() );
				return vfp.getPanel();
			}
			@Override
			public void display(GLAutoDrawable drawable) {
				GL2 gl = drawable.getGL().getGL2();
				gl.glDisable( GL2.GL_LIGHTING);
				gl.glPointSize(3f);
				gl.glBegin( GL2.GL_POINTS );
				for ( int i = 0; i < n.getValue(); i++ ){
					 Point2d p = fpd.get( i, n.getValue() );
					gl.glVertex2d( p.x, p.y );
				}
				gl.glEnd();
			}
		});
	}
	
	public FastPoissonDisk() {
		createSamples();
	}
	
	public FastPoissonDisk( double r ) {
		this.r = r;
		createSamples();
	}
	
	/**
	 * Creates samples.
	 * See http://www.cs.ubc.ca/~rbridson/docs/bridson-siggraph07-poissondisk.pdf
	 * Note that this is a simplified implementation, which always generates a sample
	 * at the origin, and at the end sorts samples based on their distance to the
	 * origin.
	 */
	public void createSamples() {		
		ArrayList<Point2d> active = new ArrayList<Point2d>();

		// start with the point in the center of the domain
		Point2d p = new Point2d( 0, 0 ); 
		active.add( p );
		samples.add( p );

		Random rand = new Random();

		while ( !active.isEmpty() ) {
			int index = rand.nextInt(active.size());
			p = active.get(index);
			int added = 0;
			for ( int k = 0; k < 30; k++ ) {
				final Vector2d v = new Vector2d();
				do {
					v.set( rand.nextDouble()*4*r - 2*r, rand.nextDouble()*4*r - 2*r );
				} while ( v.length() > 2*r || v.length() < r );
				Point2d pnew = new Point2d();
				pnew.add( p, v );
				if ( pnew.x >  1 ) pnew.x -= 2;
				if ( pnew.x < -1 ) pnew.x += 2;
				if ( pnew.y >  1 ) pnew.y -= 2;
				if ( pnew.y < -1 ) pnew.y += 2;
				boolean adequatelyFar = true;
				for ( int i = 0; i < samples.size(); i++ ) {
					Point2d ptmp = samples.get( i );
					if ( ptmp == p ) continue;
					if ( ptmp.distance(pnew) < r ) {
						adequatelyFar = false;
						break;
					}
				}
				if ( adequatelyFar ) {
					active.add( pnew );
					samples.add( pnew );					
					added++;
				}
			}
			if ( added == 0 ) {
				active.remove(index);
			}
		}
		
		// sort the samples in order from center outwards
		Collections.sort( samples, new Comparator<Point2d>() {
			@Override
			public int compare( Point2d p1, Point2d p2 ) {
				final Point2d mid = new Point2d(0,0);
				double v = p1.distance(mid) - p2.distance(mid);
				if ( v < 0 ) return -1;
				if ( v > 0 ) return 1;
				return 0;
			}
		});
	}
	
	/** 
	 * Gets a sample point.
	 * The point p will be set to the ith sample of N, and
	 * will also be scaled such that the Nth sample is at 
	 * a distance of 1 from the origin.
	 * @param p
	 * @param i
	 * @param N
	 */
	public Point2d get( int i, int N ) {
		Point2d p = new Point2d();
		N = Math.min(N, samples.size());
		i = i % N;
		final Point2d mid = new Point2d(0,0);
		Point2d farthest = samples.get(N-1);
		double radius = farthest.distance(mid);
		if ( radius == 0 ) radius = 1;
		p.set( samples.get(i) );
		p.scale( 1.0d/radius );
		return p;
	}
	
}
