package BulletGame$1;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellatorCallback;
import javax.swing.JFrame;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.opengl.GLPersist;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.GLPersist.GLPersistCB;
import TaiGameCore.GameSprite;
import TaiGameCore.GameVirtualFS;
import TaiGameCore.P5GLExtend;
import TaiGameCore.SinCosLUT;
import TaiGameCore.GameSprite.GameGraphic;
import TaiGameCore.RelativelyTimed.RelativeTimeNode;
import TaiGameCore.RelativelyTimed.TimeSource;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;

/**
 * NOTE: If you instantiate THIS CLASS, you will get just that: no screens.
 * Instantiate a subclass to provide more and more screens. (See
 * bulletGame$1Engine$L{N}, so that N is large.)
 */
public abstract class BulletGame$1Engine$ABasicEngine extends P5GLExtend implements SinCosLUT {
	/**
	 * Game genesis follows
	 */
	public BulletGame$1Engine$ABasicEngine(JFrame holder, PApplet hold) {
		super(hold);
		
		/*
		AllocationRecorder.addSampler(new Sampler() {
			private boolean inStackTrace = false;
			    public void sampleAllocation(int count, String desc,
			    			Object newObj, long size) {
			    	if (!g.mousePressed){
			    		return;
			    	}
			    	if (g.frameCount<2){
			    		return;
			    	}
			    	
			    	if (inStackTrace){ 
			    		return;
			    	}
			    	inStackTrace = true;
			    	StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			    	inStackTrace = false;
			
			    	for(int i = 3; i < stackTrace.length && i < 10; i++){
			    		System.out.println(stackTrace[i]);
			    	}
			    	System.out.println("I just allocated the object " + newObj + 
			    			" of type " + desc + " whose size is " + size);
			    	if (count != -1) { 
			    		System.out.println("It's an array of size " + count); 
			    	}
			  }
		});
		*/
		
		this.holder = holder;
		try {
			AUDIO = new Minim(hold);
		} catch (Throwable e) { //Note to readers: I'm explicitly
			//catching any "ClassNotFound" exceptions with this.
			e.printStackTrace();
			System.err.println("WARNING: Audio unsupported.");
			AUDIO = null;
		}
		FILE_SYSTEM = new GameVirtualFS(hold);
		GAME_TIME = new TimeSource() {
			public double time() {
				return System.nanoTime() / 1e9;
			}
		};
		firstFrameGoToDefaultScreen = true;

		System.gc();
		sinLUT = new float[SC_PERIOD];
		cosLUT = new float[SC_PERIOD];
		initSinCosLookup();
		
		//GO!
		if (hold!=null){
			hold.registerDraw(this);
			hold.registerKeyEvent(this);
		}
	}

	// TO BE IMPLEMENTED AT THE BASE LAYER!
	public abstract BulletGameScreen SCREEN(int num); 

	public final TimeSource GAME_TIME;
	public Minim AUDIO;
	public GameVirtualFS FILE_SYSTEM;
	public boolean isResized = false;
	public boolean wantsClearImages = false;

	/**
	 * Frames seem to be more reliable. Hmm....
	 * @deprecated
	 */
	public boolean TIME_BASED_INTEGRATION = false;

	private JFrame holder;
	private Dimension restoreToDim = null; // Before maximizing.
	private Point oldLocation = null;
	private ArrayList<KeyEvent> toDeque = new ArrayList<KeyEvent>();
	private int needsCallMaximizeGame = 0;
	private Dimension lastSize = new Dimension();
	private boolean firstFrameGoToDefaultScreen = false;
	private Dimension lastDim = new Dimension();

	private BulletGameScreen currentScreen;
	private int[] dialogStoleThePreciousMouseCoord = new int[2];
	private ModalDialog currentDialog;

	public boolean hasCurrentDialog() {
		return currentDialog != null;
	}

	private BulletGameScreen shoeingInScreen;
	private TransitioningScreen midMan;

