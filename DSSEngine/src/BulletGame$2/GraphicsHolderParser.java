package BulletGame$2;

import TaiGameCore.GameDataBase;
import TaiGameCore.GameDataBase.StringBase.ValidationException;

/**
 * @see GraphicsHolder.java
 */
public class GraphicsHolderParser {
	public static class AnimationValidator implements GameDataBase.StringBase.Validator {
		public static final int ANIMATION_VALIDATOR = 8;

		public void validate(String fieldname, String data)
				throws ValidationException {
			try {
				parseAnimationCommands(data);
			} catch (Throwable e){
				throw new ValidationException(e.getMessage(),fieldname,e);
			}
		}
	}
	public static final int LEFT_ANIMATION = 0;
	public static final int NORM_ANIMATION = 1;
	public static final int RIGHT_ANIMATION = 2;
	public static int[][] parseAnimationCommands(String motionFrame){
		motionFrame = motionFrame.replaceAll("\\s+","");
		if (motionFrame.equals(GraphicsHolder.MOTION_FRAMES_DEFAULT)){
			return new int[][]{{0}};
		}
		if (motionFrame.matches("[,0-9]*")){
	 		String[] numbers = motionFrame.split(",");
			int[] toRet = new int[numbers.length];
			for(int k = 0; k < numbers.length; k++){
				toRet[k] = new Integer(numbers[k]);
			}
			return new int[][]{toRet};
		}
		//It's the other format
		try {
			String[] actions = motionFrame.split("&");
			int[][] toRet = new int[3][];
			boolean[] did = new boolean[3];
			for(String k : actions){
				String[] named = k.split("[=:]");
				int whichName = getAnimationLabel(named[0]);
				String[] numbers = named[1].split(",");
				int[] toRetSub = new int[numbers.length];
				for(int eid = 0; eid < numbers.length; eid++){
					toRetSub[eid] = new Integer(numbers[eid]);
				}
				toRet[whichName] = toRetSub;
				did[whichName] = true;
			}
			boolean sumDid = true; for(boolean k : did) sumDid &= k;
			if (!sumDid){
				throw new IllegalArgumentException("3 States Required");
			}
			return toRet;
		} catch (IllegalArgumentException e){
			throw new RuntimeException(e.getMessage());
		} catch (Throwable e){
			//fallthrough
		}
		throw new RuntimeException("Invalid Animation");
	}
	private static int getAnimationLabel(String string) {
		string = string.toLowerCase();
		if (string.equals("left")){
			return LEFT_ANIMATION;
		}
		if (string.equals("right")){
			return RIGHT_ANIMATION;
		}
		if (string.equals("norm") || string.equals("normal")){
			return NORM_ANIMATION;
		}
		throw new RuntimeException("No Act: "+string);
	}
}
