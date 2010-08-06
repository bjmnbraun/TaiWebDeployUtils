package Deployments;

import java.awt.Color;

import processing.core.PApplet;
import TaiGameCore.ProceGLHybrid;

/**
 * This is a Lite PApplet, which has no config file and just behaves as one would expect
 * @author Benjamin
 */
public class BulletHell_Lite extends BulletHell_Lite_JOGL2{
	public void setup(){
		size(new Integer(getParameter("width")),
				new Integer(getParameter("height")),OPENGL);
		frameRate(60);
		background(0);
		
		gameActual = makeActual();
	}
	public ProceGLHybrid makeActual(){
		return new BulletHell.BulletGame$1Engine$GROUND(null, BulletHell_Lite.this);
	}
	private ProceGLHybrid gameActual;
	int loadState = 0;
	public void draw(){
		if (loadState==0){
			g.fill(0);
			rect(0,0,100,100);
		}
	};
	public void init(){
		setBackground(new Color(7,9,43));
		super.init();
		setBackground(new Color(125,125,255));
	}
}
