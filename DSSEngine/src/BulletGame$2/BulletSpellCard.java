package BulletGame$2;

import java.util.ArrayList;

import BulletGame$1.BulletGame$1Engine$L2$3$BulletGameBHIP.BulletHellInstancePlayer;
import BulletGame$2.BulletPattern.ExpressionEvaluator;
import TaiGameCore.GameDataBase;
import TaiGameCore.MultiExpression;
import TaiGameCore.TaiDAWG;
import TaiGameCore.GameDataBase.StringBase.FromScript;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;

import com.iabcinc.jmep.Environment;
import com.iabcinc.jmep.XExpression;

public class BulletSpellCard extends BulletExpressionGameDB implements GameDataBase.StringBase{
	public BulletSpellCard(String hash) {
		super(hash);
	}
	@FromScript()
	@CriticalScriptField()
	public String name;
	/**
	 * width of the playing area. Unlike the Level node's version of this parameter,
	 * this one is an expression, and so can change. When not defined (or empty string), 
	 * the width is taken from the Level node.
	 */
	@FromScript()
	@DefaultValue(value = "")
	public String level_w;
	/**
	 * height of the playing area. Unlike the Level node's version of this parameter,
	 * this one is an expression, and so can change. When not defined (or empty string), 
	 * the height is taken from the Level node.
	 */
	@FromScript()
	@DefaultValue(value = "")
	public String level_h;
	/**
	 * horizontal center of the playing area. This is an expression. When not defined,
	 * the stage is sensibly fit to be slightly to the left, with all of it onscreen.
	 */
	@FromScript()
	@DefaultValue(value = "")
	public String level_xo;
	/**
	 * vertical center of the playing area. This is an expression. When not defined,
	 * the stage is centered vertically.
	 */
	@FromScript()
	@DefaultValue(value = "")
	public String level_yo;
	/**
	 * Rotation (orientation) of the playing area. Rotation is about the center
	 * of the playing area. Bullets fired in a modified orientation will continue
	 * to "fly" in the direction of the old orientation even if the orientation
	 * is updated. This is a feature I purposefully added, not a bug. This is an expression.
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String orientation;
	/**
	 * (1) -1 < duration <= 0 makes the card never end
	 * (2) duration <= -1 makes the card end after the boss
	 * sustains -duration damage.
	 * (3) duration > 0 makes the card end after duration time
	 * 
	 * In all cases, duration can be in beats or seconds. The beat flag
	 * is only paid attention to in case 3.
	 */
	@FromScript()
	@DefaultValue(value = "-.5")
	public String duration;
	private float duration_f;
	private boolean duration_b;
	
	/**
	 * Size of the Boss's healthbar.
	 */
	@FromScript()
	@HasValidator(num = BulletPattern.PositiveValidator.POSITIVE_VALIDATOR)
	@DefaultValue(value = "1")
	public float health;
	public float health0;
	
	/**
	 * Backgrounds which are nested in a SpellCard are automatically rendered as
	 * part of this spellcard. However, if you would like to use the backgrounds
	 * from elsewhere in your script (say, a background that's in the top level)
	 * you may list their paths (comma seperated) here. This is especially useful for background videos
	 * - Each background node represents a link to a movie file, and it will be cached.
	 * Future references to any background node (even if they are in different spellcards)
	 * will use the same cache, which is better for performance (this does not occur if you
	 * copy and paste the URL of the movie into multiple background nodes...). Using this parameter
	 * may be preferable to nesting backgrounds in spellcards, since backgrounds have a high
	 * re-use potential. 
	 * Either way, I try to keep the memory overhead pretty low with the background videos, so don't worry
	 * too much about duplication if it's necessary.
	 */
	@FromScript()
	public String addBgs;

	public String myInstanceName;

	public float start_time;
	/**
	 * What the compiler predicts for the card.
	 */
	public float predicted_start_time;
	
	public void doDamageAgainst(BulletPattern bp){
		health--;
	}
	public float getHealthDuration(float bulletMethodBegins, BulletHellInstancePlayer bhip) {
		if (bhip.currentPart.myBoss>=0){
			return -1; //No matter what, the health bar shouldn't be here without a boss
		}
		if (duration_f <= -1){ //these are based on damage
			return health/health0;
		}
		if (-1 < duration_f && duration_f <= 0){
			return -1; //Infinite, no health bar
		}
		//Otherwise, interpret duration_f as a raw amount of time
		return -1; //No health bar
	}
	public boolean isFinished(float bulletMethodBegins, BulletHellInstancePlayer bhip){
		if (-1 < duration_f && duration_f <= 0){
			return false;
		}		
		if (duration_f <= -1){
			if (bhip.currentPart.myBoss>=0){
				return health < 0;
			} else {
				//NO boss, so... don't die?
				return false;
			}
		}
		float durationTimeRatio = getDurationTimeRatio(bulletMethodBegins,bhip);
		return durationTimeRatio<0;
	}
	/**
	 * Only works if duration is in the "time" area.
	 * 0 = time up
	 * 1 = full time
	 */
	private float getDurationTimeRatio(float bulletMethodBegins, BulletHellInstancePlayer bhip){
		if (duration_b){
			float timeb = bhip.getBeatFromSongTime(bulletMethodBegins);
			if (timeb < 0){
				return 0; //Not allowed.
			}
			timeb -= Math.max(bhip.getBeatFromSongTime(start_time),0);
			return 1 - timeb / duration_f;
		}
		float time = bulletMethodBegins - start_time;
		return 1 - time / duration_f;
	}
	/**
	 * Actively predict the duration of this card. Used in the editor.
	 * Return -1 if a prediction is impossible. This will breakdown the prediction
	 * chain.
	 */
	public float predictDuration(float startTime, BulletHellInstancePlayer bhip){
		if (-1 < duration_f && duration_f <= 0){
			return -1; //Infinity?
		}		
		if (duration_f <= -1){
			return -1; //Can't predict how long it will take to kill boss.
		}
		if (duration_b){
			return bhip.getTimeFromBeat(bhip.getBeatFromSongTime(startTime)+duration_f)-startTime;
		}
		return duration_f;
	}