	// TEXT SHADER ATTRIBUTE MAP
	public static enum Shader1VertShader {
		// short corner : ATTR2, float4 data : ATTR3, float2 texSize : ATTR4
		Center_X("Center_X", GL.GL_FLOAT, 3, 0), // 
		Center_Y("Center_Y", GL.GL_FLOAT, 3, 1), //
		RectWidth("RectWidth", GL.GL_FLOAT, 4, 0), // TODO
		// nonsquare
		RectHeight("RectHeight", GL.GL_FLOAT, 4, 1), //
		Rotation("Rotation", GL.GL_FLOAT, 3, 3), //
		X_TexOffset("X_TexOffset", GL.GL_FLOAT, 5, 0), //
		Y_TexOffset("Y_TexOffset", GL.GL_FLOAT, 5, 1), //
		TexScaleX("TexScaleX", GL.GL_FLOAT, 5, 2), //
		TexScaleY("TexScaleY", GL.GL_FLOAT, 5, 3), //
		// DO NOT USE ATTRIBUTE 6.
		// tint("tint",GL.GL_FLOAT,)
		;
		public final String name;
		public final int Type;
		public final int attribNum;
		public final int attribOff;

		private Shader1VertShader(String name, int Type, int attribNum, int off) {
			this.name = name;
			this.Type = Type;
			this.attribNum = attribNum;
			this.attribOff = off;
		}
	}

	/*****************************************************************
	 *****************************************************************/
	/*****************************************************************
	 *****************************************************************/
	/*****************************************************************
	 *****************************************************************/
	/*****************************************************************
	 *****************************************************************/
	/*****************************************************************
	 *****************************************************************/
	/*****************************************************************
	 *****************************************************************/
	/*****************************************************************
	 *****************************************************************/
	/*****************************************************************
	 *****************************************************************/
	/*****************************************************************
	 *****************************************************************/
	/*****************************************************************
	 *****************************************************************/

	// END SUPER GLOBALS.
	public class TimeRenderer {
		{
			dFont = FILE_SYSTEM.getFont("Comfortaa-Thin-48.vlw");
		}
		PFont dFont;

		public void draw() {
			g.textMode(g.MODEL);
			g.textFont(dFont);
			g.textAlign(g.LEFT, g.TOP);
			g.fill(255, 238, 85);
			g.pushMatrix();
			g.scale(1f / currentViewPortWidth, 1f / currentViewPortHeight);
			g.text(String.format("%.2f",g.frameRate), -7, 5);
			g.popMatrix();
			// screen2D();
			/*
			 * screen2D4GL(640,480); GL gl = ((PGraphicsOpenGL)g.g).gl;
			 * gl.glPushMatrix(); gl.glColor3f(0,0,0); gl.glBegin(GL.GL_QUADS);
			 * gl.glVertex3f(0,0,0); gl.glVertex3f(0,.5f,0);
			 * gl.glVertex3f(.5f,.5f,0); gl.glVertex3f(.5f,0,0); gl.glEnd();
			 * gl.glPopMatrix();
			 */
		}
	}

	public static abstract class Transitioner {
		public abstract double getLength();

		public abstract void drawTrans(float lerpr);

		public static int WIPE = -1, SHUTTERS = -1, FLASH = -1,
		FADE_WITH_BLACK = -1;
	}

