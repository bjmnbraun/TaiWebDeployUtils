package BulletGame$2;

import java.util.ArrayList;

import TaiGameCore.GameSprite;
import TaiGameCore.MultiExpression;
import TaiGameCore.TaiDAWG;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;

import com.iabcinc.jmep.Environment;
import com.iabcinc.jmep.XExpression;

public class BulletBoss extends BulletRelativitable{
	public BulletBoss(String hash) {
		super(hash);
	}

	public GameSprite myGraphic;
	public String myInstanceName;

	/**
	 * X position of the boss
	 */
	@FromScript()
	@HasValidator(num = 0)
	@DefaultValue(value = ".05")
	public String x;
	/**
	 * Y position of the boss
	 */
	@FromScript()
	@HasValidator(num = 0)
	@DefaultValue(value = ".05")
	public String y;
	/**
	 * Sprite rotation of boss
	 */
	@FromScript()
	@HasValidator(num = 0)
	@DefaultValue(value = "0")
	public String rotate;
	/**
	 * Width of boss's character
	 */
	@FromScript()
	@DefaultValue(value = ".05")
	public double w;
	/**
	 * Height of boss's character
	 */
	@FromScript()
	@DefaultValue(value = ".05")
	public double h;
	/**
	 * number of columns in the collision grid
	 */
	@FromScript()
	@DefaultValue(value="5")
	public int w_tex;
	/**
	 * number of rows in the collision grid
	 */
	@FromScript()
	@DefaultValue(value="5")
	public int h_tex;
	/**
	 * The collision grid, as a consecutive (scanned) sequence of numbers.
	 * 0 means bullets go through, 1 means collisions possible, otherwise, the
	 * area counts as graze. This is the same system for Bosses as Bullets.
	 */
	@FromScript()
	@DefaultValue(value="1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1")
	public int[] tex_grid;
	public int getH_tex() {
		return h_tex;
	}
	public int[] getTexGrid() {
		return tex_grid;
	}
	public int getW_tex() {
		return w_tex;
	}
	/**
	 * Same as player's animate
	 */
	@FromScript()
	@HasValidator(num = GraphicsHolderParser.AnimationValidator.ANIMATION_VALIDATOR)
	@DefaultValue(value = GraphicsHolder.MOTION_FRAMES_DEFAULT)
	public String animate;
	/**
	 * Same as player's animatefps
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
	
	/** state stuff **/
	public float start_time;
	public float predicted_start_time;
	
	private static final int relative = CENTER_SPOT;

	public int getRelativeMode() {
		return relative;
	}
	
	public int[][] animate_I;
	
	public MultiExpression[] createExpression(Environment env, BulletGlobals bg) throws XExpression {
		animate_I = GraphicsHolderParser.parseAnimationCommands(getAnimate());
		
		return new MultiExpression[]{
				expr(x,env,bg),
				expr(y,env,bg),
				expr(rotate,env,bg),
		};
	}
	//AUTOWRITTEN
	private void customSerializeanimate_I() {
		// TODO Auto-generated method stub
		
	}
	private void customDeserializeanimate_I() {
		// TODO Auto-generated method stub
		
	}
	
	public ArrayList<Exception> parseFromStrings(TaiDAWG<String> data, Validator ... valid) {
		ArrayList<Exception> toRet = new ArrayList();
		WordByRef<String> word;
		word = data.get("x");
		if (word!=null){String val = word.getContentData();
	try {
		valid[0].validate("x",val);
	} catch (ValidationException e){
	toRet.add(e);}
		x= val;
		}
		word = data.get("y");
		if (word!=null){String val = word.getContentData();
	try {
		valid[0].validate("y",val);
	} catch (ValidationException e){
	toRet.add(e);}
		y= val;
		}
		word = data.get("rotate");
		if (word!=null){String val = word.getContentData();
	try {
		valid[0].validate("rotate",val);
	} catch (ValidationException e){
	toRet.add(e);}
		rotate= val;
		}
		word = data.get("w");
		if (word!=null){String val = word.getContentData();
		w= new Double(val.trim());
		}
		word = data.get("h");
		if (word!=null){String val = word.getContentData();
		h= new Double(val.trim());
		}
		word = data.get("w_tex");
		if (word!=null){String val = word.getContentData();
		w_tex= new Integer(val.trim());
		}
		word = data.get("h_tex");
		if (word!=null){String val = word.getContentData();
		h_tex= new Integer(val.trim());
		}
		word = data.get("tex_grid");
		if (word!=null){String val = word.getContentData();
		String[] spli = val.split(",");
		tex_grid= new int[spli.length];
		for(int k = 0; k < spli.length; k++){
			tex_grid[k]=new Integer(spli[k].trim());
		}
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
	StringTreeIterator<WordByRef<String>> iterator = data.iterator();
	while(iterator.hasNext()){
		String key = iterator.next();
	if (!key.equals("x")&&!key.equals("y")&&!key.equals("rotate")&&!key.equals("w")&&!key.equals("h")&&!key.equals("w_tex")&&!key.equals("h_tex")&&!key.equals("tex_grid")&&!key.equals("animate")&&!key.equals("animfps")){
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
		x = ((StringEntry)readField("x", new StringEntry(".05"))).getString();
		y = ((StringEntry)readField("y", new StringEntry(".05"))).getString();
		rotate = ((StringEntry)readField("rotate", new StringEntry("0"))).getString();
		w = ((DoubleEntry)readField("w", new DoubleEntry(.05))).getDouble();
		h = ((DoubleEntry)readField("h", new DoubleEntry(.05))).getDouble();
		w_tex = ((IntEntry)readField("w_tex", new IntEntry(5))).getInt();
		h_tex = ((IntEntry)readField("h_tex", new IntEntry(5))).getInt();
		tex_grid = ((IntArrayEntry)readField("tex_grid", new IntArrayEntry(new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}))).getIntArray();
		animate = ((StringEntry)readField("animate", new StringEntry("null"))).getString();
		animfps = (float)((DoubleEntry)readField("animfps", new DoubleEntry(5))).getDouble();
		start_time = (float)((DoubleEntry)readField("start_time", new DoubleEntry())).getDouble();
		customDeserializeanimate_I();
	}

	public void autoWrittenSerializeCode(){
		writeField("myGraphic", new StringEntry(myGraphic!=null?myGraphic.hashToString():""));
		writeField("myInstanceName", new StringEntry(myInstanceName));
		writeField("x", new StringEntry(x));
		writeField("y", new StringEntry(y));
		writeField("rotate", new StringEntry(rotate));
		writeField("w", new DoubleEntry(w));
		writeField("h", new DoubleEntry(h));
		writeField("w_tex", new IntEntry(w_tex));
		writeField("h_tex", new IntEntry(h_tex));
		writeField("tex_grid", new IntArrayEntry(tex_grid));
		writeField("animate", new StringEntry(animate));
		writeField("animfps", new DoubleEntry(animfps));
		writeField("start_time", new DoubleEntry(start_time));
		customSerializeanimate_I();
	}

}
