package BulletGame$1;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.media.opengl.GL;
import javax.swing.JFrame;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import BulletGame$1.BulletGame$1Engine$L3$1$BulletGameEditorScreen.ScriptExtraPropertyHandler.ScriptPropertyCustomizable;
import BulletGame$2.BulletBoss;
import BulletGame$2.BulletHellEnv;
import BulletGame$2.BulletLevel;
import BulletGame$2.BulletPattern;
import BulletGame$2.BulletSpellCard;
import BulletGame$2.GraphicsHolder;
import Deployments.BulletHell;
import TaiGameCore.GameSprite;
import TaiGameCore.PressActionThreshold;
import TaiGameCore.ProceGLHybrid;
import TaiGameCore.ScreenSelector;
import TaiGameCore.TaiBenchmark;
import TaiGameCore.GameSprite.GameGraphic;
import TaiGameCore.ScreenSelector.TMenuItem;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;
import TaiScript.parsing.TaiScriptEditor;
import TaiScript.parsing.TaiScriptLanguage$Constants;
import TaiScript.parsing.TaiScriptLanguage$Rules;


public abstract class BulletGame$1Engine$L3$1$BulletGameEditorScreen extends BulletGame$1Engine$L2$3$BulletGameBHIP {
	
	public class EditorScreen extends BulletGameScreen {

		Rectangle2D.Float textR = new Rectangle2D.Float(3 / 640f, 64 / 480f,
				.5f - 6 / 640f, 1 - 64 / 480f * 2 - 4 / 480f);
		Rectangle2D.Float parsedR = new Rectangle2D.Float(.5f + 9 / 640f,
				64 / 480f, .5f - 6 / 640f, 1 - 64 / 480f * 2 - 4 / 480f);
		Rectangle2D.Float hintsR = new Rectangle2D.Float(3 / 640f,
				1f-64/480f, 1f-6/640f, 64/480f-4/480f);
		MouseChecker parserMym = new MouseChecker(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this);
		MouseChecker etwMym = new MouseChecker(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this);	
		private class EditorScreenTopMenuBarClickItem extends TMenuItem {
			public EditorScreenTopMenuBarClickItem(int ScreenTarget,
					String text, int i, boolean needsSave) {
				target = ScreenTarget;
				IndexInSelection = i;
				this.text = text;
				float eachWidth = .3f;
				myBounds = new Rectangle2D.Float(eachWidth * i, 0f, eachWidth,
						textR.y - .01f);
				this.needsSave = needsSave;
			}

			private Rectangle2D.Float myBounds;
			private int IndexInSelection;
			private String text;
			private int target;
			private boolean needsSave;

			public void act() {
				//What to do in all cases:
				final Runnable runAfter = new Runnable(){
					public void run(){
						SceneChange(Transitioner.FADE_WITH_BLACK, target);
					}
				};

				if (etw.tse.hasUnsavedChanges() && needsSave) {
					/**
					 * Show 3 options: 
					 * 
					 * 1) Save in default folder (/Files/ (text box with name
					 * input) 2) Save to custom location (Opens up file browser)
					 * 3) Don't save, discard changes (OK)
					 */
					ModalDialogCallback mdc = new ModalDialogCallback() {
						boolean shouldSave = true; // initial to true
						{
							ModalDialog md = new ModalDialog(EditorScreen.this,this) {
								private MessageDialogGUI gui;
								{
									gui = new MessageDialogGUI(.5f,.2f,"Save your changes?");
									g.registerDraw(gui);
								}
								public boolean drawDialog() {
									shouldSave=!gui.wasCancelled();
									return gui.isComplete();
								}
								public void cleanup() {
									super.cleanup();
									g.unregisterDraw(gui);
								}
							};
						}

						public void dialogFinished(ModalDialog self) {
							self.cleanup();
							if (shouldSave) {
								File_SaveAction(runAfter); //runAfter is run
							} else {
								// Discard changes!
								runAfter.run(); //runAfter is run
							}
						}
					};
				} else { //end "if we have unsaved changes"
					runAfter.run(); //runAfter is run
				}
			}// end of act(). runAfter must have been run.

			public void draw() {
				g.pushMatrix();
				g.translate(1f / currentViewPortWidth,
						1f / (currentViewPortHeight));
				PImage MenuBarBg = FILE_SYSTEM.getImg("EditorScreen/MenuBarBg.png");
				g.image(MenuBarBg, myBounds.x, myBounds.y, myBounds.width,
						myBounds.height);
				g.popMatrix();
				g.pushMatrix();
				g.translate(myBounds.x, myBounds.y + myBounds.height / 2);
				g.scale(1f / currentViewPortWidth,
						1f / (currentViewPortHeight));
				g.textFont(menuBarFont);
				g.fill(255);
				g.textAlign(g.LEFT, g.CENTER);
				g.text(text, 55, 0);
				g.popMatrix();
			}

			public Rectangle2D getClickBounds() {
				return myBounds;
			}
		}

		private void File_SaveAction(Runnable runDo) {
			new SaveGameDialog(EditorScreen.this,runDo,bhi,"EditorScreen/ScrollArrowRed.png");
		}
		public void File_OpenAction() {
			new LoadBulletDialog(EditorScreen.this, new Runnable(){
				public void run(){
					etw.useTSE(bhi.script);
				}
			});
		}

		private EditorTextSheet makeEditorTextSheet(Rectangle2D.Float textR2, int i, boolean b, boolean c, boolean d) {
			EditorTextSheet thestuf =  new EditorTextSheet(textR2,i,b,c,d,512,16);
			thestuf.scaleText(.8f);
			return thestuf;
		}

		private EditorScreenTopMenuBarClickItem[] myItems = new EditorScreenTopMenuBarClickItem[] {
				new EditorScreenTopMenuBarClickItem(BulletHell.BulletGame$1Engine$GROUND.MAINSCREEN, "return", 0, true),
				new EditorScreenTopMenuBarClickItem(BulletHell.BulletGame$1Engine$GROUND.GAMESCREEN_FROMEDITOR, "Run", 1, false),};

		private abstract class CurvedTopRightOptions extends TMenuItem {
			public CurvedTopRightOptions(String desc, String iconName, int i) {
				IndexInSelection = i;
				this.desc = desc;
				float eachWidth = .3f;
				float rad = ((2 - i) / 10f) * g.TWO_PI + g.PI * 1.03f;
				float ox = g.cos(rad) * .1f;
				float oy = -g.sin(rad) * .1f;
				myBounds = new Rectangle2D.Float(1 - ox - .15f, 0 - oy + .1f,
						.045f, .045f * NSPH);
				this.iconName = iconName;
			}
			private String iconName;

			private Rectangle2D.Float myBounds;
			private int IndexInSelection;
			private String desc;

			public abstract void act();

			public void draw() {
				g.pushMatrix();
				// 1px buffer
				g.translate(1f / currentViewPortWidth,
						1f / (currentViewPortHeight));
				PImage Icon = FILE_SYSTEM.getImg("EditorScreen/" + iconName);
				g.image(Icon, myBounds.x, myBounds.y, myBounds.width,
						myBounds.height);
				g.popMatrix();
				if (isHoverOver(g)) {
					g.pushMatrix();
					g.translate(myBounds.x, myBounds.y);
					// Text the desc
					float w = g.textWidth(desc) / currentViewPortWidth;
					// g.rect(-w,0,w,myBounds.height);
					g.translate(-w + myBounds.width / 2, myBounds.height / 2);
					g.scale(.7f / currentViewPortWidth,
							.7f / (currentViewPortHeight));
					g.textFont(menuBarFont);
					g.textAlign(g.LEFT, g.TOP);
					g.fill(255);
					g.text(desc, 5, 0);
					g.scale(1, .9f);
					g.fill(0, 0, 0, 100);
					g.text(desc, 5, 0);
					g.popMatrix();
				}
			}

			public Rectangle2D getClickBounds() {
				return myBounds;
			}
		}


		private void run(Runnable ... runnables){
			if (runnables!=null){
				for(Runnable k : runnables){
					k.run();
				}
			}
		}


		private CurvedTopRightOptions[] myFileOptions = new CurvedTopRightOptions[] {
				new CurvedTopRightOptions("Open", "editorfileicons/Open.png", 0) {
					public void act() {
						File_OpenAction();
					}
				},
				new CurvedTopRightOptions("Save", "editorfileicons/Save.png", 1) {
					public void act() {
						File_SaveAction(new Runnable(){
							public void run(){
								//Do nothing.
							}
						});
					}
				},
		};

