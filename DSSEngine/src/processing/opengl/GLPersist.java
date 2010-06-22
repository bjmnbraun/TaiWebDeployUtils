package processing.opengl;


import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix3D;

public class GLPersist implements PConstants{
	private PApplet app;
	private int glMipmappingMode = GL.GL_NEAREST;
	private float COPYQUALITY = 1f;
	private boolean COPYQUALITYNOTONE = false;
	private int GlTargetDrawBuffer = -1;
	private int TEXIMAGEH = -1, TEXIMAGEW = -1;
	private int lastImgStored = -1;
	private int xRect1, yRect1;
	private int gWidth, gHeight;
	public GLPersist(PApplet app){
		this.app = app;
	}
	private boolean inPersist = false;
	public void beginPersist(float Quality, int x1, int y1, int x2, int y2){
		beginPersist(Quality,GL.GL_NEAREST,x1,y1,x2,y2);
	}
	public void beginPersist(float Quality, int GLMipMappingMode, int x1, int y1, int x2, int y2){
		if (inPersist){
			app.die("Nested persistance is not allowed.");
		}
		glMipmappingMode = GLMipMappingMode;
		inPersist = true;
		COPYQUALITY = Quality;
		COPYQUALITYNOTONE = COPYQUALITY!=1f;
		xRect1 = x1;
		gHeight = y2-y1;
		yRect1 = app.height-y2;
		gWidth = x2-x1; 
		PGraphics g = app.g;
		GL2 gl = ((PGraphicsOpenGL)g).gl;
		int ViewWidth= (int)(gWidth*COPYQUALITY);
		int ViewHeight = (int)(COPYQUALITY*gHeight);
		gl.glViewport(xRect1,yRect1,ViewWidth,ViewHeight);

		if (lastImgStored==-1){
			int[] res = new int[1];
			gl.glGetIntegerv(gl.GL_DRAW_BUFFER,res,0);
			GlTargetDrawBuffer = res[0];
			int[] txtnumber = new int[1];
			gl.glGenTextures(1, txtnumber, 0);                                // Create 1 Texture
			lastImgStored = txtnumber[0];
			copy();
		}
		
		setSetColor(1,1,1);
	}
	private void checkPersist(){
		if(!inPersist)
			app.die("This method may only be called after beginPersist");
	}
	public void copy(){
		checkPersist();
		PGraphics g = app.g;
		GL2 gl = ((PGraphicsOpenGL)g).gl;


		g.flush();
		int useWidth = (int)(gWidth * COPYQUALITY);
		int useHeight = (int)(gHeight * COPYQUALITY);
		CopyBuffer(gl, xRect1, yRect1, useWidth, useHeight); //Copies upwards
	}
	public void set(){
		checkPersist();
		set(0,0,1);
	}
	public interface GLPersistCB{
		public void transform(GL2 gl);
	}
	public void set(float xOffset, float yOffset, float alpha){
		set(xOffset,yOffset,alpha,null);
	}
	public void set(float xOffset, float yOffset, float alpha, GLPersistCB cb){
		checkPersist();
		PGraphics g = app.g;

		GL2 gl = ((PGraphicsOpenGL)g).gl;
		screen2D4GL(app.g,gWidth,gHeight);
		xOffset/=gWidth;
		yOffset/=gHeight;
		xOffset/=COPYQUALITY;
		yOffset/=COPYQUALITY;
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		if (cb!=null){
			gl.glPushMatrix();
			cb.transform(gl);
		}
		drawToScreen(gl, lastImgStored, xOffset, yOffset, alpha);
		if (cb!=null){
			gl.glPopMatrix();
		}
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		unScreen2D4GL(g,gl);
	}
	public void endPersist(){
		checkPersist();
		PGraphics g = app.g;
		GL2 gl = ((PGraphicsOpenGL)g).gl;
		if (COPYQUALITYNOTONE){
			copy();
			//Fill viewport with copy.
			gl.glViewport(xRect1, yRect1, gWidth, gHeight);
			set();
		}
		//Go back to full viewport.
		gl.glViewport(0,0,g.width,g.height);
		inPersist = false;
	}

	//////METHODS

	private void unScreen2D4GL(PGraphics g, GL2 gl) {
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		//projection.print();
		PMatrix3D projection = ((PGraphicsOpenGL)g).projection;
		float[] projectionFloats = new float[] {
				projection.m00, projection.m10, projection.m20, projection.m30,
				projection.m01, projection.m11, projection.m21, projection.m31,
				projection.m02, projection.m12, projection.m22, projection.m32,
				projection.m03, projection.m13, projection.m23, projection.m33
		};
		gl.glLoadMatrixf(projectionFloats, 0);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		// Flip Y-axis to make y count from 0 downwards
		gl.glScalef(1, -1, 1);
	}
	
	public void setSetColor(float r, float g, float b){
		this.setR = r;
		this.setG = g;
		this.setB = b;
	}
	
	private float setR = 1f, setG = 1f, setB = 1f;

