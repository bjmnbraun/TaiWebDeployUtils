package BulletGame$1;


import static TaiScript.parsing.TaiScriptEditor.LINE_END_SUBCHAR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JFrame;

import processing.core.PApplet;
import BulletGame$1.BulletGame$1Engine$L3$1$BulletGameEditorScreen.ScriptExtraPropertyHandler;
import BulletGame$1.BulletGame$1Engine$L3$1$BulletGameEditorScreen.ScriptExtraPropertyHandler.ScriptPropertyCustomizable;
import BulletGame$2.BulletAccessory;
import BulletGame$2.BulletBackground;
import BulletGame$2.BulletBoss;
import BulletGame$2.BulletGlobals;
import BulletGame$2.BulletHellEnv;
import BulletGame$2.BulletHellInstance;
import BulletGame$2.BulletLevel;
import BulletGame$2.BulletPattern;
import BulletGame$2.BulletPlayer;
import BulletGame$2.BulletSpellCard;
import BulletGame$2.GraphicsHolderParser;
import BulletGame$2.TopDownSplayTree;
import BulletGame$2.TopDownSplayTree.TreeNode;
import TaiGameCore.MultiExpression;
import TaiGameCore.TaiDAWG;
import TaiGameCore.GameDataBase.StringBase.FieldRequiredException;
import TaiGameCore.GameDataBase.StringBase.ValidationException;
import TaiGameCore.GameDataBase.StringBase.Validator;
import TaiGameCore.GameSprite.GameGraphic;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;
import TaiScript.parsing.TaiScriptEditor;
import TaiScript.parsing.TaiScriptLanguage$Constants;

import com.iabcinc.jmep.Environment;
import com.iabcinc.jmep.XExpression;

public abstract class BulletGame$1Engine$L2$3$BulletGameBHIP extends BulletGame$1Engine$L2$2$BulletGameTaiScriptCompiler{
	public BulletGame$1Engine$L2$3$BulletGameBHIP(JFrame holder, PApplet hold) {
		super(holder, hold);
	}
	/**
	 * The product of this class; a BHIP.
	 */
	public BulletHellInstancePlayer bhip;
	//Passed from Editor  - >  Simulation screen. Where to start simulation.
	public String simulationStartRequest;
	