		private PFont menuBarFont;
		private EditorTextSheet etw;
		private EditorTextSheet hints;
		//private TaiScriptFileResources tsfr;
		private EditorRunningParse epw;

		private TimeRenderer timeFrames;
		private ScreenSelector topMenuBar, returnWithEscape, fileButtons;

		private EditorTextSheet inputTargetFocus;

		public EditorScreen() {
			menuBarFont = FILE_SYSTEM.getFont("SquareTypeB-24.vlw");
			g.registerDraw(this);
			etw = makeEditorTextSheet(textR, 14, true, true, true);
			etw.useTSE(bhi.script); //Edit our script
			//Move the window so that the area below the cursor is showing:
			inputTargetFocus = etw;
			//Themed editor:
			etw.setArrowScrollGraphic("EditorScreen/ScrollArrowRed.png");
			//End theming;
			epw = new EditorRunningParse(parsedR);
			//Bottom hints bar
			hints = makeEditorTextSheet(hintsR, 1, true, false, false); 
			hints.setTextRestrictions(1, 0);
			hints.setSelectable(false);
			timeFrames = new TimeRenderer();
			topMenuBar = new ScreenSelector(ScreenSelector.MODE.MOUSE) {
				public TMenuItem[] getItems() {
					return myItems;
				}
			};
			fileButtons = new ScreenSelector(ScreenSelector.MODE.MOUSE) {
				public TMenuItem[] getItems() {
					return myFileOptions;
				}
			};
			returnWithEscape = new ScreenSelector(ScreenSelector.MODE.KEYBOARD) {
				public TMenuItem[] getItems() {
					return null;
				}

				public TMenuItem getBackItem() {
					return myItems[0];
				}
				{
					inPureNavMenu = false; // Disables the 'z' and 'x' ways of
					// navigating
				}
			};
			addSubKeyListener(this);
			g.registerDraw(timeFrames);
		}

		private boolean dbgParseErrors = false;

		public void keyPressed(KeyEvent e) {
			if (!isInputBlocked()) {
				//Ctrl+s saves
				//System.out.println(isCtrlHeld+" "+g.keyCode+" "+g.keyEvent.getKeyCode()+" "+g.keyEvent.getKeyChar());
				if(truth(keyboard.get(KeyEvent.VK_CONTROL)) && g.keyEvent.getKeyCode()==KeyEvent.VK_S){
					File_SaveAction(new Runnable(){
						public void run(){
							//Do nothing.
						}
					});
					return;
				}
				if (g.keyEvent.getKeyCode()==KeyEvent.VK_F3){
					dbgParseErrors = true;
				}
				//ok.
				returnWithEscape.keyPressed(e);
				inputTargetFocus.keyPressed(e);
			}
		}

		public final TaiBenchmark bulletEditorScreenDraw_2 = new TaiBenchmark();
		public class EditorRunningParse {
			private Rectangle2D.Float drawLocation;
			private ScriptExtraPropertyHandler textParser;
			private EditorTextSheet etsInner;
			private EditorTextSheet etsErrors;
			private EditorTextSheet etsTree;
			private ArrayList<ScriptPropertyCustomizable> textParsedCustomizables = new ArrayList();

