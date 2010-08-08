package TaiGameCore;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import BulletGame$1.BulletGame$1Engine$ABasicEngine.Shader1VertShader;


/**
 * ATTRIBUTE 2 IS RESERVED FOR AN INCREASING SHORT (0 to n) FOR ALL GEOMETRY.
 */
public class TaiVBO {
	private int MAXVERTICES = 32*1024;
	private class Attrib {
		private int myIndex;
		private int DataType;
		private Object[] codeIds = new Object[4];
		private int numUsed = 0;
		private boolean isModified = false;
		private int myBuffer = -1;
		public Attrib(int i) {
			myIndex = i;
		}
		private float[] bufferF;
		private short[] bufferS;
		private int FORMATSIZE = -1;
		public void setData(int r, Object object) {
			if (FORMATSIZE==-1){
				//Time to init this sucker.
				if (DataType==GL.GL_FLOAT){
					FORMATSIZE = 4;
					bufferF = new float[MAXVERTICES*FORMATSIZE];
				} else if (DataType==GL.GL_SHORT){
					FORMATSIZE = 2;
					bufferS = new short[MAXVERTICES*FORMATSIZE];
				} else {
					throw new Error();
				}
			}
			int offset = queuedVertices * numUsed + r;
			if (DataType==GL.GL_FLOAT){
				bufferF[offset]=(Float)object;
			} else if (DataType == GL.GL_SHORT){
				bufferS[offset]=(Short)object;
			} else {
				throw new Error();
			}
			isModified = true;
		}
	}
	private Attrib[] attribs;
	private int queuedVertices;
	public void reset(){
		queuedVertices = 0;
	}
	private GL2 gl;
	public TaiVBO(GL2 gl){
		this.gl = gl;
		attribs = new Attrib[8];
		for(int k = 0; k < attribs.length; k++){
			attribs[k] = new Attrib(k);
		}
		registerAttrib("CORNER_COUNT", GL.GL_SHORT, 2, 0);
	}
	public float[] CurrentColor = new float[3];
	public void drawQueuedElements(){
		gl.glColor3f(CurrentColor[0], CurrentColor[1], CurrentColor[2]);
		gl.glBegin(GL2.GL_QUADS);
		/*
		for(int k = 0; k < queuedVertices; k++){
			for(Attrib c : attribs){
				if (c.isModified){
					//System.out.println(c.myIndex+" "+c.numUsed);
					if (c.DataType==GL.GL_FLOAT){
						if (c.numUsed==2){
							gl.glVertexAttrib2fARB(c.myIndex,
									c.bufferF[k*2],c.bufferF[k*2+1]);
						} else {
							gl.glVertexAttrib4fARB(c.myIndex,
									c.bufferF[k*4],c.bufferF[k*4+1],
									c.bufferF[k*4+2],c.bufferF[k*4+3]);
						}
					} else {
						gl.glVertexAttrib1sARB(c.myIndex,c.bufferS[k]);
					}
				}
			}
			gl.glVertexAttrib1fARB(0,0);
		}
		*/
		for(int k = 0; k < queuedVertices; k++){
			/*		
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
		*/
			short[] data2 = attribs[2].bufferS;
			float[] data3 = attribs[3].bufferF;
			float[] data4 = attribs[4].bufferF;
			float[] data5 = attribs[5].bufferF;

			int corner = data2[k];
			float cornerX = -.5f;
			float cornerY = -.5f;
			if (corner == 2 || corner == 3){
				cornerX = .5f;
			}
			if (corner == 3 || corner == 4){
				cornerY = .5f;
			}

			float xtexoffset = data5[k*4+0];
			float ytexoffset = data5[k*4+1];
			float xtexscl = data5[k*4+2];
			float ytexscl = data5[k*4+3];
			gl.glTexCoord2f(xtexoffset+(xtexscl)*(cornerX + .5f),
					ytexoffset+(ytexscl)*(cornerY + .5f));

			float xoff = data3[k*4+0];
			float yoff = data3[k*4+1];
			float rotation = data3[k*4+3];
			float xscl = data4[k*2+0];
			float yscl = data4[k*2+1];
			gl.glVertex3f(
					xoff + xscl * cornerX,
					yoff + yscl * cornerY,
					0);
		}
		gl.glEnd();
		return;
	}

	public void addNgon(int numTimes, Object ... data){
		short corner = 0;
		for(int Time = 0; Time < numTimes; Time++){
			for(Attrib c : attribs){
				for(int r = 0; r < c.numUsed; r++){
					for(int k = 0; k < data.length; k+=2){
						Object codeId = data[k];
						if (c.codeIds[r]==codeId){
							c.setData(r,data[k+1]);
						}
					}
				}
			}
			corner++;
			attribs[2].setData(0, corner);
			queuedVertices++;
		}
	}
	/**
	 * Are either floats or shorts.
	 */
	public void registerAttrib(Object CodeID, int DataType, int attribNumber, int index){
		if (attribNumber==0){
			throw new IllegalArgumentException("Attrib 0 is reserved for setting vertices.");
		}
		Attrib toUse = attribs[attribNumber];
		toUse.DataType = DataType;
		toUse.codeIds[index]=CodeID;
		toUse.numUsed = Math.max(toUse.numUsed,index+1);
	}
}
