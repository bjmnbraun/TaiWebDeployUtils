package TaiGameCore;

import javax.media.opengl.GL2;


public class TaiShaders {

	/**
	 * Pass null to go back to default gl shader
	 */
	public void switchToShader(TaiShaders other) {
		if (other == null) {
			gl.glDisable(gl.GL_VERTEX_PROGRAM_ARB); //Just disable.
			return;
		}
		gl.glEnable(gl.GL_VERTEX_PROGRAM_ARB);
		gl.glBindProgramARB(gl.GL_VERTEX_PROGRAM_ARB, shaders[0]);
	}

	private GL2 gl;

	public TaiShaders(GL2 gl) {
		this.gl = gl;
	}

	private int[] shaders;

	public void initFromStrings(String vertTxt) {
		//NOTE: fragment shaders not supported! Parram is ignored.
		gl.glEnable(gl.GL_VERTEX_PROGRAM_ARB);
		shaders = new int[1];
		gl.glGenProgramsARB(1, shaders, 0);
		gl.glBindProgramARB(gl.GL_VERTEX_PROGRAM_ARB, shaders[0]);
		gl.glProgramStringARB(gl.GL_VERTEX_PROGRAM_ARB, 
				gl.GL_PROGRAM_FORMAT_ASCII_ARB, vertTxt.length(), vertTxt);

		String programErrorString = gl.glGetString(gl.GL_PROGRAM_ERROR_STRING_ARB);

		int errorPos[] = new int[1];

		gl.glGetIntegerv(gl.GL_PROGRAM_ERROR_POSITION_ARB, errorPos,0);
		if(errorPos[0]!=-1)
			System.err.println("VP Error:" + programErrorString);

		gl.glDisable(gl.GL_VERTEX_PROGRAM_ARB);
	}

	public void cleanup() {
		gl.glDeleteProgramsARB(1,shaders,0);
	}
}