			public EditorRunningParse(Rectangle2D.Float drawLocation) {
				this.drawLocation = drawLocation;
				textParser = new ScriptExtraPropertyHandler();
				//Replica of input
				etsInner = makeEditorTextSheet(drawLocation, etw.getLinesToShow(),false,false,false);
				etsInner.setTextRestrictions(etw.getLinesToShow(), 0);
				etsInner.setSelectable(false);
				//Errors
				etsErrors = makeEditorTextSheet(drawLocation, etw.getLinesToShow(),false,false,false);
				etsErrors.setTextRestrictions(etw.getLinesToShow(), 0);
				etsErrors.tse.clearBlanks();
				etsErrors.setSelectable(true);
				//Tree
				etsTree = makeEditorTextSheet(drawLocation, etw.getLinesToShow(),false,false,false);
				etsTree.setSelectable(true);
				etsTree.setArrowScrollGraphic("EditorScreen/ScrollArrowRed.png");	
			}
			public void cleanup(){
				etsInner.cleanup();
				etsErrors.cleanup();
				g.unregisterDraw(timeFrames);
			}
			public void parseErrors(){
				errors.clear();
				comments.clear();
				hintsOut.clear();
				timedElements.clear();
				BulletHellInstancePlayer bhit = new BulletHellInstancePlayer();
				//new env
				BulletHellEnv bhe = new BulletHellEnv(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this);
				ParseResult createFromBHI = bhit.createFromBHI(bhi,bhe);
				for(Comment c : createFromBHI.comments){
					if (c.commentText.trim().length()>0){
						ArrayList<Comment> got = comments.get(c.lineNum);
						if (got==null){
							got = new ArrayList<Comment>();
							comments.put(c.lineNum,got);
						}
						got.add(c);
					}
				}
				for(ParseCrash pc : createFromBHI.toRetCrashes){
					String prior = errors.get(pc.lineNum);
					if (prior==null){
						prior = "";
					} else {
						prior+=", ";
					}
					errors.put(pc.lineNum, prior+pc.msg);
					if (dbgParseErrors){
						pc.printStackTrace();
					}
				}
				for(BulletSpellCard sc : bhit.bscs){
					timedElements.put(sc.myInstanceName,sc.predicted_start_time);
				}
				for(BulletLevel sc : bhit.blevels){
					timedElements.put(sc.myInstanceName,sc.predicted_start_time);
				}
				hintsOut.addAll(createFromBHI.hints);
				dbgParseErrors = false;
				updateErrors();
			}
			private TreeMap<Integer, ArrayList<Comment>> comments = new TreeMap();
			private TreeMap<Integer, String> errors = new TreeMap();
			private TreeSet<String> hintsOut = new TreeSet();
			private TreeMap<String, Float> timedElements = new TreeMap();
			public void updateErrors(){
				etsErrors.tse.clearBlanks();
				for(int line = etw.getWindowLine(); line < etw.getWindowLine() + etw.getLinesToShow() && line < etw.tse.Editing.size(); line++){
					String got = errors.get(line);
					if (got!=null){
						etsErrors.tse.setLine(got, line-etw.getWindowLine());
					}
				}
				hints.tse.clearBlanks();
				if (!hintsOut.isEmpty()){
					String hintStr = "";
					for(Iterator<String> itr = hintsOut.iterator(); itr.hasNext(); ){
						String got = itr.next();
						hintStr += got;
						if (itr.hasNext()){
							hintStr+=", ";
						}
					}
					hints.tse.setLine("Hint: "+hintStr,0);
				}
				hints.isTextModified = true;
				etsErrors.isTextModified = true;
			}	
			private boolean drawParseBox = true;
			private boolean drawTree = false;
			private boolean hasUpdatedRecently = false;
			private final long parserRunsInterval = (long).25e9;
			private long lastUpdate = System.nanoTime()-parserRunsInterval;
			private boolean wantsParseRun = false;
			public void draw() {bulletEditorScreenDraw_2.mark();
			screen2D();
			if (etw.textWasModifiedThisFrame){
				//Update the parsing side, even though a new parse may not have occurred
				hasUpdatedRecently = true;
				wantsParseRun = true;
			}
			if (System.nanoTime()-lastUpdate > parserRunsInterval && wantsParseRun){
				lastUpdate = (long) (System.nanoTime());
				wantsParseRun = false;
				//do a "full parse"
				parseErrors();
				hasUpdatedRecently = true;
			}

			viewport(parsedR.x, parsedR.y, parsedR.width, parsedR.height);
			if (hasUpdatedRecently){
				hasUpdatedRecently = false;
				updateErrors();
				//Parsed metadatabuttons:
				//Copy text from etw
				etsInner.tse.clearBlanks();
				textParsedCustomizables = new ArrayList();
				textParser.setOn(null);
				for(int line = 0; line < etw.tse.Editing.size(); line++){
					String code = etw.getRowText(line);
					textParser.setOn(code);
					ScriptPropertyCustomizable got;
					while((got=textParser.nextCustomizableWord())!=null){
						textParsedCustomizables.add(got);
						if (got instanceof ScriptExtraPropertyHandler.Timed_ScriptPropertyCustomizable){
							Float timeF = timedElements.get(got.getInstanceName());
							if (timeF!=null){
								String time;
								if (timeF >= 0){
									time = String.format("%d:%02d",(int)(timeF/60),(int)(timeF%60));
								} else {
									time = "?:??";
								}
								((ScriptExtraPropertyHandler.Timed_ScriptPropertyCustomizable)got).setTime(time,timeF);
							}
							String time = ((ScriptExtraPropertyHandler.Timed_ScriptPropertyCustomizable)got).getTime();
							String postpend = code.substring(Math.min(code.length()-1,time.length()));
							code = time+postpend;
						}
					}
					int k = line-etw.getWindowLine();
					if(k >= 0 && k < etsInner.getLinesToShow()){
						etsInner.tse.setLine(code,k);
					}
				}
				etsInner.isTextModified = true;
			}
			if (drawParseBox){
				drawParseBox();
			} 
			if (drawTree){
				drawTree();
			}
			drawTreeModeToggleButton();
			if (etwMym.mouseChecker(textR)){
				//Transfer focus
				inputTargetFocus.hasMouseFocus = false;
				inputTargetFocus = etw;
				inputTargetFocus.hasMouseFocus = true;
			}
			viewport(textR.x, textR.y, textR.width, textR.height);
			if (comments!=null){
				//Draw the comment boxes.
				GL gl = ((PGraphicsOpenGL)g.g).gl;
				gl.glBlendFunc(GL.GL_DST_COLOR, GL.GL_ONE);
				for(int line = etw.getWindowLine(); line < etw.getWindowLine() + etw.getLinesToShow() && line < etw.tse.Editing.size(); line++){
					String lineText = etw.getRowText(line);
					int cLine = line-etw.getWindowLine();
					ArrayList<Comment> got = comments.get(line);
					if(got==null) continue;
					g.noStroke();
					g.fill(125,255,0,255);
					for(Comment p : got){
						int start = lineText.indexOf(p.commentText);
						int end = start + p.commentText.length();
						if (start==-1 || end==-1){
							//System.err.println("Incorrect commenting: Couldn't find "+p.commentText+" in "+lineText+" ( "+line+")");
							continue;
						}
						float x = etw.getXcoordinate(line, start);
						float ex = etw.getXcoordinate(line, end);
						float yscl = 1f/etw.getLinesToShow();
						float y = yscl * cLine;
						g.rect(x,y,ex-x,yscl);
					}
				}
				gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			}
			bulletEditorScreenDraw_2.markDone();}
			private void drawParseBox(){
				float yscl = 1f/etw.getLinesToShow();
				etsInner.draw();
				textParser.setOn(null);
				/*
				for(int line = 0; line < etw.getWindowLine(); line++){
					//quickscan the earlier lines, to get an idea of the encapsulation heiarchy
					textParser.setOn(etw.getRowText(line));
					ScriptPropertyCustomizable got;
					while((got=textParser.nextCustomizableWord())!=null){
						//nothing.
					}
				}
				 */
				ArrayList<ScriptPropertyCustomizable> showThese = textParsedCustomizables;
				HashMap<Rectangle2D.Float, ScriptPropertyCustomizable> map = new HashMap();
				ListIterator<ScriptPropertyCustomizable> iterator = showThese.listIterator();
				for(int line = 0; line < etw.tse.Editing.size(); line++){
					float y = yscl * (line-etw.getWindowLine());
					float eraseX = -1;
					boolean shouldContinueInstead = line < etw.getWindowLine() || line >= etw.getWindowLine() + etw.getLinesToShow();
					if (shouldContinueInstead){
						continue;
					}
					int lineVis = line-etw.getWindowLine();
					eraseX = etsInner.getXcoordinate(lineVis,0);
					//Technically, we allow multiple starts on one line. In reality,
					//this works best with just 1.
					while(iterator.hasNext()){
						final ScriptPropertyCustomizable got = iterator.next();
						if (got.startingLine<line){
							continue;
						}
						if (got.startingLine>line){
							iterator.previous();
							break;
						}
						if (got instanceof ScriptExtraPropertyHandler.Timed_ScriptPropertyCustomizable){
							//Move cursor to the 
							eraseX = etsInner.getXcoordinate(lineVis,((ScriptExtraPropertyHandler.Timed_ScriptPropertyCustomizable)got).getTime().length());
							float x = etsInner.getXcoordinate(lineVis,0);
							Rectangle2D.Float r = new Rectangle2D.Float(x,y,eraseX-x,yscl);
							scaleRect(r,drawLocation);
							ScriptPropertyCustomizable dummy = textParser.new ScriptPropertyCustomizable(){
								public String getType() {
									return null;
								}
								public int highlightColor(PApplet g) {
									return 0;
								}
								public void pressed(EditorScreen es) {
									etw.tse.CaretLine = got.startingLine;
									simulationStartRequest = got.getInstanceName();
									SceneChange(Transitioner.FADE_WITH_BLACK,BulletHell.BulletGame$1Engine$GROUND.GAMESCREEN_FROMEDITOR);
								}
							};
							map.put(r,dummy);
						}
						try {
							//add a menu item
							float x = etsInner.getXcoordinate(lineVis, got.startChar);
							g.noStroke();
							g.fill(0);
							g.rect(eraseX,y,x-eraseX,yscl);
							float x2 = etsInner.getXcoordinate(lineVis, got.endChar);
							eraseX = x2+1f/currentViewPortWidth;

							Rectangle2D.Float r = new Rectangle2D.Float(x,y,x2-x,yscl);

							g.stroke(255);
							g.fill(got.highlightColor(g));
							g.rect(r.x,r.y+1f/currentViewPortHeight,r.width,r.height-2f/currentViewPortHeight);

							//we need to expand r outside of the box.
							scaleRect(r,drawLocation);
							map.put(r,got);
						} catch (Throwable e){
							//Don't need to do anything about it.
							e.printStackTrace();
						}
					}
					g.noStroke();
					g.fill(0);
					g.rect(eraseX,y,1-eraseX,yscl);			
				}


				if (false){
					for(ScriptPropertyCustomizable k : showThese){
						int q = k.endLine-etw.getWindowLine();
						if (q>=0 && q < etw.getLinesToShow()){
							int endLinePos = k.endLinePos;
							String endName = k.getInstanceName();
							endName = "/"+endName.substring(endName.lastIndexOf(".")+1);
							if (etsErrors!=null){
								String original = etsErrors.getRowText(q);
								StringBuffer sb = new StringBuffer();
								for(int r = 0; r < endLinePos; r++){
									sb.append(" ");
								}
								sb.append(endName);
								sb.append(original);
								etsErrors.tse.setLine(sb.toString(),q);
								etsErrors.isTextModified = true;
							}
						}
					}
				}
				viewport(0,0,1,1);
				//Draw the images of the bullets
				for(Entry<Rectangle2D.Float, ScriptPropertyCustomizable> k : map.entrySet()){
					Rectangle2D.Float r = k.getKey();
					if (k.getValue().getType()==TaiScriptLanguage$Constants.BULLET){
						//Draw the bullet image.
						/*
						TaiScriptProperty tsp = sProp.getProperty("bullet",k.getValue().getInstanceName(),"show",TaiScriptProperties.RECT_TYPE);
						Rectangle rectMade = tsp.getRectangleValue();
						if (rectMade.width==0 || rectMade.height==0){
							//draw null
							g.fill(255,0,0);
							g.rect(r.x-.05f,r.y,.05f,.05f*NSPH);
						}
						 */
					}
				}
				//Alright, take care of the mouse things.
				for(Entry<Rectangle2D.Float, ScriptPropertyCustomizable> k : map.entrySet()){
					if (k.getValue().mym==null){
						k.getValue().mym = new MouseChecker(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this);
					}
					if (k.getValue().mym.mouseChecker(k.getKey())){
						k.getValue().pressed(EditorScreen.this);
					}
				}
				viewport(parsedR.x, parsedR.y, parsedR.width, parsedR.height);
				etsErrors.draw();
				viewport(0,0,1,1);
				if (parserMym.mouseChecker(parsedR)){
					//Transfer focus
					inputTargetFocus.hasMouseFocus = false;
					inputTargetFocus = etsErrors;
					inputTargetFocus.hasMouseFocus = true;
				}
			}
			private void drawTree(){
				//Work with etsErrors
				ArrayList<String> names = new ArrayList();
				ArrayList<Integer> locations = new ArrayList();
				
				for(int i = 0; i < textParsedCustomizables.size(); i++){
					ScriptPropertyCustomizable sc = textParsedCustomizables.get(i);
					names.add(sc.getInstanceName());
					locations.add(sc.startingLine);
				}
				int i;
				for(i = 0; i < locations.size(); i++){
					String old = null;
					if (i < etsTree.tse.Editing.size()){
						old = etsTree.tse.Editing.get(i);
					}
					String neu = "->"+names.get(i)+TaiScriptEditor.LINE_END_SUBCHAR;
					etsTree.tse.Editing.add(i,neu);
					if (!neu.equals(old)){
						etsTree.isTextModified = true;
					}
				}
				for(; i < etsTree.tse.Editing.size();){
					etsTree.tse.Editing.remove(i);
					etsTree.isTextModified = true;
				}
				if (inputTargetFocus == etsTree){
					//Then we can take the caretline:
					int line = etsTree.tse.Selection.LineBegin;
					if (line < locations.size()){
						etw.tse.CaretLine = locations.get(line);
						etw.tse.CaretPosition = 0;
						etw.singleWidthSelect();
						etw.isTextModified = true;
					}
				}
				viewport(parsedR.x, parsedR.y, parsedR.width, parsedR.height);
				etsTree.draw();
				viewport(0,0,1,1);
				if (parserMym.mouseChecker(parsedR)){
					//Transfer focus
					inputTargetFocus.hasMouseFocus = false;
					inputTargetFocus = etsTree;
					inputTargetFocus.hasMouseFocus = true;
				}
			}
			private MouseChecker treeView;
			private Rectangle2D.Float treeViewButton;
			private void drawTreeModeToggleButton(){
				if (treeView==null){
					treeView = new MouseChecker(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this);
					treeViewButton = new Rectangle2D.Float(.86f,0f,.13f,.09f);
					scaleRect(treeViewButton,parsedR);
				}
				viewport(treeViewButton);
				PImage button = FILE_SYSTEM.getImg("EditorScreen/TreeView.png");
				g.image(button,0,0,1,1);
				viewport(0,0,1,1);
				if (treeView.mouseChecker(treeViewButton)){
					if (drawTree){
						drawTree = false;
						drawParseBox = true;
					} else {
						drawTree = true;
						drawParseBox = false;
					}
				}
			}
		}

