package TaiGameCore;

/**
 * Allows the use of Key_Pressed elements for sane Typing tools.
 * 
 * Just call isTypeTime(KeyCode) each keyPressed, it will tell you whether the delay has been long enough.
 */
public class PressTypeThreshold {
	private long initialD, slowNess;
	public PressTypeThreshold(double initialDelay, double delayBetween){
		initialD = (long)(initialDelay*1e9);
		slowNess = (long)(delayBetween*1e9);
	}
	private long now = -1;
	private int lastPress = -1;
	private long numberTimesPressed = 0;
	public boolean isTypeTime(int keyCode){
		if (lastPress==keyCode){
			//DELAY EFFECTS
			long timeHeld = System.nanoTime()-now;
			if (timeHeld > initialD){
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
		now = System.nanoTime();
		lastPress = keyCode;
		return true;
	}
	public void release() {
		numberTimesPressed = 0;
		lastPress = -1;
	}
}
