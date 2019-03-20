//Elias Al Homsi 260797449
package comp557.a2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.vecmath.Point2d;

import com.jogamp.opengl.DebugGL2;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.fixedfunc.GLLightingFunc;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.swing.ControlFrame;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.FlatMatrix4d;
import mintools.viewer.Interactor;
import mintools.viewer.TrackBallCamera;

/**
 * Assignment 2 - depth of field blur, and anaglyphys
 * 
 * For additional information, see the following paper, which covers
 * more on quality rendering, but does not cover anaglyphs.
 * 
 * The Accumulation Buffer: Hardware Support for High-Quality Rendering
 * Paul Haeberli and Kurt Akeley
 * SIGGRAPH 1990
 * 
 * http://http.developer.nvidia.com/GPUGems/gpugems_ch23.html
 * GPU Gems [2007] has a slightly more recent survey of techniques.
 *
 * @author Elias Al Homsi 260797449
 */
public class A2App implements GLEventListener, Interactor {
	private String name = "Comp 557 Assignment 2 - Elias Al Homsi 260797449";
	
    /** Viewing mode as specified in the assignment */
    int viewingMode = 1;
        
    /** eye Z position in world coordinates */
    private DoubleParameter eyeZPosition = new DoubleParameter( "eye z", 0.5, 0.25, 3 ); 
    /** near plane Z position in world coordinates */
    private DoubleParameter nearZPosition = new DoubleParameter( "near z", 0.25, -0.2, 0.24 ); 
    /** far plane Z position in world coordinates */
    private DoubleParameter farZPosition  = new DoubleParameter( "far z", -0.5, -2, -0.25 ); 
    /** focal plane Z position in world coordinates */
    private DoubleParameter focalPlaneZPosition = new DoubleParameter( "focal z", 0, -2, 2 );     

    /** Samples for drawing depth of field blur */    
    private IntParameter samples = new IntParameter( "samples", 5, 1, 100 );   
    
    /** 
     * Aperture size for drawing depth of field blur
     * In the human eye, pupil diameter ranges between approximately 2 and 8 mm
     */
    private DoubleParameter aperture = new DoubleParameter( "aperture size", 0.003, 0, 0.01 );
    
    /** x eye offsets for testing (see objective 4) */         
    private DoubleParameter eyeXOffset = new DoubleParameter("eye offset in x", 0.0, -0.3, 0.3);
    /** y eye offsets for testing (see objective 4) */
    private DoubleParameter eyeYOffset = new DoubleParameter("eye offset in y", 0.0, -0.3, 0.3);
    
    private BooleanParameter drawCenterEyeFrustum = new BooleanParameter( "draw center eye frustum", true );    
    
    private BooleanParameter drawEyeFrustums = new BooleanParameter( "draw left and right eye frustums", true );
    
	/**
	 * The eye disparity should be constant, but can be adjusted to test the
	 * creation of left and right eye frustums or likewise, can be adjusted for
	 * your own eyes!! Note that 63 mm is a good inter occular distance for the
	 * average human, but you may likewise want to lower this to reduce the
	 * depth effect (images may be hard to fuse with cheap 3D colour filter
	 * glasses). Setting the disparity negative should help you check if you
	 * have your left and right eyes reversed!
	 */
    private DoubleParameter eyeDisparity = new DoubleParameter("eye disparity", 0.063, -0.1, 0.1 );

    private GLUT glut = new GLUT();
    
    private Scene scene = new Scene();

    /**
     * Launches the application
     * @param args
     */
    public static void main(String[] args) {
        new A2App();
    }
    
    GLCanvas glCanvas;
    
    /** Main trackball for viewing the world and the two eye frustums */
    TrackBallCamera tbc = new TrackBallCamera();
    /** Second trackball for rotating the scene */
    TrackBallCamera tbc2 = new TrackBallCamera();
    
    /** Poisson sampler */
    FastPoissonDisk fdp = null;

