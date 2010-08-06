package processing.opengl;

import java.awt.BorderLayout;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.AWTGLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;

import Deployments.BulletHell_Lite;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;

/** Shows how to deploy an applet using JOGL. This demo must be
    referenced from a web page via an &lt;applet&gt; tag. */

public class GearsApplet extends BulletHell_Lite {
	private Animator animator;

	public void frameRate(float val){
		super.frameRate(val);
		if (animator!=null){
			animator.stop();
			animator = new FPSAnimator(canvas,(int)val);
			animator.start();
		}
	}
	
	private PGraphicsOpenGL superG;
	public void superDisplay(GL2 gl){
		if (superG==null){
			if (g instanceof PGraphicsOpenGL){
				superG = (PGraphicsOpenGL)g;
				hijackAnimationThread();
			}
		} else {
			superG.gl = gl;
			handleDraw();
		}
	}
	private GLCanvas canvas;
	public void init() {
		super.init();
		setLayout(new BorderLayout());
		canvas = new GLCanvas();
		canvas.addGLEventListener(new Gears());
		canvas.setSize(getSize());
		add(canvas, BorderLayout.CENTER);
		animator = new FPSAnimator(canvas, 60);
		animator.start();
	}

	public void stop() {
		super.stop();
		animator.stop();
	}

	/**
	 * Tada!
	 */

	public class Gears implements GLEventListener {
		public void init(GLAutoDrawable drawable) {
			// Use debug pipeline
			// drawable.setGL(new DebugGL(drawable.getGL()));

			GL2 gl = drawable.getGL().getGL2();
			System.err.println("INIT GL IS: " + gl.getClass().getName());
			System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

			gl.setSwapInterval(1);
			
			AWTGLAutoDrawable awtDrawable = (AWTGLAutoDrawable) drawable;
			awtDrawable.addMouseListener(GearsApplet.this);
			awtDrawable.addMouseMotionListener(GearsApplet.this);
			awtDrawable.addKeyListener(GearsApplet.this);
			awtDrawable.addFocusListener(GearsApplet.this);
		}

		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
			GL2 gl = drawable.getGL().getGL2();
		}
		public void dispose(GLAutoDrawable drawable) {
			System.out.println("Gears.dispose: "+drawable);
		}
		public void display(GLAutoDrawable drawable) {
			GL2 gl = drawable.getGL().getGL2();
			superDisplay(gl);
		}

		public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
	}

}

