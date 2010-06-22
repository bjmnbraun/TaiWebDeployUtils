package BulletGame$2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import TaiGameCore.GameDataBase;
import TaiGameCore.GameVirtualFS;
import TaiGameCore.TaiDAWG;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;
import ddf.minim.AudioSample;
import ddf.minim.Minim;

/**
 * Global Settings that affect the entire script.
 * Syntactically, this is a degenerate item, but it obeys the normal syntax rules.
 */
public class BulletGlobals extends GameDataBase implements GameDataBase.StringBase{
	public BulletGlobals(String hash) {
		super(hash);
	}
	public static class AspectRatioValidator implements StringBase.Validator{
		public static final int ASPECT_RATIO_VALIDATOR = 7; 
		public void validate(String fieldname, String data)
		throws ValidationException {
			try {
				float aspectRatio2 = getAspectRatio(data);
				if (aspectRatio2 < 0){
					throw new RuntimeException("Negative AR");
				}
			} catch (Throwable e){
				throw new ValidationException(e.getMessage(),fieldname,e);
			}
		}

	}
	public static class SoundValidator implements StringBase.Validator{
		public static final int SOUND_VALIDATOR = 10; 
		public void validate(String fieldname, String data)
		throws ValidationException {
			try {
				//Currently, everything goes.
			} catch (Throwable e){
				throw new ValidationException(e.getMessage(),fieldname,e);
			}
		}

	}
	
	public static final String NOT_DEFINED = "null";

	/**
	 * Imposes that the entire Game Area will have a certain aspect ratio. The entire Game Area uses the interval [0,1] x [0,1] to display. 
	 * When displaying the Game Area, the window will be Centered and Scaled until the Height or the Width
	 * is the maximum value allowed.
	 **/
	@FromScript()
	@HasValidator(num = AspectRatioValidator.ASPECT_RATIO_VALIDATOR)
	@DefaultValue(value = "1:1")
	public String AspectRatio;	
	/**
	 * When the player is hit, he becomes invulnerable for a certain amount of time.
	 * Units are in seconds.
	 **/
	@FromScript()
	@HasValidator(num = BulletPattern.PositiveValidator.POSITIVE_VALIDATOR)
	@DefaultValue(value = "1")
	public float InvincibilityTime;
	/**
	 * I want you to guess what this does. It has units of seconds.
	 */
	@FromScript()
	@HasValidator(num = BulletPattern.PositiveValidator.POSITIVE_VALIDATOR)
	@DefaultValue(value = ".08f")	
	public float BorderOfLife;
	/**
	 * Customizable sound
	 */
	@FromScript()
	@DefaultValue(value = BulletLevel.NO_AUDIO)
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String levelUpSound;
	/**
	 * Customizable sound, also played on picking up a small powerup
	 */
	@FromScript()
	@DefaultValue(value = "Tock.mp3")
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String powerUpSound;
	/**
	 * Customizable sound
	 */
	@FromScript()
	@DefaultValue(value = "Vawoosh.mp3")
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String familiarSpawnSound;
	/**
	 * Customizable sound
	 */
	@FromScript()
	@DefaultValue(value = "WarmUp.mp3")
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String chargeUpSound;
	/**
	 * Customizable sound
	 */
	@FromScript()
	@DefaultValue(value = "WhooshCard.mp3")
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String bulletFlipSound;
	/**
	 * Customizable sound, also plays on player death
	 */
	@FromScript()
	@DefaultValue(value = BulletLevel.NO_AUDIO)
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String playerDeathSound;
	/**
	 * Customizable sound
	 */
	@FromScript()
	@DefaultValue(value = "Cymbal.mp3")
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String sparkleSound;
	/**
	 * Customizable sound, also plays on enemy death
	 */
	@FromScript()
	@DefaultValue(value = BulletLevel.NO_AUDIO)
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String enemyDeathSound;
	/**
	 * Customizable sound, also plays at the start of spellcards
	 */
	@FromScript()
	@DefaultValue(value = BulletLevel.NO_AUDIO)
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String spellCardStartSound;
	/**
	 * Customizable sound, also plays when the boss takes damage
	 */
	@FromScript()
	@DefaultValue(value = BulletLevel.NO_AUDIO)
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String bossDamageSound;
	/**
	 * Customizable sound, also plays when you graze a bullet
	 */
	@FromScript()
	@DefaultValue(value = BulletLevel.NO_AUDIO)
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String grazeSound;
	/**
	 * Customizable sound
	 */
	@FromScript()
	@DefaultValue(value = BulletLevel.NO_AUDIO)
	@HasValidator(num = SoundValidator.SOUND_VALIDATOR)
	public String laserSound;

