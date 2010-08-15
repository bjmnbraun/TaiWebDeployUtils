package BulletGame$1;

import javax.swing.JFrame;

import processing.core.PApplet;

public abstract class BulletGame$1Engine$L1$2$P5Link extends PApplet{
	public abstract class BulletGame$1Engine$L1$2$P5Link_Trunk extends BulletGame$1Engine$L1$1$OpenglTextRenderer{
		public BulletGame$1Engine$L1$2$P5Link_Trunk(JFrame holder, PApplet hold) {
			super(holder, hold);
		}
		
		public abstract class SkinnedElement {
			public SkinnedElement (RArea wholeArea){
				this.wholeArea = wholeArea;
			}
			private RArea wholeArea;
			public void draw(){
				viewport(wholeArea);
				float dx = 1f/currentViewPortWidth;
				float dy = 1f/currentViewPortHeight;
				noFill();
				stroke(0);
				line(dx,dy,1,0);
				line(dx,dy,dx,1);
				stroke(100);
				rect(dx*2,dy*2,1-dx*3,1-dy*3);
				float scrollRegion = .1f;
				dx = 1f/width;
				dy = 1f/height;
				viewport(wholeArea.x+dx*2,wholeArea.y+dy*2,wholeArea.width-dy*2-scrollRegion,wholeArea.height-dy*3);
				drawSub(ydrag);
			}
			private float totalHeight = 1;
			float ydrag = 0;
			/**
			 * Theoretical "max height" of the element. Used in deciding magnification of the scrollbar.
			 */
			public void setTotalHeight(float newTotalHeight){
				this.totalHeight = newTotalHeight;
				ydrag = Math.min(ydrag,newTotalHeight-1);
			}
			/**
			 * The viewport will be set to enclose the viewable area. Additionally, a scrollbar is visible.
			 * As usual, the drawing range is [0,1]x[0,1].
			 */
			public abstract void drawSub(float yoffset);
			public void cleanup(){
				
			}
			public RArea subArea() {
				return wholeArea;
			}
		}
	}
}
