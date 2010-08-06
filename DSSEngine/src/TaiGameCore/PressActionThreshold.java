package TaiGameCore;

/**
 * Very much like PressTypeThreshold, except this requests the "current state" every call to isTypeTime
 */
public class PressActionThreshold {
	private long initialD, slowNess;
	public PressActionThreshold(double initialDelay, double delayBetween){
		initialD = (long)(initialDelay*1e9);
		slowNess = (long)(delayBetween*1e9);
	}
	private long now = -1;
	private boolean lastWasAlsoPressed = false;
	private long numberTimesPressed = 0;
	public boolean isActionTime(boolean isActionRequesting){
		if (lastWasAlsoPressed && isActionRequesting){
			if (initialD>1e9*10){
				return false; //Way too long.
			}
			//DELAY EFFECTS
			long timeHeld = System.nanoTime()-now;
			if (timeHeld > initialD){
				if (slowNess<1e-6){
					return true; //Instantaneous...
				}
				if (slowNess>1e9*10){
					return false; //Way too long.
				}
				timeHeld-=initialD;
				long couldBe = timeHeld / slowNess;
				if (couldBe >= (numberTimesPressed-1)){
					numberTimesPressed++;
					return true;
				} else {
					return false; //Nope, in the middle of the interval.
				}
			} else {
				return false; //Waiting...
			}
		}
		release();
		lastWasAlsoPressed = isActionRequesting;
		now = System.nanoTime();
		return isActionRequesting;
	}
	public void release() {
		numberTimesPressed = 0;
		lastWasAlsoPressed = false;
	}
	/**
	 * Simulates a press, and puts the timer past the delay period (so that you're in the state of "multipress")
	 */
	public void skipDelayPeriod() {
		now = System.nanoTime() - initialD - (long)1e9;
		lastWasAlsoPressed = true;
	}
}
