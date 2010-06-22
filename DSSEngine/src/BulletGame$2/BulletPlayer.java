package BulletGame$2;

import java.util.ArrayList;

import TaiGameCore.GameDataBase;
import TaiGameCore.GameSprite;
import TaiGameCore.TaiDAWG;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;

public class BulletPlayer extends GameDataBase implements GameDataBase.StringBase, GraphicsHolder {
	public BulletPlayer(String hash) {
		super(hash);
	}
	
	public static class YesNoValidator implements StringBase.Validator{
		public static final int YES_NO_VALIDATOR = 6;
		public void validate(String fieldname, String data)
				throws ValidationException {
			boolean isBad = isBad(data);
			boolean isGood  = isGood(data);
			if (isBad || isGood){
				if (isBad&&isGood){
					throw new ValidationException("Unclear Answer",fieldname,null);
				}
				//Alright~
			} else {
				throw new ValidationException("Y/N Quantity",fieldname,null);
			}
		}
		public static boolean isGood(String data) {
			return data.contains("y") || data.contains("t") ||  data.contains("1");
		}
		public static boolean isBad(String data){
			return data.contains("0") || data.contains("f") || data.contains("n");
		}
	}
	
	public GameSprite myGraphic;
	public String myInstanceName;
	@DefaultValue(value="0,0")
	public float[] position;
	@DefaultValue(value="0f")
	public float bombTime;
	@DefaultValue(value="0")
	public float killTime;
	@DefaultValue(value="0")
	public float timeProtect;
	@DefaultValue(value="0")
	public int bombs;
	@DefaultValue(value="0")
	public int lives;
	@DefaultValue(value="0")
	public float power;
	@DefaultValue(value="0")
	public int score;
	@DefaultValue(value="0")
	public int graze;
	@DefaultValue(value="0")
	public boolean wantsDeathBomb;
	@DefaultValue(value="")
	public float[] bombPosition;
	/**
	 * width of the player's collision box 
	 */
	@FromScript()
	@DefaultValue(value = ".01f")
	public float collisionW;
	/**
	 * height of the player's collision box 
	 */
	@FromScript()
	@DefaultValue(value = ".01f")
	public float collisionH;	
	/**
	 * width of the player on screen (remember, coordinates are 0-1) 
	 */
	@FromScript()
	@DefaultValue(value = ".05")
	public double w;
	/**
	 * height of the player on screen (remember, coordinates are 0-1) 
	 */
	@FromScript()
	@DefaultValue(value = ".05")
	public double h;
	/**
	 * Allows animation of the player sprite. If not animated, frame 0 is shown. 
	 */
	@FromScript()
	@HasValidator(num = GraphicsHolderParser.AnimationValidator.ANIMATION_VALIDATOR)
	@DefaultValue(value = GraphicsHolder.MOTION_FRAMES_DEFAULT)
	public String animate;
	public int[][] animate_I;
	public void setupPlayer(){
		animate_I = GraphicsHolderParser.parseAnimationCommands(getAnimate());
	}
	/**
	 * Framerate of the animation
	 */
	@FromScript()
	@DefaultValue(value = "5")
	@HasValidator(num = BulletPattern.PositiveValidator.POSITIVE_VALIDATOR)
	public float animfps;
	
	public float getAnimFps() {
		return animfps;
	}
	public String getAnimate() {
		// TODO Auto-generated method stub
		return animate;
	}
	
	public int getH_tex() {
		return 1;
	}
	public int[] getTexGrid() {
		return new int[]{1};
	}
	public int getW_tex() {
		return 1;
	}

