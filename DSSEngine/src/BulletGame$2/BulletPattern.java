package BulletGame$2;

import java.awt.Polygon;
import java.util.ArrayList;

import processing.core.PApplet;
import BulletGame$1.BulletGame$1Engine$ABasicEngine;
import BulletGame$1.BulletGame$1Engine$L2$3$BulletGameBHIP.BulletHellInstancePlayer;
import BulletGame$2.BulletPlayer.YesNoValidator;
import TaiGameCore.GameSprite;
import TaiGameCore.MultiExpression;
import TaiGameCore.TaiDAWG;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;

import com.iabcinc.jmep.Environment;
import com.iabcinc.jmep.XExpression;

/**
 * Bullet patterns can be nested; In the same way that a "top level" bullet
 * is fired in a rhythym specified by fireOn parameters, bullets spawn their
 * "sub" bullets in a rhythym that begins at the time they are fired.
 */
public class BulletPattern extends BulletRelativitable {
	public static final float X2(float[] array, int index, int index2) {
		return array[index * states_delta_numFrames + index2];
	}

	public static final void X2(float[] array, int index, int index2,
			float value) {
		array[index * states_delta_numFrames + index2] = value;
	}

	public static final float X3(float[] array, int index) {
		return array[index];
	}

	public static final void X3(float[] array, int index, float value) {
		array[index] = value;
	}

	public static final boolean ENEMY_VALENCE = false;
	public static final boolean PLAYER_VALENCE = true;

	public BulletPattern(String hash) {
		super(hash);
	}

	/**
	 * STRICTLY positive.
	 */
	public static class PositiveValidator implements StringBase.Validator {
		public static final int POSITIVE_VALIDATOR = 5;

		public void validate(String fieldname, String data)
				throws ValidationException {
			try {
				Float data2 = new Float(data);
				if (data2 <= 0) {
					throw new ValidationException("must be > 0.", fieldname,
							null);
				}
			} catch (NumberFormatException e) {
				throw new ValidationException("Invalid number", fieldname, e);
			}
		}
	}

	public static class NeighborAlgorithmValidator implements
			StringBase.Validator {
		public static final int NEIGHBOR_VALIDATOR = 9;
		public static final String NO_ALG = "NO ALG";

		public void validate(String fieldname, String data)
				throws ValidationException {
			try {
				int closest = parseNeighborsClosest(data);
			} catch (NumberFormatException e) {
				throw new ValidationException("Invalid number", fieldname, e);
			} catch (RuntimeException e) {
				throw new ValidationException(e.getMessage(), fieldname, e);
			}
		}
	}

	public static int parseNeighborsClosest(String field) {
		if (field.equals(NeighborAlgorithmValidator.NO_ALG)) {
			return -1;
		}
		String[] parsed = field.trim().split("\\s+");
		if (parsed.length != 2) {
			throw new RuntimeException("Bad Syntax");
		}
		if (!parsed[0].equalsIgnoreCase("closest")) {
			throw new RuntimeException("No Alg(" + parsed[0] + ")");
		}
		return new Integer(parsed[1]);
	}

	public static class ModeValidator implements StringBase.Validator {
		public static final int MODE_VALIDATOR = 1;
		public final static String RECT = "rect";
		public final static String POLAR = "polar";

		public void validate(String fieldname, String data)
				throws ValidationException {
			data = data.toLowerCase();
			if (!(data.equalsIgnoreCase(RECT) || data.equals(POLAR))) {
				throw new ValidationException("Invalid mode", fieldname, null);
			}
		}
	}

	public static class RelativeCoordsValidator implements StringBase.Validator {
		public static final int RELATIVE_COORDS_VALIDATOR = 2;

		public static final String TOP_LEFT_CORNER = "topleft";
		public static final String TOP_RIGHT_CORNER = "topright";
		public static final String BOTTOM_RIGHT_CORNER = "bottomright";
		public static final String BOTTOM_LEFT_CORNER = "bottomleft";
		public static final String CENTER_SPOT = "center";
		public static final String SOURCE = "source";
		public static final String SOURCELOCK = "sourcelock";
		public static final String PLAYER = "player";

		public void validate(String fieldname, String data)
				throws ValidationException {
			data = data.toLowerCase();
			if (!(data.equalsIgnoreCase(TOP_LEFT_CORNER)
					|| data.equals(TOP_RIGHT_CORNER)
					|| data.equals(BOTTOM_RIGHT_CORNER)
					|| data.equals(BOTTOM_LEFT_CORNER) || data.equals(SOURCE)
					|| data.equals(SOURCELOCK) || data.equals(PLAYER) || data
					.equals(CENTER_SPOT))) {
				throw new ValidationException("Invalid mode", fieldname, null);
			}
		}
	}

	public static class ExpressionEvaluator implements StringBase.Validator {
		public ExpressionEvaluator(Environment env, BulletGlobals bg) {
			this.env = env;
			this.bg = bg;
		}

		public static final int EXPRESSION_VALIDATOR = 0;
		private BulletGlobals bg;
		private Environment env;

		public void validate(String fieldname, String data)
				throws ValidationException {
			try {
				MultiExpression made = new MultiExpression(data, env, bg);
				made.evaluatef();
			} catch (ClassCastException e) {
				//A . OK!
			} catch (XExpression e) {
				//e.printStackTrace();
				String errMsg = e.getMessage().trim();
				if (errMsg.toLowerCase().startsWith("error")) {
					errMsg = errMsg.substring("error".length());
				}
				throw new ValidationException(errMsg, fieldname, e);
			}
		}
	}

	public static class FireOnValidator implements StringBase.Validator {
		public static final int FIRE_ON_VALIDATOR = 3;

		public void validate(String fieldname, String data)
				throws ValidationException {
			try {
				ParseFireOn(data, null);
			} catch (Throwable e) {
				throw new ValidationException(e.getMessage(), fieldname, e);
			}
		}
	}

	private static void ParseFireOn(String data, BulletPattern target) {
		if (target != null) {
			target.fireOn_F = null;
		}
		data = data.toLowerCase();
		if (data.equals("collision")) {
			if (target != null) {
				target.fireOn_Collision = true;
				target.firesOncePerQuery = true; //Special cases use this
				target.fireOn_F = null;
			}
			return;
		}
		String[] dat = data.split(",");
		ArrayList<Float> fireOn_A = new ArrayList();
		for (String p : dat) {
			p = p.replaceAll("\\s+", "");
			if (p.length() == 0) {
				continue; //No problem with an empty value.
			}
			StringBuffer c = new StringBuffer();
			float[] command = new float[3];
			int cmdP = 0;
			int state = 0;
			//0 = still numeric
			//1 = x encountered, rest should be numeric
			//2 = - encountered, should have numeric then @
			//3 = @ encountered after -, rest should be numeric
			for (int ki = 0; ki < p.length(); ki++) {
				char k = p.charAt(ki);
				boolean dedicate = ki == p.length() - 1;
				switch (state) {
				case 0:
					if (k == 'x') {
						state = 1;
						dedicate = true;
						break;
					}
					if (k == '-') {
						state = 2;
						dedicate = true;
						break;
					}
					c.append(k);
					break;
				case 1:
					c.append(k);
					break;
				case 2:
					if (k == '@') {
						state = 3;
						dedicate = true;
						break;
					}
					c.append(k);
					break;
				case 3:
					c.append(k);
					break;
				}

				if (dedicate) {
					if (c.length() == 0) {
						throw new RuntimeException("Empty argument");
					}
					command[cmdP++] = new Float(c.toString());
					c = new StringBuffer();
				}
			}
			if (cmdP == 1) {
				fireOn_A.add(command[0] - 1);
			}
			if (cmdP == 2) {
				if ((int) command[1] <= 0) {
					throw new RuntimeException("Nonpos multiplier");
				}
				for (int k = 0; k < (int) command[1]; k++) {
					fireOn_A.add(command[0] - 1);
				}
			}
			if (cmdP == 3) {
				if (command[2] < 1e-5) {
					throw new RuntimeException("0 = modulus");
				}
				for (float a = command[0]; a <= command[1]; a += command[2]) {
					fireOn_A.add(a - 1);
				}
			}
		}
		float[] toRet = new float[fireOn_A.size()];
		int i = 0;
		for (float p : fireOn_A) {
			toRet[i++] = p;
		}
		if (target != null)
			target.fireOn_F = toRet;
	}

	/**
	 * All subnodes of a bullet which are also bullets will be default be considered
	 * "sub bullets". This field allows you to also include other bullets in your stage
	 * to be fired as sub bullets from this bullet. Format is a comma separated list of
	 * fully addressed bullet nodes. See the "Addressing Nodes" section.
	 */
	@FromScript()
	public String addSubs;
	/**
	 * Specifies the units which fireOn and fireOn_measure are in. Can be in seconds or beats.
	 * For seconds, give a floating point number. For beats, add 'b' at the end of the number.
	 */
	@FromScript()
	@CriticalScriptField()
	public String fireOn_Int;
	/**
	 * Specifies when to fire this bullet. 
	 */
	@FromScript()
	@CriticalScriptField()
	@HasValidator(num = FireOnValidator.FIRE_ON_VALIDATOR)
	public String fireOn;
	public float[] fireOn_F;
	private float fireOn_Int_F;
	private boolean firesOncePerQuery = false;
	private boolean fireOn_Int_B;
	public boolean fireOn_Collision;
	public boolean fireOn_Death;
	/**
	 * Length of a measure; Together with Fire_On, a bullet "rhythym" 
	 * can be created.
	 */
	@FromScript()
	//TODO: FloatValidator?
	@CriticalScriptField()
	public float fireOn_measure;
	/**
	 * When to dispose of bullets.
	 * Negative values means as soon as they leave the screen
	 * Should be rarely used, I guess ...
	 */
	@FromScript()
	@DefaultValue(value = "-2")
	public double lifetime;
	/**
	 * This is currently not scriptable, because people will abuse it
	 */
	private static final float integrationInterval = 1f / 120;
	/** Position **/
	/**
	 * Whether to use Rectangular (Rect, x, y, and rotate are used) or Polar (theta and v are used)
	 * expressions to calculate the bullet's position. Polar mode is very different from Rectangular mode,
	 * in that in Polar mode, the coordinates are calculated by simple integration; the instantaneous
	 * values returned by theta and v represent a velocity vector. 
	 */
	@FromScript()
	@HasValidator(num = ModeValidator.MODE_VALIDATOR)
	@DefaultValue(value = ModeValidator.RECT)
	public String mode;

