package BulletGame$2;

import java.util.ArrayList;

import BulletGame$2.BulletGlobals.SoundValidator;
import BulletGame$2.TopDownSplayTree.TreeNode;
import TaiGameCore.GameDataBase;
import TaiGameCore.TaiDAWG;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;

public class BulletLevel extends GameDataBase implements GameDataBase.StringBase{
	public BulletLevel(String hash) {
		super(hash);
	}

	public static class BPMValidator implements StringBase.Validator{
		public static final int BPM_VALIDATOR = 4;
		public void validate(String fieldname, String data) throws ValidationException {
			try {
				parseBPMS(0, data);
			} catch(Throwable e){
				e.printStackTrace();
				throw new ValidationException(e.getMessage(),fieldname,e);
			}
		}		
	}

	/**
	 * 0: Forward (time -> bpm,beat)
	 * 1: Reverse (beat -> bpm,time)
	 */
	public static TopDownSplayTree<float[]>[] parseBPMS(float gap, String data){
		TopDownSplayTree<float[]>[] toRet = new TopDownSplayTree[2];
		for(int k = 0; k < 2; k++){
			toRet[k] = new TopDownSplayTree<float[]>();
		}
		
		float cTime = -gap;
		float lBeat = -1e-8f; //assert that we increase
		float lbpm = 1;
		String[] entries = data.split(",");
		for(String k : entries){
			k = k.replaceAll("\\s+","");
			if (k.length()==0){
				continue; //no prob
			}
			String[] dat = k.split("[:=]");
			if (dat.length!=2){
				throw new RuntimeException("try: bt=bpm");
			}
			float beat = new Float(dat[0]);
			float bpm = new Float(dat[1]);
			if (bpm < 0){
				throw new RuntimeException("bpm<0@"+k);
			}
			if (beat <= lBeat){
				throw new RuntimeException("beat didn't increase!@"+k);
			}
			float dBeat = beat-lBeat;
			cTime += dBeat / lbpm * 60;
			toRet[0].insert(cTime, new float[]{bpm,beat});
			toRet[1].insert(beat, new float[]{bpm,cTime});
			//System.out.println(cTime+" "+beat);
			lbpm = bpm;
			lBeat = beat;
		}
		if (toRet[0].size()==0){
			throw new RuntimeException("Empty bpmset");
		}
		
		TreeNode<float[]> min = toRet[0].min();
		if (Math.abs(min.getValue()[1])>1e-5){
			throw new RuntimeException("Beat0 != 0");
		}
		return toRet;
	}

	@FromScript()
	@CriticalScriptField()
	public String levelName;
	/**
	 * Represents a list of named subnodes of this level, which will be executed in the order listed.
	 * The named subnodes must be either spellcards (representing actions taken during the "stage" portion of the
	 * level) or bosses (when a boss is called, he will go through all of his spellcards before the
	 * next event is called)
	 */
	@FromScript()
	@CriticalScriptField()
	public String events;
	/**
	 * The width of the actual Playing Area inside the Game Area. 
	 * The Playing Area will be centered with a bias, somewhere around .3, but will
	 * be shifted to the right as the width increases, such that all of the playing area
	 * is visible. 
	 */
	@FromScript()
	@DefaultValue(value = ".5")
	public String level_w;
	/**
	 * The height of the actual Playing Area inside the Game Area.
	 * The playing area is centered, vertically.
	 */
	@FromScript()
	@DefaultValue(value = "1")
	public String level_h;
	/**
	 * The initial "start" location of the player, and the location that
	 * the player reverts to each time she uses up a life. Remember that
	 * these coordinates are relative to the total Game Area. Be careful to 
	 * put them inside the Playing Area!
	 */
	@FromScript()
	@DefaultValue(value = ".5f,.91f")
	public float[] playerInit;
	public static final String NO_AUDIO = "null";
	/**
	 * The music to play in the background of this stage. Numerous opportunities
	 * exist to synchronize events with the music. Music is currently only 
	 * supported in MP3 or OGG codecs.
	 */
	@FromScript()
	@DefaultValue(value = NO_AUDIO)
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String music;
	/**
	 * The skin image to draw over the entire Game Area (URL to an image, that is)
	 */
	@FromScript()
	@DefaultValue(value = "GameDefault/defaultskin.png")
	public String skin;
	/**
	 * Represents the BPM list for the sound file (see "music") attached to this level.
	 */
	@FromScript()
	@DefaultValue(value = "0=120")
	@HasValidator(num = 4)
	public String bpms;
	/**
	 * Represents the time at which beat 0 occurs. Can be positive or negative, to correctly synchronize the beats. See "bpm"
	 */
	@FromScript()
	@DefaultValue(value = "0")
	public float gap;

