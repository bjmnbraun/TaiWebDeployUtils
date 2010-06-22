package BulletGame$2;

import java.util.ArrayList;

import TaiGameCore.GameSprite;
import TaiGameCore.MultiExpression;
import TaiGameCore.TaiDAWG;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;

import com.iabcinc.jmep.Environment;
import com.iabcinc.jmep.XExpression;


public class BulletAccessory extends BulletRelativitable{
	public BulletAccessory(String hash) {
		super(hash);
	}

	public GameSprite myGraphic;
	@FromScript()
	@HasValidator(num = 0)
	@DefaultValue(value = ".05")
	public String x;
	@FromScript()
	@HasValidator(num = 0)
	@DefaultValue(value = ".05")
	public String y;
	@FromScript()
	@HasValidator(num = 0)
	@DefaultValue(value = "0")
	public String rotation;
	@FromScript()
	@HasValidator(num = 0)
	@DefaultValue(value = "0.05")
	public String w;
	@FromScript()
	@HasValidator(num = 0)
	@DefaultValue(value = "0.05")
	public String h;
	public String myInstanceName;

	//source for accessories is boss
	private static final int relative = SOURCE;

	public int getRelativeMode() {
		return relative;
	}
		
	public void calculateExpressions(MultiExpression[] mov, float[] xyrwh_access) throws XExpression {
		for(int k = 0; k < 5; k++){
			xyrwh_access[k] = mov[k].evaluatef();
		}
	}

	public int[][] animate_I;
	public MultiExpression[] createExpression(Environment env, BulletGlobals bg) throws XExpression {
		animate_I = GraphicsHolderParser.parseAnimationCommands(getAnimate());
		return new MultiExpression[]{
			expr(x,env,bg),
			expr(y,env,bg),
			expr(rotation,env,bg),
			expr(w,env,bg),
			expr(h,env,bg)
		};
	}
	
	public int getH_tex() {
		return 1;
	}
	public int[] getTexGrid() {
		return new int[1];
	}
	public int getW_tex() {
		return 1;
	}
	
	@FromScript()
	@HasValidator(num = GraphicsHolderParser.AnimationValidator.ANIMATION_VALIDATOR)
	@DefaultValue(value = GraphicsHolder.MOTION_FRAMES_DEFAULT)
	public String animate;
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
	

	//AUTOWRITTEN
	
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
		word = data.get("rotation");
		if (word!=null){String val = word.getContentData();
	try {
		valid[0].validate("rotation",val);
	} catch (ValidationException e){
	toRet.add(e);}
		rotation= val;
		}
		word = data.get("w");
		if (word!=null){String val = word.getContentData();
	try {
		valid[0].validate("w",val);
	} catch (ValidationException e){
	toRet.add(e);}
		w= val;
		}
		word = data.get("h");
		if (word!=null){String val = word.getContentData();
	try {
		valid[0].validate("h",val);
	} catch (ValidationException e){
	toRet.add(e);}
		h= val;
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
	if (!key.equals("x")&&!key.equals("y")&&!key.equals("rotation")&&!key.equals("w")&&!key.equals("h")&&!key.equals("animate")&&!key.equals("animfps")){
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
		x = ((StringEntry)readField("x", new StringEntry(".05"))).getString();
		y = ((StringEntry)readField("y", new StringEntry(".05"))).getString();
		rotation = ((StringEntry)readField("rotation", new StringEntry("0"))).getString();
		w = ((StringEntry)readField("w", new StringEntry("0.05"))).getString();
		h = ((StringEntry)readField("h", new StringEntry("0.05"))).getString();
		myInstanceName = ((StringEntry)readField("myInstanceName", new StringEntry(""))).getString();
		animate = ((StringEntry)readField("animate", new StringEntry("null"))).getString();
		animfps = (float)((DoubleEntry)readField("animfps", new DoubleEntry(5))).getDouble();
	}
	public void autoWrittenSerializeCode(){
		writeField("myGraphic", new StringEntry(myGraphic!=null?myGraphic.hashToString():""));
		writeField("x", new StringEntry(x));
		writeField("y", new StringEntry(y));
		writeField("rotation", new StringEntry(rotation));
		writeField("w", new StringEntry(w));
		writeField("h", new StringEntry(h));
		writeField("myInstanceName", new StringEntry(myInstanceName));
		writeField("animate", new StringEntry(animate));
		writeField("animfps", new DoubleEntry(animfps));
	}


}
