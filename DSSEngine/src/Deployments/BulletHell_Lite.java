package Deployments;

import java.awt.Color;

import processing.core.PApplet;
import TaiGameCore.ProceGLHybrid;

/**
 * This is a Lite PApplet, which has no config file and just behaves as one would expect
 * @author Benjamin
 */
public class BulletHell_Lite extends PApplet{
	public void setup(){
		size(getWidth(),getHeight(),OPENGL);
		frameRate(60);
		fill(0,255,0);
		rect(0,0,width,height);
	}
	int loadState = 0;
	public void draw(){
		if (loadState==0){
			g.fill(0);
			rect(0,0,100,100);
		}
		if (loadState==1){
			//Ok, now make the code:
			new Thread(){
				public void run(){
					ProceGLHybrid gameActual = new BulletHell.BulletGame$1Engine$GROUND(null, BulletHell_Lite.this);
				}
			}.start();
			loadState = 2;
		}
	};
	public void init(){
		setBackground(new Color(7,9,43));
		super.init();
		setBackground(new Color(125,125,255));
		loadState = 1;
	}
}
