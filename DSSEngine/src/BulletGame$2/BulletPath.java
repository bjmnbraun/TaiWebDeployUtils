package BulletGame$2;

/**
 * Allows for passing "Path" data between bullet classes
 * 
 * Used by passing in a holding buffer, and a time value at which to evaluate position.
 */
public interface BulletPath {
	/**
	 * Gets an x,y coordinate.
	 */
	public void getPosition(float[] sourcePos, float time);
}