	/**
	 * String replacements, have effect on every Expression-valued parameter in the
	 * file. To use a replacement in an expression, simply use the name (say, $E) anywhere 
	 * inside the expression. If $A - $G aren't enough for you, you can put multiple ($ sign
	 * delimited) strings into these fields, and address them via $A[index].
	 * For Example, $A = 3$4$5 can be used <var>=$A[0]+$A[1]+$A[2].
	 * Note that the replacements are parenthesized, so 
	 * $B[2]*$B[3], where $B[2] = 3+2 and $B[3] = 2 will be
	 * "(3+2)*2". This is for your convenience, of course.
	 */
	@FromScript()
	@DefaultValue(value = NOT_DEFINED)
	public String $A;
	/**
	 * See $A
	 */
	@FromScript()
	@DefaultValue(value = NOT_DEFINED)
	public String $B;
	/**
	 * See $A
	 */
	@FromScript()
	@DefaultValue(value = NOT_DEFINED)
	public String $C;
	/**
	 * See $A
	 */
	@FromScript()
	@DefaultValue(value = NOT_DEFINED)
	public String $D;
	/**
	 * See $A
	 */
	@FromScript()
	@DefaultValue(value = NOT_DEFINED)
	public String $E;
	/**
	 * See $A
	 */
	@FromScript()
	@DefaultValue(value = NOT_DEFINED)
	public String $F;
	/**
	 * See $A
	 */
	@FromScript()
	@DefaultValue(value = NOT_DEFINED)
	public String $G;

	
	
