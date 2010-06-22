package TaiGameCore;

/**
 * Chain relativetimenodes to get relative times "from time of birth" for many objects.
 * 
 * These optimize their chains, so the middlemen are removed (?)
 */
public interface RelativelyTimed {
	public static class RelativeTimeNode implements RelativelyTimed{
		private double delta;
		private TimeSource source;
		public RelativeTimeNode(TimeSource source){
			this.source = source;
			delta = source.time();
		}
		public RelativeTimeNode(RelativeTimeNode rtn){
			delta = rtn.time();
			delta += rtn.delta;
			source = rtn.source;
		}
		public double time(){
			return source.time()-delta;
		}
		public void reset(){
			delta = source.time(); 
		}
		public float timef(){
			return (float)time();
		}
	}
	/**
	 * Unofficially, I advise timesources should return seconds-amounts.
	 * @author Benjamin
	 *
	 */
	public static abstract class TimeSource implements RelativelyTimed{
		public float timef(){
			return (float)time();
		}
	}
	public double time();
	/**
	 * Sometimes, the double-to-float conversion just really makes client code look messy.
	 */
	public float timef();
}