	public Transitioner[] transitions = new Transitioner[] {
			new Transitioner() {
				{
					Transitioner.SHUTTERS = 0;
				}

				public double getLength() {
					return .8;
				}

				public void drawTrans(final float lerpr) {
					final int[] numberings = new int[] { 0 };
					int numW = 6;
					int numH = 6;
					float bright = g.constrain(lerpr * 5, 0, 1);
					for (int x = 0; x < numW; x++) {
						for (int y = 0; y < numH; y++) {
							GLPersistCB xFormer = new GLPersistCB() {
								private int number = numberings[0]++;

								public void transform(GL2 gl) {
									gl.glTranslatef(.5f, .5f, 0f);
									float rot = g.constrain(1.05f - lerpr
											* (number / 40f + .95f), 0, 1) * 90;
									gl.glRotatef(rot, 1f, .8f, 0f);
									gl.glTranslatef(-.5f, -.5f, 0f);
								}
							};
							Rectangle2D.Float useRect = new Rectangle2D.Float(
									1f / numW * x, 1f / numH * y, 1f / numW,
									1f / numH);
							currentScreen.frozen.beginPersist(1,
									(int) (useRect.x * g.width),
									(int) (useRect.y * g.height),
									(int) (useRect.getMaxX() * g.width),
									(int) (useRect.getMaxY() * g.height));
							currentScreen.frozen.setSetColor(bright, bright,
									bright);
							currentScreen.frozen
							.set(
									-(int) (useRect.x * g.width),
									-(int) Math
									.ceil((1f - useRect.height - useRect.y)
											* g.height),
											bright, xFormer);
							currentScreen.frozen.endPersist();
						}
					}
				}
			}, new Transitioner() {
				{
					Transitioner.WIPE = 1;
				}

				public double getLength() {
					return .4;
				}

				public void drawTrans(final float lerpr) {
					currentScreen.frozen.beginPersist(1, 0, 0,
							(int) (g.width * lerpr), g.height);
					currentScreen.frozen.set(0, 0, 1, null);
					currentScreen.frozen.endPersist();
				}
			}, new Transitioner() {
				{
					Transitioner.FLASH = 2;
				}

				public double getLength() {
					return .4;
				}

				public void drawTrans(final float lerpr) {
					currentScreen.frozen.beginPersist(1, 0, 0, g.width,
							g.height);
					float bright = g.cos((1 - lerpr) * 1.5f * g.TWO_PI) * .5f + .5f;
					currentScreen.frozen.setSetColor(bright, bright, bright);
					currentScreen.frozen.set(0, 0, 1, null);
					currentScreen.frozen.endPersist();
				}
			},

			new Transitioner() {
				{
					Transitioner.FADE_WITH_BLACK = 3;
				}

				public double getLength() {
					return .6;
				}

				public void drawTrans(final float lerpr) {
					if (lerpr > .5f) {
						// Fade to black.
						currentScreen.frozen.beginPersist(1, 0, 0, g.width,
								g.height);
						float bright = g.cos((1 - lerpr) * g.TWO_PI) * .5f + .5f;
						currentScreen.frozen
						.setSetColor(bright, bright, bright);
						currentScreen.frozen.set(0, 0, 1, null);
						currentScreen.frozen.endPersist();
					} else {
						// Remove a black screen
						float alpha = lerpr * 2f;
						g.fill(0, 0, 0, alpha * 255);
						g.rect(0, 0, 1, 1);
					}
				}
			}, };

	public class TransitioningScreen {
		private RelativeTimeNode clock;
		private Transitioner currentTrans;
		private int nowScreen;
		private double outTime;

		public TransitioningScreen(int whichTrans, int now) {
			currentTrans = transitions[whichTrans];
			// Logic to decide behavior...
			outTime = currentTrans.getLength();
			nowScreen = now;
		}

		private Snapshotter snapshotter = new Snapshotter();
		private OverlayRender overlayer = new OverlayRender();
		public boolean warned = false; //Ignore this. Not important!

		public class Snapshotter {
			public void draw() {
				viewport(0, 0, 1, 1);
				screen2D();
				if (currentScreen.wantsFrozen && currentScreen.frozen == null) {
					((PGraphicsOpenGL) g.g).MAKE_MIPMAPS = false;
					// TODO this thing sucks.
					currentScreen.frozen = new GLPersist(g);
					currentScreen.frozen.beginPersist(1, GL.GL_LINEAR, 0, 0,
							g.width, g.height); // Copies on first time.
					currentScreen.frozen.endPersist();
					try {
						currentScreen.cleanup();
					} catch (Throwable e){
						e.printStackTrace();
					}
					FILE_SYSTEM.clearImages();
					// Trigger the next screen
					shoeingInScreen = SCREEN(nowScreen);

					shoeingInScreen.denyInput(true);
					g.registerDraw(midMan.overlayer);
				}
			}
		}

		public class OverlayRender {
			int frameSkips = 0;

			public void draw() {
				viewport(0, 0, 1, 1);
				screen2D();
				if (currentScreen.frozen != null) {
					if (clock != null && clock.time() > outTime) {
						currentScreen.frozen.cleanup();
						currentScreen.frozen = null;
						currentScreen = shoeingInScreen; // Note: this method
						// makes problems if
						// you have > 1
						// transition going!
						// watch out.
						shoeingInScreen = null;
						midMan = null;
						currentScreen.denyInput(false);
						g.unregisterDraw(snapshotter);
						g.unregisterDraw(overlayer);
					} else {
						javax.media.opengl.GL gl = ((PGraphicsOpenGL) g.g).gl;
						gl.glClear(javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT);
						final float lerpr = 1 - (float) ((clock == null ? 0
								: clock.time()) / outTime); // ==
						// useWidth

						// Begin transformation
						currentTrans.drawTrans(lerpr);
						// End transformation
						if (frameSkips++ == 2) {
							if (clock == null) {
								clock = new RelativeTimeNode(GAME_TIME);// Reset
							}
						}
					}
				} else if (clock.time() > 10) {
					System.err
					.println("Whoa, snapshotting this screen for the Transition took > 10 seconds!!!");
				}
			}
		}
	}