	public static int LEVELUPSOUND = 0;
	public static int POWERUPSOUND = 1;
	public static int FAMILIARSPAWNSOUND = 2;
	public static int CHARGEUPSOUND = 3;
	public static int BULLETFLIPSOUND = 4;
	public static int PLAYERDEATHSOUND = 5;
	public static int SPARKLESOUND = 6;
	public static int ENEMYDEATHSOUND = 7;
	public static int SPELLCARDSTARTSOUND = 8;
	public static int BOSSDAMAGESOUND = 9;
	public static int GRAZESOUND = 10;
	public static int LASERSOUND = 11;
	public AudioSample[] getAudioSamples(GameVirtualFS file_system, Minim m){AudioSample[] toRet = new AudioSample[12];
	if (!levelUpSound.equals(BulletLevel.NO_AUDIO)){toRet[LEVELUPSOUND] = file_system.loadAudioSample(levelUpSound,m);}
	if (!powerUpSound.equals(BulletLevel.NO_AUDIO)){toRet[POWERUPSOUND] = file_system.loadAudioSample(powerUpSound,m);}
	if (!familiarSpawnSound.equals(BulletLevel.NO_AUDIO)){toRet[FAMILIARSPAWNSOUND] = file_system.loadAudioSample(familiarSpawnSound,m);}
	if (!chargeUpSound.equals(BulletLevel.NO_AUDIO)){toRet[CHARGEUPSOUND] = file_system.loadAudioSample(chargeUpSound,m);}
	if (!bulletFlipSound.equals(BulletLevel.NO_AUDIO)){toRet[BULLETFLIPSOUND] = file_system.loadAudioSample(bulletFlipSound,m);}
	if (!playerDeathSound.equals(BulletLevel.NO_AUDIO)){toRet[PLAYERDEATHSOUND] = file_system.loadAudioSample(playerDeathSound,m);}
	if (!sparkleSound.equals(BulletLevel.NO_AUDIO)){toRet[SPARKLESOUND] = file_system.loadAudioSample(sparkleSound,m);}
	if (!enemyDeathSound.equals(BulletLevel.NO_AUDIO)){toRet[ENEMYDEATHSOUND] = file_system.loadAudioSample(enemyDeathSound,m);}
	if (!spellCardStartSound.equals(BulletLevel.NO_AUDIO)){toRet[SPELLCARDSTARTSOUND] = file_system.loadAudioSample(spellCardStartSound,m);}
	if (!bossDamageSound.equals(BulletLevel.NO_AUDIO)){toRet[BOSSDAMAGESOUND] = file_system.loadAudioSample(bossDamageSound,m);}
	if (!grazeSound.equals(BulletLevel.NO_AUDIO)){toRet[GRAZESOUND] = file_system.loadAudioSample(grazeSound,m);}
	if (!laserSound.equals(BulletLevel.NO_AUDIO)){toRet[LASERSOUND] = file_system.loadAudioSample(laserSound,m);}
	return toRet;}
	public static int getSampleFromName(String name){
	if (name.equalsIgnoreCase("levelUpSound")){return LEVELUPSOUND;}
	if (name.equalsIgnoreCase("powerUpSound")){return POWERUPSOUND;}
	if (name.equalsIgnoreCase("familiarSpawnSound")){return FAMILIARSPAWNSOUND;}
	if (name.equalsIgnoreCase("chargeUpSound")){return CHARGEUPSOUND;}
	if (name.equalsIgnoreCase("bulletFlipSound")){return BULLETFLIPSOUND;}
	if (name.equalsIgnoreCase("playerDeathSound")){return PLAYERDEATHSOUND;}
	if (name.equalsIgnoreCase("sparkleSound")){return SPARKLESOUND;}
	if (name.equalsIgnoreCase("enemyDeathSound")){return ENEMYDEATHSOUND;}
	if (name.equalsIgnoreCase("spellCardStartSound")){return SPELLCARDSTARTSOUND;}
	if (name.equalsIgnoreCase("bossDamageSound")){return BOSSDAMAGESOUND;}
	if (name.equalsIgnoreCase("grazeSound")){return GRAZESOUND;}
	if (name.equalsIgnoreCase("laserSound")){return LASERSOUND;}
	return -1;}
	
	public String replaceGlobalConstants(String str){
		Pattern compile = Pattern.compile("\\$[A-G](\\[\\d+\\])?");
		Matcher m = compile.matcher(str);
		StringBuffer toRet = new StringBuffer();
		while(true){
			if (!m.find()){
				break;
			}
			char whichChar = str.charAt(m.start()+1);
			String rep = null;
			switch(whichChar){
			//GLOBALS CASE BLOCK
			case 'A':
				if ($A!=null){
					if ($A.equals(NOT_DEFINED)){
						throw new RuntimeException("$A not defined.");
					}}
				rep = $A;
				break;
			case 'B':
				if ($B!=null){
					if ($B.equals(NOT_DEFINED)){
						throw new RuntimeException("$B not defined.");
					}}
				rep = $B;
				break;
			case 'C':
				if ($C!=null){
					if ($C.equals(NOT_DEFINED)){
						throw new RuntimeException("$C not defined.");
					}}
				rep = $C;
				break;
			case 'D':
				if ($D!=null){
					if ($D.equals(NOT_DEFINED)){
						throw new RuntimeException("$D not defined.");
					}}
				rep = $D;
				break;
			case 'E':
				if ($E!=null){
					if ($E.equals(NOT_DEFINED)){
						throw new RuntimeException("$E not defined.");
					}}
				rep = $E;
				break;
			case 'F':
				if ($F!=null){
					if ($F.equals(NOT_DEFINED)){
						throw new RuntimeException("$F not defined.");
					}}
				rep = $F;
				break;
			case 'G':
				if ($G!=null){
					if ($G.equals(NOT_DEFINED)){
						throw new RuntimeException("$G not defined.");
					}}
				rep = $G;
				break;
			}
			if (rep!=null){
				if ((m.end()-m.start())!=2){
					int index = Integer.parseInt(str.substring(m.start()+3,m.end()-1));
					//Part.
					String[] line = rep.split("\\$");
					try{
						rep = line[index];
					} catch (ArrayIndexOutOfBoundsException e){
						throw new RuntimeException("Bad Index: "+index);
					}
				} else {
					//Whole string
					//System.out.println(whichChar);
				}
				//Parenthesize the results, so it behaves as expected.
				m.appendReplacement(toRet,m.quoteReplacement("("+rep+")"));
			} else {
				m.appendReplacement(toRet,"");
			}
			if (m.hitEnd()){
				break;
			}
		}
		m.appendTail(toRet);
		
		return toRet.toString();
	}