	public class ParseCrash extends Exception{
		public ParseCrash(String message, int lineNum, Throwable source){
			super("Error on line : "+lineNum+", "+message,source);
			this.lineNum = lineNum;
			msg = message;
			this.e = source;
		}
		public Throwable e;
		public String msg;
		public int lineNum;
	}
	public interface readPropertyFieldCB {
		public void reportField(String name, int line);
	}
	public class Comment {
		public Comment(String string, int line) {
			commentText = string;
			lineNum = line;
		}
		public int lineNum;
		public String commentText;
	}
	public static class StageEndException extends Exception{

	}
	public class ParseResult {
		public ArrayList<ParseCrash> toRetCrashes = new ArrayList<ParseCrash>();
		public ArrayList<Comment> comments = new ArrayList<Comment>();
		public ArrayList<String> hints = new ArrayList();
	}
	public class BulletHellInstancePlayer {
		//Load the bullet class objects here:
		public class BulletStagePart {
			public ArrayList<Integer> firable = new ArrayList();
			public ArrayList<Integer> accessories = new ArrayList();
			public boolean isFirstStage = false;
			/**
			 * -1 means open stage, or keep boss sames
			 * any other negative number means remove boss.
			 */
			public int myBoss = -1;
			/**
			 * should not be negative one ever.
			 */
			public int mySpellCard = -1;
			/**
			 * Leave this at negative one unless you want the level info to be displayed
			 */
			public int myLevel = -1;
			public BulletStagePartTransition toNext;
		}
		/**
		 * Is an IMMEDIATE member of a class.
		 */
		public boolean isMemberOf(String test, String possibleParent){
			return test.startsWith(possibleParent+".") && !test.substring(possibleParent.length()+1).contains(".");
		}
		/**
		 * Pass in the current time.
		 * @throws StageEndException 
		 */
		public void loadNext(float time) throws StageEndException{
			if (currentPart==null){
				//Parse exception already.
				throw new StageEndException();
			}
			BulletStagePartTransition nex = currentPart.toNext;
			if (nex==null){
				currentPart = null;
				throw new StageEndException();
			}
			BulletStagePart cp = currentPart; //LAST
			currentPart = nex.next; //CURRENT
			
			if (cp.isFirstStage){
				//New game! give the player his lives and bombs
				BulletPlayer player = bhip.bplys[bhip.whichPlayer];
				player.lives = 3;
				player.bombs = 3;
			}
			
			//ok, populate the new part, and run any transitions
			if (currentPart.myLevel==-1){
				//take level from last
				currentPart.myLevel = cp.myLevel;
				blevels[currentPart.myLevel].current_event++;
			} else {
				//new myLevel! load him in.
				BulletLevel bl = blevels[currentPart.myLevel];
				bl.current_event = 0;
				useBpms(bl);
				//Loading level: place the player.
				BulletPlayer player = bhip.bplys[bhip.whichPlayer];
				player.timeProtect = -Float.MAX_VALUE/2;
				newPlayerLife(false);
			}
			BulletSpellCard sc = bscs[currentPart.mySpellCard];
			if (currentPart.myBoss==-1){
				//take boss from last
				currentPart.myBoss = cp.myBoss;
			} else if (currentPart.myBoss < -1){
				//Remove boss from field
				currentPart.myBoss = -1; // no boss
			} else {
				//new boss! load him in.
				//Ok, we're going to use the "predicted" values for real here.
				BulletBoss vb = bbosses[currentPart.myBoss];
				if (sc.predicted_start_time > 0 && vb.predicted_start_time > 0){
					//time - sc.predicted_start_time + 
					vb.start_time = vb.predicted_start_time;
				} else {
					vb.start_time = time;
				}
			}
			sc.start_time = sc.predicted_start_time;
			if (currentPart.myBoss>=0){
				BulletBoss bb = bbosses[currentPart.myBoss];
				//redo boss:
				for(int k = 0; k < baccess.length; k++){
					BulletAccessory ba = baccess[k];
					if(isMemberOf(ba.myInstanceName,bb.myInstanceName)){
						currentPart.accessories.add(k);
					}
				}
			}
			//new firables:
			for(int k = 0; k < firables.length; k++){
				BulletPattern bp = firables[k];
				if(isMemberOf(bp.myInstanceName,sc.myInstanceName)){
					bp.entire_start_time = sc.start_time;
					currentPart.firable.add(k);
					if (subfirables[k]!=null){
						for(int ie : subfirables[k]){
							firables[ie].entire_start_time = sc.start_time;
						}
					}
				}
			}
		}
		/**
		 * Specifies which bulletLevel is "current", so that beat queries
		 * can use it's bpmset.
		 */
		public void useBpms(BulletLevel bl) {
			//New music?
			if (bl.music.equals(BulletLevel.NO_AUDIO)){
				bpmMap = null;
				invBpmMap = null;
			} else {
				TopDownSplayTree<float[]>[] parseBPMS = BulletLevel.parseBPMS(bl.gap, bl.bpms);
				bpmMap = parseBPMS[0];
				invBpmMap = parseBPMS[1];
			}
		}
		public void newPlayerLife(boolean replenishBombs){
			BulletLevel bl = blevels[currentPart.myLevel];
			BulletPlayer player = bhip.bplys[bhip.whichPlayer];
			player.position[0] = bl.playerInit[0];
			player.position[1] = bl.playerInit[1];
			player.bombTime = -Float.MAX_VALUE/2;
			player.killTime = Float.MAX_VALUE/2;
			if (replenishBombs){
				player.bombs = 3;
			}
		}
		public class BulletStagePartTransition {
			public BulletStagePart next;
			//decide to take down level by comparing if the level changes
			//todo: more booleans for other things.
		}
		public TopDownSplayTree<float[]> bpmMap,invBpmMap;
		public BulletStagePart currentPart;
		public int whichPlayer = 0;
		public ArrayList<Integer> bpsForPlayer;
		public ArrayList<Integer> accessoriesForPlayer;
		public BulletPattern[] firables;
		public int deadBulletFirable;
		public int[][] subfirables; //Children of firables
		public int[][] subBackgrounds;
		public float simulation_offset;
		public BulletBoss[] bbosses;
		public BulletLevel[] blevels;
		public BulletAccessory[] baccess;
		public BulletSpellCard[] bscs;
		public BulletPlayer[] bplys;
		public BulletBackground[] bgs;
		public BulletGlobals bGlob;
		/** We actually cache all of these when the game begins. I like that alot **/
		public MultiExpression[][] bulletExpressionCache;
		public MultiExpression[][] bulletGfxExpressionCache;
		public MultiExpression[][] bulletColorExpressionCache;
		public MultiExpression[][] bulletEdgeExpressionCache;
		public MultiExpression[][] bossExpressionCache;
		public MultiExpression[][] accessoryExpressionCache;
		public MultiExpression[][] bossSpellcardsExpressionCache;
		public MultiExpression[][] backgroundExpressionCache;

