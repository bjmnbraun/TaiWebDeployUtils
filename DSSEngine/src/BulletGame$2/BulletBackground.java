package BulletGame$2;

import java.util.ArrayList;

import com.iabcinc.jmep.Environment;
import com.iabcinc.jmep.XExpression;

import BulletGame$2.BulletPattern.ExpressionEvaluator;
import BulletGame$2.BulletPattern.PositiveValidator;
import TaiGameCore.GameDataBase;
import TaiGameCore.MultiExpression;
import TaiGameCore.TaiDAWG;
import TaiGameCore.GameDataBase.DefaultValue;
import TaiGameCore.GameDataBase.StringBase.FromScript;
import TaiGameCore.GameDataBase.StringBase.HasValidator;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;

/**
 * Describes a background, which will be displayed behind the danmaku. Backgrounds are
 * layered in the order they appear in their super-element. 
 * 
 * Backgrounds have slightly more graphical flexibility than some other elements, in that
 * they can be movies (MP4) or still images, and that they can be transformed in full 3D.
 */
public class BulletBackground extends BulletExpressionGameDB implements
		GameDataBase.StringBase {
	public BulletBackground(String hash) {
		super(hash);
	}

	/**
	 * The number of multiples of this background, in the x direction. An integer.
	 */
	@FromScript()
	@DefaultValue(value = "1")
	@HasValidator(num = PositiveValidator.POSITIVE_VALIDATOR)
	public int background_tilex;
	/**
	 * The number of multiples of this background, in the y direction. An integer.
	 */
	@FromScript()
	@DefaultValue(value = "1")
	@HasValidator(num = PositiveValidator.POSITIVE_VALIDATOR)
	public int background_tiley;
	/**
	 * A full OpenGL style transformation matrix, used for displaying the
	 * background. All 16 entries are expressions.
	 * 
	 * The matrix defaults to the identity (m1, m5, m9, m13 <= 1)
	 */
	@FromScript()
	@DefaultValue(value = "1")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m1;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m2;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m3;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m4;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m5;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "1")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m6;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m7;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m8;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m9;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m10;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "1")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m11;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m12;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m13;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m14;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m15;
	/**
	 * See bg_m1
	 */
	@FromScript()
	@DefaultValue(value = "1")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String bg_m16;
	/**
	 * Offset the background tile a fraction in the X direction.
	 * The result of this equation is modulus'ed one.
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String offsetX;
	/**
	 * Offset the background tile a fraction in the Y direction.
	 * The result of this equation is modulus'ed one.
	 */
	@FromScript()
	@DefaultValue(value = "0")
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	public String offsetY;

	/**
	 * Tints the red color component of the background, 0-1. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "1")
	public String tintR;
	/**
	 * Tints the green color component of the background, 0-1. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "1")
	public String tintG;
	/**
	 * Tints the blue color component of the background, 0-1. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "1")
	public String tintB;
	/**
	 * Tints the alpha color component of the background, 0-1. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "1")
	public String tintA;

	public static final String NO_BACKGROUND = "null";
	/**
	 * A URL to an MP4 movie file to stream, or a still image to display, in the background of the Playing Area.
	 */
	@FromScript()
	@DefaultValue(value = NO_BACKGROUND)
	public String background;
	/**
	 * The framerate at which to display the movie.
	 */
	@FromScript()
	@HasValidator(num = BulletPattern.PositiveValidator.POSITIVE_VALIDATOR)
	@DefaultValue(value = "30")
	public float backgroundFPS;

	/**
	 * 
	 */
	public float[] xFormBuffer;

	public String myInstanceName;

	public MultiExpression[] createExpression(Environment env, BulletGlobals bg)
			throws XExpression {
		return new MultiExpression[] {
				expr(bg_m1, env, bg), //0
				expr(bg_m2, env, bg), expr(bg_m3, env, bg),
				expr(bg_m4, env, bg), expr(bg_m5, env, bg),
				expr(bg_m6, env, bg), expr(bg_m7, env, bg),
				expr(bg_m8, env, bg), expr(bg_m9, env, bg),
				expr(bg_m10, env, bg), expr(bg_m11, env, bg),
				expr(bg_m12, env, bg), expr(bg_m13, env, bg),
				expr(bg_m14, env, bg),
				expr(bg_m15, env, bg),
				expr(bg_m16, env, bg), //15
				expr(tintR, env, bg), //16
				expr(tintG, env, bg), expr(tintB, env, bg),
				expr(tintA, env, bg), //19
				expr(offsetX, env, bg), //20
				expr(offsetY, env, bg), //21
		};
	}

	//AUTOWRITTEN
	public ArrayList<Exception> parseFromStrings(TaiDAWG<String> data,
			Validator... valid) {
		ArrayList<Exception> toRet = new ArrayList();
		WordByRef<String> word;
		word = data.get("background_tilex");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[5].validate("background_tilex", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			background_tilex = new Integer(val.trim());
		}
		word = data.get("background_tiley");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[5].validate("background_tiley", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			background_tiley = new Integer(val.trim());
		}
		word = data.get("bg_m1");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m1", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m1 = val;
		}
		word = data.get("bg_m2");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m2", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m2 = val;
		}
		word = data.get("bg_m3");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m3", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m3 = val;
		}
		word = data.get("bg_m4");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m4", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m4 = val;
		}
		word = data.get("bg_m5");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m5", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m5 = val;
		}
		word = data.get("bg_m6");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m6", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m6 = val;
		}
		word = data.get("bg_m7");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m7", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m7 = val;
		}
		word = data.get("bg_m8");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m8", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m8 = val;
		}
		word = data.get("bg_m9");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m9", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m9 = val;
		}
		word = data.get("bg_m10");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m10", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m10 = val;
		}
		word = data.get("bg_m11");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m11", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m11 = val;
		}
		word = data.get("bg_m12");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m12", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m12 = val;
		}
		word = data.get("bg_m13");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m13", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m13 = val;
		}
		word = data.get("bg_m14");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m14", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m14 = val;
		}
		word = data.get("bg_m15");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m15", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m15 = val;
		}
		word = data.get("bg_m16");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("bg_m16", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bg_m16 = val;
		}
		word = data.get("offsetX");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("offsetX", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			offsetX = val;
		}
		word = data.get("offsetY");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("offsetY", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			offsetY = val;
		}
		word = data.get("tintR");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("tintR", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			tintR = val;
		}
		word = data.get("tintG");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("tintG", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			tintG = val;
		}
		word = data.get("tintB");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("tintB", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			tintB = val;
		}
		word = data.get("tintA");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("tintA", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			tintA = val;
		}
		word = data.get("background");
		if (word != null) {
			String val = word.getContentData();
			background = val;
		}
		word = data.get("backgroundFPS");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[5].validate("backgroundFPS", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			backgroundFPS = new Float(val.trim());
		}
		StringTreeIterator<WordByRef<String>> iterator = data.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (!key.equals("background_tilex")
					&& !key.equals("background_tiley") && !key.equals("bg_m1")
					&& !key.equals("bg_m2") && !key.equals("bg_m3")
					&& !key.equals("bg_m4") && !key.equals("bg_m5")
					&& !key.equals("bg_m6") && !key.equals("bg_m7")
					&& !key.equals("bg_m8") && !key.equals("bg_m9")
					&& !key.equals("bg_m10") && !key.equals("bg_m11")
					&& !key.equals("bg_m12") && !key.equals("bg_m13")
					&& !key.equals("bg_m14") && !key.equals("bg_m15")
					&& !key.equals("bg_m16") && !key.equals("offsetX")
					&& !key.equals("offsetY") && !key.equals("tintR")
					&& !key.equals("tintG") && !key.equals("tintB")
					&& !key.equals("tintA") && !key.equals("background")
					&& !key.equals("backgroundFPS")) {
				toRet.add(new ValidationException("Unrecognized var: " + key
						+ ".", key));
			}
			iterator.tryNext();
		}
		return toRet;
	}

	public void autoWrittenDeSerializeCode() {
		background_tilex = ((IntEntry) readField("background_tilex",
				new IntEntry(1))).getInt();
		background_tiley = ((IntEntry) readField("background_tiley",
				new IntEntry(1))).getInt();
		bg_m1 = ((StringEntry) readField("bg_m1", new StringEntry("1")))
				.getString();
		bg_m2 = ((StringEntry) readField("bg_m2", new StringEntry("0")))
				.getString();
		bg_m3 = ((StringEntry) readField("bg_m3", new StringEntry("0")))
				.getString();
		bg_m4 = ((StringEntry) readField("bg_m4", new StringEntry("0")))
				.getString();
		bg_m5 = ((StringEntry) readField("bg_m5", new StringEntry("0")))
				.getString();
		bg_m6 = ((StringEntry) readField("bg_m6", new StringEntry("1")))
				.getString();
		bg_m7 = ((StringEntry) readField("bg_m7", new StringEntry("0")))
				.getString();
		bg_m8 = ((StringEntry) readField("bg_m8", new StringEntry("0")))
				.getString();
		bg_m9 = ((StringEntry) readField("bg_m9", new StringEntry("0")))
				.getString();
		bg_m10 = ((StringEntry) readField("bg_m10", new StringEntry("0")))
				.getString();
		bg_m11 = ((StringEntry) readField("bg_m11", new StringEntry("1")))
				.getString();
		bg_m12 = ((StringEntry) readField("bg_m12", new StringEntry("0")))
				.getString();
		bg_m13 = ((StringEntry) readField("bg_m13", new StringEntry("0")))
				.getString();
		bg_m14 = ((StringEntry) readField("bg_m14", new StringEntry("0")))
				.getString();
		bg_m15 = ((StringEntry) readField("bg_m15", new StringEntry("0")))
				.getString();
		bg_m16 = ((StringEntry) readField("bg_m16", new StringEntry("1")))
				.getString();
		offsetX = ((StringEntry) readField("offsetX", new StringEntry("0")))
				.getString();
		offsetY = ((StringEntry) readField("offsetY", new StringEntry("0")))
				.getString();
		tintR = ((StringEntry) readField("tintR", new StringEntry("1")))
				.getString();
		tintG = ((StringEntry) readField("tintG", new StringEntry("1")))
				.getString();
		tintB = ((StringEntry) readField("tintB", new StringEntry("1")))
				.getString();
		tintA = ((StringEntry) readField("tintA", new StringEntry("1")))
				.getString();
		background = ((StringEntry) readField("background", new StringEntry(
				"null"))).getString();
		backgroundFPS = (float) ((DoubleEntry) readField("backgroundFPS",
				new DoubleEntry(30))).getDouble();
		xFormBuffer = ((FloatArrayEntry) readField("xFormBuffer",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		myInstanceName = ((StringEntry) readField("myInstanceName",
				new StringEntry(""))).getString();
	}

	public void autoWrittenSerializeCode() {
		writeField("background_tilex", new IntEntry(background_tilex));
		writeField("background_tiley", new IntEntry(background_tiley));
		writeField("bg_m1", new StringEntry(bg_m1));
		writeField("bg_m2", new StringEntry(bg_m2));
		writeField("bg_m3", new StringEntry(bg_m3));
		writeField("bg_m4", new StringEntry(bg_m4));
		writeField("bg_m5", new StringEntry(bg_m5));
		writeField("bg_m6", new StringEntry(bg_m6));
		writeField("bg_m7", new StringEntry(bg_m7));
		writeField("bg_m8", new StringEntry(bg_m8));
		writeField("bg_m9", new StringEntry(bg_m9));
		writeField("bg_m10", new StringEntry(bg_m10));
		writeField("bg_m11", new StringEntry(bg_m11));
		writeField("bg_m12", new StringEntry(bg_m12));
		writeField("bg_m13", new StringEntry(bg_m13));
		writeField("bg_m14", new StringEntry(bg_m14));
		writeField("bg_m15", new StringEntry(bg_m15));
		writeField("bg_m16", new StringEntry(bg_m16));
		writeField("offsetX", new StringEntry(offsetX));
		writeField("offsetY", new StringEntry(offsetY));
		writeField("tintR", new StringEntry(tintR));
		writeField("tintG", new StringEntry(tintG));
		writeField("tintB", new StringEntry(tintB));
		writeField("tintA", new StringEntry(tintA));
		writeField("background", new StringEntry(background));
		writeField("backgroundFPS", new DoubleEntry(backgroundFPS));
		writeField("xFormBuffer", new FloatArrayEntry(xFormBuffer));
		writeField("myInstanceName", new StringEntry(myInstanceName));
	}

}
