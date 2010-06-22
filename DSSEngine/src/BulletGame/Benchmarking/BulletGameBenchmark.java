package BulletGame.Benchmarking;

import TaiGameCore.GameDataBase;

public class BulletGameBenchmark extends GameDataBase{
	public BulletGameBenchmark(String hash) {
		super(hash);
	}
	public void setupFor(int n){
		timepoints= new float[n];
		bulletsLiveAtPoint= new float[n];
		fpspoints= new float[n];
	}
	public void benchmark(int i, float bulletMethodBegins,
			int numBulletsRendered, float frameRate) {
		timepoints[i] = bulletMethodBegins;
		bulletsLiveAtPoint[i] = numBulletsRendered;
		fpspoints[i] = frameRate;
	}
	public int gameversionNumber;
	public float[] timepoints;
	public float[] bulletsLiveAtPoint; //it'll be integral
	public float[] fpspoints;
	
	public void autoWrittenDeSerializeCode(){
		gameversionNumber = ((IntEntry)readField("gameversionNumber", new IntEntry())).getInt();
		timepoints = ((FloatArrayEntry)readField("timepoints", new FloatArrayEntry(new float[]{}))).getFloatArray();
		bulletsLiveAtPoint = ((FloatArrayEntry)readField("bulletsLiveAtPoint", new FloatArrayEntry(new float[]{}))).getFloatArray();
		fpspoints = ((FloatArrayEntry)readField("fpspoints", new FloatArrayEntry(new float[]{}))).getFloatArray();
	}
	public void autoWrittenSerializeCode(){
		writeField("gameversionNumber", new IntEntry(gameversionNumber));
		writeField("timepoints", new FloatArrayEntry(timepoints));
		writeField("bulletsLiveAtPoint", new FloatArrayEntry(bulletsLiveAtPoint));
		writeField("fpspoints", new FloatArrayEntry(fpspoints));
	}

}