    /**
     * Creates the application
     */
    public A2App() {      
        Dimension controlSize = new Dimension(640, 640);
        Dimension size = new Dimension(640, 480);
        ControlFrame controlFrame = new ControlFrame("Controls");
        controlFrame.add("Camera", tbc.getControls());
        controlFrame.add("Scene TrackBall", tbc2.getControls());
        controlFrame.add("Scene", getControls());
        controlFrame.setSelectedTab("Scene");
        controlFrame.setSize(controlSize.width, controlSize.height);
        controlFrame.setLocation(size.width + 20, 0);
        controlFrame.setVisible(true);    
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities glc = new GLCapabilities(glp);
        glCanvas = new GLCanvas( glc );
        glCanvas.setSize( size.width, size.height );
        glCanvas.setIgnoreRepaint( true );
        glCanvas.addGLEventListener( this );
        glCanvas.requestFocus();
        FPSAnimator animator = new FPSAnimator( glCanvas, 60 );
        animator.start();        
        tbc.attach( glCanvas );
        tbc2.attach( glCanvas );
        // initially disable second trackball, and improve default parameters given our intended use
        tbc2.enable(false);
        tbc2.setFocalDistance( 0 );
        tbc2.panRate.setValue(5e-5);
        tbc2.advanceRate.setValue(0.005);

        //poisson
        fdp = new FastPoissonDisk();
        
        this.attach( glCanvas );        
        JFrame frame = new JFrame( name );
        frame.getContentPane().setLayout( new BorderLayout() );
        frame.getContentPane().add( glCanvas, BorderLayout.CENTER );
        frame.setLocation(0,0);        
        frame.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent e ) {
                System.exit(0);
            }
        });
        frame.pack();
        frame.setVisible( true );        
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
    	// nothing to do
    }
        
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        // do nothing
    }
    
    @Override
    public void attach(Component component) {
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_7) {
                    viewingMode = e.getKeyCode() - KeyEvent.VK_1 + 1;
                }
                // only use the tbc trackball camera when in view mode 1 to see the world from
                // first person view, while leave it disabled and use tbc2 ONLY FOR ROTATION when
                // viewing in all other modes
                if ( viewingMode == 1 ) {
                	tbc.enable(true);
                	tbc2.enable(false);
	            } else {
                	tbc.enable(false);
                	tbc2.enable(true);
	            }
            }
        });
    }
    
    /**
     * @return a control panel
     */
    public JPanel getControls() {     
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        
        VerticalFlowPanel vfp2 = new VerticalFlowPanel();
        vfp2.setBorder(new TitledBorder("Z Positions in WORLD") );
        vfp2.add( eyeZPosition.getSliderControls(false));        
        vfp2.add( nearZPosition.getSliderControls(false));
        vfp2.add( farZPosition.getSliderControls(false));        
        vfp2.add( focalPlaneZPosition.getSliderControls(false));     
        vfp.add( vfp2.getPanel() );
        
        vfp.add ( drawCenterEyeFrustum.getControls() );
        vfp.add ( drawEyeFrustums.getControls() );        
        vfp.add( eyeXOffset.getSliderControls(false ) );
        vfp.add( eyeYOffset.getSliderControls(false ) );        
        vfp.add ( aperture.getSliderControls(false) );
        vfp.add ( samples.getSliderControls() );        
        vfp.add( eyeDisparity.getSliderControls(false) );
        VerticalFlowPanel vfp3 = new VerticalFlowPanel();
        vfp3.setBorder( new TitledBorder("Scene size and position" ));
        vfp3.add( scene.getControls() );
        vfp.add( vfp3.getPanel() );        
        return vfp.getPanel();
    }
             
    public void init( GLAutoDrawable drawable ) {
    	drawable.setGL( new DebugGL2( drawable.getGL().getGL2() ) );
        GL2 gl = drawable.getGL().getGL2();
        gl.glShadeModel(GL2.GL_SMOOTH);             // Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    // Black Background
        gl.glClearDepth(1.0f);                      // Depth Buffer Setup
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_LINE_SMOOTH);
        gl.glEnable(GL2.GL_POINT_SMOOTH);
        gl.glEnable(GL2.GL_NORMALIZE );
        gl.glEnable(GL.GL_DEPTH_TEST);              // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL);               // The Type Of Depth Testing To Do 
        gl.glLineWidth( 2 );                        // slightly fatter lines by default!
    }   

    // 14"	diagona:l 35.56 cm	width: 31.00 cm therefore width 0.31
	double screenWidthPixels = 1920;
	double screenWidthMeters = 0.31;
	double metersPerPixel = screenWidthMeters / screenWidthPixels;
    
    @Override
    public void display(GLAutoDrawable drawable) {        
        GL2 gl = drawable.getGL().getGL2();
        GLU glu = GLU.createGLU(gl);
        
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);            

        // screen dimensions
        double w = drawable.getSurfaceWidth() * metersPerPixel;
        double h = drawable.getSurfaceHeight() * metersPerPixel;

        // Init some variables
        Point2d leftTopCorner, leftBottomCorner, rightTopCorner, rightBottomCorner;
        leftTopCorner = new Point2d( -w/2.0d,  h/2.0d);
        leftBottomCorner = new Point2d(-w/2.0d, -h/2.0d);
        rightTopCorner = new Point2d( w/2.0d, h/2.0d);
        rightBottomCorner = new Point2d( w/2.0d, -h/2.0d);

        //define near and far in terms of eye coordinates
        double near, far;
        near = eyeZPosition.getValue() - nearZPosition.getValue();
        far = eyeZPosition.getValue() - farZPosition.getValue();

        //near plane for single frustrum
        double left, bottom, right, top;
        left = ((leftBottomCorner.x - eyeXOffset.getValue()) / eyeZPosition.getValue()) * near ;      
        right = ((rightBottomCorner.x - eyeXOffset.getValue()) /eyeZPosition.getValue()) * near ;      
        bottom = ((rightBottomCorner.y - eyeYOffset.getValue()) /eyeZPosition.getValue()) * near ;      
        top = ((leftTopCorner.y - eyeYOffset.getValue()) /eyeZPosition.getValue()) * near ;    

        //near plane for double frustrum
        double offset = eyeDisparity.getValue()/2.0d;
        double leftLeft, leftRight, rightLeft, rightRight;
        
        leftLeft = ((leftBottomCorner.x - eyeXOffset.getValue() + offset) / eyeZPosition.getValue()) * near ;      
        leftRight = ((rightBottomCorner.x - eyeXOffset.getValue() + offset) /eyeZPosition.getValue()) * near ;      
        
        rightLeft = ((leftBottomCorner.x - eyeXOffset.getValue() - offset) / eyeZPosition.getValue()) * near ;      
        rightRight = ((rightBottomCorner.x - eyeXOffset.getValue() - offset) /eyeZPosition.getValue()) * near ;      

        //for frustrum calculation
        FlatMatrix4d mat = new FlatMatrix4d();
	    FlatMatrix4d inv = new FlatMatrix4d();
        
        if ( viewingMode == 1 ) {
        	// We will use a trackball camera, but also apply an 
        	// arbitrary scale to make the scene and frustums a bit easier to see
        	// (noten part of the initializaiton of
        	// the tbc track ball camera, but thise the extra scale could have be is eaiser)
        	gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
            tbc.prepareForDisplay(drawable);
            gl.glScaled(15,15,15);        
            
            gl.glPushMatrix();
            {
            	tbc2.applyViewTransformation(drawable); // only the view transformation
                scene.display( drawable );
            }
            gl.glPopMatrix();

            //screen loop
            gl.glColor3f(1.0f, 1.0f, 0.0f);
            gl.glBegin(GL.GL_LINE_LOOP);
            {
                gl.glVertex2d( leftBottomCorner.x, leftBottomCorner.y);
                gl.glVertex2d( rightBottomCorner.x, rightBottomCorner.y);
                gl.glVertex2d( rightTopCorner.x,rightTopCorner.y);
                gl.glVertex2d( leftTopCorner.x,  leftTopCorner.y);
            }
            gl.glEnd();

            //eye
            gl.glPushMatrix();
            {
                gl.glColor3f(1.0f, 1.0f, 1.0f);
                gl.glTranslated(eyeXOffset.getValue(), eyeYOffset.getValue(), eyeZPosition.getValue());
                glut.glutSolidSphere(0.0125f, 80, 80);
            }
            gl.glPopMatrix();
          
            //single frustrum
            if(drawCenterEyeFrustum.getValue()) {
                gl.glPushMatrix();
                {
                    gl.glLoadIdentity();
                    gl.glFrustum(left, right, bottom, top, near, far);
                    gl.glGetDoublev(GLMatrixFunc.GL_MODELVIEW_MATRIX, mat.asArray(), 0);
                    mat.reconstitute();
	                inv.getBackingMatrix().invert(mat.getBackingMatrix());
                }
                gl.glPopMatrix();
                
                //draw the frustrum
                gl.glPushMatrix();
                {   
                    gl.glTranslated(eyeXOffset.getValue(), eyeYOffset.getValue(), eyeZPosition.getValue());
                	gl.glMultMatrixd(inv.asArray(), 0);
                    glut.glutWireCube(2.0f);
                }
                gl.glPopMatrix();
            }

            //draw focal plane
            gl.glPushMatrix();
            {
                gl.glLoadIdentity();
                gl.glFrustum(left, right, bottom, top, near, far);
                gl.glGetDoublev(GLMatrixFunc.GL_MODELVIEW_MATRIX, mat.asArray(), 0);
                mat.reconstitute();
                inv.getBackingMatrix().invert(mat.getBackingMatrix());
            }
            gl.glPopMatrix();

            //focal plane
            gl.glPushMatrix();
            {   
                gl.glTranslated(eyeXOffset.getValue(), eyeYOffset.getValue(), eyeZPosition.getValue());
                gl.glMultMatrixd(inv.asArray(), 0);
                gl.glColor3f(0.7f, 0.7f, 0.7f);
                gl.glBegin(GL.GL_LINE_LOOP);
                {
                    gl.glVertex3d(-1.0d, -1.0d , focalPlaneZPosition.getValue() );
                    gl.glVertex3d(1.0d, -1.0d ,  focalPlaneZPosition.getValue());
                    gl.glVertex3d(1.0d, 1.0d ,  focalPlaneZPosition.getValue());
                    gl.glVertex3d(-1.0d, 1.0d ,  focalPlaneZPosition.getValue());
                }
                gl.glEnd();
            }
            gl.glPopMatrix();

            //multiple frustrums
            if (drawEyeFrustums.getValue()) {
                //right
                gl.glPushMatrix();
                {
                    gl.glLoadIdentity();
                    gl.glFrustum(rightLeft, rightRight, bottom, top, near, far);
                    gl.glGetDoublev(GLMatrixFunc.GL_MODELVIEW_MATRIX, mat.asArray(), 0);
                    mat.reconstitute();
                    inv.getBackingMatrix().invert(mat.getBackingMatrix());
                }
                gl.glPopMatrix();
                
                //draw right
                gl.glPushMatrix();
                {
                    gl.glTranslated(eyeXOffset.getValue() + offset, eyeYOffset.getValue(), eyeZPosition.getValue());
                    
                    //eye
                    gl.glDisable(GLLightingFunc.GL_LIGHTING);
                    gl.glColor3f(0.0f, 1.0f, 1.0f);
                    glut.glutSolidSphere(0.0125f, 80, 80);
                    gl.glEnable(GLLightingFunc.GL_LIGHTING);

                    //frusturm
                    gl.glMultMatrixd(inv.asArray(), 0);
                    gl.glDisable(GLLightingFunc.GL_LIGHTING);
                    glut.glutWireCube(2.0f);
                    gl.glEnable(GLLightingFunc.GL_LIGHTING);  
                }
                gl.glPopMatrix();  
                
                
                //left
                gl.glPushMatrix();
                {
                    gl.glLoadIdentity();
                    gl.glFrustum(leftLeft, leftRight, bottom, top, near, far);
                    gl.glGetDoublev(GLMatrixFunc.GL_MODELVIEW_MATRIX, mat.asArray(), 0);
                    mat.reconstitute();
                    inv.getBackingMatrix().invert(mat.getBackingMatrix());
                }
                gl.glPopMatrix();
                
                //draw left
                gl.glPushMatrix();
                {
                    gl.glTranslated(eyeXOffset.getValue() - offset, eyeYOffset.getValue(), eyeZPosition.getValue());
                    
                    //eye
                    gl.glDisable(GLLightingFunc.GL_LIGHTING);
                    gl.glColor3f(1.0f, 0.0f, 0.0f);
                    glut.glutSolidSphere(0.0125f, 80, 80);
                    gl.glEnable(GLLightingFunc.GL_LIGHTING);

                    //frustrum
                    gl.glMultMatrixd(inv.asArray(), 0);
                    gl.glDisable(GLLightingFunc.GL_LIGHTING);
                    glut.glutWireCube(2.0f);
                    gl.glEnable(GLLightingFunc.GL_LIGHTING);  
                }
                gl.glPopMatrix();  
            }   
        } else if ( viewingMode == 2 ) {
            tbc.prepareForDisplay(drawable);
        	gl.glPushMatrix();
            {
                gl.glLoadIdentity();		
                glu.gluLookAt(eyeXOffset.getValue(), eyeYOffset.getValue(), eyeZPosition.getValue(), eyeXOffset.getValue(), eyeYOffset.getValue(), focalPlaneZPosition.getValue(), 0.0d, 1.0d, 0.0d);;

                gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glFrustum(left, right, bottom, top, near, far);
                            
                gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
                tbc2.applyViewTransformation(drawable); // only the view transformation
                scene.display(drawable);    
            }
        	gl.glPopMatrix();
        } else if ( viewingMode == 3 ) {            
        	tbc.prepareForDisplay(drawable);
        	gl.glClear(GL2.GL_ACCUM_BUFFER_BIT);
            gl.glPushMatrix();
            {
                for (int i = 0; i < samples.getValue(); i++) {
                    Point2d p = new Point2d();
                    int size = samples.getValue();
                    int sampleSize = fdp.getSampleSize();

                    if(size > sampleSize) {
                        size = sampleSize-1;
                    }

                    fdp.get(p, i, size);
                    
                    gl.glLoadIdentity();	
                    glu.gluLookAt(eyeXOffset.getValue() +  p.x * aperture.getValue(), eyeYOffset.getValue() + p.y * aperture.getValue(), eyeZPosition.getValue(), eyeXOffset.getValue() +  p.x * aperture.getValue(), eyeYOffset.getValue() +  p.y * aperture.getValue(), focalPlaneZPosition.getValue(), 0.0d, 1.0d, 0.0d);;
                    
					//  // Focal Length, offset for hor and ver
                    //  double lh, lv, offseth, offsetv;
                    //  lh = (-focalPlaneZPosition.getValue() + eyeZPosition.getValue()) * (rightTopCorner.x) / (eyeZPosition.getValue());
                    //  lv = (-focalPlaneZPosition.getValue() + eyeZPosition.getValue()) * (rightTopCorner.y) / (eyeZPosition.getValue());
                    //  offseth = p.x * aperture.getValue();
                    //  offsetv = p.y * aperture.getValue();
					
					// // calculate the new frustrum the frustrum is not working :(
                    // double ratio = near / (-focalPlaneZPosition.getValue() + eyeZPosition.getValue());  // the focal plane must not be at the same pos as the eye
                    // left = -( lh - offseth )/ ratio;
                    // right = ( lh + offseth )/ ratio;
                    // top =  ( lv + offsetv )/ ratio;
                    // bottom =  -( lv - offsetv )/ ratio;
					
					gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
                    gl.glLoadIdentity();
                    gl.glFrustum(left, right, bottom, top, near, far);
                                
                    gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
                    tbc2.applyViewTransformation(drawable); // only the view transformation
                    scene.display(drawable);    
                    gl.glAccum(GL2.GL_ACCUM, 1.0f/samples.getValue());
                }
                gl.glAccum(GL2.GL_RETURN, 1.0f);
            }
        	gl.glPopMatrix();        		
        } else if ( viewingMode == 4 ) {
            // left eye view
        	tbc.prepareForDisplay(drawable);
        	  
        	gl.glPushMatrix();
            {
                gl.glLoadIdentity();		
                glu.gluLookAt(eyeXOffset.getValue() - offset, eyeYOffset.getValue(), eyeZPosition.getValue(), eyeXOffset.getValue() - offset, eyeYOffset.getValue(), focalPlaneZPosition.getValue(), 0.0d, 1.0d, 0.0d);;
                
                gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glFrustum(leftLeft, leftRight, bottom, top, near, far);
                
                gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
                tbc2.applyViewTransformation(drawable); // only the view transformation
                scene.display(drawable);   
            }
            gl.glPopMatrix();
        } else if ( viewingMode == 5 ) {  
            // right eye view
        	tbc.prepareForDisplay(drawable);
        	  
        	gl.glPushMatrix();
            {
                gl.glLoadIdentity();		
                glu.gluLookAt(eyeXOffset.getValue() + offset, eyeYOffset.getValue(), eyeZPosition.getValue(), eyeXOffset.getValue() + offset, eyeYOffset.getValue(), focalPlaneZPosition.getValue(), 0.0d, 1.0d, 0.0d);;
                
                gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glFrustum(rightLeft, rightRight, bottom, top, near, far);
                
                gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
                tbc2.applyViewTransformation(drawable); // only the view transformation
                scene.display(drawable);   
            }
            gl.glPopMatrix();
        } else if ( viewingMode == 6 ) {            
            // draw the anaglyph view using glColouMask
            
        	//left
        	tbc.prepareForDisplay(drawable);
            gl.glPushMatrix();
            {
                gl.glColorMask(true, false, false, true);
                gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
                
                gl.glLoadIdentity();		
                glu.gluLookAt(eyeXOffset.getValue() - offset, eyeYOffset.getValue(), eyeZPosition.getValue(), eyeXOffset.getValue() - offset, eyeYOffset.getValue(), focalPlaneZPosition.getValue(), 0.0d, 1.0d, 0.0d);;
                
                gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glFrustum(leftLeft, leftRight, bottom, top, near, far);
                
                gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
                tbc2.applyViewTransformation(drawable); // only the view transformation
                scene.display(drawable);   
            }
        	gl.glPopMatrix();
        	
        	//right
            gl.glPushMatrix();
            {
        	    gl.glColorMask(false, true, true, true);
        	    gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
                
                gl.glLoadIdentity();		
                glu.gluLookAt(eyeXOffset.getValue() + offset, eyeYOffset.getValue(), eyeZPosition.getValue(), eyeXOffset.getValue() + offset, eyeYOffset.getValue(), focalPlaneZPosition.getValue(), 0.0d, 1.0d, 0.0d);;
                
                gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glFrustum(rightLeft, rightRight, bottom, top, near, far);
                
                gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
                tbc2.applyViewTransformation(drawable); // only the view transformation
                scene.display(drawable);   
            }
        	gl.glPopMatrix();
            
            //reset color mask
            gl.glColorMask(true, true, true, true);
        } else if ( viewingMode == 7 ) {            
            // draw the anaglyph view with depth of field blur (not quite working)
            tbc.prepareForDisplay(drawable);
            gl.glClear(GL2.GL_ACCUM_BUFFER_BIT);
            gl.glPushMatrix();
            {
                for (int i = 0; i < samples.getValue(); i++) {
                    Point2d p = new Point2d();
                    int size = samples.getValue();
                    int sampleSize = fdp.getSampleSize();

                    if(size > sampleSize) {
                        size = sampleSize-1;
                    }

                    fdp.get(p, i, size);
                    
                    //left
                    tbc.prepareForDisplay(drawable);
                    gl.glPushMatrix();
                    {
                        gl.glColorMask(true, false, false, true);
                        gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
                        
                        gl.glLoadIdentity();		
                        glu.gluLookAt(eyeXOffset.getValue() - offset +  p.x * aperture.getValue(), eyeYOffset.getValue() + p.y * aperture.getValue(), eyeZPosition.getValue(), eyeXOffset.getValue() +  p.x * aperture.getValue() - offset, eyeYOffset.getValue() +  p.y * aperture.getValue(), focalPlaneZPosition.getValue(), 0.0d, 1.0d, 0.0d);;
                        
                        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
                        gl.glLoadIdentity();
                        gl.glFrustum(leftLeft, leftRight, bottom, top, near, far);
                        
                        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
                        tbc2.applyViewTransformation(drawable); // only the view transformation
                        scene.display(drawable);   
                    }
                    gl.glPopMatrix();
                    
                    //right
                    gl.glPushMatrix();
                    {
                        gl.glColorMask(false, true, true, true);
                        gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
                        
                        gl.glLoadIdentity();
                        glu.gluLookAt(eyeXOffset.getValue() + offset +  p.x * aperture.getValue(), eyeYOffset.getValue() + p.y * aperture.getValue(), eyeZPosition.getValue(), eyeXOffset.getValue() +  p.x * aperture.getValue() + offset, eyeYOffset.getValue() +  p.y * aperture.getValue(), focalPlaneZPosition.getValue(), 0.0d, 1.0d, 0.0d);;
		
                        
                        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
                        gl.glLoadIdentity();
                        gl.glFrustum(rightLeft, rightRight, bottom, top, near, far);
                        
                        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
                        tbc2.applyViewTransformation(drawable); // only the view transformation
                        scene.display(drawable);   
                    }
                    gl.glPopMatrix();
                    gl.glAccum(GL2.GL_ACCUM, 1.0f/samples.getValue());
                }
                gl.glAccum(GL2.GL_RETURN, 1.0f);
            }
            gl.glPopMatrix();        	
            
            //reset color mask
            gl.glColorMask(true, true, true, true);
        }        
    }
}