		public final TaiBenchmark bulletEditorScreenDraw_1 = new TaiBenchmark();
		private boolean hadDialogLastFrame = false;
		public void drawScreen() {
			TaiBenchmark.bench(this);

			bulletEditorScreenDraw_1.mark();

			g.background(0);
			screen2D();
			g.hint(PApplet.DISABLE_DEPTH_TEST);

			boolean shouldDrawAnyway = true;
			if (hasCurrentDialog()){
				if (hadDialogLastFrame){
					shouldDrawAnyway = false;
				}
				hadDialogLastFrame = true;
			} else {
				hadDialogLastFrame = false;
			}
			if (shouldDrawAnyway){
				g.stroke(255);
				g.noFill();
				g.rect(textR.x - 1f / currentViewPortWidth, textR.y - 1f
						/ currentViewPortHeight, textR.width + 2f
						/ currentViewPortWidth, textR.height + 2f
						/ currentViewPortHeight);
				viewport(textR.x, textR.y, textR.width, textR.height);
				bulletEditorScreenDraw_1.mark();
				etw.draw();
				bulletEditorScreenDraw_1.markDone();
				epw.draw();
				viewport(hintsR);
				hints.draw();
				viewport(0, 0, 1, 1);
				screen2D();
				topMenuBar.mouseCheck(g); // Also draws them.
				fileButtons.mouseCheck(g);
			}

			g.hint(PApplet.ENABLE_DEPTH_TEST);

			bulletEditorScreenDraw_1.markDone();}

		public void cleanup() {
			g.unregisterDraw(this);
			removeSubKeyListener(this);
			g.unregisterDraw(etw);
			etw.cleanup();
			epw.cleanup();
		}

		//Used among the graphic choose dialogs
		private PressActionThreshold frameAdvanceFor = new PressActionThreshold(.3,.3);
		private PressActionThreshold frameAdvanceRev = new PressActionThreshold(.3,.3);
		
		public class ImageSelectorDialog extends ModalDialog {
			private void recreateDialog(String newURL, int gridX, int gridY){
				bhi.setTextureFile(wantsTextureSlotFilled, newURL);
				if (!editing.frame[currentEditingFrame].filename.equals(newURL)){
					editing.frame[currentEditingFrame].filename = newURL;
					editing.frame[currentEditingFrame].rect = new int[]{-1,-1,-101,-101};
				}
				new ImageSelectorDialog(EditorScreen.this, dialogPlace, whichObject, objectType,currentEditingFrame, gridX, gridY);
			}
			private void createNext(final ImageSelectorDialog self){
				new GetInputDialog(self,bhi.getTextureFile(wantsTextureSlotFilled));
			}
			private void createNextFrame(int whichFrame){
				new ImageSelectorDialog(EditorScreen.this, dialogPlace, whichObject, objectType, whichFrame, gridX, gridY);
			}
			private class GetInputDialog extends ModalDialog {
				public GetInputDialog(final ImageSelectorDialog self2, WordByRef<String> wordByRef){
					super(EditorScreen.this,new ModalDialogCallback<GetInputDialog>(){
						public void dialogFinished(GetInputDialog self) {
							self.cleanup();
							if (!self.inputTextDialogGUI.wasCancelled()){
								String newURL = self.inputTextDialogGUI.getInputString(0).trim();
								int gridX = 1;
								int gridY = 1;
								try {
									int rX = new Integer(self.inputTextDialogGUI.getInputString(1).split("=")[1].trim());
									int rY = new Integer(self.inputTextDialogGUI.getInputString(2).split("=")[1].trim());
									if (rX < 0 || rY < 0){
										throw new RuntimeException();
									}
									gridX = rX;
									gridY = rY;
								}catch (Throwable e){

								}
								System.out.println(gridX+" "+gridY);
								recreateDialog(newURL,gridX,gridY);
							}
						}
					});
					if(wordByRef!=null){
						inputTextDialogGUI = new InputTextDialogGUI("You may modify the URL of this graphic:",3);
						inputTextDialogGUI.setText(wordByRef.getContentData(),0);
					} else {
						inputTextDialogGUI = new InputTextDialogGUI("Enter the URL of the bullet graphic:",3);
					}
					inputTextDialogGUI.setText("gridW = ",1);
					inputTextDialogGUI.setText("gridH = ",2);
				}
				public void cleanup(){
					if (inputTextDialogGUI!=null)
						inputTextDialogGUI.cleanup();
				}
				private InputTextDialogGUI inputTextDialogGUI;
				public boolean drawDialog() {
					inputTextDialogGUI.draw();
					return inputTextDialogGUI.isComplete();
				}
			};
			public ImageSelectorDialog(EditorScreen editorScreen, Rectangle2D.Float dialogSpace, String whichObject, String type, int whichFrame) {
				this(editorScreen,dialogSpace,whichObject,type,whichFrame,1,1);
			}
			public ImageSelectorDialog(EditorScreen editorScreen, Rectangle2D.Float dialogSpace, String whichObject, String type, int whichFrame, int gridX, int gridY) {
				super(editorScreen, new ModalDialogCallback<ImageSelectorDialog>(){
					public void dialogFinished(ImageSelectorDialog self) {
						self.cleanup();
						if (self.wantsTextureSlotFilled!=-1){
							self.createNext(self);
						}
						if (self.wantsFrameSlotFilled!=-1){
							self.createNextFrame(self.wantsFrameSlotFilled);
						}
						if (self.surroundings.wasCancelled()){
							self.editing.frame[self.currentEditingFrame].rect = self.revertRect;
						}
					}
				});
				this.gridX = gridX;
				this.gridY = gridY;
				this.dialogPlace = dialogSpace;
				float textureSelectorHeight = .1f;
				textureTextSpace = new Rectangle2D.Float(
						0,0,1,textureSelectorHeight);
				scaleRect(textureTextSpace,dialogPlace);
				bht = new BulletHellText(textureTextSpace,1);
				bht.setText("Textures (Click to load):");
				fileButton = FILE_SYSTEM.getImg("EditorScreen/TextureFileIcon.png");
				frameButton = FILE_SYSTEM.getImg("EditorScreen/TextureFrameIcon.png");

				this.whichObject = whichObject;
				this.objectType = type;

				editing = bhi.getGameGraphic(whichObject, objectType);
				//Do we have the frame we want to edit?

				GameGraphic[] newFrames = new GameGraphic[Math.max(editing.frame.length,whichFrame+1)];
				int k = 0;
				for(; k < editing.frame.length; k++){
					newFrames[k] = editing.frame[k];
				}
				for(; k < newFrames.length; k++){
					newFrames[k] = new GameGraphic("");
				}
				editing.frame = newFrames;
				currentEditingFrame = whichFrame;
				revertRect = new int[4];
				System.arraycopy(editing.frame[currentEditingFrame].rect,0,revertRect,0,4);
				//Ok, our editing frame is prepared.
				StringTreeIterator<WordByRef<String>> iterator = bhi.textureURLs.iterator();
				int toUseIndex = 0;
				int count = 0;
				while(iterator.hasNext()){
					WordByRef<String> got = ((WordByRef<String>)iterator.getCurrentNode());
					if (got.getContentData().equalsIgnoreCase(editing.frame[currentEditingFrame].filename)){
						toUseIndex = count;
					}
					count++;
					iterator.tryNext();
				}
				getUpImage(toUseIndex);
				if (cImg==null){
					wantsTextureSlotFilled = toUseIndex; //fill the base
				}

				surroundings = new DialogGUI(dialogSpace){
					public void drawDialog() {
						//just blank
					}
				};

				BulletHellInstancePlayer parser = new BulletHellInstancePlayer();
				collisionEnv = new BulletHellEnv(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this);
				ParseResult createFromBHI = parser.createFromBHI(bhi, collisionEnv);
				if (type.equals(TaiScriptLanguage$Constants.BULLET)){
					int counterr = 0;
					for(BulletPattern bp : parser.firables){
						if (bp.myInstanceName.equals(whichObject)){
							forCollisionData = bp;
							try {
								collisionWidthEval = parser.bulletGfxExpressionCache[counterr][0].evaluatef();
								collisionHeightEval = parser.bulletGfxExpressionCache[counterr][1].evaluatef();
							} catch (Throwable e){
								collisionWidthEval = .05f;
								collisionHeightEval = .05f;
							}
							break;
						}
						counterr++;
					}
				}
				if (type.equals(TaiScriptLanguage$Constants.BOSS)){
					for(BulletBoss bp : parser.bbosses){
						if (bp.myInstanceName.equals(whichObject)){
							forCollisionData = bp;
						}
					}
				}
				if (forCollisionData==null){
					throw new RuntimeException("No such object found: "+whichObject);
				}
			}

