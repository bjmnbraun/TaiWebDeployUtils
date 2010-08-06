package TaiGameCore;

import static java.awt.event.KeyEvent.KEY_TYPED;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_TAB;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.TreeMap;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import processing.core.PApplet;
import processing.core.PMatrix3D;
import processing.opengl.PGraphicsOpenGL;
import BulletGame$1.BulletGame$1Engine$ABasicEngine.ModalDialog;


public class ProceGLHybrid extends KeyAdapter{
	public PApplet g;
	public int currentViewPortWidth;
	public int currentViewPortHeight;
	public int currentViewPortX;
	public int currentViewPortY;
	public ProceGLHybrid(PApplet hold){
		g = hold;
		if (g!=null){
			g.registerDispose(this);
		}
	}
	/*
	public ProceGLHybrid(ProceGLHybrid hold){
		g = hold.g;
	}
	*/
	/**
	 * Non-square pixel correction factor due to 0.0.1.1 pix mapping not matching game ar (640x480)
	 */
	public float NSPH = 640f/480;
	/**
	 * NOTE: in RATIOS of total width/ height.
	 */
	private double[] viewportStack = new double[]{0.0,0.0,1.0,1.0};
	public void viewport(double xp, double yp, double wp, double hp){
		viewportStack[0]=xp; viewportStack[1]=yp; viewportStack[2]=wp; viewportStack[3]=hp;
		PGraphicsOpenGL gog = (PGraphicsOpenGL)(g.g);
		GL gl = gog.gl; //Don't try this at home!
		gl.glViewport(currentViewPortX=(int)(xp*g.width),
				currentViewPortY=(int)((1-yp-hp)*g.height),
				currentViewPortWidth = (int)(wp*g.width),
				currentViewPortHeight = (int)(hp*g.height));
		//NSPH = currentViewPortWidth/(float)currentViewPortHeight; WRONG
	}
	/** For convenience, use a rectangle works as well **/
	public void viewport(Rectangle2D area){
		viewport(area.getMinX(),area.getMinY(),area.getWidth(),area.getHeight());
	}
	/**
	 * If the viewport doesn't hvae ar= wantedWidth/wantedHeight, you get squishing: That's desired behavior.
	 * 
	 * NOTE: this metohd causes the p5 matrixes and opengl's to be NONSYNCED> to return to normalcy, return to 
	 * the screen2D. 
	 */
	public void view3D(float wantedWidth, float wantedHeight){
		GLU glu = ((PGraphicsOpenGL)g.g).glu;
		GL2 gl = ((PGraphicsOpenGL)g.g).gl;

		gl.glMatrixMode(gl.GL_PROJECTION);	// Select The Projection Matrix
		gl.glLoadIdentity();		// Reset The Projection Matrix
		glu.gluPerspective(45.0f,wantedWidth/wantedHeight,.1f,100.0f);
		// Reset The Current Modelview Matrix

		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f,-g.sqrt(2)-1);		// Center The Triangle
	}
	/**
	 * If the viewport doesn't hvae ar= viewportWidth/viewPortHeight, you get squishing: That's desired behavior.
	 * 
	 * NOTE: calling this method resets the modelviewmatrix. Also desired behavior.
	 */
	public void screen2D(){
		GL2 gl = ((PGraphicsOpenGL)g.g).gl;
		
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		//Copy the viewport:
		double[] oldViewport = new double[]{viewportStack[0],viewportStack[1],viewportStack[2],viewportStack[3]};
		viewport(0,0,1,1);
		//Taken from PApplet.beginDraw
		gl.glMatrixMode(GL2.GL_PROJECTION);
		PMatrix3D projection = ((PGraphicsOpenGL)g.g).projection;
		float[] projectionFloats = new float[] {
				projection.m00, projection.m10, projection.m20, projection.m30,
				projection.m01, projection.m11, projection.m21, projection.m31,
				projection.m02, projection.m12, projection.m22, projection.m32,
				projection.m03, projection.m13, projection.m23, projection.m33
		};
		gl.glLoadMatrixf(projectionFloats, 0);


		g.perspective();
		g.camera();

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		// apply this modelview and get to work
		gl.glLoadIdentity();

		gl.glScalef(1, -1, 1);
		float n = (currentViewPortWidth-.5f)/2;
		float nY = (currentViewPortHeight-.5f)/2;
		gl.glTranslatef(g.width*n,g.height*nY,0);
		float mag = n+.5f;
		float mag2 = nY+.5f;
		float scX = 2f, scY = 2f;
		gl.glScalef(scX*mag,scY*mag2,1);
		viewport(oldViewport[0],oldViewport[1],oldViewport[2],oldViewport[3]);
	}
	public void screen2D4GL(float wantedWidth, float wantedHeight) {
		GL2 gl = ((PGraphicsOpenGL)g.g).gl;
		/*
		screen2D();
		 */
		float[] vals = g.getMatrix().get(null);
		//gl.glTranslatef(vals[3],vals[7],vals[11]);

		view3D(wantedWidth,wantedHeight);
		gl.glScalef(1/(wantedHeight/wantedWidth),-1,1);
		gl.glTranslatef(-1,-1,0);
		gl.glScalef(2,2,1/vals[11]);
	}
	/*
	public static abstract class TeGl extends TraceGL2{
		public TeGl(GL2 gl, PrintStream arg1,ProceGLHybrid g) {
			super(gl, arg1);
			ProceGLthis = g;
			draw(gl);
		}
		public ProceGLHybrid ProceGLthis;
		private static final PrintStream crapStream = new PrintStream(new OutputStream(){
			public void write(int b) throws IOException {
			}			
		});
		public TeGl(ProceGLHybrid gh){
			this(((PGraphicsOpenGL)gh.g.g).gl,crapStream,gh);
		}
		public abstract void draw(GL2 gl);
	}
*/
	/**
	 * Has capability to consume a "hotkey" with returning false.
	 */
	public boolean keyEventSyncSuper(){return true;};
	private ArrayList<KeyListener> klist = new ArrayList();
	public TreeMap<Integer, Boolean> keyboard = new TreeMap();
	/**
	 * key handling is done only in draw loops.
	 */
	public void handleInputAtBeginningOfDraw(){
		try {
			if (g.keyEvent==null){ //keyEvent is used for querying things that SHOULD be passed with keyTyped.
				if (g.keyCode==0)
					return; //No input this frame;
				g.keyEvent = new KeyEvent(g,KeyEvent.KEY_PRESSED,System.currentTimeMillis(),0,g.keyCode,'?');
			} else if (g.keyEvent.getID()==KEY_TYPED){
				int[] remapping = new int[]{
						10,VK_ENTER,
						8,VK_BACK_SPACE,
						27,VK_ESCAPE,
						9,VK_TAB,
						127,VK_DELETE,
						'z',KeyEvent.VK_Z,
						'Z',KeyEvent.VK_Z,
						'x',KeyEvent.VK_X,
						'X',KeyEvent.VK_X,
				};
				for(int k = 0; k < remapping.length; k+=2){
					if (g.keyEvent.getKeyChar()==remapping[k]){
						g.keyCode = remapping[k+1];
						g.keyEvent.setKeyCode(remapping[k+1]);
					}
				}
			}	
			if (g.keyEvent.getID()==KeyEvent.KEY_RELEASED){
				keyboard.put(g.keyEvent.getKeyCode(),false);
				g.keyCode=0; //Make sure to pass it through, neutralized. It's like a "cleanser".
				g.keyEvent.setKeyCode(0);
				g.keyEvent.setKeyChar('?');
			}
			if (g.keyEvent.isShiftDown()){
				keyboard.put(KeyEvent.VK_SHIFT,true);
			}
			if (g.keyEvent.isControlDown()){
				keyboard.put(KeyEvent.VK_CONTROL,true);
			}
			if (g.keyEvent.getID()==KeyEvent.KEY_PRESSED){
				keyboard.put(g.keyEvent.getKeyCode(),true);
			}

			//.out.println(g.keyEvent.getKeyChar()+" "+g.keyEvent.getKeyCode());
			if (keyEventSyncSuper()){
				//	System.out.println(g.keyEvent.getKeyCode()+" "+g.keyEvent.getKeyChar()+" "+g.keyEvent.getID());
				for(int newFirst = klist.size()-1; newFirst>=0; newFirst--){
					KeyListener kl = klist.get(newFirst);
					kl.keyPressed(g.keyEvent); //KEY_TYPED, OR KEY_PRESSED
					if (kl instanceof ModalDialog){
						break; //Nothing under a modaldialog gets keypresses.
					}
				}
			}
		} catch (ConcurrentModificationException f){
			//Nonfatal.
		} finally {
			g.keyEvent = null; //g.keyCode==0
		}
	}