	private void drawToScreen(GL2 gl, int whichTex, float xoff, float yoff, float alpha)           // Draw The Blurred Image
	{
		float texMaxActualW = gWidth/((float)TEXIMAGEW/COPYQUALITY);
		float texMaxActualH = gHeight/((float)TEXIMAGEH/COPYQUALITY);
		xoff*=texMaxActualW;
		yoff*=texMaxActualH;
		xoff*=-1; 
		yoff*=-1;
		gl.glEnable(GL.GL_TEXTURE_2D);
		if (whichTex!=-1)
			gl.glBindTexture(GL.GL_TEXTURE_2D, whichTex);
		float spost = 0;                                       // Starting Alpha Value
		//Note: recode this so that it doesn't clip the vertices outside of the viewport?
		gl.glBegin(GL2.GL_QUADS);                                 // Begin Drawing Quads
		gl.glColor4f(setR, setG, setB, alpha);               // Set The Alpha Value (Starts At 0.2)
		gl.glTexCoord2f(0+xoff, texMaxActualH+yoff);               // Texture Coordinate	( 0, 1 )
		gl.glVertex2f(spost, spost);                                 // First Vertex		(   0,   0 )

		gl.glTexCoord2f(0+xoff, 0+yoff);               // Texture Coordinate	( 0, 0 )
		gl.glVertex2f(0+spost, 1-spost);                               // Second Vertex	(   0, 480 )

		gl.glTexCoord2f(texMaxActualW+xoff, 0+yoff);               // Texture Coordinate	( 1, 0 )
		gl.glVertex2f(1-spost, 1-spost);                             // Third Vertex		( 640, 480 )

		gl.glTexCoord2f(texMaxActualW+xoff, texMaxActualH+yoff);               // Texture Coordinate	( 1, 1 )
		gl.glVertex2f(1-spost, spost);                               // Fourth Vertex	( 640,   0 )

		gl.glEnd();                                              // Done Drawing Quads
		gl.glDisable(GL.GL_TEXTURE_2D);                          // Disable 2D Texture Mapping
		gl.glBindTexture(GL.GL_TEXTURE_2D, 0);                   // Unbind The Blur Texture
	}

	private void view3D(PGraphics g, float wantedWidth, float wantedHeight){
		GLU glu = ((PGraphicsOpenGL)g).glu;
		GL2 gl = ((PGraphicsOpenGL)g).gl;

		gl.glMatrixMode(gl.GL_PROJECTION);	// Select The Projection Matrix
		gl.glLoadIdentity();		// Reset The Projection Matrix
		glu.gluPerspective(45.0f,wantedWidth/wantedHeight,0.1f,100f);
		// Reset The Current Modelview Matrix

		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f,-(float)Math.sqrt(2)-1);		// Center The Triangle
	}
	private void CopyBuffer(GL2 gl, int offX, int offY, int useWidth, int useHeight) {
		gl.glReadBuffer(GlTargetDrawBuffer);
		int powTwoUseWidth = getNextPowerOfTwo(useWidth);
		int powTwoUseHeight = getNextPowerOfTwo(useHeight);
		if ((TEXIMAGEW!=powTwoUseWidth) || (TEXIMAGEH!=powTwoUseHeight)){
			//Note: must handle resizes!!!
			gl.glBindTexture(GL.GL_TEXTURE_2D, lastImgStored);
			gl.glCopyTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB,offX, offY,TEXIMAGEW=powTwoUseWidth,TEXIMAGEH=powTwoUseHeight,0);

			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, glMipmappingMode);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, glMipmappingMode);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
			gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T,  GL2.GL_CLAMP);
		} 
		else {
			// Copy Our ViewPort To The Blur Texture (From 0,0 To useWidth,useHeight... No Border)
			gl.glBindTexture(GL.GL_TEXTURE_2D, lastImgStored);
			gl.glCopyTexSubImage2D(GL.GL_TEXTURE_2D, 0, 
					0, 0, 
					offX, offY, useWidth, useHeight);
		}
	}
	private int getNextPowerOfTwo(int x){
		int test = 1;
		while(test < x){
			test <<= 1;
		}
		return test;
	}

	private void screen2D4GL(PGraphics g, float wantedWidth, float wantedHeight) {
		GL2 gl = ((PGraphicsOpenGL)g).gl;
		/*
			screen2D();
		 */
		float[] vals = g.getMatrix().get(null);
		//gl.glTranslatef(vals[3],vals[7],vals[11]);

		view3D(g, wantedWidth, wantedHeight);
		gl.glScalef(1/(wantedHeight/wantedWidth),-1,1); //NOTE: IS UPSIDE DOWN!
		gl.glTranslatef(-1,-1,0);
		gl.glScalef(2,2,1/vals[11]);
	}
	public void cleanup() {
		GL2 gl = ((PGraphicsOpenGL)app.g).gl;
		gl.glDeleteTextures(1, new int[]{lastImgStored}, 0);
	}
}