			private void getUpImage(int which){
				WordByRef<String> textureFile = bhi.getTextureFile(which);
				if (textureFile!=null){
					try {
						String contentData = textureFile.getContentData();
						cImg = FILE_SYSTEM.getImgFresh(contentData);
						((PGraphicsOpenGL)g.g).checkImageSizeValid(cImg);
						editing.frame[currentEditingFrame].filename = contentData;
						System.out.println(
								Arrays.toString(
										editing.frame[currentEditingFrame].rect));
					} catch (OutOfMemoryError e){
						e.printStackTrace();
						cImg = null;
					} catch (RuntimeException e){
						e.printStackTrace();
						cImg = null;
					}
				}
				if (cImg!=null){
					if (cImg.width < 5 || cImg.height < 5){
						cImg = null;
					}
				}
			}
			public void cleanup(){
				super.cleanup();
				bht.cleanup();
				cImg = null;
				FILE_SYSTEM.clearImages();
			}
			private MouseChecker[] myms;
			private MouseChecker[] mymsFrames;
			private int wantsFrameSlotFilled = -1;
			private DialogGUI surroundings;
			private GameSprite editing;
			private int currentEditingFrame = 0;
			private Rectangle2D.Float textureTextSpace;
			private int wantsTextureSlotFilled = -1;
			private PImage cImg;
			private BulletHellText bht;
			private Rectangle2D.Float dialogPlace;
			private PImage fileButton,frameButton;
			private String whichObject; //Which "sprite" we're editing.
			private String objectType; //what kind of object
			private int[] revertRect;

			private int gridX, gridY; //round on rect dragging