	public static void main(String[] args){
		BulletGlobals bulletGlobals = new BulletGlobals("");
		bulletGlobals.$A = "max(3,2)$b";
		System.out.println(bulletGlobals.replaceGlobalConstants("$A[0]"));
		if (true) return;
		
		/** Handle sounds **/
		mainSoundsAutowrite();

		/** Ok, now handle the #define statements**/
		mainConstantsAutowrite();
	}
	private static void mainConstantsAutowrite(){
		ArrayList<String> names = new ArrayList();
		for(Field k : BulletGlobals.class.getFields()){
			if (k.getName().startsWith("$")){
				names.add(k.getName());
			}
		}
		System.out.println("//GLOBALS CASE BLOCK");
		for(String k : names){
			System.out.println("case \'"+k.charAt(1)+"\':");
			System.out.println("if ("+k+"!=null){");
			System.out.println("if ("+k+".equals(NOT_DEFINED)){");
			System.out.println("throw new RuntimeException(\""+k+" not defined.\");");
			System.out.println("}}");
			System.out.println("rep = "+k+";");
			System.out.println("break;");
		}
	}
	private static void mainSoundsAutowrite(){

		ArrayList<String> names = new ArrayList();
		for(Field k : BulletGlobals.class.getFields()){
			if (k.getName().endsWith("Sound")){
				names.add(k.getName());
			}
		}
		int count = 0;
		for(String k : names){
			System.out.println("public static int "+k.toUpperCase()+" = "+count+++";");
		}
		System.out.println("public AudioSample[] getAudioSamples(GameVirtualFS file_system, Minim m){AudioSample[] toRet = new AudioSample["+names.size()+"];");
		for(String k : names){
			System.out.println("if (!"+k+".equals(BulletLevel.NO_AUDIO)){toRet["+k.toUpperCase()+"] = file_system.loadAudioSample("+k+",m);}");
		}
		System.out.println("return toRet;}");
		System.out.println("public static int getSampleFromName(String name){");
		for(String k : names){
			System.out.println("if (name.equalsIgnoreCase(\""+ k +"\")){return "+k.toUpperCase()+";}");
		}
		System.out.println("return -1;}");
	}

	public static float getAspectRatio(String data){
		if (data.contains(":")){
			String[] val = data.split(":");
			float w = new Float(val[0]);
			float h = new Float(val[1]);
			return w/h;
		}
		return new Float(data);
	}