	public abstract class BulletGameScreen implements KeyListener {
		public BulletGameScreen() {

		}

		public void keyPressed(KeyEvent e) {
		};

		public void keyReleased(KeyEvent e) {
		};

		public void keyTyped(KeyEvent e) {
		};

		private long lastIgnoringInput = -1;

		public final void draw() {
			if (this != currentScreen && currentScreen != null) {
				// Then, if the currentScreen isn't ready to be disposed...
				if (currentScreen.frozen == null) {
					return;
				}
			}
			if (ignoringInput && currentDialog == null) {
				if (lastIgnoringInput == -1) {
					lastIgnoringInput = System.nanoTime();
				}
				// Fix the escape key freezeing feature
				if (g.mousePressed) {
					ignoringInput = false;
				}
				if ((System.nanoTime() - lastIgnoringInput) > .2e9) {
					ignoringInput = false;
				}
			} else {
				lastIgnoringInput = -1;
			}
			drawScreen();
		}

		public abstract void drawScreen();

		private boolean wantsFrozen = false;
		public GLPersist frozen;

		public void freeze() {
			wantsFrozen = true;
		}

		public abstract void cleanup();

		public boolean ignoringInput = false;

		private boolean specialModalDialogInputIgnore = false;

		public void denyInput(boolean b) {
			ignoringInput = b;
		}

		public boolean isInputBlocked() {
			return ignoringInput;
		}
	}

	public interface ModalDialogCallback<E extends ModalDialog> {
		public void dialogFinished(E self);
	}

	public abstract class ModalDialog implements KeyListener {
		private ModalDialogCallback cb;

		/*
		 * public ModalDialog(ModalDialogCallback onReturn){
		 * this(null,onReturn); }
		 */
		private BulletGameScreen parent;