		public void calculateXPosition(float[] pos, MultiExpression[] whichOne) throws XExpression {
			for(int p = 0; p < pos.length && p < whichOne.length; p++){
				pos[p] = whichOne[p].evaluatef();
			}
		}


		public final float getTimeFromBeat(float beat) {
			if (bpmMap==null){
				return beat;
			}
			if (beat<=0){
				beat = 0;
			}
			TreeNode<float[]> lower = invBpmMap.getLower(beat);
			float interpBeat = lower.getKey();
			float[] interpPoint = lower.getValue();
			interpBeat = beat-interpBeat;
			float timeOff = interpBeat / interpPoint[0] * 60;
			return interpPoint[1] + timeOff;
		}

		public final float getBeatFromSongTime(float t){
			if (bpmMap==null){
				return t;
			}
			float gap = getTimeFromBeat(0);
			if (t <= gap){
				return -1;
			}
			//Alright, so now interpolate using the tree:
			TreeNode<float[]> lower = bpmMap.getLower(t);
			float interpTime = lower.getKey();
			float[] interpPoint = lower.getValue();
			interpTime = t - interpTime;
			float beatOff = interpTime * interpPoint[0] / 60;
			return interpPoint[1] + beatOff;
		}

		/**
		 * analyzes the stage.
		 */
		public String createStageGraph(String eventStart) {
			boolean isSkippingEvents = eventStart!=null && eventStart.trim().length()>0; //Skip until we reach "eventStart"
			if (blevels==null || blevels.length==0){
				return "Create a level";
			}
			/* Bosses are NOT essential!
			if ( bbosses==null || bbosses.length==0){
				return "Create a boss";
			}
			*/
			if (bplys==null || bplys.length==0){
				return "Create a player";
			}
			currentPart = new BulletStagePart(); //default constructor = empty stage
			currentPart.isFirstStage = true; //Flag, means load up player with lives
			BulletStagePart lbsp = currentPart;
			float predictTime = 0;
			int lastLevel = -1, lastBoss = -1;
			for(int blIndex = 0; blIndex < blevels.length; blIndex++){
				BulletLevel bl = blevels[blIndex];
				if (bl.myInstanceName.equals(eventStart)){
					isSkippingEvents = false;
				}
				useBpms(bl);
				predictTime = 0;
				String[] events = bl.events.split(",");
				for(String event : events){
					event = event.trim();
					//Boss events?
					for(int bbIndex = 0; bbIndex < bbosses.length; bbIndex++){
						//Add a boss entrance, and link all his spellcards.
						BulletBoss bb = bbosses[bbIndex];
						if (!isMemberOf(bb.myInstanceName,bl.myInstanceName) || !bb.myInstanceName.endsWith("."+event)){
							continue;
						}
						//Ok! time the boss.
						bb.predicted_start_time = predictTime;
						for(int bsIndex = 0; bsIndex < bscs.length; bsIndex++){
							BulletSpellCard sc = bscs[bsIndex];
							if (!isMemberOf(sc.myInstanceName, bb.myInstanceName)){
								continue;
							}
							//Predict timing
							sc.predicted_start_time = predictTime;
							float predictDuration = sc.predictDuration(predictTime, this);
							if (predictDuration < 0){
								predictTime = -1; //Break.
							}
							if (predictTime >= 0){
								predictTime += predictDuration;
							}
							//See if we're at the startevent
							if (sc.myInstanceName.equals(eventStart)){
								isSkippingEvents = false;
							}
							//add these.
							if (!isSkippingEvents){
								BulletStagePart bsp = new BulletStagePart();

								if (lastLevel!= blIndex){
									lastLevel = bsp.myLevel = blIndex;
								}
								if (lastBoss != bbIndex){
									lastBoss = bsp.myBoss = bbIndex;
								}

								bsp.mySpellCard = bsIndex;

								//Stack operation.
								lbsp.toNext = new BulletStagePartTransition();
								lbsp.toNext.next = bsp;
								lbsp = bsp;
							}
						}
					}
					//Nonboss spellcards.
					for(int bscindex = 0; bscindex < bscs.length; bscindex++){
						//Add a stage spellcard (monsters and such)
						BulletSpellCard bb = bscs[bscindex];
						if (!isMemberOf(bb.myInstanceName,bl.myInstanceName) || !bb.myInstanceName.endsWith("."+event)){
							continue;
						}
						//Predict timing
						bb.predicted_start_time = predictTime;
						float predictDuration = bb.predictDuration(predictTime, this);
						if (predictDuration < 0){
							predictTime = -1; //Break.
						}
						if (predictTime >= 0){
							predictTime += predictDuration;
						}
						//See if we're at the startevent
						if (bb.myInstanceName.equals(eventStart)){
							isSkippingEvents = false;
						}
						if (!isSkippingEvents){
							//add these.
							BulletStagePart bsp = new BulletStagePart();

							if (lastLevel!= blIndex){
								lastLevel = bsp.myLevel = blIndex;
							}
							bsp.myBoss = -2;

							bsp.mySpellCard = bscindex;

							//Stack operation.
							lbsp.toNext = new BulletStagePartTransition();
							lbsp.toNext.next = bsp;
							lbsp = bsp;
						}
					}
				}
			}
			//Player stuff:
			BulletPlayer bulletPlayer = bplys[whichPlayer];
			bpsForPlayer = new ArrayList();
			for(int i = 0; i < firables.length; i++){
				if (isMemberOf(firables[i].myInstanceName,bulletPlayer.myInstanceName)){
					bpsForPlayer.add(i);
				}
			}
			accessoriesForPlayer = new ArrayList();
			for(int k = 0; k < baccess.length; k++){
				BulletAccessory ba = baccess[k];
				if(isMemberOf(ba.myInstanceName,bulletPlayer.myInstanceName)){
					accessoriesForPlayer.add(k);
				}
			}
			
			//Firable linkages: (non symmetric)
			subfirables = new int[firables.length][];
			for(int i = 0; i < firables.length; i++){
				ArrayList<Integer> SubFirableTemp = new ArrayList<Integer>();
				ArrayList<String> additionalFirables = new ArrayList<String>();
				String[] addtio = firables[i].addSubs.split(",");
				for(String q : addtio){
					if (q.equalsIgnoreCase("this")){
						q = firables[i].myInstanceName;
					}
					additionalFirables.add(q.toLowerCase());
				}
				for(int n = 0; n < firables.length; n++){
					if (isMemberOf(firables[n].myInstanceName,firables[i].myInstanceName)){
						SubFirableTemp.add(n);
					} else
					if (additionalFirables.contains(firables[n].myInstanceName.toLowerCase())){
						SubFirableTemp.add(n);
					}
				}
				if (SubFirableTemp.size()>0){
					subfirables[i] = new int[SubFirableTemp.size()];
					int count = 0;
					for(Integer q : SubFirableTemp){
						subfirables[i][count++]= q;
					}
				}
			}

			subBackgrounds = new int[bscs.length][];
			for(int bsc = 0; bsc < bscs.length; bsc++){
				ArrayList<Integer> SubBackgrounds = new ArrayList();
				ArrayList<String> additionalBgs = new ArrayList<String>();
				String[] addtio = bscs[bsc].addBgs.split(",");
				for(String q : addtio){
					additionalBgs.add(q.toLowerCase());
				}
				for(int bg = 0; bg < bgs.length; bg++){
					if (isMemberOf(bscs[bsc].myInstanceName,bgs[bg].myInstanceName)){
						SubBackgrounds.add(bg);
					} else
					if (additionalBgs.contains(bgs[bg].myInstanceName.toLowerCase())){
						SubBackgrounds.add(bg);
					}
				}				
				if (SubBackgrounds.size()>0){
					subBackgrounds[bsc] = new int[SubBackgrounds.size()];
					int count = 0;
					for(Integer q : SubBackgrounds){
						subBackgrounds[bsc][count++]= q;
					}
				}
			}
			
			
			//Return if we could pick the first:
			if (currentPart.toNext!=null){
				BulletSpellCard offset = bscs[currentPart.toNext.next.mySpellCard];
				simulation_offset = offset.predicted_start_time;
				return null; //Flawless victory.
			} else {
				return "Empty stage (?)";
			}
		}
		private final String[] defaultAboves = new String[]{
				"bullet deadBullet {"+LINE_END_SUBCHAR,
				"fireOn_Int = 0"+LINE_END_SUBCHAR,
				"fireOn = 0"+LINE_END_SUBCHAR,
				"fireOn_measure = 1"+LINE_END_SUBCHAR,
				"lifetime = 3"+LINE_END_SUBCHAR,
				"mode = polar"+LINE_END_SUBCHAR,
				"v = 1+t"+LINE_END_SUBCHAR,
				"w = .03"+LINE_END_SUBCHAR,
				"h = .03"+LINE_END_SUBCHAR,
				"theta = angle2P"+LINE_END_SUBCHAR,
				"harmful = false"+LINE_END_SUBCHAR,
				"}"+LINE_END_SUBCHAR,
		};
		private void handleAboves(){
			deadBulletFirable = 0;
			firables[deadBulletFirable].myBulletType.frame = new GameGraphic[]{
					new GameGraphic("")
			};
			firables[deadBulletFirable].myBulletType.frame[0].filename = "bulletDead.png";
		}
		public ParseResult createFromBHI(BulletHellInstance bhi, BulletHellEnv env2) {
			return createFromBHI(bhi, env2, true);
		}
		public ParseResult createFromBHI(BulletHellInstance bhi, BulletHellEnv env2, boolean entireLevel) {
			ParseResult pr = new ParseResult();
			if (bhi==null){
				pr.toRetCrashes.add(new ParseCrash("Null BHI",0,new NullPointerException()));
				return pr;
			}
			
			String eventStart = "";
			if (!entireLevel && simulationStartRequest!=null){
				eventStart = simulationStartRequest;
				simulationStartRequest = null; //Clear request once done.
			}
				
			ArrayList<String> EditingStrings = new ArrayList();
			int errorShiftLines = -defaultAboves.length;
			for(String q : defaultAboves){
				EditingStrings.add(q);
			}
			EditingStrings.addAll(bhi.script.Editing);

			//Defaults:
			bGlob = new BulletGlobals("");
			//Gogo!
			Environment env = env2.env;
			ScriptExtraPropertyHandler parser = new ScriptExtraPropertyHandler();
			LinkedHashMap<String, ArrayList<ScriptPropertyCustomizable>> allProps = new LinkedHashMap();
			int[] lineOnParsing = new int[]{0};
			try {
				parser.setOn(null);
				lineOnParsing[0] = 0;
				for(String line : EditingStrings){
					parser.setOn(line);
					lineOnParsing[0]++;
					while(true){
						ScriptPropertyCustomizable next = parser.nextCustomizableWord();
						if (next==null){
							break;
						}
						ArrayList<ScriptPropertyCustomizable> items = allProps.get(next.getInstanceName());
						if (items==null){
							items = new ArrayList();
							allProps.put(next.getInstanceName(),items);
						}
						items.add(next);
					}
				}
				//Alright, we've read them in. Parse!
				ArrayList<BulletPattern> parseFirables = new ArrayList<BulletPattern>();
				ArrayList<BulletBoss> bosses = new ArrayList();
				ArrayList<BulletLevel> levels = new ArrayList();
				ArrayList<BulletAccessory> bossAccessories = new ArrayList();
				ArrayList<BulletSpellCard> spellcards = new ArrayList();
				ArrayList<BulletPlayer> players = new ArrayList();
				ArrayList<BulletBackground> backgrounds = new ArrayList();

				for(ArrayList<ScriptPropertyCustomizable> qList : allProps.values()){
					for(ScriptPropertyCustomizable q : qList){
						ArrayList<String> instanceNameStack = new ArrayList<String>();
						final TaiDAWG<Integer> linesOfFields = new TaiDAWG<Integer>();
						readPropertyFieldCB cb = new readPropertyFieldCB(){
							public void reportField(String name, int line) {
								linesOfFields.insert(name,line);
							}
						};
						TaiDAWG<String> data2 = readProperties(q,lineOnParsing,allProps,pr,instanceNameStack,cb);
						/* debug
					StringTreeIterator<WordByRef<String>> iterator = data2.iterator();
					while(iterator.hasNext()){
						System.out.println(iterator.next()+" = "+((WordByRef<String>)iterator.getCurrentNode()).getContentData());
						iterator.tryNext();
					}
						 */
						Validator[] bulletValidators = new Validator[]{
								new BulletPattern.ExpressionEvaluator(env,bGlob),
								new BulletPattern.ModeValidator(),
								new BulletPattern.RelativeCoordsValidator(),
								new BulletPattern.FireOnValidator(),
								new BulletLevel.BPMValidator(),
								new BulletPattern.PositiveValidator(),
								new BulletPlayer.YesNoValidator(),
								new BulletGlobals.AspectRatioValidator(),
								new GraphicsHolderParser.AnimationValidator(),
								new BulletPattern.NeighborAlgorithmValidator(),
								new BulletGlobals.SoundValidator(),
						};
						if (q.getType().equals(TaiScriptLanguage$Constants.BULLET)){
							BulletPattern made = new BulletPattern("");
							ArrayList<Exception> got = made.parseFromStrings(data2, bulletValidators);
							handleParseExceptions(got,pr,linesOfFields,q);
							if (got.isEmpty()){
								made.myBulletType = bhi.getGameGraphic(q.getInstanceName(),q.getType());
								made.myInstanceName = q.getInstanceName();
								made.postScriptRead();
								parseFirables.add(made);
							}
						}
						if (q.getType().equals(TaiScriptLanguage$Constants.BOSS)){
							BulletBoss made = new BulletBoss("");
							ArrayList<Exception> got = made.parseFromStrings(data2, bulletValidators);
							handleParseExceptions(got,pr,linesOfFields,q);
							if (got.isEmpty()){
								made.myGraphic = bhi.getGameGraphic(q.getInstanceName(),q.getType());
								made.myInstanceName = q.getInstanceName();
								bosses.add(made);
							}
						}
						if (q.getType().equals(TaiScriptLanguage$Constants.PLAYER)){
							BulletPlayer made = new BulletPlayer("");
							ArrayList<Exception> got = made.parseFromStrings(data2, bulletValidators);
							handleParseExceptions(got,pr,linesOfFields,q);
							if (got.isEmpty()){
								made.myGraphic = bhi.getGameGraphic(q.getInstanceName(),q.getType());
								made.myInstanceName = q.getInstanceName();
								made.setupPlayer();
								players.add(made);
							}
						}
						if (q.getType().equals(TaiScriptLanguage$Constants.ACCESSORY)){
							BulletAccessory made = new BulletAccessory("");
							ArrayList<Exception> got = made.parseFromStrings(data2, bulletValidators);
							handleParseExceptions(got,pr,linesOfFields,q);
							if (got.isEmpty()){
								made.myGraphic = bhi.getGameGraphic(q.getInstanceName(),q.getType());
								made.myInstanceName = q.getInstanceName();
								bossAccessories.add(made);
							}
						}
						if (q.getType().equals(TaiScriptLanguage$Constants.LEVEL)){
							BulletLevel made = new BulletLevel("");
							ArrayList<Exception> got = made.parseFromStrings(data2, bulletValidators);
							handleParseExceptions(got,pr,linesOfFields,q);
							if (got.isEmpty()){
								levels.add(made);
								made.myInstanceName = q.getInstanceName();
							}
						}
						if (q.getType().equals(TaiScriptLanguage$Constants.GLOBAL)){
							BulletGlobals made = bGlob;
							ArrayList<Exception> got = made.parseFromStrings(data2, bulletValidators);
							handleParseExceptions(got,pr,linesOfFields,q);
						}

						if (q.getType().equals(TaiScriptLanguage$Constants.SPELLCARD)){
							BulletSpellCard made = new BulletSpellCard("");
							ArrayList<Exception> got = made.parseFromStrings(data2, bulletValidators);
							handleParseExceptions(got,pr,linesOfFields,q);
							if (got.isEmpty()){
								spellcards.add(made);
								made.myInstanceName = q.getInstanceName();
							}
						}
						
						if (q.getType().equals(TaiScriptLanguage$Constants.BACKGROUND)){
							BulletBackground made = new BulletBackground("");
							ArrayList<Exception> got = made.parseFromStrings(data2, bulletValidators);
							handleParseExceptions(got,pr,linesOfFields,q);
							if (got.isEmpty()){
								backgrounds.add(made);
								made.myInstanceName = q.getInstanceName();
							}
						}
					} 
				} 
				firables = new BulletPattern[parseFirables.size()];
				parseFirables.toArray(firables);

				bbosses = new BulletBoss[bosses.size()];
				bosses.toArray(bbosses);

				blevels = new BulletLevel[levels.size()];
				levels.toArray(blevels);

				baccess = new BulletAccessory[bossAccessories.size()];
				bossAccessories.toArray(baccess);

				bscs = new BulletSpellCard[spellcards.size()];
				spellcards.toArray(bscs);

				bplys = new BulletPlayer[players.size()];
				players.toArray(bplys);
				
				bgs = new BulletBackground[backgrounds.size()];
				backgrounds.toArray(bgs);

				bulletExpressionCache = new MultiExpression[firables.length][];
				bulletGfxExpressionCache = new MultiExpression[firables.length][];
				bulletColorExpressionCache = new MultiExpression[firables.length][];
				bulletEdgeExpressionCache = new MultiExpression[firables.length][];
				bossExpressionCache = new MultiExpression[bbosses.length][];
				accessoryExpressionCache = new MultiExpression[baccess.length][];
				bossSpellcardsExpressionCache = new MultiExpression[bscs.length][];
				backgroundExpressionCache = new MultiExpression[bgs.length][];
				for(int k = 0; k < firables.length; k++){
					BulletPattern bp = firables[k];
					bulletExpressionCache[k] = bp.createExpression(env2,bGlob);
					bulletGfxExpressionCache[k] = bp.createGfxExpression(env2,bGlob);
					bulletColorExpressionCache[k] = bp.createColorExpression(env2, bGlob);
					bulletEdgeExpressionCache[k] = bp.createEdgeExpression(env2,bGlob);
				}
				for(int k = 0; k < bbosses.length; k++){
					bossExpressionCache[k] = bbosses[k].createExpression(env,bGlob);
				}
				for(int k = 0; k < baccess.length; k++){
					accessoryExpressionCache[k] = baccess[k].createExpression(env,bGlob);
				}
				for(int k = 0; k < bscs.length; k++){
					bossSpellcardsExpressionCache[k] = bscs[k].createExpression(env,bGlob);
				}
				for(int k = 0; k < bgs.length; k++){
					backgroundExpressionCache[k] = bgs[k].createExpression(env,bGlob);
				}
				//make the stage graph
				String invalidStageHint = createStageGraph(eventStart);
				if (invalidStageHint!=null){
					pr.hints.add(invalidStageHint);
				}		
				handleAboves();	
			} catch (Throwable e){
				e.printStackTrace();
				pr.toRetCrashes.add(new ParseCrash(e.getMessage(),lineOnParsing[0],e));	
			}
			for(ParseCrash k : pr.toRetCrashes){
				k.lineNum += errorShiftLines;
			}
			return pr;
		}
		private void handleParseExceptions(ArrayList<Exception> got, ParseResult pr, TaiDAWG<Integer> linesOfFields, ScriptPropertyCustomizable q) {
			for(Exception p : got){
				System.out.println(p.getCause()+" "+p.getStackTrace()[0].getClassName()+" "+p.getStackTrace()[0].getLineNumber());
				if (p instanceof FieldRequiredException){
					FieldRequiredException e = (FieldRequiredException)p;
					pr.toRetCrashes.add(new ParseCrash(e.getMessage(),q.startingLine+1,e));;
				} else if (p instanceof ValidationException){
					ValidationException e = (ValidationException)p;
					String fieldname = e.fieldname;
					int lineNum = linesOfFields.get(fieldname).getContentData();
					pr.toRetCrashes.add(new ParseCrash(e.getMessage(),lineNum,e));
				} else {
					pr.toRetCrashes.add(new ParseCrash(p.getMessage(),q.startingLine+1,p));;
				}
			}
		}
		public TaiDAWG<String> readProperties(ScriptPropertyCustomizable q, int[] lineOnParsing, HashMap<String, ArrayList<ScriptPropertyCustomizable>> allProps, ParseResult pr, ArrayList<String> instanceNameStack, readPropertyFieldCB cb){
			if (instanceNameStack.contains(q.getInstanceName())){
				throw new RuntimeException("Circular Template Dependency");
			}
			if (q.getInstanceName().equals("this")){
				throw new RuntimeException("Unavailable name: \"this\"");
			}
			TaiDAWG<String> data2 = new TaiDAWG<String>();
			String[] data = q.getTextBody().split(""+TaiScriptEditor.LINE_END_SUBCHAR);	
			String lastLineWasField = null;
			lineloop: for(int lineNum = 0; lineNum < data.length; lineNum++){
				int cLine = lineNum + q.startingLine;
				lineOnParsing[0] = cLine;
				String p = data[lineNum];
				String[] line = modSplit((" "+p),"=",1); //put in a whitespace to make things nice, but be careful.
				if (line.length==1){
					String rawTemplate = line[0].trim();
					if (rawTemplate.startsWith("~")){
						//Deref
						String instanceName = rawTemplate.substring(1);
						//Templates are unique.
						ScriptPropertyCustomizable got = null;
						ArrayList<ScriptPropertyCustomizable> cGot = allProps.get(instanceName);
						if (cGot!=null){
							got = cGot.get(0);
						}
						if (got==null){
							pr.toRetCrashes.add(new ParseCrash("No such template",cLine,null));
						} else if (!got.getType().equals(TaiScriptLanguage$Constants.TEMPLATE)){
							pr.toRetCrashes.add(new ParseCrash(instanceName + " is not a template.",cLine,null));
						} else {
							//Copy all fields:
							instanceNameStack.add(q.getInstanceName());
							TaiDAWG<String> data3 = readProperties(got,lineOnParsing,allProps,pr,instanceNameStack,cb);
							instanceNameStack.remove(instanceNameStack.size()-1);
							StringTreeIterator<WordByRef<String>> iterator = data3.iterator();
							while(iterator.hasNext()){
								String key = iterator.next();
								String val = ((WordByRef<String>)iterator.getCurrentNode()).getContentData();
								data2.insert(key,val);
								iterator.tryNext();
							}
						}
					}
					//It may be a comment, or it may continue our last evaluation.
					if (!rawTemplate.startsWith("~")){
						if (rawTemplate.startsWith("\\") && lastLineWasField != null){
							rawTemplate = rawTemplate.substring(1);
							data2.insert(lastLineWasField, data2.get(lastLineWasField).getContentData()+rawTemplate);
						} else {
							//Ok. it's a comment
							pr.comments.add(new Comment(line[0].substring(1),cLine));
							lastLineWasField = null;
						}
					} else {
						//Anything else
						lastLineWasField = null;
					}
				} else {
					//So, we can split it.
					String ket = line[0].trim();
					if (ket.length()==0){
						pr.toRetCrashes.add(new ParseCrash("Unnamed variable",cLine,null));
					}
					String value = line[1].trim();
					if (data2.get(ket)!=null){
						pr.toRetCrashes.add(new ParseCrash("Duplicate variable",cLine,null));
					}
					data2.insert(ket,value);
					lastLineWasField = ket;
					cb.reportField(ket, cLine);
				}
			}
			return data2;
		}
	}
}
