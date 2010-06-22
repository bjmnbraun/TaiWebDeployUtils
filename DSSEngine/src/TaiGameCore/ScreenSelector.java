package TaiGameCore;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import processing.core.PApplet;

/**
 * Select sub-pages on a page!
 */
public abstract class ScreenSelector extends KeyAdapter{
	public static enum MODE {MOUSE, KEYBOARD, BOTH};
	public static abstract class TMenuItem {
		public void draw(){};
		public abstract void act();
		public void onSelect(){};
		public void cleanup(){}
		public void onDeselect(){};
		/**
		 * NOTE: rectangle must refer to pixel coordinates. So, undo any crazy scaling your doing...
		 */
		public Rectangle2D getClickBounds(){
			throw new RuntimeException("You didn't override \' getClickBounds\' like you shoulda!");
		}

		public boolean isHoverOver(PApplet g){
			Point2D isIn = new Point2D.Double(g.mouseX/(double)g.width,g.mouseY/(double)g.height);
			if (getClickBounds().contains(isIn)){
				return true;
			}
			return false;
		}
	}
	public ScreenSelector(MODE mod){
		allMenus = getItems();
		switch(mod){
		case MOUSE:
			initmouse();
			break;
		case KEYBOARD:
			initkeyboard();
			back = getBackItem();
			break;
		case BOTH:
			initmouse();
			initkeyboard();
			back = getBackItem();
			break;
		}
	}
	private TMenuItem[] allMenus;
	private TMenuItem back;
	private int nowSelected = 0;
	public boolean inPureNavMenu = true;
	private boolean usingMouse = false, usingKeyboard = false;
	private void initmouse() {
		usingMouse = true;
		for(TMenuItem c : allMenus){
			c.getClickBounds(); //Just make sure.
		}
	}
	private void initkeyboard() {
		usingKeyboard = true;
		if (allMenus!=null){
			allMenus[0].onSelect();
		}
		lastKeyPressTime = System.nanoTime();
		lastKeyPress = KeyEvent.VK_Z;
	}
	public abstract TMenuItem[] getItems();
	/**
	 * Override to provide a "back" action.
	 * Example:
	 * return new TMenuItem(){
			public void act() {
				//Overrided action here.
			}
		};
	 * @return
	 */
	public TMenuItem getBackItem(){
		return new TMenuItem(){
			public void act() {
			}
		};
	}
	private int lastKeyPress;
	private long lastKeyPressTime = -1;
	private boolean onSuperSpeedScroll = false;
	public void keyPressed(KeyEvent e){
		if ((System.nanoTime()-lastKeyPressTime)/1e9<(!onSuperSpeedScroll?.5f:.15f)){
			if (lastKeyPress==e.getKeyCode()){
				return; //block this input, it already got done.
			} else 
				onSuperSpeedScroll = false;
		} else {
			onSuperSpeedScroll = lastKeyPress==e.getKeyCode();
		}
		if (allMenus!=null){ //Sometimes, when we just want the Back object, we have no seelectable objs.
			lastKeyPress = e.getKeyCode();
			lastKeyPressTime = System.nanoTime();
			//g.keyCode;
			int reallyTempNow = nowSelected;
			int tempNowSelected = nowSelected;
			switch (TaiInput.getDirectionalFromKeys(e.getKeyCode())){
			case TaiInput.DOWN:
				tempNowSelected=tempNowSelected+1;
				if (tempNowSelected==allMenus.length) tempNowSelected=0;
				nowSelected = tempNowSelected;
				break;
			case TaiInput.UP:
				tempNowSelected=tempNowSelected-1;
				if (tempNowSelected<0) tempNowSelected+=allMenus.length;
				nowSelected = tempNowSelected;
				break;
			}
			if (reallyTempNow!=nowSelected){
				allMenus[reallyTempNow].onDeselect();
				allMenus[nowSelected].onSelect();
			}
			if (e.getKeyCode()==KeyEvent.VK_ENTER || (inPureNavMenu && e.getKeyCode()==KeyEvent.VK_Z)){
				allMenus[nowSelected].act();
			}
		}
		if (e.getKeyCode()==KeyEvent.VK_ESCAPE || (inPureNavMenu && e.getKeyCode()==KeyEvent.VK_X)){
			back.act();
		}
	}
	/**
	 * Sets viewports and such for each box.
	 */
	public void mouseCheck(PApplet g){
		Point2D isIn = new Point2D.Double(-1,-1);
		if (g.mousePressed){
			isIn = new Point2D.Double(g.mouseX/(double)g.width,g.mouseY/(double)g.height);
		} else {
			dontSpamAButton.release();
		}
		for(TMenuItem d : allMenus){			
			if (d.getClickBounds().contains(isIn)){
				if (dontSpamAButton.isActionTime(true)){
					d.act();
				}
			}
			d.draw();
		}
	}
	private PressActionThreshold dontSpamAButton = new PressActionThreshold(20,20);

	public void cleanup() {
		for(TMenuItem d : allMenus){
			d.cleanup();
		}
	}
}