	/**
	 * Motion speed, when Shift is not pressed 
	 */
	@FromScript()
	@DefaultValue(value = ".05f")
	public float normalSpeed;
	/**
	 * Motion speed, when Shift is pressed 
	 */
	@FromScript()
	@DefaultValue(value = ".02f")
	public float focusSpeed;
	/**
	 * Whether or not to adjust the speed of the player over time. When yes, 
	 * the speed will be multiplied by the change in physical time to get a
	 * translation vector. Otherwise, the player's position will change by a set
	 * speed every frame. The actual effect of this setting is more of an aesthetics
	 * thingy.
	 */
	@FromScript()
	@HasValidator(num = YesNoValidator.YES_NO_VALIDATOR)
	@DefaultValue(value = "y")
	public String motionTimeAdjust;
	@FromScript()
	@CriticalScriptField()
	public String CharName;
	@FromScript()
	@CriticalScriptField()
	public String SignName;

	public boolean isMotionTimeAdjusted(){
		String data = motionTimeAdjust;
		boolean isGood  = YesNoValidator.isGood(data);
		return isGood;
	}
	
	
	/*** AUTOWRITTEN ***/
	private void customDeserializeanimate_I() {
		// TODO Auto-generated method stub
		
	}
	private void customSerializeanimate_I() {
		// TODO Auto-generated method stub
		
	}
	public ArrayList<Exception> parseFromStrings(TaiDAWG<String> data, Validator ... valid) {
		ArrayList<Exception> toRet = new ArrayList();
		WordByRef<String> word;
		word = data.get("collisionW");
		if (word!=null){String val = word.getContentData();
		collisionW= new Float(val.trim());
		}
		word = data.get("collisionH");
		if (word!=null){String val = word.getContentData();
		collisionH= new Float(val.trim());
		}
		word = data.get("w");
		if (word!=null){String val = word.getContentData();
		w= new Double(val.trim());
		}
		word = data.get("h");
		if (word!=null){String val = word.getContentData();
		h= new Double(val.trim());
		}
		word = data.get("animate");
		if (word!=null){String val = word.getContentData();
	try {
		valid[8].validate("animate",val);
	} catch (ValidationException e){
	toRet.add(e);}
		animate= val;
		}
		word = data.get("animfps");
		if (word!=null){String val = word.getContentData();
	try {
		valid[5].validate("animfps",val);
	} catch (ValidationException e){
	toRet.add(e);}
		animfps= new Float(val.trim());
		}
		word = data.get("normalSpeed");
		if (word!=null){String val = word.getContentData();
		normalSpeed= new Float(val.trim());
		}
		word = data.get("focusSpeed");
		if (word!=null){String val = word.getContentData();
		focusSpeed= new Float(val.trim());
		}
		word = data.get("motionTimeAdjust");
		if (word!=null){String val = word.getContentData();
	try {
		valid[6].validate("motionTimeAdjust",val);
	} catch (ValidationException e){
	toRet.add(e);}
		motionTimeAdjust= val;
		}
		word = data.get("CharName");
		if (word!=null){String val = word.getContentData();
		CharName= val;
		}
		 else {
			toRet.add(new FieldRequiredException("CharName required."));
		}
		word = data.get("SignName");
		if (word!=null){String val = word.getContentData();
		SignName= val;
		}
		 else {
			toRet.add(new FieldRequiredException("SignName required."));
		}
	StringTreeIterator<WordByRef<String>> iterator = data.iterator();
	while(iterator.hasNext()){
		String key = iterator.next();
	if (!key.equals("collisionW")&&!key.equals("collisionH")&&!key.equals("w")&&!key.equals("h")&&!key.equals("animate")&&!key.equals("animfps")&&!key.equals("normalSpeed")&&!key.equals("focusSpeed")&&!key.equals("motionTimeAdjust")&&!key.equals("CharName")&&!key.equals("SignName")){
			toRet.add(new ValidationException("Unrecognized var: "+key+".",key));
	}
		iterator.tryNext();
	}
		return toRet;
	}
	public void autoWrittenDeSerializeCode(){
		String myGraphic_strTmp= ((StringEntry)readField("myGraphic", new StringEntry(""))).getString();
		if (myGraphic_strTmp.length()>0){
			myGraphic = new GameSprite(myGraphic_strTmp);
		}
		myInstanceName = ((StringEntry)readField("myInstanceName", new StringEntry(""))).getString();
		position = ((FloatArrayEntry)readField("position", new FloatArrayEntry(new float[]{0,0}))).getFloatArray();
		bombTime = (float)((DoubleEntry)readField("bombTime", new DoubleEntry(0f))).getDouble();
		killTime = (float)((DoubleEntry)readField("killTime", new DoubleEntry(0))).getDouble();
		timeProtect = (float)((DoubleEntry)readField("timeProtect", new DoubleEntry(0))).getDouble();
		bombs = ((IntEntry)readField("bombs", new IntEntry(0))).getInt();
		lives = ((IntEntry)readField("lives", new IntEntry(0))).getInt();
		power = (float)((DoubleEntry)readField("power", new DoubleEntry(0))).getDouble();
		wantsDeathBomb = ((IntEntry)readField("wantsDeathBomb", new IntEntry(0))).getInt()==1;
		bombPosition = ((FloatArrayEntry)readField("bombPosition", new FloatArrayEntry(new float[]{}))).getFloatArray();
		collisionW = (float)((DoubleEntry)readField("collisionW", new DoubleEntry(.01f))).getDouble();
		collisionH = (float)((DoubleEntry)readField("collisionH", new DoubleEntry(.01f))).getDouble();
		w = ((DoubleEntry)readField("w", new DoubleEntry(.05))).getDouble();
		h = ((DoubleEntry)readField("h", new DoubleEntry(.05))).getDouble();
		animate = ((StringEntry)readField("animate", new StringEntry("null"))).getString();
		customDeserializeanimate_I();
		animfps = (float)((DoubleEntry)readField("animfps", new DoubleEntry(5))).getDouble();
		normalSpeed = (float)((DoubleEntry)readField("normalSpeed", new DoubleEntry(.05f))).getDouble();
		focusSpeed = (float)((DoubleEntry)readField("focusSpeed", new DoubleEntry(.02f))).getDouble();
		motionTimeAdjust = ((StringEntry)readField("motionTimeAdjust", new StringEntry("y"))).getString();
		CharName = ((StringEntry)readField("CharName", new StringEntry(""))).getString();
		SignName = ((StringEntry)readField("SignName", new StringEntry(""))).getString();
	}
	public void autoWrittenSerializeCode(){
		writeField("myGraphic", new StringEntry(myGraphic!=null?myGraphic.hashToString():""));
		writeField("myInstanceName", new StringEntry(myInstanceName));
		writeField("position", new FloatArrayEntry(position));
		writeField("bombTime", new DoubleEntry(bombTime));
		writeField("killTime", new DoubleEntry(killTime));
		writeField("timeProtect", new DoubleEntry(timeProtect));
		writeField("bombs", new IntEntry(bombs));
		writeField("lives", new IntEntry(lives));
		writeField("power", new DoubleEntry(power));
		writeField("wantsDeathBomb", new IntEntry(wantsDeathBomb?1:0));
		writeField("bombPosition", new FloatArrayEntry(bombPosition));
		writeField("collisionW", new DoubleEntry(collisionW));
		writeField("collisionH", new DoubleEntry(collisionH));
		writeField("w", new DoubleEntry(w));
		writeField("h", new DoubleEntry(h));
		writeField("animate", new StringEntry(animate));
		customSerializeanimate_I();
		writeField("animfps", new DoubleEntry(animfps));
		writeField("normalSpeed", new DoubleEntry(normalSpeed));
		writeField("focusSpeed", new DoubleEntry(focusSpeed));
		writeField("motionTimeAdjust", new StringEntry(motionTimeAdjust));
		writeField("CharName", new StringEntry(CharName));
		writeField("SignName", new StringEntry(SignName));
	}
}
