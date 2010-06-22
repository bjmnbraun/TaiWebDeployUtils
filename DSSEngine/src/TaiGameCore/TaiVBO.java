package TaiGameCore;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;


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
		gl.glBegin(GL2.GL_QUADS);
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