	public MultiExpression[] createExpression(Environment env, BulletGlobals defines) throws XExpression {
		duration = duration.replaceAll("\\s+","");
		if (duration.length()>0){
			duration_b = duration.charAt(duration.length()-1)=='b';
			duration_f = duration_b?new Float(duration.substring(0,duration.length()-1)):new Float(duration);
		} else {
			throw new RuntimeException("No duration");
		}
		health = -duration_f;
		health0 = health;

		return new MultiExpression[]{
				(level_w.trim().equals("")?null:expr(level_w,env,defines)),
				(level_h.trim().equals("")?null:expr(level_h,env,defines)),
				(level_xo.trim().equals("")?null:expr(level_xo,env,defines)),
				(level_yo.trim().equals("")?null:expr(level_yo,env,defines)),
				expr(orientation,env,defines),
		};
	}
//AUTOWRITTEN
	public ArrayList<Exception> parseFromStrings(TaiDAWG<String> data, Validator ... valid) {
		ArrayList<Exception> toRet = new ArrayList();
		WordByRef<String> word;
		word = data.get("name");
		if (word!=null){String val = word.getContentData();
		name= val;
		}
		 else {
			toRet.add(new FieldRequiredException("name required."));
		}
		word = data.get("level_w");
		if (word!=null){String val = word.getContentData();
		level_w= val;
		}
		word = data.get("level_h");
		if (word!=null){String val = word.getContentData();
		level_h= val;
		}
		word = data.get("level_xo");
		if (word!=null){String val = word.getContentData();
		level_xo= val;
		}
		word = data.get("level_yo");
		if (word!=null){String val = word.getContentData();
		level_yo= val;
		}
		word = data.get("orientation");
		if (word!=null){String val = word.getContentData();
	try {
		valid[0].validate("orientation",val);
	} catch (ValidationException e){
	toRet.add(e);}
		orientation= val;
		}
		word = data.get("duration");
		if (word!=null){String val = word.getContentData();
		duration= val;
		}
		word = data.get("health");
		if (word!=null){String val = word.getContentData();
	try {
		valid[5].validate("health",val);
	} catch (ValidationException e){
	toRet.add(e);}
		health= new Float(val.trim());
		}
		word = data.get("addBgs");
		if (word!=null){String val = word.getContentData();
		addBgs= val;
		}
	StringTreeIterator<WordByRef<String>> iterator = data.iterator();
	while(iterator.hasNext()){
		String key = iterator.next();
	if (!key.equals("name")&&!key.equals("level_w")&&!key.equals("level_h")&&!key.equals("level_xo")&&!key.equals("level_yo")&&!key.equals("orientation")&&!key.equals("duration")&&!key.equals("health")&&!key.equals("addBgs")){
			toRet.add(new ValidationException("Unrecognized var: "+key+".",key));
	}
		iterator.tryNext();
	}
		return toRet;
	}
	public void autoWrittenDeSerializeCode(){
		name = ((StringEntry)readField("name", new StringEntry(""))).getString();
		level_w = ((StringEntry)readField("level_w", new StringEntry(""))).getString();
		level_h = ((StringEntry)readField("level_h", new StringEntry(""))).getString();
		level_xo = ((StringEntry)readField("level_xo", new StringEntry(""))).getString();
		level_yo = ((StringEntry)readField("level_yo", new StringEntry(""))).getString();
		orientation = ((StringEntry)readField("orientation", new StringEntry("0"))).getString();
		duration = ((StringEntry)readField("duration", new StringEntry("-.5"))).getString();
		duration_f = (float)((DoubleEntry)readField("duration_f", new DoubleEntry())).getDouble();
		duration_b = ((IntEntry)readField("duration_b", new IntEntry())).getInt()==1;
		health = (float)((DoubleEntry)readField("health", new DoubleEntry(1))).getDouble();
		health0 = (float)((DoubleEntry)readField("health0", new DoubleEntry())).getDouble();
		addBgs = ((StringEntry)readField("addBgs", new StringEntry(""))).getString();
		myInstanceName = ((StringEntry)readField("myInstanceName", new StringEntry(""))).getString();
		start_time = (float)((DoubleEntry)readField("start_time", new DoubleEntry())).getDouble();
	}
	public void autoWrittenSerializeCode(){
		writeField("name", new StringEntry(name));
		writeField("level_w", new StringEntry(level_w));
		writeField("level_h", new StringEntry(level_h));
		writeField("level_xo", new StringEntry(level_xo));
		writeField("level_yo", new StringEntry(level_yo));
		writeField("orientation", new StringEntry(orientation));
		writeField("duration", new StringEntry(duration));
		writeField("duration_f", new DoubleEntry(duration_f));
		writeField("duration_b", new IntEntry(duration_b?1:0));
		writeField("health", new DoubleEntry(health));
		writeField("health0", new DoubleEntry(health0));
		writeField("addBgs", new StringEntry(addBgs));
		writeField("myInstanceName", new StringEntry(myInstanceName));
		writeField("start_time", new DoubleEntry(start_time));
	}

	
	
}
