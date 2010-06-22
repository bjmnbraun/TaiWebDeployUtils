package BulletGame.Benchmarking;

import BulletGame$2.BulletHellInstance;
import TaiGameCore.GameDataBase;

public class BulletGameBenchmarkSet extends GameDataBase{
	public BulletGameBenchmarkSet(String hash) {
		super(hash);
		if (benchs==null){
			benchs = new BulletGameBenchmark[0];
		}
	}
	public BulletGameBenchmark[] benchs;
	public BulletHellInstance constantEnv;
	public String ProcessorName;
	//Notes on versions:
	public void addBenchmark(BulletGameBenchmark runningBenchmark) {
		BulletGameBenchmark[] nbenchs = new BulletGameBenchmark[benchs.length+1];
		for(int k = 0; k < benchs.length; k++){
			nbenchs[k] = benchs[k];
		}
		nbenchs[nbenchs.length-1] = runningBenchmark;
		benchs = nbenchs;
	}

	public void autoWrittenDeSerializeCode(){
		String benchs_strTmp = ((StringEntry)readField("benchs", new StringEntry(""))).getString();
		if (benchs_strTmp.length()>0){
			String[] parts123456 = benchs_strTmp.split(",");
			benchs = new BulletGameBenchmark[parts123456.length];
			for(int qqq = 0; qqq < parts123456.length; qqq++){
				benchs[qqq]=new BulletGameBenchmark(parts123456[qqq]);
		}}
		String constantEnv_strTmp= ((StringEntry)readField("constantEnv", new StringEntry(""))).getString();
		if (constantEnv_strTmp.length()>0){
			constantEnv = new BulletHellInstance(constantEnv_strTmp);
		}
		ProcessorName = ((StringEntry)readField("ProcessorName", new StringEntry(""))).getString();
	}
	public void autoWrittenSerializeCode(){
		writeField("benchs", new StringEntry(benchs!=null?hashAllToString(benchs):""));
		writeField("constantEnv", new StringEntry(constantEnv!=null?constantEnv.hashToString():""));
		writeField("ProcessorName", new StringEntry(ProcessorName));
	}

}