			private GraphicsHolder forCollisionData;
			private BulletHellEnv collisionEnv;
			private float collisionWidthEval, collisionHeightEval;
			private boolean inBoundingBoxEditor = true;
			private MouseChecker 
			toggleBoundingBox = new MouseChecker(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this),
			toggleTrueSize = new MouseChecker(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this);
			public boolean drawDialog(){
				if (cImg==null){
					return true;
				}
				if (surroundings.isComplete()){
					return true;
				}
				surroundings.draw();
				boolean toRet;
				if (inBoundingBoxEditor){
					toRet =  drawDialog_EditBoundingBoxMode();
				} else {
					toRet = drawDialog_PreviewCollisionData();
					//Draw the actual size toggle:
					Rectangle2D.Float toDrawIn = new Rectangle2D.Float(.06f,.2f,.06f,.6f);
					scaleRect(toDrawIn,dialogPlace);
					viewport(toDrawIn);
					PImage toggleBounder = FILE_SYSTEM.getImg("EditorScreen/ViewCollisionActual.png");
					g.image(toggleBounder,0,0,1,1);
					viewport(0,0,1,1);
					if(toggleTrueSize.mouseChecker(toDrawIn)){
						showCollisionActualSize = !showCollisionActualSize;
					} 	
				}
				Rectangle2D.Float toDrawIn = new Rectangle2D.Float(0,.2f,.06f,.6f);
				scaleRect(toDrawIn,dialogPlace);
				viewport(toDrawIn);
				PImage toggleBounder = FILE_SYSTEM.getImg("EditorScreen/ViewCollisionData.png");
				g.image(toggleBounder,0,0,1,1);
				viewport(0,0,1,1);
				if(toggleBoundingBox.mouseChecker(toDrawIn)){
					inBoundingBoxEditor = !inBoundingBoxEditor;
				}
				return toRet;
			}
			private final float offsetLeft = .5f;
			private final float offsetTop = .5f-.025f;
			private boolean showCollisionActualSize = false;
			private long startTime = System.nanoTime();
			public boolean drawDialog_PreviewCollisionData(){
				g.pushMatrix();
				g.translate(offsetLeft,offsetTop);
				float sqSide = PApplet.min(currentViewPortWidth,currentViewPortHeight);
				float bound = .4f*sqSide/currentViewPortWidth;
				float bound2 = .4f*sqSide/currentViewPortHeight;
				if (showCollisionActualSize){
					try {
						collisionEnv.t = (float)((System.nanoTime()-startTime)/1e9);
						bound = collisionWidthEval;
						bound2 = collisionHeightEval;
					} catch(Throwable e){
						e.printStackTrace();
					}
				}
				Rectangle2D.Float drawnRect = new Rectangle2D.Float(
						-bound,-bound2,2*bound,2*bound2);
				Rectangle2D.Float evalRect = editing.frame[currentEditingFrame].evalRect(cImg);
				evalRect.x*=cImg.width;
				evalRect.y*=cImg.height;
				evalRect.width*=cImg.width;
				evalRect.height*=cImg.height;
				g.noStroke();
				g.fill(255);
				g.image(cImg,
						drawnRect.x,drawnRect.y,
						drawnRect.width,drawnRect.height,
						(int)(evalRect.x),(int)(evalRect.y),
						(int)(evalRect.x+evalRect.width),
						(int)(evalRect.y+evalRect.height));
				g.popMatrix();
				drawnRect.x+=offsetLeft;
				drawnRect.y+=offsetTop;
				viewport(drawnRect);
				int red = g.color(255,0,0,125);
				//need to check for nonpositive values.
				g.noStroke();
				for(int x = 0; x < forCollisionData.getW_tex(); x++){
					for(int y = 0; y < forCollisionData.getH_tex(); y++){
						int c = red;
						try {
							int val = forCollisionData.getTexGrid()[y * forCollisionData.getW_tex()+x];
							c = val==0?g.color(0,0,0,125):g.color(255,255,255,125);
						} catch(Throwable e){
							//do nothing
						}
						g.fill(c);
						float xr = 1f/forCollisionData.getW_tex()*x;
						float yr = 1f/forCollisionData.getH_tex()*y;
						g.rect(xr,yr,1f/forCollisionData.getW_tex(), 1f/forCollisionData.getH_tex());
					}
				}
				return false;
			}
			public boolean drawDialog_EditBoundingBoxMode() {
				g.pushMatrix();
				g.translate(offsetLeft,offsetTop);
				g.noFill();
				float sqSide = PApplet.min(currentViewPortWidth,currentViewPortHeight);
				float check = .1f;
				float bound = .4f*sqSide/currentViewPortWidth;
				float bound2 = .4f*sqSide/currentViewPortHeight;
				g.noStroke();
				boolean on = true;
				for(float checkX = -bound; checkX<bound; checkX+=check){
					boolean tOn = on;
					for(float checkY = -bound2; checkY<bound2; checkY+=check*NSPH){
						g.fill(tOn?100:150);
						g.rect(checkX,checkY,g.min(check,bound-checkX),
								g.min(check*NSPH,bound2-checkY));
						tOn = !tOn;
					}

					on = !on;
				}
				g.stroke(0);
				g.noFill();
				g.rect(-bound,-bound2,2*bound,2*bound2);
				g.fill(255);
				Rectangle2D.Float drawnRect = new Rectangle2D.Float(
						-bound,-bound2,2*bound,2*bound2);
				g.image(cImg,
						drawnRect.x,drawnRect.y,
						drawnRect.width,drawnRect.height);
				//We need the rectangle to be draggable.
				g.popMatrix();
				//Image selectors:
				float texWidth = .35f;
				float imgHeight = .07f;
				float imgWidth = imgHeight*NSPH;
				if (myms==null){
					myms = new MouseChecker[bhi.textureURLs.size()+1];
					for(int k = 0; k < myms.length; k++){
						myms[k] = new MouseChecker(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this);
					}
				}
				if (mymsFrames==null){
					//get the current frames:
					mymsFrames = new MouseChecker[currentEditingFrame+1+1];
					for(int k = 0; k < mymsFrames.length; k++){
						mymsFrames[k] = new MouseChecker(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this);
					}
				}
				for(int k = 0; k < myms.length; k++){
					Rectangle2D.Float area = (Rectangle2D.Float) textureTextSpace.clone();
					area.x+=texWidth;
					area.width=.1f;
					area.x+=imgWidth*k;
					area.height = imgHeight;
					viewport(area);
					g.image(fileButton,0,0,1,1);
					viewport(0,0,1,1);
					if (myms[k].mouseChecker(area)){
						wantsTextureSlotFilled = k;
						return true;
					}
				}
				
				int oldSlot = currentEditingFrame;
				if (frameAdvanceFor.isActionTime(truth(keyboard.get(KeyEvent.VK_1)))){
					oldSlot = Math.max(0,currentEditingFrame-1);
				}
				if (frameAdvanceRev.isActionTime(truth(keyboard.get(KeyEvent.VK_2)))){
					oldSlot = currentEditingFrame+1;
				}
				if (currentEditingFrame != oldSlot){
					wantsFrameSlotFilled = oldSlot;
					return true;
				}
				
				float imgHeight_frame = .05f;
				float imgWidth_frame = imgHeight_frame*NSPH;
				for(int k = 0; k < mymsFrames.length; k++){
					Rectangle2D.Float area = new Rectangle2D.Float(
							1-imgWidth_frame,
							imgHeight, //texture
							imgWidth_frame,
							imgHeight_frame);
					scaleRect(area, dialogPlace);
					area.y+=imgHeight_frame*k;
					viewport(area);
					if (currentEditingFrame==k){
						g.image(frameButton,.2f,.2f,.6f,.6f);
					} else {
						g.image(frameButton,0,0,1,1);
					}
					g.fill(255,255,255);
					viewport(0,0,1,1);
					if (mymsFrames[k].mouseChecker(area)){
						wantsFrameSlotFilled = k;
						return true;
					}
				}
				bht.draw();
				viewport(0,0,1,1);
				//Now scale for mouse coords
				//Remove the centering:
				if (draggingCorner==-1){
					if (keepResultThisFrame){
						editing.frame[currentEditingFrame].setRect(originalBox,cImg,gridX,gridY);
						keepResultThisFrame = false;
					}
				}
				Rectangle2D.Float evalRect = editing.frame[currentEditingFrame].evalRect(cImg);
				drawnRect.x+=offsetLeft;
				drawnRect.y+=offsetTop;
				//Drawing:
				Rectangle2D.Float editClone = (Rectangle2D.Float) evalRect;
				scaleRect(editClone,drawnRect);
				drawDraggableBox(editClone);
				unScaleRect(editClone,drawnRect);
				if (!keepResultThisFrame){
					originalBox = editClone;
				}
				return false;
			}
			private boolean keepResultThisFrame = false;
			private void drawDraggableBox(Rectangle2D.Float r){
				float twoPixels = 3f/Math.min(currentViewPortWidth,currentViewPortHeight);
				Point2D.Float mouse = myms[0].getMouse();
				//Four corners:
				Point2D.Float[] corners = new Point2D.Float[]{
						new Point2D.Float(r.x,r.y), //TL
						new Point2D.Float(r.x+r.width,r.y), //TR
						new Point2D.Float(r.x+r.width,r.y+r.height), //BR
						new Point2D.Float(r.x,r.y+r.height), //BL
				};
				if (!g.mousePressed){
					if (draggingCorner!=-1){
						keepResultThisFrame = true;
					}
					draggingCorner = -1;
				}
				for(int index = 0; index < 4; index++){
					Point2D.Float k = corners[index];
					if ((draggingCorner==index) || (k.distance(mouse)<=twoPixels)){
						if (g.mousePressed){
							if (draggingCorner==-1){
								draggingCorner = index;
								originalTexPoint = k;
								originalCursor = mouse;
							} else {
								float deltaX = mouse.x-originalCursor.x;
								float deltaY = mouse.y-originalCursor.y;
								if (index==0||index==3){
									r.x+=deltaX;
									r.width-=deltaX;
								} else {
									r.width+=deltaX;
								}
								if (index==0||index==1){
									r.y += deltaY;
									r.height-=deltaY;
								} else {
									r.height+=deltaY;
								}
							}
						}
					}
				}
				g.stroke(255,0,0);
				g.noFill();
				g.rect(r.x,r.y,r.width,r.height);
				for(int index = 0; index < 4; index++){
					Point2D.Float k = corners[index];
					g.noStroke();
					g.fill(255);
					g.rect(k.x-twoPixels/2,k.y-twoPixels/2,twoPixels,twoPixels);
				}
			}
			private int draggingCorner = -1;
			private Point2D.Float originalTexPoint = null;
			private Point2D.Float originalCursor = null;
			private Rectangle2D.Float originalBox = null;
		}
	}

	public BulletGame$1Engine$L3$1$BulletGameEditorScreen(JFrame holder, PApplet hold) {
		super(holder, hold);
	}

	/**
	 * Use me together with a modaldialog / modaldialogcallback to get good dialogs
	 */
	public abstract class DialogGUI {
		public Rectangle2D.Float dialogPlace;
		private ModalDialogButton okButton;
		private ModalDialogButton cancelButton;
		private boolean wasOkayed = false;
		private boolean wasCancelled = false;
		public PFont buttonFonts;

		public DialogGUI(Rectangle2D.Float dialogSpace) {
			buttonFonts = FILE_SYSTEM.getFont("SquareTypeB-24.vlw");
			final float p5 = .5f;
			//System.out.println(dialogSpace);
			dialogPlace = dialogSpace;
			Rectangle2D.Float d = dialogPlace;
			// Of that, texPlace takes up a subrectangle:
			Rectangle2D.Float buttonPlace_OK;
			Rectangle2D.Float buttonPlace_CANCEL;
			float w = .12f/d.width;
			float h = .03f/d.height;
			float cx = .25f;
			float cy = 1 - h;
			buttonPlace_OK = new Rectangle2D.Float(cx-w, cy-h*2, w*2, h*2);
			cx = .75f;
			buttonPlace_CANCEL = new Rectangle2D.Float(cx-w, cy-h*2, w*2, h*2);
			scaleRect(buttonPlace_OK, dialogPlace);
			scaleRect(buttonPlace_CANCEL, dialogPlace);
			okButton = new ModalDialogButton("Ok", buttonPlace_OK) {
				public void act() {
					wasOkayed = true;
				}
			};
			cancelButton = new ModalDialogButton("Cancel", buttonPlace_CANCEL) {
				public void act() {
					wasCancelled = true;
				}
			};
			g.mousePressed = false;
		}

		public boolean wasCancelled() {
			return wasCancelled;
		}

		public boolean isComplete() {
			return wasOkayed || wasCancelled;
		}


