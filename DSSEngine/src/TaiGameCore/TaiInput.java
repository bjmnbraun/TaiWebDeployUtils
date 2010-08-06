package TaiGameCore;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import processing.core.PApplet;

public class TaiInput {
	/**
	 * To get radians counterclockwise (polar), do PI*(DIR/4.0)
	 * 
	 * NOTE: check for -1 first though!!!!
	 */
	public static final int UP=2, DOWN=6, LEFT=4, RIGHT=0, UPRIGHT=1, UPLEFT=3, DOWNLEFT=5, DOWNRIGHT=7;
	/**
	 * Returns 0-8 depending on the current arrow keys pressed on G.
	 * 
	 * NOT THREAD SAFE!!!!!
	 */
	private static Set<Integer> singleKey = new HashSet<Integer>();
	public static int getDirectionalFromKeys(int single){
		singleKey.clear();
		singleKey.add(single);
		return getDirectionalFromKeys(singleKey);
	}
	public static int getDirectionalFromKeys(Set<Integer> pressedButtons){
		if (pressedButtons.contains(KeyEvent.VK_LEFT)){
			if (pressedButtons.contains(KeyEvent.VK_UP)){
				return UPLEFT;
			} 
			if (pressedButtons.contains(KeyEvent.VK_DOWN)){
				return DOWNLEFT;
			}
			return LEFT;
		}
		if (pressedButtons.contains(KeyEvent.VK_RIGHT)){
			if (pressedButtons.contains(KeyEvent.VK_UP)){
				return UPRIGHT;
			} 
			if (pressedButtons.contains(KeyEvent.VK_DOWN)){
				return DOWNRIGHT;
			}
			return RIGHT;
		}
		if (pressedButtons.contains(KeyEvent.VK_UP)){
			return UP;
		}
		if (pressedButtons.contains(KeyEvent.VK_DOWN)){
			return DOWN;
		}
		return -1;
	}
}
