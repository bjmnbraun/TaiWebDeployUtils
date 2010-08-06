package BulletGame$2;

public interface GraphicsHolder {

	public int getW_tex();

	public int getH_tex();

	public int[] getTexGrid();

	/**
	 * For animate, format:
	 * 
	 * = LEFT=0,1&RIGHT=2,3&NORM=3,4
	 * 
	 * Means that when the player is moving to the left, etc etc.
	 * 
	 * However, if you only specify...
	 * = 0,1,2,3,4 ...
	 * 
	 * Then no matter what the actions are, that loop is performed.
	 * 
	 * 
	 */
	public static String MOTION_FRAMES_DEFAULT = "null";
	public String getAnimate();
	public float getAnimFps();
}