		public void draw() {
			screen2D();
			g.noStroke();
			g.fill(0, 0, 0, 100); //darken the screen.
			g.rect(0, 0, 1, 1);
			float opixX = 1f / currentViewPortWidth;
			float opixY = 1f / currentViewPortHeight;
			g.stroke(38, 37, 28);
			g.noFill();
			Rectangle2D.Float r = dialogPlace;
			g.rect(r.x - opixX, r.y - opixY, r.width + 2 * opixX, r.height + 2
					* opixY);
			g.stroke(0, 0, 0);
			g.fill(32, 92, 110);
			g.rect(r.x, r.y, r.width, r.height);
			// Draw the info:
			drawDialog();
			if (sTime==-1L){
				g.mousePressed = false;
				sTime = System.nanoTime();
			}
			// Draw the buttons:
			okButton.draw();
			cancelButton.draw();
			//Impose a delay:
			if (System.nanoTime()-sTime<.1e9){
				wasCancelled = false;
				wasOkayed = false;
			}
		}
		private long sTime = -1;
		public abstract void drawDialog();
	}
	public class MessageDialogGUI extends DialogGUI{
		private String message;
		public MessageDialogGUI(float dialogW, float dialogH, String message) {
			super(new Rectangle2D.Float(.5f - dialogW / 2, .5f - dialogH / 2,
					dialogW, dialogH));
			this.message = message;
		}

		public void drawDialog() {
			Rectangle2D.Float r = dialogPlace;
			g.textFont(buttonFonts);
			g.textAlign(g.LEFT, g.TOP);
			g.pushMatrix();
			g.translate(r.x + .045f, r.y + .02f);
			g.scale(1f / currentViewPortWidth, 1f / currentViewPortHeight);
			g.fill(0);
			g.text(message, 0, 0);
			g.popMatrix();
		}
	}


	public class InputTextDialogGUI extends DialogGUI implements KeyListener{
		private Rectangle2D.Float texPlace;
		private static final float dialogW = .7f;
		private static final float dialogH = .6f;

		private EditorTextSheet etw;
		private BulletHellText bht;
		private String prompt;
		public InputTextDialogGUI(String prompt, int lines) {
			super(new Rectangle2D.Float(.5f - dialogW / 2, .5f - dialogH / 2,
					dialogW, dialogH));
			this.prompt = prompt;

			texPlace = new Rectangle2D.Float(.08f, .22f, .84f, .35f);
			scaleRect(texPlace, dialogPlace);

			etw = new EditorTextSheet(texPlace, lines);
			etw.setTextRestrictions(lines, 0);
			etw.tse.clearBlanks();
			etw.setDisplaysLineEnd(false);
			addSubKeyListener(this);
			Rectangle2D.Float texPromptArea = new Rectangle2D.Float(0,0,1,.2f);
			scaleRect(texPromptArea, dialogPlace);
			bht = new BulletHellText(texPromptArea,1,"RursusCompactMono-16.vlw");
			bht.setText(prompt);
			bht.setTextScale(1.4f);
		}

		public void setText(String contentData, int line) {
			etw.tse.setLine(contentData,line);
		}

		public void drawDialog() {
			Rectangle2D.Float r = dialogPlace;
			float opixX = 1f / currentViewPortWidth;
			float opixY = 1f / currentViewPortHeight;
			g.fill(0,0,0);
			bht.draw();
			// Now work with the text box:
			r = texPlace;
			g.noFill();
			g.stroke(30, 0, 0);
			g.rect(r.x - opixX, r.y - opixY, r.width + 2 * opixX, r.height + 2
					* opixY);
			g.fill(20, 130, 30);
			g.rect(r.x, r.y, r.width, r.height);
			viewport(r.x, r.y, r.width, r.height);
			etw.draw();
			viewport(0, 0, 1, 1);
		}

		public String getInputString(int line) {
			String txt = etw.getRowText(line);
			return txt;
		}

		public void cleanup() {
			etw.cleanup();
			bht.cleanup();
			removeSubKeyListener(this);
		}

		public void keyPressed(KeyEvent e) {
			etw.keyPressed(e);
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}
	}

	public abstract class ModalDialogButton {
		private String txt;
		private PFont toDraw;
		private Rectangle2D.Float drawRect;
		private MouseChecker mym = new MouseChecker(BulletGame$1Engine$L3$1$BulletGameEditorScreen.this);

		public ModalDialogButton(String text, Rectangle2D.Float drawRect) {
			txt = text;
			this.drawRect = drawRect;
		}

		public void draw() {
			if (toDraw == null) {
				toDraw = FILE_SYSTEM.getFont("RursusCompactMono-16.vlw");
			}
			g.textFont(toDraw);
			g.fill(0);
			g.noStroke();
			Rectangle2D.Float r = drawRect;
			g.rect(r.x, r.y, r.width, r.height);
			g.pushMatrix();
			g.translate(r.x + r.width / 2, r.y + r.height / 2);
			g.textAlign(PConstants.CENTER, PConstants.CENTER);
			g.scale(1f / currentViewPortWidth, 1f / currentViewPortHeight);
			g.fill(255);
			g.text(txt, 0, 0);
			g.popMatrix();

			if (mym.mouseChecker(r)){
				act();
			}
		}

		public abstract void act();

	}

	public static class MouseChecker {
		public MouseChecker(ProceGLHybrid g){
			this.g2 = g;
		}
		private boolean isInvalidPress;
		private ProceGLHybrid g2;
		public Point2D.Float getMouse(){
			return new Point2D.Float(g2.g.mouseX / (float) g2.currentViewPortWidth,
					g2.g.mouseY / (float) g2.currentViewPortHeight);
		}
		private PressActionThreshold pat = new PressActionThreshold(.3f,20f);
		public boolean mouseChecker(Rectangle2D.Float r){
			PApplet g = g2.g;
			float moX = g.mouseX / (float) g2.currentViewPortWidth;
			float moY = g.mouseY / (float) g2.currentViewPortHeight;
			Point2D.Float mo = new Point2D.Float(moX, moY);
			if (g.mousePressed){
				if (r.contains(mo) && pat.isActionTime(true)){
					if (isInvalidPress){
						return false;
					}
					return true;
				} else {
					isInvalidPress = true;
				}
			} else {
				pat.isActionTime(false);
				isInvalidPress = false;
			}
			return false;
		}
	}
	public static class ScriptExtraPropertyHandler {
		private StringTokenizer current;
		private int currentPos;
		private int lastSpacePos = -1;
		private int cLine = 0;
		private boolean skipFirstLine = true;
		private ArrayList<String> classNestStack = new ArrayList(); //not valid to have too deeply nested classes
		private ArrayList<ScriptPropertyCustomizable> classObjectStack = new ArrayList();
		private int classNestStackSize = 0;
		public void setOn(String line){
			if (line==null){
				classNestStackSize = 0;
				cLine = 0;
				skipFirstLine = true;
				classNestStack.clear();
				classObjectStack.clear();
				return;
			}
			//Line end:
			if (classNestStackSize>0){
				//We have to propagate the line ends through:
				for(int k = 0; k < classNestStackSize; k++){
					classObjectStack.get(k).appendToken(""+TaiScriptEditor.LINE_END_SUBCHAR);		
				}
			}
			current = new StringTokenizer(line," "+TaiScriptEditor.LINE_END_SUBCHAR+'\t'+new String(TaiScriptLanguage$Rules.reservedPunctuation)+"",true);
			currentPos = 0;
			if(skipFirstLine){
				skipFirstLine = false;
			} else {
				cLine++;
			}
		}
		/**
		 * Marker interface, a script property that can be used as a 
		 * simulation starting point (spellcards or levels, essentially)
		 */
		public static interface Timed_ScriptPropertyCustomizable {
			public float getTimef();
			public void setTime(String time, float timeF);
			public String getTime();
		}
		public abstract class ScriptPropertyCustomizable {
			public final int HI_LITE_ALPHA = 100;
			public int reallyStartChar;
			public int startChar;
			public int endChar;
			public int endLinePos;
			public int startingLine, endLine = -1;
			public void setLocation(int start, int end){
				startChar = start;
				endChar = end;
			}
			private String name;
			private StringBuffer myVal = new StringBuffer("");
			/**
			 * Does not check that name is a valid instanceName!
			 */
			public void setInstanceName(String name, int startingLine){
				this.name = name;
				this.startingLine = startingLine;
			}
			public String getInstanceName() {
				return name;
			}
			public abstract String getType();
			public abstract int highlightColor(PApplet g);
			public MouseChecker mym;
			public abstract void pressed(EditorScreen es);
			public void appendToken(String instanceName) {
				myVal.append(instanceName);
			}
			public String getTextBody() {
				return myVal.toString();
			}
			public void trimFromEnd(int i) {
				myVal.delete(myVal.length()-i, myVal.length());
			}
			public void setTypeBegin(int lastSpacePos) {
				reallyStartChar = lastSpacePos;
			}
			public void setLineEnd(int line, int currentPos) {
				endLine = line;
				endLinePos = currentPos;
			}
		}
		public class BulletCustomizableProperty extends ScriptPropertyCustomizable{
			public void pressed(EditorScreen es){
				EditorScreen.ImageSelectorDialog isd = es.new ImageSelectorDialog(
						es, 
						new Rectangle2D.Float(.1f,0,.8f,1f),
 						this.getInstanceName(),
						getType(),0);
			}
			public String getType() {return TaiScriptLanguage$Constants.BULLET;}
			public int highlightColor(PApplet g) {
				return g.color(255,0,0,HI_LITE_ALPHA);
			}
		}
		public class BossCustomizableProperty extends ScriptPropertyCustomizable {
			public String getType() {return TaiScriptLanguage$Constants.BOSS;}
			public void pressed(EditorScreen es){
				EditorScreen.ImageSelectorDialog isd = es.new ImageSelectorDialog(
						es, 
						new Rectangle2D.Float(.1f,0,.8f,1f),
						this.getInstanceName(),
						getType(),0);
			}
			public int highlightColor(PApplet g) {
				return g.color(100,0,130,HI_LITE_ALPHA);
			}
		}
		public class PlayerCustomizableProperty extends ScriptPropertyCustomizable {
			public String getType() {return TaiScriptLanguage$Constants.PLAYER;}
			public void pressed(EditorScreen es){
				EditorScreen.ImageSelectorDialog isd = es.new ImageSelectorDialog(
						es, 
						new Rectangle2D.Float(.1f,0,.8f,1f),
						this.getInstanceName(),
						getType(),0);
			}
			public int highlightColor(PApplet g) {
				return g.color(200,200,50,HI_LITE_ALPHA);
			}
		}