	public int current_event;
	public String myInstanceName;	
	/**
	 * What the compiler predicts for the card.
	 */
	@DefaultValue(value="0f")
	public float predicted_start_time;

	public ArrayList<Exception> parseFromStrings(TaiDAWG<String> data, Validator ... valid) {
		ArrayList<Exception> toRet = new ArrayList();
		WordByRef<String> word;
		word = data.get("levelName");
		if (word!=null){String val = word.getContentData();
		levelName= val;
		}
		 else {
			toRet.add(new FieldRequiredException("levelName required."));
		}
		word = data.get("events");
		if (word!=null){String val = word.getContentData();
		events= val;
		}
		 else {
			toRet.add(new FieldRequiredException("events required."));
		}
		word = data.get("level_w");
		if (word!=null){String val = word.getContentData();
		level_w= val;
		}
		word = data.get("level_h");
		if (word!=null){String val = word.getContentData();
		level_h= val;
		}
		word = data.get("playerInit");
		if (word!=null){String val = word.getContentData();
		String[] spli = val.split(",");
		playerInit= new float[spli.length];
		for(int k = 0; k < spli.length; k++){
			playerInit[k]=new Float(spli[k].trim());
		}
		}
		word = data.get("music");
		if (word!=null){String val = word.getContentData();
		music= val;
		}
		word = data.get("skin");
		if (word!=null){String val = word.getContentData();
		skin= val;
		}
		word = data.get("bpms");
		if (word!=null){String val = word.getContentData();
	try {
		valid[4].validate("bpms",val);
	} catch (ValidationException e){
	toRet.add(e);}
		bpms= val;
		}
		word = data.get("gap");
		if (word!=null){String val = word.getContentData();
		gap= new Float(val.trim());
		}
	StringTreeIterator<WordByRef<String>> iterator = data.iterator();
	while(iterator.hasNext()){
		String key = iterator.next();
	if (!key.equals("levelName")&&!key.equals("events")&&!key.equals("level_w")&&!key.equals("level_h")&&!key.equals("playerInit")&&!key.equals("music")&&!key.equals("skin")&&!key.equals("bpms")&&!key.equals("gap")){
			toRet.add(new ValidationException("Unrecognized var: "+key+".",key));
	}
		iterator.tryNext();
	}
		return toRet;
	}
	public void autoWrittenDeSerializeCode(){
		levelName = ((StringEntry)readField("levelName", new StringEntry(""))).getString();
		events = ((StringEntry)readField("events", new StringEntry(""))).getString();
		level_w = ((StringEntry)readField("level_w", new StringEntry(".5"))).getString();
		level_h = ((StringEntry)readField("level_h", new StringEntry("1"))).getString();
		playerInit = ((FloatArrayEntry)readField("playerInit", new FloatArrayEntry(new float[]{.5f,.91f}))).getFloatArray();
		music = ((StringEntry)readField("music", new StringEntry("null"))).getString();
		skin = ((StringEntry)readField("skin", new StringEntry("GameDefault/defaultskin.png"))).getString();
		bpms = ((StringEntry)readField("bpms", new StringEntry("0=120"))).getString();
		gap = (float)((DoubleEntry)readField("gap", new DoubleEntry(0))).getDouble();
		current_event = ((IntEntry)readField("current_event", new IntEntry())).getInt();
		myInstanceName = ((StringEntry)readField("myInstanceName", new StringEntry(""))).getString();
	}
	public void autoWrittenSerializeCode(){
		writeField("levelName", new StringEntry(levelName));
		writeField("events", new StringEntry(events));
		writeField("level_w", new StringEntry(level_w));
		writeField("level_h", new StringEntry(level_h));
		writeField("playerInit", new FloatArrayEntry(playerInit));
		writeField("music", new StringEntry(music));
		writeField("skin", new StringEntry(skin));
		writeField("bpms", new StringEntry(bpms));
		writeField("gap", new DoubleEntry(gap));
		writeField("current_event", new IntEntry(current_event));
		writeField("myInstanceName", new StringEntry(myInstanceName));
	}


}
