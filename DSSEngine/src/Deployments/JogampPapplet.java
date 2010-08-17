package Deployments;

import java.awt.BorderLayout;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.AWTGLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;

import processing.opengl.PGraphicsOpenGL;
import BulletGame$1.BulletGame$1Engine$L1$2$P5Link;

/** Shows how to deploy an applet using JOGL. This demo must be
    referenced from a web page via an &lt;applet&gt; tag. */

public class JogampPapplet extends BulletGame$1Engine$L1$2$P5Link {
	public void frameRate(float val) {
		super.frameRate(val);

		frameRateTarget = val * 2;
	}

	public String getSketchRenderer(){
		//Prevent the p3d from being defaulted to on the first frame.
		//Thus, do not render anything in setup()!!!
		return OPENGL;
	}
	public void setSynchWithFirstGlDisplay(syncGLAwtTransfer r){
		syncWithFirstGLDisplay = r;
	}
	public interface syncGLAwtTransfer {
		public boolean isAWTLoadFinished();
	}
	private syncGLAwtTransfer syncWithFirstGLDisplay = new syncGLAwtTransfer(){
		public boolean isAWTLoadFinished() {
			return true;
		}
	};
	/**
	 * Don't load the game, just test a red screen.
	 */
	private boolean TEST_APPLET = false;
	
	private boolean started = false;
	public void start() {
		if (started){
			return;
		}
		started = true;
		
		setLayout(new BorderLayout());
		canvas = new GLCanvas();
		canvas.addGLEventListener(new Jogl2Adaptor());
		add(canvas, BorderLayout.CENTER);
		
		if (!TEST_APPLET){
			JogampPapplet.super.init();
		}
		
		new Thread() {
			public void run() {
				beginLoop();
			}
		}.start();
	}

	private PGraphicsOpenGL superG;
	private boolean awtLoadFinished = false;
	public boolean isAWTLoadFinished(){
		return awtLoadFinished;
	}
	
	public void superDisplay(GL2 gl) {
		if (superG == null && g!=null) {
			superG = (PGraphicsOpenGL) g;
			superG.gl = gl;
		}
		if (superG!=null){
			superG.gl = gl;
			handleDraw();
		}
	}

	private GLCanvas canvas;

	public void init() {
		
	}

	/**
	 *	Run Emulation. This method should not be called directly.
	 *	Instead use startThread();
	 *
	 *  Friday, March 19, 2010 3:05 PM: Framerate loop and logic shamelessly stolen from the processing.org project
	 */
	public float frameRate;
	private float frameRateTarget = 60;
	boolean running = true;

	public void beginLoop() {

		long beforeTime = System.nanoTime();
		long overSleepTime = 0L;
		long frameRatePeriod = (long) (1e9 / frameRateTarget);
		long frameRateLastNanos = 0;

		int noDelays = 0;
		// Number of frames with a delay of 0 ms before the
		// animation thread yields to other running threads.
		final int NO_DELAYS_PER_YIELD = 15;

		while (running) {
			//Calculate frameRate:
			long now = System.nanoTime();
			double rate = 1000000.0 / ((now - frameRateLastNanos) / 1000000.0);
			float instantaneousRate = (float) rate / 1000.0f;
			frameRate = (frameRate * 0.9f) + (instantaneousRate * 0.1f);

			//boolean draw_frame = !Throttle.skipFrame();

			//##########################################################################
			//##########################################################################

			//Second frame is first ability to swap
			if (frameCount>=2 && !awtLoadFinished){
				if (syncWithFirstGLDisplay.isAWTLoadFinished()){
					awtLoadFinished = true;
				}
			}
			
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

			if (sleepTime > 0) { // some time left in this cycle
				try {
					//          Thread.sleep(sleepTime / 1000000L);  // nanoseconds -> milliseconds
					Thread.sleep(sleepTime / 1000000L,
							(int) (sleepTime % 1000000L));
					noDelays = 0; // Got some sleep, not delaying anymore
				} catch (InterruptedException ex) {
				}

				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
				//System.out.println("  oversleep is " + overSleepTime);

			} else { // sleepTime <= 0; the frame took longer than the period
				//        excess -= sleepTime;  // store excess time value
				overSleepTime = 0L;

				if (noDelays > NO_DELAYS_PER_YIELD) {
					Thread.yield(); // give another thread a chance to run
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
			System.err.println("Chosen GLCapabilities: "
					+ drawable.getChosenGLCapabilities());

			gl.setSwapInterval(1);

			AWTGLAutoDrawable awtDrawable = (AWTGLAutoDrawable) drawable;
			awtDrawable.addMouseListener(JogampPapplet.this);
			awtDrawable.addMouseMotionListener(JogampPapplet.this);
			awtDrawable.addKeyListener(JogampPapplet.this);
			awtDrawable.addFocusListener(JogampPapplet.this);
		}

		public void reshape(GLAutoDrawable drawable, int x, int y, int width,
				int height) {
			GL2 gl = drawable.getGL().getGL2();
			if (g!=null && (width!=g.width || height != g.height)){
				System.out.println("RESIZE");
				JogampPapplet.this.resizeRenderer(width, height);
			}
		}

		public void dispose(GLAutoDrawable drawable) {
			System.out.println("Gears.dispose: " + drawable);
		}

		public void display(GLAutoDrawable drawable) {
			GL2 gl = drawable.getGL().getGL2();
			superDisplay(gl);
		}

		public void displayChanged(GLAutoDrawable drawable,
				boolean modeChanged, boolean deviceChanged) {
		}
	}

	public void resizeRenderer2(int iwidth, int iheight) {
		resizeRenderer(iwidth, iheight);
	}
}