		public class GlobalsCustomizableProperty extends ScriptPropertyCustomizable {
			public String getType() {return TaiScriptLanguage$Constants.GLOBAL;}
			public void pressed(EditorScreen es){

			}
			public int highlightColor(PApplet g) {
				return g.color(255,0,0,HI_LITE_ALPHA);
			}
		}		
		public class BackgroundCustomizableProperty extends ScriptPropertyCustomizable {
			public String getType() {return TaiScriptLanguage$Constants.BACKGROUND;}
			public void pressed(EditorScreen es){

			}
			public int highlightColor(PApplet g) {
				return g.color(100,100,100,HI_LITE_ALPHA);
			}
		}
		public class AccessoryCustomizableProperty extends ScriptPropertyCustomizable {
			public String getType() {return TaiScriptLanguage$Constants.ACCESSORY;}
			public void pressed(EditorScreen es){
				EditorScreen.ImageSelectorDialog isd = es.new ImageSelectorDialog(
						es, 
						new Rectangle2D.Float(.1f,0,.8f,1f),
						this.getInstanceName(),
						getType(),0);
			}
			public int highlightColor(PApplet g) {
				return g.color(100,150,0,HI_LITE_ALPHA);
			}
		}
		public class LevelCustomizableProperty extends ScriptPropertyCustomizable implements Timed_ScriptPropertyCustomizable{
			public String getType() {return TaiScriptLanguage$Constants.LEVEL;}
			public void pressed(EditorScreen es) {
				// TODO Auto-generated method stub

			}
			public int highlightColor(PApplet g) {
				return g.color(180,255,0,HI_LITE_ALPHA);
			}

			private String time = "";
			private float timeF = 0;
			public String getTime() {
				return time;
			}
			public float getTimef() {
				return timeF;
			}
			public void setTime(String time, float timeF) {
				this.timeF = timeF;
				this.time = time;
			}
		}
		public class TemplateProperties extends ScriptPropertyCustomizable {
			public String getType() { return TaiScriptLanguage$Constants.TEMPLATE; }
			public void pressed(EditorScreen es) {
				// TODO Auto-generated method stub

			}
			public int highlightColor(PApplet g) {
				return g.color(255,255,255,HI_LITE_ALPHA);
			}
		}
		public class SpellCardProperties extends ScriptPropertyCustomizable implements Timed_ScriptPropertyCustomizable{
			public String getType() { return TaiScriptLanguage$Constants.SPELLCARD; }
			public void pressed(EditorScreen es) {
				// TODO Auto-generated method stub
			}
			public int highlightColor(PApplet g) {
				return g.color(150,0,100,HI_LITE_ALPHA);
			}
			private String time = "";
			private float timeF = 0;
			public String getTime() {
				return time;
			}
			public float getTimef() {
				return timeF;
			}
			public void setTime(String time, float timeF) {
				this.timeF = timeF;
				this.time = time;
			}
		}
		public ScriptPropertyCustomizable nextCustomizableWord(){
			String lastToken = "";
			boolean inSpaceLoop = false; //workaround
			boolean goUntilEndOfLine = false;
			while(current.hasMoreTokens()){
				String instanceName = current.nextToken();
				if (instanceName.equals("}")){ //ending the last class
					if (classNestStackSize>0){
						classObjectStack.get(classNestStackSize-1).setLineEnd(cLine,currentPos);
						classNestStackSize--;
						classObjectStack.remove(classNestStackSize);
						classNestStack.remove(classNestStackSize);
					}
				} else if (instanceName.equals(""+TaiScriptEditor.LINE_END_SUBCHAR)){
					//Taking care of these myself.
				} else {
					if (classNestStackSize>0){
						classObjectStack.get(classNestStackSize-1).appendToken(instanceName);
					}
				}
				if (instanceName.equals("=") || instanceName.equals("\\")){
					goUntilEndOfLine = true;
				}
				if (goUntilEndOfLine){
					continue; //no more parsing to do after = encountered
				}
				if (instanceName.equals(" ")){
					inSpaceLoop = true;
					currentPos++;
					continue;
				}
				if (lastToken.equals("+")){
					inSpaceLoop = true;
				}
				String got = lastToken;
				//THE REMAINDER OF THE CODE DEALS ENTIRELY WITH
				//FINDING CLASS STATEMENTS .  (SPC's)
				int start = currentPos+got.length();
				int end = start+instanceName.length();
				ScriptPropertyCustomizable spc = null;
				if (inSpaceLoop){
					if (got.equals(TaiScriptLanguage$Constants.BULLET)){
						spc = new BulletCustomizableProperty();
					} else if (got.equals(TaiScriptLanguage$Constants.BOSS)){
						spc = new BossCustomizableProperty();
					} else if (got.equals(TaiScriptLanguage$Constants.LEVEL)){
						spc = new LevelCustomizableProperty();
					} else if (got.equals(TaiScriptLanguage$Constants.ACCESSORY)){
						spc = new AccessoryCustomizableProperty();
					} else if (got.equals(TaiScriptLanguage$Constants.SPELLCARD)){
						spc = new SpellCardProperties();
					} else if (got.equals(TaiScriptLanguage$Constants.PLAYER)){
						spc = new PlayerCustomizableProperty();
					} else if (got.equals(TaiScriptLanguage$Constants.GLOBAL)){
						spc = new GlobalsCustomizableProperty();
					} else if (got.equals(TaiScriptLanguage$Constants.BACKGROUND)){
						spc = new BackgroundCustomizableProperty();
					} else if (got.equals("+")){
						spc = new TemplateProperties();
					}//to do, more.
					if (spc!=null){
						spc.setTypeBegin(lastSpacePos);
					}
				} else {
					lastSpacePos = currentPos;
				}
				currentPos = start;
				try {
					if (spc!=null){
						//Form true instanceName:
						String trueName = "";
						for(int p = 0; p < classNestStackSize; trueName+=classNestStack.get(p)+".", p++);
						trueName+=instanceName;

						if (!TaiScriptLanguage$Rules.isValidInstanceName(instanceName)){
							continue; //not valid.
						}

						spc.setLocation(start, end);
						//Remove that many from the buffer:
						if (classNestStackSize>0){
							try {
								classObjectStack.get(classNestStackSize-1).trimFromEnd(end-spc.reallyStartChar);
							} catch(Throwable e){
								//System.out.println("Warning: bug 29483");
							}
						}

						classNestStackSize++;

						classNestStack.add(instanceName);
						classObjectStack.add(spc);

						spc.setInstanceName(trueName,cLine); //by induction, this is valid.
						currentPos = end;
						return spc;
					}
				} finally {
					inSpaceLoop = false;
					lastToken = instanceName;
				}
			}
			return null;	
		}
	}
}