	/**
	 * 
	 */
	public ArrayList<Exception> parseFromStrings(TaiDAWG<String> data, Validator ... valid) {
		ArrayList<Exception> toRet = new ArrayList();
		WordByRef<String> word;
		word = data.get("AspectRatio");
		if (word!=null){String val = word.getContentData();
	try {
		valid[7].validate("AspectRatio",val);
	} catch (ValidationException e){
	toRet.add(e);}
		AspectRatio= val;
		}
		word = data.get("InvincibilityTime");
		if (word!=null){String val = word.getContentData();
	try {
		valid[5].validate("InvincibilityTime",val);
	} catch (ValidationException e){
	toRet.add(e);}
		InvincibilityTime= new Float(val.trim());
		}
		word = data.get("BorderOfLife");
		if (word!=null){String val = word.getContentData();
	try {
		valid[5].validate("BorderOfLife",val);
	} catch (ValidationException e){
	toRet.add(e);}
		BorderOfLife= new Float(val.trim());
		}
		word = data.get("levelUpSound");
		if (word!=null){String val = word.getContentData();
		levelUpSound= val;
		}
		word = data.get("powerUpSound");
		if (word!=null){String val = word.getContentData();
		powerUpSound= val;
		}
		word = data.get("familiarSpawnSound");
		if (word!=null){String val = word.getContentData();
		familiarSpawnSound= val;
		}
		word = data.get("chargeUpSound");
		if (word!=null){String val = word.getContentData();
		chargeUpSound= val;
		}
		word = data.get("bulletFlipSound");
		if (word!=null){String val = word.getContentData();
		bulletFlipSound= val;
		}
		word = data.get("playerDeathSound");
		if (word!=null){String val = word.getContentData();
		playerDeathSound= val;
		}
		word = data.get("sparkleSound");
		if (word!=null){String val = word.getContentData();
		sparkleSound= val;
		}
		word = data.get("enemyDeathSound");
		if (word!=null){String val = word.getContentData();
		enemyDeathSound= val;
		}
		word = data.get("spellCardStartSound");
		if (word!=null){String val = word.getContentData();
		spellCardStartSound= val;
		}
		word = data.get("bossDamageSound");
		if (word!=null){String val = word.getContentData();
		bossDamageSound= val;
		}
		word = data.get("grazeSound");
		if (word!=null){String val = word.getContentData();
		grazeSound= val;
		}
		word = data.get("laserSound");
		if (word!=null){String val = word.getContentData();
		laserSound= val;
		}
		word = data.get("$A");
		if (word!=null){String val = word.getContentData();
		$A= val;
		}
		word = data.get("$B");
		if (word!=null){String val = word.getContentData();
		$B= val;
		}
		word = data.get("$C");
		if (word!=null){String val = word.getContentData();
		$C= val;
		}
		word = data.get("$D");
		if (word!=null){String val = word.getContentData();
		$D= val;
		}
		word = data.get("$E");
		if (word!=null){String val = word.getContentData();
		$E= val;
		}
		word = data.get("$F");
		if (word!=null){String val = word.getContentData();
		$F= val;
		}
		word = data.get("$G");
		if (word!=null){String val = word.getContentData();
		$G= val;
		}
	StringTreeIterator<WordByRef<String>> iterator = data.iterator();
	while(iterator.hasNext()){
		String key = iterator.next();
	if (!key.equals("AspectRatio")&&!key.equals("InvincibilityTime")&&!key.equals("BorderOfLife")&&!key.equals("levelUpSound")&&!key.equals("powerUpSound")&&!key.equals("familiarSpawnSound")&&!key.equals("chargeUpSound")&&!key.equals("bulletFlipSound")&&!key.equals("playerDeathSound")&&!key.equals("sparkleSound")&&!key.equals("enemyDeathSound")&&!key.equals("spellCardStartSound")&&!key.equals("bossDamageSound")&&!key.equals("grazeSound")&&!key.equals("laserSound")&&!key.equals("$A")&&!key.equals("$B")&&!key.equals("$C")&&!key.equals("$D")&&!key.equals("$E")&&!key.equals("$F")&&!key.equals("$G")){
			toRet.add(new ValidationException("Unrecognized var: "+key+".",key));
	}
		iterator.tryNext();
	}
		return toRet;
	}
	public void autoWrittenDeSerializeCode(){
		AspectRatio = ((StringEntry)readField("AspectRatio", new StringEntry("1:1"))).getString();
		InvincibilityTime = (float)((DoubleEntry)readField("InvincibilityTime", new DoubleEntry(1))).getDouble();
		BorderOfLife = (float)((DoubleEntry)readField("BorderOfLife", new DoubleEntry(.08f))).getDouble();
		levelUpSound = ((StringEntry)readField("levelUpSound", new StringEntry("null"))).getString();
		powerUpSound = ((StringEntry)readField("powerUpSound", new StringEntry("Tock.mp3"))).getString();
		familiarSpawnSound = ((StringEntry)readField("familiarSpawnSound", new StringEntry("Vawoosh.mp3"))).getString();
		chargeUpSound = ((StringEntry)readField("chargeUpSound", new StringEntry("WarmUp.mp3"))).getString();
		bulletFlipSound = ((StringEntry)readField("bulletFlipSound", new StringEntry("WhooshCard.mp3"))).getString();
		playerDeathSound = ((StringEntry)readField("playerDeathSound", new StringEntry("null"))).getString();
		sparkleSound = ((StringEntry)readField("sparkleSound", new StringEntry("Cymbal.mp3"))).getString();
		enemyDeathSound = ((StringEntry)readField("enemyDeathSound", new StringEntry("null"))).getString();
		spellCardStartSound = ((StringEntry)readField("spellCardStartSound", new StringEntry("null"))).getString();
		bossDamageSound = ((StringEntry)readField("bossDamageSound", new StringEntry("null"))).getString();
		grazeSound = ((StringEntry)readField("grazeSound", new StringEntry("null"))).getString();
		laserSound = ((StringEntry)readField("laserSound", new StringEntry("null"))).getString();
		$A = ((StringEntry)readField("$A", new StringEntry("null"))).getString();
		$B = ((StringEntry)readField("$B", new StringEntry("null"))).getString();
		$C = ((StringEntry)readField("$C", new StringEntry("null"))).getString();
		$D = ((StringEntry)readField("$D", new StringEntry("null"))).getString();
		$E = ((StringEntry)readField("$E", new StringEntry("null"))).getString();
		$F = ((StringEntry)readField("$F", new StringEntry("null"))).getString();
		$G = ((StringEntry)readField("$G", new StringEntry("null"))).getString();
	}
	public void autoWrittenSerializeCode(){
		writeField("AspectRatio", new StringEntry(AspectRatio));
		writeField("InvincibilityTime", new DoubleEntry(InvincibilityTime));
		writeField("BorderOfLife", new DoubleEntry(BorderOfLife));
		writeField("levelUpSound", new StringEntry(levelUpSound));
		writeField("powerUpSound", new StringEntry(powerUpSound));
		writeField("familiarSpawnSound", new StringEntry(familiarSpawnSound));
		writeField("chargeUpSound", new StringEntry(chargeUpSound));
		writeField("bulletFlipSound", new StringEntry(bulletFlipSound));
		writeField("playerDeathSound", new StringEntry(playerDeathSound));
		writeField("sparkleSound", new StringEntry(sparkleSound));
		writeField("enemyDeathSound", new StringEntry(enemyDeathSound));
		writeField("spellCardStartSound", new StringEntry(spellCardStartSound));
		writeField("bossDamageSound", new StringEntry(bossDamageSound));
		writeField("grazeSound", new StringEntry(grazeSound));
		writeField("laserSound", new StringEntry(laserSound));
		writeField("$A", new StringEntry($A));
		writeField("$B", new StringEntry($B));
		writeField("$C", new StringEntry($C));
		writeField("$D", new StringEntry($D));
		writeField("$E", new StringEntry($E));
		writeField("$F", new StringEntry($F));
		writeField("$G", new StringEntry($G));
	}

}
 