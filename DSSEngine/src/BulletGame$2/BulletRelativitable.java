package BulletGame$2;

import TaiGameCore.GameDataBase;

public abstract class BulletRelativitable extends BulletExpressionGameDB implements GameDataBase.StringBase, GraphicsHolder {
	public BulletRelativitable(String hash) {
		super(hash);
	}

	public static final int TOP_LEFT_CORNER = 0;
	public static final int TOP_RIGHT_CORNER = 1;
	public static final int BOTTOM_RIGHT_CORNER = 2;
	public static final int BOTTOM_LEFT_CORNER = 3;
	public static final int SOURCE = 4;
	public static final int SOURCELOCK = 5;
	public static final int PLAYER = 6;
	public static final int CENTER_SPOT = 7;
	

	
	
	public static interface PositionRelativitator {
		/**<pre>
		 * Bullet sources are "shoved" by the camera / viewport.
		 * The x/y coords of bullet sources should be calculated, then processed here last.
		 * The flight of bullets are not affected by the camera / viewport.
		 * Note: the player's location is shoved by the camera's viewport.
		 * 
		 * This class applies the correct shovings.
		 * 
		 * Idea: an aesthetic implementation of positionrelativitator will "center" itself
		 * around .5f,.5f. 
		 * 
		 * Examples:
		 * Viewport: centered around .3f,.5f
		 * Viewport: rotation: 10* clockwise (1 oclock)
		 * VIewport: width = .4f (.1f - .5f), height = 1f (0 - 1)
		 * 
		 * Player location: .5f,.8f
		 * 
		 * 1) (Central vector): 0f,.3f
		 * 2) (rotation): -.1f, .2f
		 * 3) (translation to wrong center): .2f,.7f
		 * 
		 * Then the glyph is rendered around that center, rotated by the camera orientation
		 *
		 * Note: source is a coordinate in "raw" coordinates. (so, 0,0 is really the top left of the screen.)
		 *</pre>
		 */
		void relativity(float[] position, float sourcex, float sourcey, BulletRelativitable br);
	}


	public abstract int getRelativeMode();

}
