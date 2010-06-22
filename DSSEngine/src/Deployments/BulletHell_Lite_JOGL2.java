package Deployments;

import java.awt.BorderLayout;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.AWTGLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;

import processing.opengl.PGraphicsOpenGL;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.FPSAnimator;

/** Shows how to deploy an applet using JOGL. This demo must be
    referenced from a web page via an &lt;applet&gt; tag. */

public class BulletHell_Lite_JOGL2 extends BulletHell_Lite {
	private Animator animator;

	public void frameRate(float val){
		super.frameRate(val);
		
		/*
		if (animator!=null){
			animator.stop();
			animator = new FPSAnimator(canvas,(int)val);
			animator.start();
		}
		*/
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
		canvas.addGLEventListener(new Jogl2Adaptor());
		canvas.setSize(getSize());
		
		add(canvas, BorderLayout.CENTER);
		/*
		animator = new FPSAnimator(canvas, 60);
		animator.start();
		*/
		new Thread(){ public void run(){
		beginLoop();
		}		}.start();
	}	/**
	 *	Run Emulation. This method should not be called directly.
	 *	Instead use startThread();
	 *
	 *  Friday, March 19, 2010 3:05 PM: Framerate loop and logic shamelessly stolen from the processing.org project
	 */
	public float frameRate;
	private static final int frameRateTarget = 60;
	private long handleDraw_now;
	boolean running = true;
	public void beginLoop()
	{

		long beforeTime = System.nanoTime();
		long overSleepTime = 0L;		
		long frameRatePeriod = (long) (1e9 / frameRateTarget);
		long frameRateLastNanos = 0;
		handleDraw_now = System.nanoTime();

		int noDelays = 0;
		// Number of frames with a delay of 0 ms before the
		// animation thread yields to other running threads.
		final int NO_DELAYS_PER_YIELD = 15;

		while (running)
		{		
			//Calculate frameRate:
			long now = System.nanoTime();
			double rate = 1000000.0 / ((now - frameRateLastNanos) / 1000000.0);
			float instantaneousRate = (float) rate / 1000.0f;
			frameRate = (frameRate * 0.9f) + (instantaneousRate * 0.1f);

			//boolean draw_frame = !Throttle.skipFrame();

			//##########################################################################
			//##########################################################################
			
			// Draw one frame
			canvas.display();

			//##########################################################################
			//##########################################################################


			frameRateLastNanos = now;
			// wait for update & paint to happen before drawing next frame
			// this is necessary since the drawing is sometimes in a
			// separate thread, meaning that the next frame will start
			// before the update/paint is completed

			long afterTime = System.nanoTime();
			long timeDiff = afterTime - beforeTime;

			//System.out.println("time diff is " + timeDiff);
			long sleepTime = (frameRatePeriod - timeDiff) - overSleepTime;

			if (sleepTime > 0) {  // some time left in this cycle
				try {
					//          Thread.sleep(sleepTime / 1000000L);  // nanoseconds -> milliseconds
					Thread.sleep(sleepTime / 1000000L, (int) (sleepTime % 1000000L));
					noDelays = 0;  // Got some sleep, not delaying anymore
				} catch (InterruptedException ex) { }

				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
				//System.out.println("  oversleep is " + overSleepTime);

			} else {    // sleepTime <= 0; the frame took longer than the period
				//        excess -= sleepTime;  // store excess time value
				overSleepTime = 0L;

				if (noDelays > NO_DELAYS_PER_YIELD) {
					Thread.yield();   // give another thread a chance to run
					noDelays = 0;
				}
			}
			

			beforeTime = System.nanoTime();
		}

		// Loop has finished
	}


	public void stop() {
		running = false;
		super.stop();
		if (animator!=null){
		try {
			animator.stop();
		} catch (Throwable e){
			//D.
		}
		}
		System.gc();
	}

	/**
	 * Tada!
	 */

	public class Jogl2Adaptor implements GLEventListener {
		public void init(GLAutoDrawable drawable) {
			// Use debug pipeline
			// drawable.setGL(new DebugGL(drawable.getGL()));

			GL2 gl = drawable.getGL().getGL2();
			System.err.println("INIT GL IS: " + gl.getClass().getName());
			System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

			gl.setSwapInterval(1);
			
			AWTGLAutoDrawable awtDrawable = (AWTGLAutoDrawable) drawable;
			awtDrawable.addMouseListener(BulletHell_Lite_JOGL2.this);
			awtDrawable.addMouseMotionListener(BulletHell_Lite_JOGL2.this);
			awtDrawable.addKeyListener(BulletHell_Lite_JOGL2.this);
			awtDrawable.addFocusListener(BulletHell_Lite_JOGL2.this);
		}

		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
			GL2 gl = drawable.getGL().getGL2();
			BulletHell_Lite_JOGL2.this.resizeRenderer(width,height);
		}
		public void dispose(GLAutoDrawable drawable) {
			System.out.println("Gears.dispose: "+drawable);
		}
		public void display(GLAutoDrawable drawable) {
			GL2 gl = drawable.getGL().getGL2();
			superDisplay(gl);
		}
		public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
		}
	}
}