	/**
	 * In the rectangular coordinate modes, this is the x coordinate of the bullet. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = ".05")
	public String x;
	private static final int X_CONST = 0;
	/**
	 * In the rectangular coordinate modes, this is the y coordinate of the bullet. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = ".05")
	public String y;
	private static final int Y_CONST = 1;
	/**
	 * In the rectangular coordinate modes, this is the rotation of the bullet glyph. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "0")
	public String rotate;
	private static final int ROTATE_CONST = 2;
	/**
	 * In the polar coordinate modes, this is the bearing (radians) of the velocity vector. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "0")
	public String theta;
	private static final int THETA_CONST = 0;
	/**
	 * In the polar coordinate modes, this is the magnitude of the velocity vector. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "0")
	public String v;
	private static final int V_CONST = 1;
	/**
	 * Tints the red color component of the bullet sprite, 0-1. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "1")
	public String tintR;
	private static final int TINTR_CONST = 3;
	/**
	 * Tints the green color component of the bullet sprite, 0-1. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "1")
	public String tintG;
	private static final int TINTG_CONST = 4;
	/**
	 * Tints the blue color component of the bullet sprite, 0-1. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "1")
	public String tintB;
	private static final int TINTB_CONST = 5;
	/**
	 * Tints the alpha color component of the bullet sprite, 0-1. An expression.
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "1")
	public String tintA;
	private static final int TINTA_CONST = 6;
	/**
	 * Sets the resolution of the Polar coordinate mode's integration. That is,
	 * this is the "step size" in euler's integration method.
	 */
	@FromScript()
	@HasValidator(num = BulletPattern.PositiveValidator.POSITIVE_VALIDATOR)
	@DefaultValue(value = "1f/30")
	public float vecUpdLimit;

	/**
	 * Specifies which sound plays when this bullet is spawned. The choices
	 * are any of the sound constants in the "globals" node, whether they
	 * are also used for another purpose in the level is irrelevent.
	 */
	@FromScript()
	@DefaultValue(value = "")
	public String sound;
	public int soundIndex;

	@FromScript()
	@HasValidator(num = NeighborAlgorithmValidator.NEIGHBOR_VALIDATOR)
	@DefaultValue(value = NeighborAlgorithmValidator.NO_ALG)
	public String neighbors;
	public int closest_num;

	/**
	 * Only if yes, can this bullet harm a player. Otherwise, it is merely a visual.
	 */
	@FromScript()
	@HasValidator(num = YesNoValidator.YES_NO_VALIDATOR)
	@DefaultValue(value = "y")
	public String physical;
	public boolean isPhysical;

	/**
	 * Specifies whether contact with a player harms the player. No for powerups.
	 */
	@FromScript()
	@HasValidator(num = YesNoValidator.YES_NO_VALIDATOR)
	@DefaultValue(value = "y")
	public String harmful;
	public boolean isHarmful;

	/**
	 * Only applies to subnodes of Players: Does this bullet fire when the bomb
	 * button is pressed (otherwise, it fires when the player presses fire).
	 * 
	 * Bullets that are bombs "sweep" enemy bullets off the stage. Additionally, they
	 * do more damage to bosses, unless the bosses enable "bomb invulnerability".
	 */
	@FromScript()
	@HasValidator(num = YesNoValidator.YES_NO_VALIDATOR)
	@DefaultValue(value = "n")
	public String bomb;
	/**
	 * Only applies to subnodes of Players: Does this bullet fire when the bomb
	 * button is pressed (otherwise, it fires when the player presses fire).
	 * 
	 * Bullets that are bombs "sweep" enemy bullets off the stage. Additionally, they
	 * do more damage to bosses, unless the bosses enable "bomb invulnerability".
	 */
	@FromScript()
	@HasValidator(num = YesNoValidator.YES_NO_VALIDATOR)
	@DefaultValue(value = "n")
	public String fireOnDeath;
	/**
	 * When a shot is fired as a bomb, specifies what cost the player pays.
	 * Positive values are floored, then subtracted by the number of bombs the player has.
	 * Negative values are subtracted from the player's power.
	 * 0 does nothing.
	 * Currently it's not possible to make a "deathbomb" cost more. Whatever.
	 */
	@FromScript()
	@DefaultValue(value = "1")
	public float bombCost;

	public boolean isBomb;

	/**
	 * Only applies to subnodes of Players, where the Bomb parameter is set.
	 * Sets the duration of the bomb effect. During this time, the player is invincible.
	 */
	@FromScript()
	@HasValidator(num = BulletPattern.PositiveValidator.POSITIVE_VALIDATOR)
	@DefaultValue(value = "1")
	public float bombDuration;