		public ModalDialog(final BulletGameScreen parent,
				ModalDialogCallback onReturn) {
			this.parent = parent;
			if (currentDialog != null) {
				throw new RuntimeException(
				"Only one modal dialog allowed at a time.");
			}
			currentDialog = this;
			cb = onReturn;
			dialogStoleThePreciousMouseCoord[0] = -1; // invalidate old state
			// this is a problem when modaldialogs get added mid-frame-draw...
			dialogStoleThePreciousMouseCoord[1] = -1;
			parent.denyInput(true);
			parent.specialModalDialogInputIgnore = true;
			
			g.registerDraw(this);
			addSubKeyListener(this);
			closeOnEscape = new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						if (parent != null && !parent.specialModalDialogInputIgnore) {
							modalDialogEscapePressed = true;
							
							parent.specialModalDialogInputIgnore = true;
						}
					}
				}
			};
			addSubKeyListener(closeOnEscape);
		}

		private boolean modalDialogEscapePressed = false;
		private KeyListener closeOnEscape;

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}

		private boolean first = false;

		public final void draw() {
			try {
				drawUnprotected();
			} catch (OutOfMemoryError e) {
				crashCleanup();
			}
		}

		private void drawUnprotected() {
			// Fix the escape key freezing feature
			if (g.mousePressed) {
				parent.specialModalDialogInputIgnore = false;
			}

			if (dialogStoleThePreciousMouseCoord[0] != -1)
				g.mouseX = dialogStoleThePreciousMouseCoord[0];
			if (dialogStoleThePreciousMouseCoord[1] != -1)
				g.mouseY = dialogStoleThePreciousMouseCoord[1];
			if (drawDialog() || modalDialogEscapePressed) {
				currentDialog = null; // we have to get rid of it FIRST.
				g.unregisterDraw(this);
				removeSubKeyListener(closeOnEscape);
				removeSubKeyListener(this);
				if (cb != null)
					cb.dialogFinished(this); // then call the callback (may set
				// up a
				// new modal dialog to replace this
				// one).
				g.mousePressed = false; // Usability problem, if the next dialog
				// has
				// a button in the same place as the last one (require a new
				// click)
			}
		}

		/**
		 * This does nothing. The actual components of the modaldialog are
		 * already cleaned up; just worry about your subclass code please.
		 */
		public void cleanup() {
		}

		/**
		 * Return TRUE if this dialog is done!
		 */
		public abstract boolean drawDialog();
	}

	/**
	 * An emergency strategy which is able to return the application
	 * to a good-state after an OOM exception.
	 * Caveat: Ability to recover is dependent on programmer properly
	 * implementing the cleanup() method to produce memory-freeing effects.
	 */
	public void crashCleanup() {
		if (currentDialog != null) {
			currentDialog.cleanup();
			g.unregisterDraw(currentDialog);
			currentDialog = null;
		}
		currentScreen.cleanup();
		currentScreen = null;
		firstFrameGoToDefaultScreen = true;
		wantsClearImages = true;
	}

	public boolean SceneChange(int trans, int now) {
		if (midMan != null) {
			if (!midMan.warned){//Debug printout at most once.
				System.err.println("Transition not finished yet! Wait!");
			}
			midMan.warned  = true;
			return false;
		}
		// Ok, if we're doing a scene change, get RID of the modaldialog.
		currentDialog = null;
		currentScreen.denyInput(true);
		currentScreen.freeze();
		midMan = new TransitioningScreen(trans, now); // The trans overlays the
		// frozen on TOP
		g.registerDraw(midMan.snapshotter);
		return true;
	}

	public void maximizeGame() {
		if (restoreToDim != null) {
			// Return to restore.
			holder.setSize(restoreToDim);
			holder.setLocation(oldLocation);
			restoreToDim = null;
			return;
		}
		oldLocation = holder.getLocation();
		restoreToDim = holder.getSize();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension dim = toolkit.getScreenSize();
		holder.setSize(dim);
		holder.setLocation(new Point(0, 0));
		g.resize(dim);
		viewport(0, 0, 1, 1);
	}

	public void keyEvent(KeyEvent e) {
		toDeque.add(e);
	}

	public void draw() {
		try {
			drawUnprotected();
		} catch (OutOfMemoryError e) {
			crashCleanup();
		}
	}

	private void drawUnprotected() {
		if (firstFrameGoToDefaultScreen) {
			currentScreen = SCREEN(0); // At this point, we can be sure our
			// context is valid.
			firstFrameGoToDefaultScreen = false;
		}
		// Do we need to flush images?
		if (wantsClearImages) {
			wantsClearImages = false;
			FILE_SYSTEM.clearImages();
		}

		// Ok, if we have a dialog, temporarily hide the mouse coords until it
		// draws:
		if (currentDialog != null) {
			dialogStoleThePreciousMouseCoord[0] = g.mouseX;
			dialogStoleThePreciousMouseCoord[1] = g.mouseY;
			g.mouseX = -1;
			g.mouseY = -1;
		}

		while (toDeque.size() > 0) {
			g.keyEvent = toDeque.remove(0);
			if (toDeque.size() > 0)
				handleInputAtBeginningOfDraw();
		}
		handleInputAtBeginningOfDraw();
		if (needsCallMaximizeGame > 0) {
			needsCallMaximizeGame = 0;
			maximizeGame();
		}
		Dimension newDim = new Dimension(g.width, g.height);
		if (!lastDim.equals(lastDim = newDim)) {
			isResized = true;
		} else {
			isResized = false;
		}
		// Just the basics:
		g.background(255);
		viewport(0, 0, 1, 1);
		screen2D(); // Game native resolution. Some pages may use less / more or
		// other random things.
		g.noSmooth();
		((PGraphicsOpenGL) g.g).gl.glDisable(GL.GL_MULTISAMPLE);
	}

	public boolean keyEventSyncSuper() {
 		if (g.keyEvent.isAltDown()
				&& g.keyEvent.paramString().contains("keyChar=Enter")) {
			needsCallMaximizeGame = 1;
			return false;
		}
		return true;
	}

	public void drawGameGraphic(GameGraphic gfx) {
		drawGameGraphic(gfx, .5f, .5f, 1, 1, 0);
	}

	public void drawGameGraphic(GameSprite gfx, int frame, float a, float b,
			float c, float d, float rotation) {
		GameGraphic toDraw = null;
		if (gfx.frame.length >= frame + 1) {
			toDraw = gfx.frame[frame];
		}
		drawGameGraphic(toDraw, a, b, c, d, rotation);
	}

	public GameGraphic getGameGraphicG(GameSprite gfx, int frame) {
		GameGraphic toDraw = null;
		if (gfx.frame.length >= frame + 1) {
			toDraw = gfx.frame[frame];
		}
		return toDraw;
	}

	public PImage getGameGraphic(GameSprite gfx, int frame) {
		GameGraphic toDraw = getGameGraphicG(gfx, frame);
		return getGameGraphic(toDraw);
	}

	public PImage getGameGraphic(GameGraphic toDraw) {
		String fname = getImgUrlFromGraphic(toDraw);
		PImage got = FILE_SYSTEM.getImg(fname);
		return got;
	}

	public String getImgUrlFromGraphic(GameGraphic toDraw) {
		String fname = "[default]";
		if (toDraw != null) {
			fname = toDraw.filename;
		}
		return fname;
	}

	public void drawGameGraphic(GameGraphic gfx, float a, float b, float c,
			float d, float rotation) {
		PImage got = getGameGraphic(gfx);
		int[] r = getGameGraphicPixels(gfx, got);
		drawGameGraphic(r, got, a, b, c, d, rotation);
	}

	public int[] getGameGraphicPixels(GameGraphic gfx, PImage got) {
		int[] r = null;
		if (gfx != null) {
			r = gfx.rect;
		}
		if (r == null) {
			r = new int[] { -1, -1, -101, -101 };
		}
		for (int k = 0; k <= 2; k += 2) {
			if (r[k] < 0) {
				r[k] = (int) ((-r[k] - 1) * got.width / 100.);
			}
		}
		for (int k = 1; k <= 4; k += 2) {
			if (r[k] < 0) {
				r[k] = (int) ((-r[k] - 1) * got.height / 100.);
			}
		}
		return r;
	}

	public int[] getGameGraphicPixelsGL(GameGraphic gfx, PImage got) {

		int[] r = null;
		if (gfx != null) {
			r = gfx.rect;
		}
		if (r == null) {
			r = new int[] { -1, -1, -101, -101 };
		}
		for (int k = 0; k <= 2; k += 2) {
			if (r[k] < 0) {
				r[k] = (int) ((-r[k] - 1) * got.width / 100.);
			}
		}
		for (int k = 1; k <= 4; k += 2) {
			if (r[k] < 0) {
				r[k] = (int) ((-r[k] - 1) * got.height / 100.);
			}
		}
		return r;
	}

	public void drawGameGraphicGL(GameSprite gfx, int frame, float a, float b,
			float c, float d, float rotation) {
		GameGraphic toDraw = null;
		if (gfx.frame.length >= frame + 1) {
			toDraw = gfx.frame[frame];
		}
		drawGameGraphicGL(toDraw, a, b, c, d, rotation);
	}

	public void drawGameGraphicGL(GameGraphic gfx, float a, float b, float c,
			float d, float rotation) {
		PImage got = getGameGraphic(gfx);
		int[] r = getGameGraphicPixelsGL(gfx, got);
		drawGameGraphicGL(r, got, a, b, c, d, rotation);
	}

	/**
	 * Use screen2D4GL(wantedWidth, wantedHeight)(); first.
	 */
	public void drawGameGraphicGL(int[] r, PImage got, float a, float b,
			float c, float d, float rotation) {
		// rotation is in degrees
		float[] t = predrawGameGraphicGL(got, r);
		GL gl = ((PGraphicsOpenGL) g.g).gl;
		duringDrawGameGraphicGL(t, a, b, c, d, rotation, 0);
		gl.glDisable(GL.GL_TEXTURE_2D);
	}

	/**
	 * When you're done, use gl.glDisable(GL.GL_TEXTURE_2D);
	 * 
	 * @param r
	 */
	public float[] predrawGameGraphicGL(PImage got, int[] r) {
		GL gl = ((PGraphicsOpenGL) g.g).gl;
		gl.glEnable(GL.GL_TEXTURE_2D);
		int[] t = ((PGraphicsOpenGL) g.g).bindTexture(got);
		float[] t2 = new float[] { t[0], t[1] };
		/**
		 * We need these texcoords: gl.glTexCoord2d(r[0]/t[0],
		 * (r[1]+r[3])/t[1]); gl.glTexCoord2d(r[0]/t[0], (r[1])/t[1]);
		 * gl.glTexCoord2d((r[0]+r[2])/t[0], (r[1])/t[1]);
		 * gl.glTexCoord2d((r[0]+r[2])/t[0], (r[1]+r[3])/t[1]);
		 **/
		float[] texCoords = new float[] { r[0] / t2[0], (r[1] + r[3]) / t2[1],
				r[0] / t2[0], (r[1]) / t2[1], (r[0] + r[2]) / t2[0],
				(r[1]) / t2[1], (r[0] + r[2]) / t2[0], (r[1] + r[3]) / t2[1], };
		return texCoords;
	}

	public final void duringDrawGameGraphicGL(float[] allTexCoords,
			float a, float b, float c, float d, float rotation, float z) {
		GL2 gl = ((PGraphicsOpenGL) g.g).gl;
		//Ok, we have pixels.
		int ind = getLUTLocus(rotation);
		float cX = cosLUT[ind]*c/2;
		float sX = sinLUT[ind]*c/2;
		float cY = cosLUT[ind]*d/2;
		float sY = sinLUT[ind]*d/2;
		int x, y;
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2f(allTexCoords[0], allTexCoords[1]);
		x = -1;
		y = 1;
		gl.glVertex3f(cX*x-sY*y+a,sX*x+cY*y+b, z); // V3
		gl.glTexCoord2f(allTexCoords[2], allTexCoords[3]);
		x = -1;
		y = -1;
		gl.glVertex3f(cX*x-sY*y+a,sX*x+cY*y+b, z); // V3
		gl.glTexCoord2f(allTexCoords[4], allTexCoords[5]);
		x = 1;
		y = -1;
		gl.glVertex3f(cX*x-sY*y+a,sX*x+cY*y+b, z); // V3
		gl.glTexCoord2f(allTexCoords[6], allTexCoords[7]);
		x = 1;
		y = 1;
		gl.glVertex3f(cX*x-sY*y+a,sX*x+cY*y+b, z); // V3
		// g.image(got,-c/2,-d/2,c,d, r[0], r[1], r[2]+ r[0], r[3]+ r[1])
		gl.glEnd();
	}

	public void draw3DPolygonGL(float[][] corners) {
		GL2 gl = ((PGraphicsOpenGL) g.g).gl;
		gl.glBegin(GL2.GL_POLYGON);
		for (float[] corner : corners) {
			gl.glVertex3f(corner[0], corner[1], corner[2]);
		}
		gl.glEnd();
	}

	/**
	 * a,b is the CENTER of the rectangle c,d, are width/height rotation is
	 * rotation
	 */
	public void drawGameGraphic(int[] r, PImage got, float a, float b, float c,
			float d, float rotation) {
		// Ok, we have pixels.
		g.translate(a, b);
		g.rotate(rotation);
		g
		.image(got, -c / 2, -d / 2, c, d, r[0], r[1], r[2] + r[0], r[3]
		                                                             + r[1]);
		g.rotate(-rotation);
		g.translate(-a, -b);
	}

	public class GameSpriteAnimator {
		public GameSpriteAnimator(GameSprite on) {
			this.on = on;
		}

		private GameSprite on;
		private boolean musicPlaying = false;
		private AudioPlayer currentMusic = null;
		private long now = -1;
		private int cFrame = -1;
		public boolean done = false;
		private GameSprite.GameGraphic currentFrame;

		/**
		 * Draws the sprite in the current viewport.
		 * 
		 * Returs true if the rendering is complete.
		 */
		public boolean draw() {
			if (!musicPlaying) {
				musicPlaying = true;
				if (on.sound != null && on.sound.filename.length() > 0) {
					currentMusic = FILE_SYSTEM.loadAudioFile(on.sound.filename,
							AUDIO);
					currentMusic.play();
				}
			}
			advanceFrame();
			try {
				drawGameGraphic(currentFrame);
			} catch (Throwable e) {
				// Do nothing.
			}
			return done;
		}

		public void advanceFrame() {
			if (now == -1) {
				now = System.nanoTime();
			}
			long tdiff = System.nanoTime() - now;
			int potentialNext = cFrame + 1;
			try {
				if (potentialNext >= on.keyFrames.length) {
					if (on.loopAfter != -1) {
						potentialNext = 0;
					} else {
						done = true;
						return;
					}
				}
				long required = on.keyFrames[potentialNext];
				required *= on.frameResolution;
				if (tdiff >= required) {
					cFrame = potentialNext;
					if (cFrame >= on.frame.length) {
						done = true;
						return;
					}
					currentFrame = on.frame[cFrame];
				}
			} catch (Throwable e) {
				e.printStackTrace();
				return;
			}
		}

		public void cleanup() {
			if (currentMusic != null) {
				currentMusic.pause();
				currentMusic.close();
			}
		}
	}

	public void dispose() {
		try {
			AUDIO.stop();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			if (currentDialog != null)
				currentDialog.cleanup();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			if (currentScreen != null)
				currentScreen.cleanup();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/*
	 * Tessellator callback implemenation with all the callback routines. YOu
	 * could use GLUtesselatorCallBackAdapter instead. But
	 */
	public class tessellCallBack implements GLUtessellatorCallback {
		private GL2 gl;
		private GLU glu;
		private PImage texture;
		private PGraphicsOpenGL g;
		private int[] texData;
		double wAS, hAS;

		public tessellCallBack(GL2 gl, GLU glu, PImage texture,
				PGraphicsOpenGL g) {
			this.gl = gl;
			this.glu = glu;
			this.texture = texture;
			this.g = g;
		}

		public void begin(int type) {
			gl.glEnable(GL.GL_TEXTURE_2D);
			texData = g.bindTexture(texture);
			wAS = texture.width / (double) texData[0];
			hAS = texture.height / (double) texData[1];
			gl.glBegin(type);
		}

		public void end() {
			gl.glEnd();
			gl.glDisable(GL.GL_TEXTURE_2D);
		}

		private boolean darken = false;

		public void setDarken(boolean darken) {
			this.darken = darken;
		}

		public void vertex(Object vertexData) {
			double[] pointer;
			if (vertexData instanceof double[]) {
				pointer = (double[]) vertexData;

				/*
				 * if (pointer.length == 6) gl.glColor4d(1,1,1,pointer[5]);
				 */
				if (darken) {
					gl.glColor4d(.2f, .2f, .2f, 1f);
				} else {
					gl.glColor4d(1, 1, 1, 1f - pointer[5]);
				}
				gl.glTexCoord2f(PApplet.constrain((float) (pointer[0] * wAS),
						0, 1), PApplet.constrain((float) (pointer[1] * hAS), 0,
								1));
				gl.glVertex3dv(pointer, 0);
			}
		}

		public void vertexData(Object vertexData, Object polygonData) {
		}

		/*
		 * combineCallback is used to create a new vertex when edges intersect.
		 * coordinate location is trivial to calculate, but weight[4] may be
		 * used to average color, normal, or texture coordinate data. In this
		 * program, color is weighted.
		 */
		public void combine(double[] coords, Object[] data, //
				float[] weight, Object[] outData) {
			double[] vertex = new double[6];
			int i;

			vertex[0] = coords[0];
			vertex[1] = coords[1];
			vertex[2] = coords[2];
			for (i = 3; i < 6/* 7OutOfBounds from C! */; i++) {
				if(data[0] !=null)
					vertex[i] = weight[0] * ((double[]) data[0])[i];
				if (data[1]!=null)
					vertex[i] += weight[1] * ((double[]) data[1])[i];
				if (data[2]!=null)
					vertex[i] += weight[2] * ((double[]) data[2])[i];
				if (data[3]!=null)
					vertex[i] += weight[3] * ((double[]) data[3])[i];
			}
			outData[0] = vertex;
		}

		public void combineData(double[] coords, Object[] data, //
				float[] weight, Object[] outData, Object polygonData) {
		}

		public void error(int errnum) {
			String estring;

			estring = glu.gluErrorString(errnum);
			System.err.println("Tessellation Error: " + estring);
		}

		public void beginData(int type, Object polygonData) {
		}

		public void endData(Object polygonData) {
		}

		public void edgeFlag(boolean boundaryEdge) {
		}

		public void edgeFlagData(boolean boundaryEdge, Object polygonData) {
		}

		public void errorData(int errnum, Object polygonData) {
		}

	}// tessellCallBack

	// SIN COS LOOKUP

	// declare arrays and params for storing sin/cos values
	public final float sinLUT[];
	public final float cosLUT[];

	// set table precision to radians
	private static final float SC_PRECISION = 1f / 360f * .13805f * 2;

	// caculate reciprocal for conversions
	private static final float SC_INV_PREC = 1 / SC_PRECISION;

	// compute required table length
	private static final int SC_PERIOD = (int) (PApplet.TWO_PI * SC_INV_PREC);

	// init sin/cos tables with values
	// should be called from setup()
	private final void initSinCosLookup() {
		for (int i = 0; i < SC_PERIOD; i++) {
			sinLUT[i] = (float) Math.sin(i * SC_PRECISION);
			cosLUT[i] = (float) Math.cos(i * SC_PRECISION);
		}
	}

	public final int getLUTLocus(float rad) {
		int locus = ((int) (rad * SC_INV_PREC)) & (SC_PERIOD-1);
		if (locus < 0) {
			locus += SC_PERIOD;
		}
		return locus;
	}

	public final float CosLUT(float rad) {
		return cosLUT[getLUTLocus(rad)];
	}

	public final float SinLUT(float rad) {
		return sinLUT[getLUTLocus(rad)];
	}
}
