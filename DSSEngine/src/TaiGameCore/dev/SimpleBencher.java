package TaiGameCore.dev;

public class SimpleBencher {
	public static void benchStart(int numTimes){
		now = System.nanoTime();
		SimpleBencher.numTimes = numTimes;
	}
	public static int numTimes(){
		return numTimes;
	}
	private static int numTimes;
	private static long now;
	public static void benchEnd(String message){
		double diff = (System.nanoTime()-now)/(double)numTimes;
		System.out.println(message+" "+"Throughput (s): "+(long)(1e9/(diff)));
	}
}