	/**
	 * Relativity: Sets the origin for the coordinates generated by the position 
	 * expressions. 
	 */
	@FromScript()
	@HasValidator(num = RelativeCoordsValidator.RELATIVE_COORDS_VALIDATOR)
	@DefaultValue(value = RelativeCoordsValidator.SOURCE)
	public String relative;
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = ".05")
	public String w;
	private static final int W_CONST = 7;
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = ".05")
	public String h;
	private static final int H_CONST = 8;

	/**
	 * Sets when to run the Neighboring algorithms.
	 */
	@FromScript()
	@DefaultValue(value = ".05")
	public float linktime;

	/** sort of out of place, but whatever **/
	@DefaultValue(value = "?")
	public String myInstanceName;
	public GameSprite myBulletType;

	@FromScript()
	@HasValidator(num = PositiveValidator.POSITIVE_VALIDATOR)
	@DefaultValue(value = "5")
	public int w_tex;
	@FromScript()
	@HasValidator(num = PositiveValidator.POSITIVE_VALIDATOR)
	@DefaultValue(value = "5")
	public int h_tex;
	@FromScript()
	@DefaultValue(value = "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1")
	public int[] tex_grid;

	//public float start_time;

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
	 * Bullets can be animated, just as Bosses and Players can.
	 */
	@FromScript()
	@HasValidator(num = GraphicsHolderParser.AnimationValidator.ANIMATION_VALIDATOR)
	@DefaultValue(value = GraphicsHolder.MOTION_FRAMES_DEFAULT)
	public String animate;
	public int[][] animate_I;
	/**
	 * Bullets can be animated, just as Bosses and Players can.
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

	/**
	 * Used when recursively firing sub bullets of this pattern. Prevents
	 * infinite loops when a bullet has the ability to fire itself.
	 */
	public boolean visited;

	/**
	 * Non-null if the allocation thread (used to double the size of the bullet
	 * array) cannot allocate the space needed.
	 */
	private OutOfMemoryError oom_exception;

	/**
	 * STATE DATA: data referring to the children of this pattern.
	 * They area:
	 * states_time: birthtime of the child
	 * states_indexCt: instance number. (can of course be moduloed)
	 * state_index_lastFiring: optimization, start here to add a new bullet
	 * state_index_lastFiring_nanos: ms time of last shot. Equal to system.nanoTime()/1e6
	 */
	public float[] states_time;
	/**
	 * Seriously? This can alternate!? rofl.
	 */
	public boolean[] states_valence;
	public int[] states_indexCt;

	/** have positive length only when in some sort of integrated coordinates **/
	public float[] states_last_x;
	public float[] states_last_y;
	public int[] states_last_integrations;
	public float[] states_last_theta;
	public float[] states_last_v;

	public float[] states_fired;
	public int[] states_measure;

	public static final int states_delta_numFrames = 2;
	public int[] states_delta_pos;
	public int[] states_delta_net;
	/**
	 * Useful regex:
	 * ($\s+)(.+)\[(.+)\]\[(.+)\]\s*=\s*(.+);
	 * $1X2($2,$3,$4,$5);
	 */

	public float[] states_delta_x;
	public float[] states_delta_y;
	public float[] states_delta_widths;
	public float[] states_delta_heights;
	public float[] states_delta_rotations;

	//It's probably wasteful that these are deltas....
	public float[] states_delta_tintr;
	public float[] states_delta_tintg;
	public float[] states_delta_tintb;
	public float[] states_delta_tinta;

	//Give each bullet a random0.
	public float[] states_random0;

	public int[][] states_neighbors;

	/**
	 * How many bullets are not -1 in the states_indexCt array.
	 * Also, how many bullets are "alive".
	 */
	@DefaultValue(value = "0")
	public int states_stillAlive;

	/**
	 * The "Edgeweight" updating expression. (Used in the "neighboring bullets" algorithms)
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "0")
	public String edgew;
	public float[][][] states_neighbor_EW;
	/**
	 * The "Store" updating expression. (Used in the "neighboring bullets" algorithms)
	 */
	@FromScript()
	@HasValidator(num = ExpressionEvaluator.EXPRESSION_VALIDATOR)
	@DefaultValue(value = "store")
	public String store;
	public float[] states_store;

	public int states_neighbor_updates;

	/** Ok, each bullet has a personal coordinate system (!!!) **/
	public float[] states_coord_system_theta;
	public float[] states_coord_system_offx;
	public float[] states_coord_system_offy;
	/** Constant in terms of t? here's how. **/
	public static final int NUM_B_CONSTANTS = 9;
	public float[] states_constants;
	public float[] states_last_integration_time;

	public float[] states_angleToGirl0;

	@FromScript()
	@DefaultValue(value = "-1")
	public int health;
	public int[] states_healths;

	public float entire_start_time;
	public float[] states_start_times;

	public int state_index_lastFiring;

	//These are specific to the subgoup.
	//public int[] state_index_lastFiring_phaseNum;
	//public float[] state_index_lastFiring_s;
	public class BulletPatternFirableDescriptor implements BulletPath {
		public int state_index_lastFiring_phaseNum;
		public int bulletIndex = -1;
		public float state_index_lastFiring_s;

		public BulletPatternFirableDescriptor() {
		}

		public BulletPatternFirableDescriptor(int wo) {
			bulletIndex = wo;
		}

		public void reset() {
			state_index_lastFiring_phaseNum = 0;
			state_index_lastFiring_s = 0;
		}

		public void getPosition(float[] sourcePos, float time) {
			if (bulletIndex == -1) {
				throw new RuntimeException(
						"A BulletPatternFirableDescriptor acts as a path only in the case of nested bullets.");
			}
			//TODO: actually recalculate the position. This is allowed to be slow!
			//We will only use this call once per bullet shot, so do not worry!
			sourcePos[0] = X2(states_delta_x, bulletIndex,
					states_delta_pos[bulletIndex]);
			sourcePos[1] = X2(states_delta_y, bulletIndex,
					states_delta_pos[bulletIndex]);
		}

		/**
		 * TODO: save the nanotime of the last update, so that we can "unintegrate"
		 * our motion and place the source of bullets where they "wouldabeen"
		 */
	}

	/**
	 * If we have a child, we pass this down so that children can create themselves.
	 */
	public BulletPatternFirableDescriptor[] bulletSubFirables;
	/**
	 * If noone is our super, then we use this to generate ourself.
	 */
	public BulletPatternFirableDescriptor rootSubFirable;

	@DefaultValue(value = "0")
	private int state_index_bulletCounter;
	@DefaultValue(value = "" + RECT_COORDS)
	private int state_coordmode;
	private static final int RECT_COORDS = 0;
	private static final int POLAR_COORDS = 1;
	/** Which relative mode we use establishes how the coord system is picked **/
	@DefaultValue(value = "" + TOP_LEFT_CORNER)
	private int state_relative;
	public boolean doBeatPositions, doAngleToGirl;

	/**
	 * Determined by script 
	 */
	public int getRelativeMode() {
		return state_relative;
	}

	/**
	 * Multiple modes are achieved by multiple expressions being returned
	 * @throws XExpression 
	 */
	public MultiExpression[] createExpression(final BulletHellEnv env2,
			BulletGlobals bg) throws XExpression {

		isPhysical = YesNoValidator.isGood(physical);
		isHarmful = YesNoValidator.isGood(harmful);
		isBomb = YesNoValidator.isGood(bomb);
		fireOn_Death = YesNoValidator.isGood(fireOnDeath);
		//Parse the fireOn:
		ParseFireOn(fireOn, this);
		if (fireOn_Death && !isBomb) {
			firesOncePerQuery = true;
			fireOn_F = null;
		}
		if (fireOn_F != null) {
			fireOn_Int = fireOn_Int.replaceAll("\\s+", "");
			if (fireOn_Int.length() > 0) {
				fireOn_Int_B = fireOn_Int.charAt(fireOn_Int.length() - 1) == 'b';
				fireOn_Int_F = fireOn_Int_B ? new Float(fireOn_Int.substring(0,
						fireOn_Int.length() - 1)) : new Float(fireOn_Int);
				if (fireOn_Int_F < 0) {
					throw new RuntimeException("Negative Interval");
				}
				if (fireOn_Int_F < 1e-7) {
					firesOncePerQuery = true;
				}
			} else {
				throw new RuntimeException("No fireOn_Int");
			}
		}
		//Do we need beat calculations?
		boolean tmpdoBeatPositions = false;
		boolean tmpdoAngleToGirl = false;
		for (String k : new String[] { x, y, rotate, theta, v, w, h }) {
			tmpdoBeatPositions |= requiresBeatPositions(k, env2, bg);
			tmpdoAngleToGirl |= requiresAngleUpdates(k, env2, bg);
		}
		doBeatPositions = tmpdoBeatPositions;
		doAngleToGirl = tmpdoAngleToGirl;

		//Animations?
		animate_I = GraphicsHolderParser.parseAnimationCommands(getAnimate());

		Environment env = env2.env;
		if (state_coordmode == RECT_COORDS) {
			return new MultiExpression[] {
					expr(x, env2, isTIndependent(x, env2, bg) ? X_CONST : -1,
							bg),
					expr(y, env2, isTIndependent(y, env2, bg) ? Y_CONST : -1,
							bg),
					expr(rotate, env2,
							isTIndependent(rotate, env2, bg) ? ROTATE_CONST
									: -1, bg), };
		} else if (state_coordmode == POLAR_COORDS) {
			//NEW HACK: is the theta independent of t?
			boolean tIndependent = isTIndependent(this.theta, env2, bg)
					&& isTIndependent(this.v, env2, bg);
			if (tIndependent) { //NO integration
				state_coordmode = RECT_COORDS;
				//No integration required!
				final MultiExpression vexpr = expr(v, env2, V_CONST, bg);
				final MultiExpression thetaexpr = expr(theta, env2,
						THETA_CONST, bg);
				return new MultiExpression[] { new MultiExpression() {
					public float evaluatef() throws XExpression {
						float v = env2.v = vexpr.evaluatef();
						float theta = env2.theta = thetaexpr.evaluatef();
						theta %= PApplet.TWO_PI;
						return v * env2.g.CosLUT(-theta + PApplet.HALF_PI)
								* env2.t;
					}
				}, new MultiExpression() {
					public float evaluatef() throws XExpression {
						float v = env2.v = vexpr.evaluatef();
						float theta = env2.theta = thetaexpr.evaluatef();
						theta %= PApplet.TWO_PI;
						return v * env2.g.SinLUT(-theta + PApplet.HALF_PI)
								* env2.t;
					}
				}, new MultiExpression() {
					public float evaluatef() throws XExpression {
						return env2.theta = thetaexpr.evaluatef();
					}
				} };
			} else { //Most integration
				return new MultiExpression[] {
						expr(theta, env2,
								isTIndependent(theta, env2, bg) ? THETA_CONST
										: -1, bg),
						expr(v, env2, isTIndependent(v, env2, bg) ? V_CONST
								: -1, bg), };
			}
		} else {
			return null; //tOdo
		}
	}

	public MultiExpression[] createGfxExpression(BulletHellEnv env2,
			BulletGlobals bg) throws XExpression {
		return new MultiExpression[] {
				expr(w, env2, isTIndependent(w, env2, bg) ? W_CONST : -1, bg),
				expr(h, env2, isTIndependent(h, env2, bg) ? H_CONST : -1, bg), };
	}

	public MultiExpression[] createColorExpression(BulletHellEnv env2,
			BulletGlobals bg) throws XExpression {
		return new MultiExpression[] {
				expr(tintR, env2, isTIndependent(tintR, env2, bg) ? TINTR_CONST
						: -1, bg),
				expr(tintG, env2, isTIndependent(tintG, env2, bg) ? TINTG_CONST
						: -1, bg),
				expr(tintB, env2, isTIndependent(tintB, env2, bg) ? TINTB_CONST
						: -1, bg),
				expr(tintA, env2, isTIndependent(tintA, env2, bg) ? TINTA_CONST
						: -1, bg), };
	}

	public MultiExpression[] createEdgeExpression(BulletHellEnv env2,
			BulletGlobals bg) throws XExpression {
		return new MultiExpression[] { expr(store, env2, -1, bg),
				expr(edgew, env2, -1, bg), };
	}

	public final void calculatePosition(BulletGame$1Engine$ABasicEngine sinlut,
			BulletHellEnv env, float time, MultiExpression[] toMov,
			float[] position, int myIndex, BulletHellInstancePlayer bhip)
			throws XExpression {
		//Use this to update the deltapos
		float stMyIndex = states_time[myIndex];
		if (state_coordmode == RECT_COORDS) {
			env.t = time - stMyIndex; //just raw, these can be arbitrarily precise
			if (doBeatPositions) {
				env.tb = bhip.getBeatFromSongTime(time)
						- bhip.getBeatFromSongTime(stMyIndex);
			}
			position[0] = toMov[0].evaluatef();
			position[1] = toMov[1].evaluatef();
			//states_last_integration_time[myIndex] = time; //???
			//states_last_x[myIndex] = position[0];
			//states_last_y[myIndex] = position[1];
		} else if (state_coordmode == POLAR_COORDS) {
			float xnextPositionVector = 0;
			float ynextPositionVector = 0;
			float xnextPositionVectorLast = 0; //Not immediately added
			float ynextPositionVectorLast = 0;
			float myIntTime = states_last_integration_time[myIndex];
			float updateThreshold = (states_last_integrations[myIndex])
					* vecUpdLimit;
			while (true) {
				env.t = myIntTime - stMyIndex; //must threshold.
				if (doBeatPositions) {
					env.tb = bhip.getBeatFromSongTime(myIntTime)
							- bhip.getBeatFromSongTime(stMyIndex);
				}

				//Difference equations now supported!
				float r = env.theta = states_last_theta[myIndex];
				float v = env.v = states_last_v[myIndex];

				if (env.t > updateThreshold) {
					//UPDATE!~
					states_last_integrations[myIndex]++;
					updateThreshold += vecUpdLimit;
					//theta
					r = toMov[0].evaluatef();
					//v
					v = toMov[1].evaluatef();

					states_last_theta[myIndex] = r;
					states_last_v[myIndex] = v;
				}

				r = PApplet.HALF_PI - r;
				int loc = sinlut.getLUTLocus(r);
				float deltaT = time - myIntTime;
				if (deltaT > integrationInterval) {
					xnextPositionVector += (v * sinlut.cosLUT[loc]);
					ynextPositionVector += (v * sinlut.sinLUT[loc]);
					myIntTime += integrationInterval;
					continue;
				}
				//otherwise, break with multiplier:
				xnextPositionVectorLast += deltaT * (v * sinlut.cosLUT[loc]);
				ynextPositionVectorLast += deltaT * (v * sinlut.sinLUT[loc]);
				break;
			}
			;
			states_last_integration_time[myIndex] = myIntTime;
			states_last_x[myIndex] += xnextPositionVector * integrationInterval;
			states_last_y[myIndex] += ynextPositionVector * integrationInterval;
			position[0] = states_last_x[myIndex] + xnextPositionVectorLast;
			position[1] = states_last_y[myIndex] + ynextPositionVectorLast;
		} else {
			throw new RuntimeException("Unknown coordsmode");
		}
		//Just so we have the same...
		env.t = time - stMyIndex;
		//Now apply the coordinate transform:

		float orientation = states_coord_system_theta[myIndex];
		if (orientation < -1e-5f || orientation > 1e-5f) {
			float x = position[0];
			float y = position[1];
			int loc = sinlut.getLUTLocus(orientation);
			float ct = sinlut.cosLUT[loc];
			float st = sinlut.sinLUT[loc];
			position[0] = ct * x;
			position[1] = ct * y;
			position[0] += st * y;
			position[1] -= st * x;
		}
		position[0] += states_coord_system_offx[myIndex];
		position[1] += states_coord_system_offy[myIndex];
	}

	public void calculateEdges(BulletHellEnv env, float time,
			MultiExpression[] EdgeEvaluate) throws XExpression {
		//Evaluate the "stores"
		//Evaluate the "edgeweights"
		//while, updating the 
		//states_neighbor_updates
		float int_time = 1 / 120f;
		float stopTime = time - (0);
		while (true) {
			float timeOfUpdate = int_time * states_neighbor_updates;
			env.t = timeOfUpdate;
			if (env.t > stopTime) {
				break;
			}
			states_neighbor_updates++;
			for (int k = 0; k < states_indexCt.length; k++) {
				if (states_indexCt[k] >= 0) {
					int[] neighbs = states_neighbors[k];
					if (neighbs != null) {
						float[][] eWs = states_neighbor_EW[k];
						//Update stuff.
						env.store = states_store[k];
						env.count = k;
						env.measure = states_measure[k];
						env.fired = states_fired[k];
						env.dotE = 0;
						for (int eW = 0; eW < neighbs.length; eW++) {
							env.dotE += eWs[eW][0] * states_store[neighbs[eW]];
						}
						env.store = states_store[k] = EdgeEvaluate[0]
								.evaluatef();
						for (int eW = 0; eW < neighbs.length; eW++) {
							int p = neighbs[eW];
							env.store2 = states_store[p];
							env.edgew = eWs[eW][0];
							eWs[eW][1] += EdgeEvaluate[1].evaluatef();
							eWs[eW][2] = 1;
						}
					}
				}
			}
			for (int k = 0; k < states_indexCt.length; k++) {
				if (states_indexCt[k] >= 0) {
					int[] neighbs = states_neighbors[k];
					if (neighbs != null) {
						float[][] eWs = states_neighbor_EW[k];
						for (int eW = 0; eW < neighbs.length; eW++) {
							if (eWs[eW][2] > .5) {
								eWs[eW][2] = 0;
								eWs[eW][0] = eWs[eW][1];
								eWs[eW][1] = 0;
							}
						}
					}
				}
			}
		}
		//End.
	}

	public float calculateAngle2P(float x, float y, float[] playerPosition) {
		float angleToGirl = PApplet.atan2(-(playerPosition[1] - y),
				playerPosition[0] - x)
				+ PApplet.HALF_PI;
		return angleToGirl;
	}

	public void calculateRotations(MultiExpression[] toMov, float[] rotation,
			int myIndex) throws XExpression {
		rotation[0] = 0;
		if (state_coordmode == RECT_COORDS) {
			rotation[0] += toMov[2].evaluatef();
		} else if (state_coordmode == POLAR_COORDS) {
			rotation[0] += toMov[0].evaluatef();
		}
		rotation[0] %= PApplet.TWO_PI;
		rotation[0] = -rotation[0];
		rotation[0] -= (float) states_coord_system_theta[myIndex];

		//Yuck, we're keeping track of screen coords... hmm...
	}

	public void calculateGraphicScales(MultiExpression[] gfxE, float[] widths,
			int myIndex) throws XExpression {
		widths[0] = gfxE[0].evaluatef();
		widths[1] = gfxE[1].evaluatef();
	}

	public void calculateColors(MultiExpression[] colorsE, float[] colors)
			throws XExpression {
		for (int i = 0; i < 4; i++) {
			colors[i] = colorsE[i].evaluatef();
		}
	}

	public void doDamageAgainst(BulletPattern bp, int wo) {
		//int damage = bp.states_strength
		states_healths[wo]--;
	}

	public boolean isShootable() {
		return health >= 0;
	}

	public boolean isDead(double clifetime, float x, float y,
			Polygon corners_polygon, float corners_polygon_res, int k) {
		if (health >= 0 && states_healths[k] < 0) {
			return true;
		}
		if (this.lifetime < 0) {
			//do it by position:
			if (!corners_polygon.contains(x * corners_polygon_res, y
					* corners_polygon_res)) {
				return true;
			}
		} else {
			return clifetime > this.lifetime;
		}
		return false;
	}

	/**
	 * sourceposition should be the result of the relativitator.relativity (0,0)
	 * the offset rotation should be the camera's orientation
	 */
	public boolean handleFirings(float time, float start_time,
			float[] sourcePosition, BulletPath sourcePositionEvaluator,
			float[] playerPosition, float offsetRotation, boolean valence,
			BulletHellInstancePlayer bhip, BulletPatternFirableDescriptor bpfd,
			boolean fireEnabled) {
		boolean firedInNet = false;
		if (bpfd == null) {
			bpfd = rootSubFirable;
		}

		float firingtime = time - start_time;
		//state_index_lastFiring_s is the time of the last measure start
		boolean fired = false;
		float lastMeasureStart = bpfd.state_index_lastFiring_s;
		if (fireOn_Int_B) {
			firingtime = bhip.getBeatFromSongTime(time);
			if (firingtime < 0) {
				return firedInNet; //No bullets at negative time please.
			}
			float startBeat = Math.max(0, bhip.getBeatFromSongTime(start_time));
			firingtime -= startBeat;
		}

		float diff = firingtime - lastMeasureStart;
		int maxMeasuresPerFrame;
		//For very long measures, this will of course limit it to 1.
		if (!firesOncePerQuery) { //div by zero
			maxMeasuresPerFrame = (1024 / fireOn_F.length) + 1;
			diff /= fireOn_Int_F;
		} else {
			maxMeasuresPerFrame = 1;
		}
		for (int k = 0; k < maxMeasuresPerFrame; k++) { //multiple shots in a frame
			//System.out.println(time+" "+start_time+" "+firingtime+" "+lastMeasureStart+" "+diff);
			shotQuery: while (true) {
				if (!firesOncePerQuery) {
					if (bpfd.state_index_lastFiring_phaseNum >= fireOn_F.length) {
						break; //Outer loop controls multiple measure wraparounds
					}
					if (diff < fireOn_F[bpfd.state_index_lastFiring_phaseNum]) {
						break; //Not yet!
					}
				}

				float shouldaFiredTime;
				if (!firesOncePerQuery) {
					shouldaFiredTime = fireOn_Int_F
							* fireOn_F[bpfd.state_index_lastFiring_phaseNum];
					if (fireOn_Int_B) {
						shouldaFiredTime += Math.max(0, bhip
								.getBeatFromSongTime(start_time));
						//Change back to seconds now.
						shouldaFiredTime += lastMeasureStart;
						shouldaFiredTime = bhip
								.getTimeFromBeat(shouldaFiredTime);
					} else {
						shouldaFiredTime += start_time;
						shouldaFiredTime += lastMeasureStart;
					}
				} else {
					shouldaFiredTime = time;
				}
				fired = true;
				firedInNet = true;
				if (fireEnabled) {
					int loc = trueFire();
					states_time[loc] = shouldaFiredTime;
					states_start_times[loc] = start_time;
					states_valence[loc] = valence;
					states_healths[loc] = health;
					//handle relative mode here:
					if (sourcePositionEvaluator != null) {
						sourcePositionEvaluator.getPosition(sourcePosition,
								shouldaFiredTime);
					}
					states_coord_system_offx[loc] = sourcePosition[0];
					states_coord_system_offy[loc] = sourcePosition[1];
					states_coord_system_theta[loc] = offsetRotation;
					//Caclaulte position to player:
					float angleToGirl = calculateAngle2P(sourcePosition[0],
							sourcePosition[1], playerPosition);
					states_angleToGirl0[loc] = angleToGirl - offsetRotation;
					//Is this an integrated calculation mode?
					//TODO: offset initial x/y coords
					states_last_x[loc] = 0; //Bullets start from 0
					states_last_y[loc] = 0;
					states_last_integrations[loc] = 0;
					states_last_theta[loc] = 0;
					states_last_v[loc] = 0;
					states_last_integration_time[loc] = shouldaFiredTime;
					states_random0[loc] = (float) Math.random();
					//Clear the delta buffer
					for (int db = 0; db < states_delta_numFrames; db++) {
						X2(states_delta_x, loc, db, 0); //Bullets start from 0.
						X2(states_delta_y, loc, db, 0);
						X2(states_delta_widths, loc, db, 0);
						X2(states_delta_heights, loc, db, 0);
						X2(states_delta_rotations, loc, db, 0);
						X2(states_delta_tintr, loc, db, 0);
						X2(states_delta_tintg, loc, db, 0);
						X2(states_delta_tintb, loc, db, 0);
						X2(states_delta_tinta, loc, db, 0);
					}
					states_delta_pos[loc] = 0;
					states_delta_net[loc] = 0;
					if (bulletSubFirables[loc] != null) {
						bulletSubFirables[loc].reset();
					}
					if (fireOn_F != null) { //We don't care, otherwise.
						states_fired[loc] = fireOn_F[bpfd.state_index_lastFiring_phaseNum];
						states_measure[loc] = bpfd.state_index_lastFiring_phaseNum;
					}
					states_store[loc] = 0;
					states_neighbor_EW[loc] = null;
					states_neighbors[loc] = null;

					//clear the constants
					for (int cons = 0; cons < NUM_B_CONSTANTS; cons++) {
						states_constants[loc * NUM_B_CONSTANTS * 2 + cons * 2] = 0;
					}
				}
				bpfd.state_index_lastFiring_phaseNum++;

				//We fired, but...
				if (firesOncePerQuery) {
					break shotQuery;
				}
			} //otherwise, there just aren't any more shots to be done.
			if (fireOn_measure > 0) {
				if (diff >= fireOn_measure) {
					lastMeasureStart += fireOn_Int_F * fireOn_measure;
					bpfd.state_index_lastFiring_phaseNum = 0;
					//If we do this, we should continue right away.
					diff = firingtime - lastMeasureStart;
					diff /= fireOn_Int_F;
					continue;
				}
			}
			if (!fired) {
				break;
			}
			fired = false;
		} //END FOR LOOP
		//Sustain
		bpfd.state_index_lastFiring_s = lastMeasureStart;
		return firedInNet;
	}

	/**
	 * The ONLY method that is allowed to create a new bullet!
	 */
	private int trueFire() {
		int myNumber = state_index_bulletCounter++;
		int indexNumber = state_index_lastFiring;
		if (states_indexCt[indexNumber] == -1) {
			//it's free
			states_indexCt[indexNumber] = myNumber;
		} else {
			System.err.println("Warning: couldn't fire bullet.");
		}
		state_index_lastFiring = (state_index_lastFiring + 1)
				% states_indexCt.length;
		//quadratically probe length times, before giving up.
		int hk = state_index_lastFiring;
		boolean succeeded = false;
		for (int k = 1; k <= states_indexCt.length; k++) {
			if (states_indexCt[hk] == -1) {
				succeeded = true;
				break;
			}
			state_index_lastFiring = (hk + k + k * k) % states_indexCt.length;
		}
		if (!succeeded) {
			//double the length, set stateIndex to the first new element.
			hk = states_indexCt.length; //newest element available

			int nSize = states_indexCt.length * 2;
			stateResize(nSize);
		}
		state_index_lastFiring = hk;
		//New bullet.
		states_stillAlive++;
		return indexNumber;
	}

	public void startBackgroundThread() {

	}

	public void stopBackgroundThread() {

	}

	/**
	 * Copies all state doubles to arrays of a larger size
	 */
	public void stateResize(int nSize) {
		if (nSize <= states_indexCt.length) {
			throw new RuntimeException("You're not making it any bigger.");
		}
		float[] new_states_time = new float[nSize];
		boolean[] new_states_valences = new boolean[nSize];
		float[] new_states_coordmode_x = new float[nSize];
		float[] new_states_coordmode_y = new float[nSize];
		float[] new_states_coordmode_theta = new float[nSize];

		int[] new_states_measure = new int[nSize];
		float[] new_states_fired = new float[nSize];

		float[] new_states_last_x = new float[nSize];
		float[] new_states_last_y = new float[nSize];
		float[] new_states_last_theta = new float[nSize];
		float[] new_states_last_v = new float[nSize];
		int[] new_states_last_integrations = new int[nSize];
		float[] new_states_last_integration_time = new float[nSize];
		float[] new_states_delta_x = new float[nSize * states_delta_numFrames];
		float[] new_states_delta_y = new float[nSize * states_delta_numFrames];
		float[] new_states_delta_widths = new float[nSize
				* states_delta_numFrames];
		float[] new_states_delta_heights = new float[nSize
				* states_delta_numFrames];
		float[] new_states_delta_rotations = new float[nSize
				* states_delta_numFrames];
		float[] new_states_delta_tintr = new float[nSize
				* states_delta_numFrames];
		float[] new_states_delta_tintg = new float[nSize
				* states_delta_numFrames];
		float[] new_states_delta_tintb = new float[nSize
				* states_delta_numFrames];
		float[] new_states_delta_tinta = new float[nSize
				* states_delta_numFrames];

		float[] new_states_random0 = new float[nSize];
		int[] new_states_delta_net = new int[nSize];
		int[] new_states_delta_pos = new int[nSize];
		float[] new_states_constants = new float[nSize * NUM_B_CONSTANTS * 2];
		int[] new_states_healths = new int[nSize];
		float[] new_states_start_times = new float[nSize];
		BulletPatternFirableDescriptor[] new_firable_descriptors = new BulletPatternFirableDescriptor[nSize];
		float[] new_states_angleToGirl0 = new float[nSize];

		float[] new_states_store = new float[nSize];
		float[][][] new_states_neighbor_EW = new float[nSize][][];
		int[][] new_states_neighbors = new int[nSize][];

		//FILLS
		int[] new_states_indexCt = new int[nSize];

		final int states_delta_length = states_delta_x.length;
		final int states_time_length = states_time.length;
		for (int k = states_time_length; k < new_states_indexCt.length; k++) {
			//Default -1
			new_states_indexCt[k] = -1;
		}

		System
				.arraycopy(states_time, 0, new_states_time, 0,
						states_time_length);
		System.arraycopy(states_valence, 0, new_states_valences, 0,
				states_time_length);
		System.arraycopy(states_indexCt, 0, new_states_indexCt, 0,
				states_time_length);
		System.arraycopy(states_coord_system_offx, 0, new_states_coordmode_x,
				0, states_time_length);
		System.arraycopy(states_coord_system_offy, 0, new_states_coordmode_y,
				0, states_time_length);
		System.arraycopy(states_coord_system_theta, 0,
				new_states_coordmode_theta, 0, states_time_length);
		System.arraycopy(states_healths, 0, new_states_healths, 0,
				states_time_length);
		System.arraycopy(states_start_times, 0, new_states_start_times, 0,
				states_time_length);
		System.arraycopy(bulletSubFirables, 0, new_firable_descriptors, 0,
				states_time_length);

		System.arraycopy(states_angleToGirl0, 0, new_states_angleToGirl0, 0,
				states_time_length);
		System.arraycopy(states_random0, 0, new_states_random0, 0,
				states_time_length);

		System.arraycopy(states_constants, 0, new_states_constants, 0,
				states_constants.length);
		//We may drop the integration values after a while
		System.arraycopy(states_last_x, 0, new_states_last_x, 0,
				states_time_length);
		System.arraycopy(states_last_y, 0, new_states_last_y, 0,
				states_time_length);
		System.arraycopy(states_last_theta, 0, new_states_last_theta, 0,
				states_time_length);
		System.arraycopy(states_last_v, 0, new_states_last_v, 0,
				states_time_length);
		System.arraycopy(states_last_integrations, 0,
				new_states_last_integrations, 0, states_time_length);
		System.arraycopy(states_last_integration_time, 0,
				new_states_last_integration_time, 0, states_time_length);

		System.arraycopy(states_delta_x, 0, new_states_delta_x, 0,
				states_delta_length);
		System.arraycopy(states_delta_y, 0, new_states_delta_y, 0,
				states_delta_length);
		System.arraycopy(states_delta_widths, 0, new_states_delta_widths, 0,
				states_delta_length);
		System.arraycopy(states_delta_heights, 0, new_states_delta_heights, 0,
				states_delta_length);
		System.arraycopy(states_delta_rotations, 0, new_states_delta_rotations,
				0, states_delta_length);
		System.arraycopy(states_delta_tintr, 0, new_states_delta_tintr, 0,
				states_delta_length);
		System.arraycopy(states_delta_tintg, 0, new_states_delta_tintg, 0,
				states_delta_length);
		System.arraycopy(states_delta_tintb, 0, new_states_delta_tintb, 0,
				states_delta_length);
		System.arraycopy(states_delta_tinta, 0, new_states_delta_tinta, 0,
				states_delta_length);

		System.arraycopy(states_delta_pos, 0, new_states_delta_pos, 0,
				states_time_length);
		System.arraycopy(states_delta_net, 0, new_states_delta_net, 0,
				states_time_length);

		System.arraycopy(states_measure, 0, new_states_measure, 0,
				states_time_length);
		System.arraycopy(states_fired, 0, new_states_fired, 0,
				states_time_length);

		System.arraycopy(states_store, 0, new_states_store, 0,
				states_time_length);
		System.arraycopy(states_neighbor_EW, 0, new_states_neighbor_EW, 0,
				states_time_length);
		System.arraycopy(states_neighbors, 0, new_states_neighbors, 0,
				states_time_length);

		states_measure = new_states_measure;
		states_fired = new_states_fired;

		states_last_x = new_states_last_x;
		states_last_y = new_states_last_y;
		states_last_theta = new_states_last_theta;
		states_last_v = new_states_last_v;
		states_last_integrations = new_states_last_integrations;
		states_last_integration_time = new_states_last_integration_time;

		states_delta_x = new_states_delta_x;
		states_delta_y = new_states_delta_y;
		states_delta_widths = new_states_delta_widths;
		states_delta_heights = new_states_delta_heights;
		states_delta_rotations = new_states_delta_rotations;
		states_delta_tintr = new_states_delta_tintr;
		states_delta_tintg = new_states_delta_tintg;
		states_delta_tintb = new_states_delta_tintb;
		states_delta_tinta = new_states_delta_tinta;

		states_angleToGirl0 = new_states_angleToGirl0;
		states_random0 = new_states_random0;

		states_delta_pos = new_states_delta_pos;
		states_delta_net = new_states_delta_net;
		states_time = new_states_time;
		states_valence = new_states_valences;
		states_indexCt = new_states_indexCt;
		states_coord_system_offx = new_states_coordmode_x;
		states_coord_system_offy = new_states_coordmode_y;
		states_coord_system_theta = new_states_coordmode_theta;
		states_constants = new_states_constants;
		states_start_times = new_states_start_times;
		states_healths = new_states_healths;
		bulletSubFirables = new_firable_descriptors;

		states_store = new_states_store;
		states_neighbor_EW = new_states_neighbor_EW;
		states_neighbors = new_states_neighbors;

	}

	/**
	 * Specific form which takes advantage of t-dependence
	 *(If its not, pass in -1 as the constantIndex)
	 */
	public MultiExpression expr(String expression, final BulletHellEnv env2,
			final int constantIndex, BulletGlobals bg) throws XExpression {
		if (constantIndex < 0) {
			return super.expr(expression, env2.env, bg);
		} else {
			if (constantIndex >= NUM_B_CONSTANTS) {
				System.out.println("WARNING: INCREASE NUM_B_CONSTANTS");
			}
			return new MultiExpression(expression, env2.env, bg) {
				public float evaluatef() throws XExpression {
					int index = (env2.bulletk * NUM_B_CONSTANTS + constantIndex) << 1;
					if (states_constants[index] < .5f) {
						/**
						 * This ones needs to be calced
						 */
						states_constants[index | 1] = super.evaluatef();
						states_constants[index] = 1f;
					}
					return states_constants[index | 1];
				}
			};
		}
	}

	/**
	 * The ONLY method that removes a bullet from the list.
	 */
	public final void killBullet(int index) {
		/*
		if (states_indexCt[index] == -1){
			throw new RuntimeException("Bullet killed twice.");
		}
		*/
		states_stillAlive--;
		states_indexCt[index] = -1;
	}

	public void postScriptRead() {
		int numBullets = 64;
		states_indexCt = new int[numBullets];
		for (int k = 0; k < states_indexCt.length; k++) {
			states_indexCt[k] = -1;
		}
		states_time = new float[numBullets];
		states_valence = new boolean[numBullets];
		states_coord_system_offx = new float[numBullets];
		states_coord_system_offy = new float[numBullets];
		states_coord_system_theta = new float[numBullets];
		states_last_x = new float[numBullets];
		states_last_y = new float[numBullets];
		states_last_theta = new float[numBullets];
		states_last_v = new float[numBullets];
		states_last_integrations = new int[numBullets];

		bulletSubFirables = new BulletPatternFirableDescriptor[numBullets];
		rootSubFirable = new BulletPatternFirableDescriptor();

		states_angleToGirl0 = new float[numBullets];
		states_random0 = new float[numBullets];

		states_fired = new float[numBullets];
		states_measure = new int[numBullets];

		//states_delta_numFrames = 2;
		states_delta_x = new float[states_indexCt.length
				* states_delta_numFrames];
		states_delta_y = new float[states_indexCt.length
				* states_delta_numFrames];
		states_delta_widths = new float[states_indexCt.length
				* states_delta_numFrames];
		states_delta_heights = new float[states_indexCt.length
				* states_delta_numFrames];
		states_delta_rotations = new float[states_indexCt.length
				* states_delta_numFrames];

		states_delta_tintr = new float[states_indexCt.length
				* states_delta_numFrames];
		states_delta_tintg = new float[states_indexCt.length
				* states_delta_numFrames];
		states_delta_tintb = new float[states_indexCt.length
				* states_delta_numFrames];
		states_delta_tinta = new float[states_indexCt.length
				* states_delta_numFrames];

		states_delta_pos = new int[states_indexCt.length]; //0's
		states_delta_net = new int[states_indexCt.length];

		states_start_times = new float[states_indexCt.length];
		states_healths = new int[states_indexCt.length];

		states_last_integration_time = new float[states_indexCt.length];

		states_constants = new float[states_indexCt.length * NUM_B_CONSTANTS
				* 2];

		states_store = new float[states_indexCt.length];
		states_neighbor_EW = new float[states_indexCt.length][][];
		states_neighbors = new int[states_indexCt.length][];

		//Fill in the state_coordmode at this time.
		if (mode.equalsIgnoreCase(ModeValidator.RECT)) {
			state_coordmode = RECT_COORDS;
		}
		if (mode.equalsIgnoreCase(ModeValidator.POLAR)) {
			state_coordmode = POLAR_COORDS;
		}
		if (relative
				.equalsIgnoreCase(RelativeCoordsValidator.BOTTOM_LEFT_CORNER)) {
			state_relative = BOTTOM_LEFT_CORNER;
		}
		if (relative
				.equalsIgnoreCase(RelativeCoordsValidator.BOTTOM_RIGHT_CORNER)) {
			state_relative = BOTTOM_RIGHT_CORNER;
		}
		if (relative.equalsIgnoreCase(RelativeCoordsValidator.PLAYER)) {
			state_relative = PLAYER;
		}
		if (relative.equalsIgnoreCase(RelativeCoordsValidator.SOURCE)) {
			state_relative = SOURCE;
		}
		if (relative.equalsIgnoreCase(RelativeCoordsValidator.SOURCELOCK)) {
			state_relative = SOURCELOCK;
		}
		if (relative.equalsIgnoreCase(RelativeCoordsValidator.TOP_LEFT_CORNER)) {
			state_relative = TOP_LEFT_CORNER;
		}
		if (relative.equalsIgnoreCase(RelativeCoordsValidator.TOP_RIGHT_CORNER)) {
			state_relative = TOP_RIGHT_CORNER;
		}
		if (relative.equalsIgnoreCase(RelativeCoordsValidator.CENTER_SPOT)) {
			state_relative = CENTER_SPOT;
		}

		soundIndex = BulletGlobals.getSampleFromName(sound);
		closest_num = parseNeighborsClosest(neighbors);
	}

	/*
	public static void main(String[] args){
		System.out.println(isTIndependent("cos(3)+3*cos(ti)-4+ata"));
	}
	 */
	private static boolean isTIndependent(String theta2, BulletHellEnv be,
			BulletGlobals bg) {
		theta2 = bg.replaceGlobalConstants(theta2);
		String[] args = theta2.split("\\W+");
		for (String k : args) {
			if (k.equals("t") || k.equals("at") || k.equals("random")
					|| k.equals(be.BIGTBSTR) || k.equals(be.BIGANGLETOGIRLSTR)
					|| k.equals(be.BIGSTORESTR) || k.equals(be.BIGSOWSTR)
					|| k.equals(be.BIGVSTR) || k.equals(be.BIGTHETASTR)) {
				return false;
			}
		}
		return true;
	}

	private static boolean requiresAngleUpdates(String theta2,
			BulletHellEnv be, BulletGlobals bg) {
		theta2 = bg.replaceGlobalConstants(theta2);
		String[] args = theta2.split("\\W+");
		for (String k : args) {
			if (k.equals(be.BIGANGLETOGIRLSTR)) {
				return true;
			}
		}
		return false;
	}

	private static boolean requiresBeatPositions(String theta2,
			BulletHellEnv be, BulletGlobals bg) {
		theta2 = bg.replaceGlobalConstants(theta2);
		String[] args = theta2.split("\\W+");
		for (String k : args) {
			if (k.equals(be.BIGTBSTR)) {
				return true;
			}
		}
		return false;
	}

	/** AUTOWRITTEN **/
	private void customSerializestates_valence() {
		// TODO Auto-generated method stub

	}

	private void customDeserializestates_valence() {
		// TODO Auto-generated method stub

	}

	private void customSerializeanimate_I() {
		// TODO Auto-generated method stub

	}

	private void customSerializerootSubFirable() {
		// TODO Auto-generated method stub

	}

	private void customSerializelockOnMe() {
		// TODO Auto-generated method stub

	}

	private void customSerializebulletSubFirables() {
		// TODO Auto-generated method stub

	}

	private void customDeserializerootSubFirable() {
		// TODO Auto-generated method stub

	}

	private void customDeserializebulletSubFirables() {
		// TODO Auto-generated method stub

	}

	private void customDeserializeanimate_I() {
		// TODO Auto-generated method stub

	}

	private void customSerializestates_neighbor_EW() {
		// TODO Auto-generated method stub

	}

	private void customSerializestates_neighbors() {
		// TODO Auto-generated method stub

	}

	private void customDeserializestates_neighbor_EW() {
		// TODO Auto-generated method stub

	}

	private void customDeserializestates_neighbors() {
		// TODO Auto-generated method stub

	}

	private void customDeserializelockOnMe() {
		// TODO Auto-generated method stub

	}

	private void customDeserializeoom_exception() {
		// TODO Auto-generated method stub

	}

	private void customSerializeoom_exception() {
		// TODO Auto-generated method stub

	}

	public ArrayList<Exception> parseFromStrings(TaiDAWG<String> data,
			Validator... valid) {
		ArrayList<Exception> toRet = new ArrayList();
		WordByRef<String> word;
		word = data.get("addSubs");
		if (word != null) {
			String val = word.getContentData();
			addSubs = val;
		}
		word = data.get("fireOn_Int");
		if (word != null) {
			String val = word.getContentData();
			fireOn_Int = val;
		} else {
			toRet.add(new FieldRequiredException("fireOn_Int required."));
		}
		word = data.get("fireOn");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[3].validate("fireOn", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			fireOn = val;
		} else {
			toRet.add(new FieldRequiredException("fireOn required."));
		}
		word = data.get("fireOn_measure");
		if (word != null) {
			String val = word.getContentData();
			fireOn_measure = new Float(val.trim());
		} else {
			toRet.add(new FieldRequiredException("fireOn_measure required."));
		}
		word = data.get("lifetime");
		if (word != null) {
			String val = word.getContentData();
			lifetime = new Double(val.trim());
		}
		word = data.get("mode");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[1].validate("mode", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			mode = val;
		}
		word = data.get("x");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("x", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			x = val;
		}
		word = data.get("y");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("y", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			y = val;
		}
		word = data.get("rotate");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("rotate", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			rotate = val;
		}
		word = data.get("theta");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("theta", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			theta = val;
		}
		word = data.get("v");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("v", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			v = val;
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
		word = data.get("vecUpdLimit");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[5].validate("vecUpdLimit", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			vecUpdLimit = new Float(val.trim());
		}
		word = data.get("sound");
		if (word != null) {
			String val = word.getContentData();
			sound = val;
		}
		word = data.get("neighbors");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[9].validate("neighbors", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			neighbors = val;
		}
		word = data.get("physical");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[6].validate("physical", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			physical = val;
		}
		word = data.get("harmful");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[6].validate("harmful", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			harmful = val;
		}
		word = data.get("bomb");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[6].validate("bomb", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bomb = val;
		}
		word = data.get("fireOnDeath");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[6].validate("fireOnDeath", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			fireOnDeath = val;
		}
		word = data.get("bombCost");
		if (word != null) {
			String val = word.getContentData();
			bombCost = new Float(val.trim());
		}
		word = data.get("bombDuration");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[5].validate("bombDuration", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			bombDuration = new Float(val.trim());
		}
		word = data.get("relative");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[2].validate("relative", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			relative = val;
		}
		word = data.get("w");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("w", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			w = val;
		}
		word = data.get("h");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("h", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			h = val;
		}
		word = data.get("linktime");
		if (word != null) {
			String val = word.getContentData();
			linktime = new Float(val.trim());
		}
		word = data.get("w_tex");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[5].validate("w_tex", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			w_tex = new Integer(val.trim());
		}
		word = data.get("h_tex");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[5].validate("h_tex", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			h_tex = new Integer(val.trim());
		}
		word = data.get("tex_grid");
		if (word != null) {
			String val = word.getContentData();
			String[] spli = val.split(",");
			tex_grid = new int[spli.length];
			for (int k = 0; k < spli.length; k++) {
				tex_grid[k] = new Integer(spli[k].trim());
			}
		}
		word = data.get("animate");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[8].validate("animate", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			animate = val;
		}
		word = data.get("animfps");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[5].validate("animfps", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			animfps = new Float(val.trim());
		}
		word = data.get("edgew");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("edgew", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			edgew = val;
		}
		word = data.get("store");
		if (word != null) {
			String val = word.getContentData();
			try {
				valid[0].validate("store", val);
			} catch (ValidationException e) {
				toRet.add(e);
			}
			store = val;
		}
		word = data.get("health");
		if (word != null) {
			String val = word.getContentData();
			health = new Integer(val.trim());
		}
		StringTreeIterator<WordByRef<String>> iterator = data.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (!key.equals("addSubs") && !key.equals("fireOn_Int")
					&& !key.equals("fireOn") && !key.equals("fireOn_measure")
					&& !key.equals("lifetime") && !key.equals("mode")
					&& !key.equals("x") && !key.equals("y")
					&& !key.equals("rotate") && !key.equals("theta")
					&& !key.equals("v") && !key.equals("tintR")
					&& !key.equals("tintG") && !key.equals("tintB")
					&& !key.equals("tintA") && !key.equals("vecUpdLimit")
					&& !key.equals("sound") && !key.equals("neighbors")
					&& !key.equals("physical") && !key.equals("harmful")
					&& !key.equals("bomb") && !key.equals("fireOnDeath")
					&& !key.equals("bombCost") && !key.equals("bombDuration")
					&& !key.equals("relative") && !key.equals("w")
					&& !key.equals("h") && !key.equals("linktime")
					&& !key.equals("w_tex") && !key.equals("h_tex")
					&& !key.equals("tex_grid") && !key.equals("animate")
					&& !key.equals("animfps") && !key.equals("edgew")
					&& !key.equals("store") && !key.equals("health")) {
				toRet.add(new ValidationException("Unrecognized var: " + key
						+ ".", key));
			}
			iterator.tryNext();
		}
		return toRet;
	}

	public void autoWrittenDeSerializeCode() {
		addSubs = ((StringEntry) readField("addSubs", new StringEntry("")))
				.getString();
		fireOn_Int = ((StringEntry) readField("fireOn_Int", new StringEntry("")))
				.getString();
		fireOn = ((StringEntry) readField("fireOn", new StringEntry("")))
				.getString();
		fireOn_F = ((FloatArrayEntry) readField("fireOn_F",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		fireOn_Int_F = (float) ((DoubleEntry) readField("fireOn_Int_F",
				new DoubleEntry())).getDouble();
		firesOncePerQuery = ((IntEntry) readField("firesOncePerQuery",
				new IntEntry())).getInt() == 1;
		fireOn_Int_B = ((IntEntry) readField("fireOn_Int_B", new IntEntry()))
				.getInt() == 1;
		fireOn_Collision = ((IntEntry) readField("fireOn_Collision",
				new IntEntry())).getInt() == 1;
		fireOn_Death = ((IntEntry) readField("fireOn_Death", new IntEntry()))
				.getInt() == 1;
		fireOn_measure = (float) ((DoubleEntry) readField("fireOn_measure",
				new DoubleEntry())).getDouble();
		lifetime = ((DoubleEntry) readField("lifetime", new DoubleEntry(-2)))
				.getDouble();
		mode = ((StringEntry) readField("mode", new StringEntry("rect")))
				.getString();
		x = ((StringEntry) readField("x", new StringEntry(".05"))).getString();
		y = ((StringEntry) readField("y", new StringEntry(".05"))).getString();
		rotate = ((StringEntry) readField("rotate", new StringEntry("0")))
				.getString();
		theta = ((StringEntry) readField("theta", new StringEntry("0")))
				.getString();
		v = ((StringEntry) readField("v", new StringEntry("0"))).getString();
		tintR = ((StringEntry) readField("tintR", new StringEntry("1")))
				.getString();
		tintG = ((StringEntry) readField("tintG", new StringEntry("1")))
				.getString();
		tintB = ((StringEntry) readField("tintB", new StringEntry("1")))
				.getString();
		tintA = ((StringEntry) readField("tintA", new StringEntry("1")))
				.getString();
		vecUpdLimit = (float) ((DoubleEntry) readField("vecUpdLimit",
				new DoubleEntry(1f / 30))).getDouble();
		sound = ((StringEntry) readField("sound", new StringEntry("")))
				.getString();
		soundIndex = ((IntEntry) readField("soundIndex", new IntEntry()))
				.getInt();
		neighbors = ((StringEntry) readField("neighbors", new StringEntry(
				"NO ALG"))).getString();
		closest_num = ((IntEntry) readField("closest_num", new IntEntry()))
				.getInt();
		physical = ((StringEntry) readField("physical", new StringEntry("y")))
				.getString();
		isPhysical = ((IntEntry) readField("isPhysical", new IntEntry()))
				.getInt() == 1;
		harmful = ((StringEntry) readField("harmful", new StringEntry("y")))
				.getString();
		isHarmful = ((IntEntry) readField("isHarmful", new IntEntry()))
				.getInt() == 1;
		bomb = ((StringEntry) readField("bomb", new StringEntry("n")))
				.getString();
		fireOnDeath = ((StringEntry) readField("fireOnDeath", new StringEntry(
				"n"))).getString();
		bombCost = (float) ((DoubleEntry) readField("bombCost",
				new DoubleEntry(1))).getDouble();
		isBomb = ((IntEntry) readField("isBomb", new IntEntry())).getInt() == 1;
		bombDuration = (float) ((DoubleEntry) readField("bombDuration",
				new DoubleEntry(1))).getDouble();
		relative = ((StringEntry) readField("relative", new StringEntry(
				"source"))).getString();
		w = ((StringEntry) readField("w", new StringEntry(".05"))).getString();
		h = ((StringEntry) readField("h", new StringEntry(".05"))).getString();
		linktime = (float) ((DoubleEntry) readField("linktime",
				new DoubleEntry(.05))).getDouble();
		myInstanceName = ((StringEntry) readField("myInstanceName",
				new StringEntry("?"))).getString();
		String myBulletType_strTmp = ((StringEntry) readField("myBulletType",
				new StringEntry(""))).getString();
		if (myBulletType_strTmp.length() > 0) {
			myBulletType = new GameSprite(myBulletType_strTmp);
		}
		w_tex = ((IntEntry) readField("w_tex", new IntEntry(5))).getInt();
		h_tex = ((IntEntry) readField("h_tex", new IntEntry(5))).getInt();
		tex_grid = ((IntArrayEntry) readField("tex_grid", new IntArrayEntry(
				new int[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
						1, 1, 1, 1, 1, 1, 1, 1 }))).getIntArray();
		animate = ((StringEntry) readField("animate", new StringEntry("null")))
				.getString();
		customDeserializeanimate_I();
		animfps = (float) ((DoubleEntry) readField("animfps",
				new DoubleEntry(5))).getDouble();
		visited = ((IntEntry) readField("visited", new IntEntry())).getInt() == 1;
		customDeserializeoom_exception();
		states_time = ((FloatArrayEntry) readField("states_time",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		customDeserializestates_valence();
		states_indexCt = ((IntArrayEntry) readField("states_indexCt",
				new IntArrayEntry(new int[] {}))).getIntArray();
		states_last_x = ((FloatArrayEntry) readField("states_last_x",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_last_y = ((FloatArrayEntry) readField("states_last_y",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_last_integrations = ((IntArrayEntry) readField(
				"states_last_integrations", new IntArrayEntry(new int[] {})))
				.getIntArray();
		states_last_theta = ((FloatArrayEntry) readField("states_last_theta",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_last_v = ((FloatArrayEntry) readField("states_last_v",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_fired = ((FloatArrayEntry) readField("states_fired",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_measure = ((IntArrayEntry) readField("states_measure",
				new IntArrayEntry(new int[] {}))).getIntArray();
		states_delta_pos = ((IntArrayEntry) readField("states_delta_pos",
				new IntArrayEntry(new int[] {}))).getIntArray();
		states_delta_net = ((IntArrayEntry) readField("states_delta_net",
				new IntArrayEntry(new int[] {}))).getIntArray();
		states_delta_x = ((FloatArrayEntry) readField("states_delta_x",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_delta_y = ((FloatArrayEntry) readField("states_delta_y",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_delta_widths = ((FloatArrayEntry) readField(
				"states_delta_widths", new FloatArrayEntry(new float[] {})))
				.getFloatArray();
		states_delta_heights = ((FloatArrayEntry) readField(
				"states_delta_heights", new FloatArrayEntry(new float[] {})))
				.getFloatArray();
		states_delta_rotations = ((FloatArrayEntry) readField(
				"states_delta_rotations", new FloatArrayEntry(new float[] {})))
				.getFloatArray();
		states_delta_tintr = ((FloatArrayEntry) readField("states_delta_tintr",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_delta_tintg = ((FloatArrayEntry) readField("states_delta_tintg",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_delta_tintb = ((FloatArrayEntry) readField("states_delta_tintb",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_delta_tinta = ((FloatArrayEntry) readField("states_delta_tinta",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_random0 = ((FloatArrayEntry) readField("states_random0",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		customDeserializestates_neighbors();
		states_stillAlive = ((IntEntry) readField("states_stillAlive",
				new IntEntry(0))).getInt();
		edgew = ((StringEntry) readField("edgew", new StringEntry("0")))
				.getString();
		customDeserializestates_neighbor_EW();
		store = ((StringEntry) readField("store", new StringEntry("store")))
				.getString();
		states_store = ((FloatArrayEntry) readField("states_store",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_neighbor_updates = ((IntEntry) readField(
				"states_neighbor_updates", new IntEntry())).getInt();
		states_coord_system_theta = ((FloatArrayEntry) readField(
				"states_coord_system_theta",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_coord_system_offx = ((FloatArrayEntry) readField(
				"states_coord_system_offx", new FloatArrayEntry(new float[] {})))
				.getFloatArray();
		states_coord_system_offy = ((FloatArrayEntry) readField(
				"states_coord_system_offy", new FloatArrayEntry(new float[] {})))
				.getFloatArray();
		states_constants = ((FloatArrayEntry) readField("states_constants",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		states_last_integration_time = ((FloatArrayEntry) readField(
				"states_last_integration_time", new FloatArrayEntry(
						new float[] {}))).getFloatArray();
		states_angleToGirl0 = ((FloatArrayEntry) readField(
				"states_angleToGirl0", new FloatArrayEntry(new float[] {})))
				.getFloatArray();
		health = ((IntEntry) readField("health", new IntEntry(-1))).getInt();
		states_healths = ((IntArrayEntry) readField("states_healths",
				new IntArrayEntry(new int[] {}))).getIntArray();
		entire_start_time = (float) ((DoubleEntry) readField(
				"entire_start_time", new DoubleEntry())).getDouble();
		states_start_times = ((FloatArrayEntry) readField("states_start_times",
				new FloatArrayEntry(new float[] {}))).getFloatArray();
		state_index_lastFiring = ((IntEntry) readField(
				"state_index_lastFiring", new IntEntry())).getInt();
		customDeserializebulletSubFirables();
		customDeserializerootSubFirable();
		state_index_bulletCounter = ((IntEntry) readField(
				"state_index_bulletCounter", new IntEntry(0))).getInt();
		state_coordmode = ((IntEntry) readField("state_coordmode",
				new IntEntry(0))).getInt();
		state_relative = ((IntEntry) readField("state_relative",
				new IntEntry(0))).getInt();
		doBeatPositions = ((IntEntry) readField("doBeatPositions",
				new IntEntry())).getInt() == 1;
		doAngleToGirl = ((IntEntry) readField("doAngleToGirl", new IntEntry()))
				.getInt() == 1;
	}

	public void autoWrittenSerializeCode() {
		writeField("addSubs", new StringEntry(addSubs));
		writeField("fireOn_Int", new StringEntry(fireOn_Int));
		writeField("fireOn", new StringEntry(fireOn));
		writeField("fireOn_F", new FloatArrayEntry(fireOn_F));
		writeField("fireOn_Int_F", new DoubleEntry(fireOn_Int_F));
		writeField("firesOncePerQuery", new IntEntry(firesOncePerQuery ? 1 : 0));
		writeField("fireOn_Int_B", new IntEntry(fireOn_Int_B ? 1 : 0));
		writeField("fireOn_Collision", new IntEntry(fireOn_Collision ? 1 : 0));
		writeField("fireOn_Death", new IntEntry(fireOn_Death ? 1 : 0));
		writeField("fireOn_measure", new DoubleEntry(fireOn_measure));
		writeField("lifetime", new DoubleEntry(lifetime));
		writeField("mode", new StringEntry(mode));
		writeField("x", new StringEntry(x));
		writeField("y", new StringEntry(y));
		writeField("rotate", new StringEntry(rotate));
		writeField("theta", new StringEntry(theta));
		writeField("v", new StringEntry(v));
		writeField("tintR", new StringEntry(tintR));
		writeField("tintG", new StringEntry(tintG));
		writeField("tintB", new StringEntry(tintB));
		writeField("tintA", new StringEntry(tintA));
		writeField("vecUpdLimit", new DoubleEntry(vecUpdLimit));
		writeField("sound", new StringEntry(sound));
		writeField("soundIndex", new IntEntry(soundIndex));
		writeField("neighbors", new StringEntry(neighbors));
		writeField("closest_num", new IntEntry(closest_num));
		writeField("physical", new StringEntry(physical));
		writeField("isPhysical", new IntEntry(isPhysical ? 1 : 0));
		writeField("harmful", new StringEntry(harmful));
		writeField("isHarmful", new IntEntry(isHarmful ? 1 : 0));
		writeField("bomb", new StringEntry(bomb));
		writeField("fireOnDeath", new StringEntry(fireOnDeath));
		writeField("bombCost", new DoubleEntry(bombCost));
		writeField("isBomb", new IntEntry(isBomb ? 1 : 0));
		writeField("bombDuration", new DoubleEntry(bombDuration));
		writeField("relative", new StringEntry(relative));
		writeField("w", new StringEntry(w));
		writeField("h", new StringEntry(h));
		writeField("linktime", new DoubleEntry(linktime));
		writeField("myInstanceName", new StringEntry(myInstanceName));
		writeField("myBulletType", new StringEntry(
				myBulletType != null ? myBulletType.hashToString() : ""));
		writeField("w_tex", new IntEntry(w_tex));
		writeField("h_tex", new IntEntry(h_tex));
		writeField("tex_grid", new IntArrayEntry(tex_grid));
		writeField("animate", new StringEntry(animate));
		customSerializeanimate_I();
		writeField("animfps", new DoubleEntry(animfps));
		writeField("visited", new IntEntry(visited ? 1 : 0));
		customSerializeoom_exception();
		writeField("states_time", new FloatArrayEntry(states_time));
		customSerializestates_valence();
		writeField("states_indexCt", new IntArrayEntry(states_indexCt));
		writeField("states_last_x", new FloatArrayEntry(states_last_x));
		writeField("states_last_y", new FloatArrayEntry(states_last_y));
		writeField("states_last_integrations", new IntArrayEntry(
				states_last_integrations));
		writeField("states_last_theta", new FloatArrayEntry(states_last_theta));
		writeField("states_last_v", new FloatArrayEntry(states_last_v));
		writeField("states_fired", new FloatArrayEntry(states_fired));
		writeField("states_measure", new IntArrayEntry(states_measure));
		writeField("states_delta_pos", new IntArrayEntry(states_delta_pos));
		writeField("states_delta_net", new IntArrayEntry(states_delta_net));
		writeField("states_delta_x", new FloatArrayEntry(states_delta_x));
		writeField("states_delta_y", new FloatArrayEntry(states_delta_y));
		writeField("states_delta_widths", new FloatArrayEntry(
				states_delta_widths));
		writeField("states_delta_heights", new FloatArrayEntry(
				states_delta_heights));
		writeField("states_delta_rotations", new FloatArrayEntry(
				states_delta_rotations));
		writeField("states_delta_tintr",
				new FloatArrayEntry(states_delta_tintr));
		writeField("states_delta_tintg",
				new FloatArrayEntry(states_delta_tintg));
		writeField("states_delta_tintb",
				new FloatArrayEntry(states_delta_tintb));
		writeField("states_delta_tinta",
				new FloatArrayEntry(states_delta_tinta));
		writeField("states_random0", new FloatArrayEntry(states_random0));
		customSerializestates_neighbors();
		writeField("states_stillAlive", new IntEntry(states_stillAlive));
		writeField("edgew", new StringEntry(edgew));
		customSerializestates_neighbor_EW();
		writeField("store", new StringEntry(store));
		writeField("states_store", new FloatArrayEntry(states_store));
		writeField("states_neighbor_updates", new IntEntry(
				states_neighbor_updates));
		writeField("states_coord_system_theta", new FloatArrayEntry(
				states_coord_system_theta));
		writeField("states_coord_system_offx", new FloatArrayEntry(
				states_coord_system_offx));
		writeField("states_coord_system_offy", new FloatArrayEntry(
				states_coord_system_offy));
		writeField("states_constants", new FloatArrayEntry(states_constants));
		writeField("states_last_integration_time", new FloatArrayEntry(
				states_last_integration_time));
		writeField("states_angleToGirl0", new FloatArrayEntry(
				states_angleToGirl0));
		writeField("health", new IntEntry(health));
		writeField("states_healths", new IntArrayEntry(states_healths));
		writeField("entire_start_time", new DoubleEntry(entire_start_time));
		writeField("states_start_times",
				new FloatArrayEntry(states_start_times));
		writeField("state_index_lastFiring", new IntEntry(
				state_index_lastFiring));
		customSerializebulletSubFirables();
		customSerializerootSubFirable();
		writeField("state_index_bulletCounter", new IntEntry(
				state_index_bulletCounter));
		writeField("state_coordmode", new IntEntry(state_coordmode));
		writeField("state_relative", new IntEntry(state_relative));
		writeField("doBeatPositions", new IntEntry(doBeatPositions ? 1 : 0));
		writeField("doAngleToGirl", new IntEntry(doAngleToGirl ? 1 : 0));
	}

}