//public boolean isShiftHeld, isCtrlHeld, isLeftHeld, isRightHeld, isUpHeld, isDownHeld;
	public void removeSubKeyListener(KeyListener kl) {
		klist.remove(kl);
	}
	public void addSubKeyListener(KeyListener kl){
		klist.add(kl);
	}
	/**
	 * Trivial utilities section. You can rely on these because I have no reason to take them out. 
	 */
	public void fill(int[] color){
		g.fill(color[0],color[1],color[2]);
	}
	public void stroke(int[] color){
		g.stroke(color[0],color[1],color[2]);
	}
	public int color(int[] color){
		return g.color(color[0],color[1],color[2]);
	}
	public void outlineViewport(){
		g.stroke(0);
		g.noFill();
		g.rect(
				1f/currentViewPortWidth,
				1f/currentViewPortHeight,
				1-2f/currentViewPortWidth,
				1-2f/currentViewPortHeight);
	}
	public void dispose(){
		
	}
	public void rect(Rectangle2D.Float r){
		g.rect(r.x,r.y,r.width,r.height);
	}
	/**
	 * A modified str.split(), where if 
	 * str.substring(str.length()-endCaseW).matches(regex), then an
	 * additional (empty) string is appended to the normal split result
	 * 
	 * NEW: additionally, the split is limited to 1 match.
	 */
	public String[] modSplit(String str, String regex, int endCaseW){
		boolean hasExtra = str.substring(str.length()-endCaseW).matches(regex);
		String[] got = str.split(regex,2);
		if (hasExtra){
			String[] toRet = new String[got.length+1];
			for(int k = 0; k < got.length; k++){
				toRet[k] = got[k];
			}
			toRet[got.length] = "";
			return toRet;
		} else {
			return got;
		}
	}
	public boolean truth(Boolean k){
		return !(k==null) && (k==true);
	}
}
