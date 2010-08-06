package BulletGame$1;


import static BulletGame$2.BulletPattern.X2;
import static BulletGame$2.BulletPattern.X3;

import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUtessellator;
import javax.swing.JFrame;

import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import BulletGame$2.BulletAccessory;
import BulletGame$2.BulletBackground;
import BulletGame$2.BulletBoss;
import BulletGame$2.BulletHellEnv;
import BulletGame$2.BulletLevel;
import BulletGame$2.BulletPath;
import BulletGame$2.BulletPattern;
import BulletGame$2.BulletPlayer;
import BulletGame$2.BulletRelativitable;
import BulletGame$2.BulletSpellCard;
import BulletGame$2.GraphicsHolder;
import BulletGame$2.GraphicsHolderParser;
import BulletGame$2.NearestNeighbor;
import BulletGame$2.BulletPattern.BulletPatternFirableDescriptor;
import BulletGame$2.BulletRelativitable.PositionRelativitator;
import BulletGame.Benchmarking.BulletGameBenchmark;
import Deployments.BulletHell;
import TaiGameCore.MultiExpression;
import TaiGameCore.RelativelyTimed;
import TaiGameCore.TaiBenchmark;
import TaiGameCore.TaiProgress;
import TaiGameCore.GameSprite.GameGraphic;
import TaiGameCore.RelativelyTimed.TimeSource;

import com.iabcinc.jmep.XExpression;

import ddf.minim.AudioPlayer;
import ddf.minim.AudioSample;
import edu.umd.cfar.lamp.mpeg1.Mpeg1File;
import edu.umd.cfar.lamp.mpeg1.Mpeg1VideoStream;
import edu.umd.cfar.lamp.mpeg1.MpegException;
import edu.umd.cfar.lamp.mpeg1.video.VideoDecoder.VideoImage;

public abstract class BulletGame$1Engine$L4$1$BulletGameBulletSimulation extends BulletGame$1Engine$L3$2$BulletGameBenchmarks{
	public BulletGame$1Engine$L4$1$BulletGameBulletSimulation(JFrame holder, PApplet hold) {
		super(holder, hold);
	}
	public class GameScreen extends BulletGameScreen{
		public class BulletGameGamePlay {
			public BulletGameGamePlay(boolean playWholeLevel){
				bhip = new BulletHellInstancePlayer();
				env = new BulletHellEnv(BulletGame$1Engine$L4$1$BulletGameBulletSimulation.this);
				ParseResult crashes = bhip.createFromBHI(bhi,env,playWholeLevel);
				if (crashes.toRetCrashes.size()>0){
					wantsBack = true;
				}
			}
			private RelativelyTimed clock = new TimeSource(){
				public double time() {
					return 0;
				}
			};

			private AudioPlayer music;
			private AudioSample[] audioSamples;
			/**
			 * VIDEO BACKGROUND COMPONENTS
			 */
			private Mpeg1VideoStream[] videoStreams;
			/**
			 * Whether the background is still or a movie, the image is loaded
			 * into this PImage.
			 */
			private boolean[] backgroundIsVideo;
			private PImage[] background_gotStillFrame;
			private boolean[] videoStream_seeking;
			private int[] videoStream_frames;
			/////
			private PImage skinBGImage;
			private BulletHellText SkinText;
			private BulletHellEnv env;
			private boolean wantsBack;
			private int loading = 0;
			public void draw(){
				/*
				if (g.frameRate < 20){
					wantsBack = true;
				}
				 */
				if (wantsBack){
					backScreen_0();
				} else {
					if (loading==0){
						//Begin loading
						loading = 1;
						new Thread(){
							public void run(){
								loadResources();
								loading = 2;
							}
						}.start();
					}
					if (loading==1){
						renderLoadProgress();
					} else {
						if (loading==3){
							loading = 4;
							startLevel();
						}
						if (loading==2){
							loading = 3;
							stopRenderLoadProgress();
						}
						bulletHellGame();
						if (bgbs_loaded!=null){
							//benchmark!
							final int numSamples = 100;
							if (runningBenchmark_sample < numSamples){
								if (runningBenchmark==null){
									runningBenchmark = new BulletGameBenchmark("");
									bgbs_loaded.addBenchmark(runningBenchmark);
									runningBenchmark.setupFor(numSamples);
									runningBenchmark.gameversionNumber = VERSION_NUMBER;
								}
								if ((bulletMethodBegins - lastSampleTaken)>.1){
									lastSampleTaken = bulletMethodBegins;
									runningBenchmark.benchmark(
											runningBenchmark_sample++,
											bulletMethodBegins,
											numBulletsRendered,
											g.frameRate
									);
								}
								//Ok, we keep going
							} else {
								//BREAK!
								backScreen_0();
							}
						}
					}
				}
			}
			private BulletGameBenchmark runningBenchmark;
			private int runningBenchmark_sample = 0;
			private float lastSampleTaken = 0;
			private float bulletMethodBegins;
			private double[][] full_corners = new double[4][6];
			private double[][] full_corners_bg = new double[4][6];
			private double[][] corners = new double[4][6];
			private double[][] corners_trans = new double[4][6];
			private double[][] corners_fudge = new double[4][3];
			private Polygon corners_polygon = null;
			private tessellCallBack tsb;
			private static final int corners_polygon_res = 100000;
			private void rtate(float[] point, float t){
				float x = point[0];
				float y = point[1];
				float ct = (float)Math.cos(t);
				float st = (float)Math.sin(t);
				point[0] = ct*x+st*y;
				point[1] = -st*x+ct*y;
			}
			private void rtate(double[] point, float t){
				double x = point[0];
				double y = point[1];
				double ct = Math.cos(t);
				double st = Math.sin(t);
				point[0] = ct*x+st*y;
				point[1] = -st*x+ct*y;
			}

			/**
			 * Run on every SUBSEQUENT transition. Not the first~~!!
			 */
			public void nextSpellCard() throws StageEndException{ 
				//Cleanup firables:
				for(int k : bhip.currentPart.firable){
					cleanupBullets(k, false);
				}
				bhip.loadNext(bulletMethodBegins);
				openBackground();//This is a good time as any to open the bg.
			}
			private void cleanupBullets(int k, boolean onDeath){
				if (bhip.subfirables[k]!=null){
					for(int q : bhip.subfirables[k]){
						cleanupBullets(q, onDeath);
					}
				}
				//SPAWN SWEEPERS
				BulletPattern bp = bhip.firables[k];
				for(int myIndex = 0; myIndex < bp.states_indexCt.length; myIndex++){
					if (bp.states_indexCt[myIndex]>=0){
						//We have to do a 'new' because we're getting rid of this bullet now.
						sweepBullet(bp,myIndex, onDeath);
					}
				}
			}
			private void sweepBullet(BulletPattern bp, int myIndex, boolean onDeath){
				if (!onDeath){
					BulletPatternFirableDescriptor bpfd = bp.new BulletPatternFirableDescriptor();
					float[] source = new float[]{
							X2(bp.states_delta_x,myIndex,bp.states_delta_pos[myIndex]),
							X2(bp.states_delta_y,myIndex,bp.states_delta_pos[myIndex])
					};
					boolean fired = bhip.firables[bhip.deadBulletFirable].handleFirings(
							bulletMethodBegins,
							bulletMethodBegins,
							source,
							null,
							currentPlayerPosition,
							level_rotation,
							BulletPattern.ENEMY_VALENCE,
							bhip,
							bpfd,
							true);
					/*
					bulletHellGame_GamePart$Bullets(bhip.deadBulletFirable,bulletMethodBegins,BulletPattern.ENEMY_VALENCE,true,
							new StaticSourcePath(source),bpfd);
							*/
				}
				bp.killBullet(myIndex);
			}
			public void openBackground(){
				openBackground(-1);
			}
			private int currently_open_background = -1;
			public void openBackground(int which){
				int whichSc = which==-1?bhip.currentPart.mySpellCard:which;
				if (currently_open_background==whichSc){
					return;
				} else {
					if (currently_open_background>=0){
						endVideo(currently_open_background); //Clear the last one
					}
				}
				currently_open_background = whichSc;

				BulletSpellCard bsc = bhip.bscs[whichSc];
				if(bhip.subBackgrounds[whichSc]!=null){
					backgroundLoop: for(int bg : bhip.subBackgrounds[whichSc]){
						Mpeg1VideoStream made = null;
						BulletBackground bgI = bhip.bgs[bg];
						if (videoStreams[bg]==null){
							if (!bgI.background.equals(BulletBackground.NO_BACKGROUND)){
								String baseCreatedFilesDirectory = FILE_SYSTEM.getBaseCreatedFilesDirectory();
								String target = baseCreatedFilesDirectory+File.separator+"bg"+bg+".mpeg";
								//Load it!
								try {
									Mpeg1File mF = new Mpeg1File(new File(target));
									made = mF.getVideoStream();
									made.seek(0);
								} catch (Throwable e) {
									//Well, this may be ok. Try to decode it as an image.
									boolean isStillImage = true;
									try {
										background_gotStillFrame[bg] = g.loadImage(target,"unknown");
										if (background_gotStillFrame[bg] == null){
											isStillImage = false;
										}
									} catch (Throwable d){
										isStillImage = false;
									}
									if (isStillImage){
										backgroundIsVideo[bg] = false;
										continue backgroundLoop; 
									}
									background_gotStillFrame[bg] = null;
									// TODO Auto-generated catch block
									e.printStackTrace();
									throw new RuntimeException("Bad movie");
								}
								backgroundIsVideo[bg] = true;
							}
							videoStreams[bg] = made;
						}
						made = videoStreams[bg];
						//Make it the current?
						videoStream_frames[bg] = 0;
					}
				}
			}
			public void endVideos(){
				videoStreams = null;
				background_gotStillFrame = null;
			}
			public void endVideo(int currently_open_background2){
				if (bhip.subBackgrounds[currently_open_background2]!=null){
				for(int bg : bhip.subBackgrounds[currently_open_background2]){
					//videoStreams[bg] = null; Leave the thing downloaded.
					videoStream_frames[bg] = 0;
					videoStream_seeking[bg] = false;
					background_gotStillFrame[bg] = null; //Doesn't hurt noone.
				}
				}
			}

			/**
			 * Run on a separate thread. You may throw a runtimeException,
			 * and the loading screen will hang in a certain state, the user
			 * will have to press escape.
			 */
			public TaiProgress loadResources_Progress;
			public void loadResources(){
				TaiProgress pr = loadResources_Progress = new TaiProgress();
				{pr.mark("Interpreting", 1);
				//Find the first level:
				try { 	
					bhip.loadNext(bhip.simulation_offset);
				} catch (StageEndException e) {
					wantsBack = true;
					return;
				}
				pr.markDone();}

				//Level music:
				BulletLevel bl = bhip.blevels[bhip.currentPart.myLevel];
				if(bl.music.equals(BulletLevel.NO_AUDIO)){
				} else {
					{pr.mark("Locating Music", 1);

					//Try to see if it exists?
					//K.
					music = AUDIO.loadFile(bl.music);
					if (music!=null){
						pr.updateState(.5f);
						music.cue((int)(bhip.simulation_offset*1000));
					} else {
						//Failure.
						pr.fail();
						throw new RuntimeException("Load music failed.");
					}

					pr.markDone();}
				}
				{pr.mark("Getting 'Skin'", 1);
				skinBGImage = FILE_SYSTEM.getImg(bl.skin);
				SkinText = new BulletHellText(null,10,"RursusCompactMono-16.vlw");
				SkinText.setTextScale(1.4f);
				pr.markDone();}
				//Does NOT return null.
				{pr.mark("Downloading Backgrounds", bhip.bgs.length);

				//Donload backgrounds:
				for(int k = 0; k < bhip.bgs.length; k++){
					BulletBackground BgB = bhip.bgs[k];
					if (!BgB.background.equals(BulletBackground.NO_BACKGROUND)){
						String baseCreatedFilesDirectory = FILE_SYSTEM.getBaseCreatedFilesDirectory();
						String target = baseCreatedFilesDirectory+File.separator+"bg"+k+".mpeg";
						try {
							FILE_SYSTEM.downloadFile(target,new URL(BgB.background));
						} catch (MalformedURLException e) {
							e.printStackTrace();
							throw new RuntimeException("Blachac");
						} catch (IOException e) {
							e.printStackTrace();
							throw new RuntimeException("Blachac");
						}
					}
					pr.updateState(k);
				}

				pr.markDone();}
				//Open first background
				{pr.mark("Loading Background", 1);


				//Support "background=still:image" to represent a nonvideo. Still download in advance, though.
				//Should also support tiling?
				videoStreams = new Mpeg1VideoStream[bhip.bgs.length];
				backgroundIsVideo = new boolean[bhip.bgs.length];
				background_gotStillFrame = new PImage[bhip.bgs.length];
				videoStream_seeking = new boolean[bhip.bgs.length];
				videoStream_frames = new int[bhip.bgs.length];
				for(int k = 0; k < bhip.bscs.length; k++){
					openBackground(k);
					pr.updateState(k);
				}

				pr.markDone();}

				//Start bulletthreads
				for(BulletPattern bp : bhip.firables){
					bp.startBackgroundThread();
				}

				//Load sounds

				audioSamples = bhip.bGlob.getAudioSamples(FILE_SYSTEM, AUDIO);
			}

			/**
			 * Deletes temporaries
			 */
			public void unloadResources(){
				File tmpDir = new File(FILE_SYSTEM.getBaseCreatedFilesDirectory());
				File[] listFiles = tmpDir.listFiles(new FilenameFilter(){
					public boolean accept(File dir, String name) {
						return name.endsWith(".mpeg");
					}
				});
				for(File k : listFiles){
					k.delete();
				}
				tmpDir.delete();
			}
			private TaiTextBox renderProgress;
			private int numLinesShown = 0;
			public void renderLoadProgress(){
				final int numLines = 8;
				g.background(0,0,0);
				if (loadResources_Progress==null){
					return;
				}
				if (renderProgress==null){
					renderProgress = new TaiTextBox(new Rectangle2D.Float(0,0,1,1),numLines);
					renderProgress.ets.tse.clearBlanks();
				}
				if (numLinesShown < loadResources_Progress.allTasks.size()){
					numLinesShown++;
				}
				for(int k = 0; k < numLines; k++){
					int index = PApplet.max(0,loadResources_Progress.allTasks.size()-1-numLines)+k;
					if (index >= loadResources_Progress.allTasks.size()){
						renderProgress.setTextRow("Press Escape to Return", k);
						break;
					}
					renderProgress.setTextRow(loadResources_Progress.allTasks.get(k), k);
					g.stroke(255,255,255);
					g.fill(255,255,255,100);
					float doneW = loadResources_Progress.progresses.get(k)/(1+loadResources_Progress.expectedMax.get(k));
					if (loadResources_Progress.progresses.get(k) < 0){
						//Failure.
						g.fill(255,0,0,100);
						doneW = 1;
					}
					g.rect(0,1f/numLines*k,doneW,1f/numLines);
				}
				g.fill(255,255,255,255);
				screen2D();
				renderProgress.draw();
			}
			public void stopRenderLoadProgress(){
				if (renderProgress!=null){
					renderProgress.cleanup();
					renderProgress = null;
				}
			}
			public void startLevel(){
				//Start at 0!
				if (music!=null){
					music.play();
				}
				clock = new TimeSource(){
					boolean started = false;
					double start = GAME_TIME.time();
					public double time() {
						if (music==null){
							return GAME_TIME.time()-start+bhip.simulation_offset;
						}
						int mt = music.position(); //Music position takes care of offset
						/*
						if (mt>0){
							if (!started){
								started = true;
								oTime = System.nanoTime();
							}
							return (System.nanoTime()-oTime)/1e9;
						}
						 */
						return mt/1000.;
					}
				};
			}

			private float levelW_new,levelH_new,centerLevelX,centerLevelY,level_rotation;
			private float ARCorrectedWidth;
			private Rectangle2D.Float gameAreaDim = new Rectangle2D.Float(0,0,1,1);
			private BulletHellPositionRelativitator pr = new BulletHellPositionRelativitator();
			private int numBulletsRendered;
			public void bulletHellGame(){
				TaiBenchmark.bench(this);
				numBulletsRendered = 0;
				try {
					//Everything.
					bulletMethodBegins = clock.timef();

					//Screen size?
					/**
					 * So, changing the coordinate system does:
					 * 1) Immediately affects the clipping bound
					 * 2) Immediately affects the boss position, 
					 * 3) Immediately affects the position of all graphics
					 * 4) Immediately affects the movement of player
					 * 5) Affects only NEW bullets. 
					 * 
					 * So... yeah
					 */
					try {
						BulletLevel bblevel = bhip.blevels[bhip.currentPart.myLevel];
						levelW_new = 1f;
						levelH_new = 1f;
						float levelW_prior = 1f;
						float levelH_prior = 1f;
						String[] lvlW = bblevel.level_w.split(",");
						String[] lvlH = bblevel.level_h.split(",");
						int current_event = bblevel.current_event;
						current_event = Math.max(0,Math.min(current_event,lvlW.length-1));
						if (current_event < lvlW.length){
							levelW_prior = new Float(lvlW[current_event]);
							levelW_new = new Float(lvlW[current_event]);
						}
						if (current_event>0 && current_event < lvlW.length){
							levelW_prior = new Float(lvlW[current_event-1]);
						}
						//redo for height
						current_event = bblevel.current_event;
						current_event = Math.max(0,Math.min(current_event,lvlH.length-1));
						if (current_event < lvlH.length){
							levelH_prior = new Float(lvlH[current_event]);
							levelH_new = new Float(lvlH[current_event]);
						}
						if (current_event>0 && current_event < lvlH.length){
							levelH_prior = new Float(lvlH[current_event-1]);
						}
						//DO INTERPOLATION HERE. TODO!
						//blah blah blah. Ok, the actual is in levelH_new.
						//Do we override with bsc?
						BulletSpellCard bsc = bhip.bscs[bhip.currentPart.mySpellCard];
						env.t = bulletMethodBegins - bsc.start_time;
						env.tb = bhip.getBeatFromSongTime(bulletMethodBegins) - bhip.getBeatFromSongTime(bsc.start_time);
						MultiExpression[] scExpr = bhip.bossSpellcardsExpressionCache[bhip.currentPart.mySpellCard];
						centerLevelX = .3f;
						centerLevelY = .5f;
						if(scExpr[0]!=null){
							levelW_new = scExpr[0].evaluatef();
						}
						if(scExpr[1]!=null){
							levelH_new = scExpr[1].evaluatef();
						}
						if (scExpr[2]!=null){
							centerLevelX = levelW_new/2 + scExpr[2].evaluatef();
						} else {
							if (centerLevelX < levelW_new/2){
								centerLevelX = levelW_new/2;
							}
						}
						if (scExpr[3]!=null){
							centerLevelY = levelH_new/2 + scExpr[3].evaluatef();
						} else {
							if (centerLevelY < levelH_new/2){
								centerLevelY = levelH_new/2;
							}
						}
						level_rotation = scExpr[4].evaluatef();

						//TOP LEFT, TOP RIGHT, BOTTOM RIGHT, BOTTOM LEFT
						corners[0][0] = corners[3][0] = -levelW_new/2;
						corners[1][0] = corners[2][0] = levelW_new/2;
						corners[0][1] = corners[1][1] = -levelH_new/2;
						corners[2][1] = corners[3][1] = levelH_new/2;
						//
						full_corners_bg[0][0] = full_corners_bg[3][0] = full_corners[0][0] = full_corners[3][0] = 0;//+corners[0][0]*4;
						full_corners_bg[1][0] = full_corners_bg[2][0] = full_corners[1][0] = full_corners[2][0] = 1;//+corners[1][0]*4;
						full_corners_bg[0][1] = full_corners_bg[1][1] = full_corners[0][1] = full_corners[1][1] = 0;//+corners[0][1]*4;
						full_corners_bg[2][1] = full_corners_bg[3][1] = full_corners[2][1] = full_corners[3][1] = 1;//+corners[2][1]*4;
						//rotation
						Polygon p = new Polygon();
						for(int k = 0; k < corners.length; k++){
							double[] corner = corners[k];
							rtate(corner,level_rotation);
							corners_fudge[k][0] = corner[0]*1.25f;
							corners_fudge[k][1] = corner[1]*1.25f;
							corners_trans[k][0] = corner[0]*.96f;
							corners_trans[k][1] = corner[1]*.96f;
							corner[0] += centerLevelX;
							corners_fudge[k][0] += centerLevelX;
							corners_trans[k][0] += centerLevelX;
							corner[1] += centerLevelY;
							corners_fudge[k][1] += centerLevelY;
							corners_trans[k][1] += centerLevelY;
							//Also, put the bounding box up a little:
							corner[2] = 0;
							corners_trans[k][2] = 0;
							full_corners[k][2] = 0;
							full_corners_bg[k][2] = 0;
							for(int i = 3; i < 6; i++){
								corner[i] = 0;
								corners_trans[k][i] = 1;
								full_corners[k][i] = 0;
								full_corners_bg[k][i] = 0;
							}
							p.addPoint((int)(corners_fudge[k][0]*corners_polygon_res),(int)(corners_fudge[k][1]*corners_polygon_res));	
						}
						corners_polygon = p;
					} catch (Throwable e){
						e.printStackTrace();
					}


					//BEGIN GL PORTION
					screen2D4GL(currentViewPortWidth, currentViewPortHeight);
					GL2 gl = ((PGraphicsOpenGL)g.g).gl;
					GLU glu = ((PGraphicsOpenGL)g.g).glu;

					//If we are at a too-wide aspect, squish and translate right.
					ARCorrectedWidth = currentViewPortHeight*bhip.bGlob.getAspectRatio(bhip.bGlob.AspectRatio);
					if (currentViewPortWidth > ARCorrectedWidth){
						float delta = currentViewPortWidth-ARCorrectedWidth;
						gameAreaDim.x = (delta/2)/currentViewPortWidth;
						gameAreaDim.width = ARCorrectedWidth/currentViewPortWidth;
						gl.glTranslatef(gameAreaDim.x, 0, 0);
						gl.glScalef(gameAreaDim.width,1,1);
					}
					
					//SEE COPY NONGL BELOW
					//If we are at a too-narrow, just do nothing for now.

					openBackground(); //Just ensure that they're open
					renderBackgroundLayers(gl);

					gl.glColor4f(1,1,1,1);


//					gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

					try {
						bulletHellGame_GamePart();
					} catch (StageEndException e){
						wantsBack = true;
					} catch (OutOfMemoryError e){
						//We need to clean something up, fast!
						quickMemoryFree();
						wantsBack = true;
					}


					//tesseleated overlay:
					gl.glColor4f(0,0,0,1);
					if (tsb==null){
						tsb = new tessellCallBack(gl, glu, skinBGImage, (PGraphicsOpenGL)g.g);
					}
					tsb.setDarken(false);
					GLUtessellator tobj = glu.gluNewTess();

					glu.gluTessCallback(tobj, GLU.GLU_TESS_VERTEX, tsb);// glVertex3dv);
					glu.gluTessCallback(tobj, GLU.GLU_TESS_BEGIN, tsb);// beginCallback);
					glu.gluTessCallback(tobj, GLU.GLU_TESS_END, tsb);// endCallback);
					glu.gluTessCallback(tobj, GLU.GLU_TESS_ERROR, tsb);// errorCallback);
					glu.gluTessCallback(tobj, GLU.GLU_TESS_COMBINE, tsb);// errorCallback);

					glu.gluTessBeginPolygon(tobj, null);
					glu.gluTessBeginContour(tobj);
					glu.gluTessVertex(tobj, full_corners[0], 0, full_corners[0]);
					glu.gluTessVertex(tobj, full_corners[1], 0, full_corners[1]);
					glu.gluTessVertex(tobj, full_corners[2], 0, full_corners[2]);
					glu.gluTessVertex(tobj, full_corners[3], 0, full_corners[3]);
					glu.gluTessEndContour(tobj);
					glu.gluTessBeginContour(tobj);
					glu.gluTessVertex(tobj, corners[0], 0, corners[0]);
					glu.gluTessVertex(tobj, corners[1], 0, corners[1]);
					glu.gluTessVertex(tobj, corners[2], 0, corners[2]);
					glu.gluTessVertex(tobj, corners[3], 0, corners[3]);
					glu.gluTessEndContour(tobj);
					glu.gluTessEndPolygon(tobj);


					glu.gluTessBeginPolygon(tobj, null);
					glu.gluTessBeginContour(tobj);
					glu.gluTessVertex(tobj, corners[0], 0, corners[0]);
					glu.gluTessVertex(tobj, corners[1], 0, corners[1]);
					glu.gluTessVertex(tobj, corners[2], 0, corners[2]);
					glu.gluTessVertex(tobj, corners[3], 0, corners[3]);
					glu.gluTessEndContour(tobj);
					glu.gluTessBeginContour(tobj);
					glu.gluTessVertex(tobj, corners_trans[0], 0, corners_trans[0]);
					glu.gluTessVertex(tobj, corners_trans[1], 0, corners_trans[1]);
					glu.gluTessVertex(tobj, corners_trans[2], 0, corners_trans[2]);
					glu.gluTessVertex(tobj, corners_trans[3], 0, corners_trans[3]);
					glu.gluTessEndContour(tobj);
					glu.gluTessEndPolygon(tobj);

					tsb.setDarken(true);
					//Ok, now we need to cover over the ends...
					gl.glPushMatrix();
					gl.glTranslatef(2f-1.5f/currentViewPortWidth, 0f, 0f);
					gl.glScalef(-1f,1f,1f);
					glu.gluTessBeginPolygon(tobj, null);
					glu.gluTessBeginContour(tobj);
					glu.gluTessVertex(tobj, full_corners[0], 0, full_corners[0]);
					glu.gluTessVertex(tobj, full_corners[1], 0, full_corners[1]);
					glu.gluTessVertex(tobj, full_corners[2], 0, full_corners[2]);
					glu.gluTessVertex(tobj, full_corners[3], 0, full_corners[3]);
					glu.gluTessEndContour(tobj);
					glu.gluTessEndPolygon(tobj);
					gl.glPopMatrix();
					gl.glPushMatrix();
					gl.glScalef(-1f,1f,1f);
					glu.gluTessBeginPolygon(tobj, null);
					glu.gluTessBeginContour(tobj);
					glu.gluTessVertex(tobj, full_corners[0], 0, full_corners[0]);
					glu.gluTessVertex(tobj, full_corners[1], 0, full_corners[1]);
					glu.gluTessVertex(tobj, full_corners[2], 0, full_corners[2]);
					glu.gluTessVertex(tobj, full_corners[3], 0, full_corners[3]);
					glu.gluTessEndContour(tobj);
					glu.gluTessEndPolygon(tobj);

					glu.gluDeleteTess(tobj);

					gl.glPopMatrix();

					gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

					screen2D();
					//SEE COPY GL ABOVE:
					if (currentViewPortWidth > ARCorrectedWidth){
						g.translate(gameAreaDim.x, 0, 0);
						g.scale(gameAreaDim.width,1,1);
					}

					//Skin overlays~
					if (bhip!=null){
						bulletHellGame_GamePart$SkinExtras();
					}

					//Return...
					screen2D();
				} catch (XExpression f){
					f.printStackTrace();
					//Need to handle this better.
					wantsBack = true;
				} catch (Throwable e){
					e.printStackTrace();
					wantsBack = true;
				}
			}

			private void renderBackgroundLayers(GL2 gl) throws IOException, MpegException {			
				//Default bg
						gl.glColor4f(0,0,0,1);
						gl.glBegin(GL2.GL_QUADS);
						gl.glVertex3f(0,0,0);
						gl.glVertex3f(1,0,0);
						gl.glVertex3f(1,1,0);
						gl.glVertex3f(0,1,0);
						gl.glEnd();


				gl.glDisable(gl.GL_DEPTH_TEST);
				gl.glBlendFunc(gl.GL_SRC_ALPHA,gl.GL_ONE);
				
				BulletSpellCard bsc = bhip.bscs[bhip.currentPart.mySpellCard];
				if (bhip.subBackgrounds[bhip.currentPart.mySpellCard]!=null){
					for(int bgC : bhip.subBackgrounds[bhip.currentPart.mySpellCard]){
						final int bg = bgC; //lol java.
						boolean doBackgroundVideo = backgroundIsVideo[bg];
						BulletBackground BgB = bhip.bgs[bg];
						if (doBackgroundVideo){ //Background is MP4
							float dTime = bulletMethodBegins-bsc.start_time;
							final int frame = (int)(dTime * BgB.backgroundFPS)%videoStreams[bg].getNumFrames();
							boolean skipModify = false;
							if (background_gotStillFrame[bg]!=null && frame==videoStream_frames[bg]){
								skipModify = true;
							}
							if ((!skipModify || background_gotStillFrame[bg]==null) && videoStream_seeking[bg] == false){
								videoStream_seeking[bg] = true;
								new Thread(){
									{
										this.setPriority(Thread.NORM_PRIORITY-1);
									}
									public void run(){
										try {
											videoStream_frames[bg] = frame;
											videoStreams[bg].seek(videoStream_frames[bg]);
											int[] outputPixels = null;
											if (background_gotStillFrame[bg]!=null){
												outputPixels = background_gotStillFrame[bg].pixels;
											}
											VideoImage image2 = videoStreams[bg].getVideoDecoder().getImageOptimize(outputPixels);
											if (background_gotStillFrame[bg]==null || background_gotStillFrame[bg].width!=image2.width || background_gotStillFrame[bg].height!=image2.height){
												background_gotStillFrame[bg] = new PImage(image2.width,image2.height,PApplet.RGB);
											}
											background_gotStillFrame[bg].pixels = image2.pixels;
											background_gotStillFrame[bg].setModified();
											videoStream_seeking[bg] = false;
										} catch (Throwable e){
											e.printStackTrace();
										}
									}
								}.start();
							}
						}
						doBackgroundVideo = background_gotStillFrame[bg]!=null;
						if(doBackgroundVideo){
							timeLoop: for(int times = 0; times < 2; times++){ //on first pass, render over last frame's transformation.	

								while(true){ //Just allows us to break.
									if (BgB.xFormBuffer==null || BgB.xFormBuffer.length==0){
										if (times==0){
											continue timeLoop; //First frame, there is no last.
										}
									}
									//Hint to increase smoothness
									background_gotStillFrame[bg].blit_resize_smooth = true;
									/** Background ? **/
									gl.glColor4f(1,1,1,1);
									gl.glEnable(GL.GL_TEXTURE_2D);
									PImage texture = background_gotStillFrame[bg];
									int[] texData = ((PGraphicsOpenGL)g.g).bindTexture(texture);
									float wAS = (texture.width-1)/(float)texData[0];
									float hAS = (texture.height-1)/(float)texData[1];
									gl.glPushMatrix();

									int numTilesX = BgB.background_tilex;
									int numTilesY = BgB.background_tiley;

									if (times == 1){
										float tNow = bulletMethodBegins;
										float tBegan = bsc.start_time;
										//Alright, but cap them to 60fps, so the backgrounds are smoother.
										tNow -= tNow % (1f/60);
										tBegan -= tBegan % (1f/60);
										env.t = tNow-tBegan;
										env.tb = bhip.getBeatFromSongTime(tNow) - bhip.getBeatFromSongTime(tBegan);
										BgB.xFormBuffer = new float[]{
												1,0,0,0,
												0,1,0,0,
												0,0,1,0,
												0,0,0,1, 0, 0
										};
										for(int k = 0; k < 16; k++){
											try {
												BgB.xFormBuffer[k] = bhip.backgroundExpressionCache[bg][k].evaluatef();
											} catch (XExpression e){
												//.
											}
										}
										try {
											BgB.xFormBuffer[16] = bhip.backgroundExpressionCache[bg][20].evaluatef();
											BgB.xFormBuffer[17] = bhip.backgroundExpressionCache[bg][21].evaluatef();
										} catch (XExpression e){
											
										}
									}
									gl.glMultMatrixf(BgB.xFormBuffer, 0);
									//Add a translation:
									gl.glTranslatef((BgB.xFormBuffer[16]%1)/numTilesX,
											(BgB.xFormBuffer[17]%1)/numTilesY,0);
									try {
										gl.glColor4f(
												bhip.backgroundExpressionCache[bg][16].evaluatef(),		
												bhip.backgroundExpressionCache[bg][17].evaluatef(),
												bhip.backgroundExpressionCache[bg][18].evaluatef(),
												bhip.backgroundExpressionCache[bg][19].evaluatef()*(times==1?.5f:1f)
										);
									}
									catch (XExpression e){
										gl.glColor4f(1,1,1,1);
									}
									gl.glBegin(GL2.GL_QUADS);
									//Because the texture may not be a power of two,
									//we cannot auto-do the tiling...
									//Go one outside the tiles, so that we can do
									//offsets via the positioning.
									for(int y_tile = -1; y_tile <= numTilesY; y_tile++){
										for(int x_tile = -1; x_tile <= numTilesX; x_tile++){
											for(float[] pos : new float[][]{{0,0},{1,0},{1,1},{0,1}}){
												gl.glTexCoord2f(pos[0]*wAS, pos[1]*hAS);
												//The text coords are correct, but map to the tile:
												gl.glVertex3f((pos[0]+x_tile)/numTilesX,(pos[1]+y_tile)/numTilesY,0);
											}
										}
									}
									gl.glEnd();
									gl.glPopMatrix();
									gl.glDisable(GL.GL_TEXTURE_2D);
									break;
								} //End while true
							} //End "Second Pass"
						}
					}
				}
				gl.glBlendFunc(gl.GL_SRC_ALPHA,gl.GL_ONE_MINUS_SRC_ALPHA);
				gl.glEnable(gl.GL_DEPTH_TEST);
			}
			/**
			 * Calculation intermediaries used for multiprocessing

			private int initial_bullet_multi_length = 2048;
			private float[][] bullet_multi_position = new float[initial_bullet_multi_length][2];
			private float[][] bullet_multi_widths = new float[initial_bullet_multi_length][2];
			private float[][] bullet_multi_rotations = new float[initial_bullet_multi_length][1];
			 */
			/*
			private void multiEnsure(int index){
				if (index < bullet_multi_position.length){
					return; //Good.
				}
				//Uhoh, have to do stuff
				int newlength = bullet_multi_position.length*2;
				float[][] new_bullet_multi_position = new float[newlength][];
				float[][] new_bullet_multi_widths = new float[newlength][];
				float[][] new_bullet_multi_rotations = new float[newlength][];
				//Copy
				int k = 0;
				for(; k < bullet_multi_position.length; k++){
					new_bullet_multi_position[k] = bullet_multi_position[k];
					new_bullet_multi_widths[k] = bullet_multi_widths[k];
					new_bullet_multi_rotations[k] = bullet_multi_rotations[k];
				}
				for(; k < new_bullet_multi_position.length; k++){
					new_bullet_multi_position[k] = new float[bullet_multi_position[0].length];
					new_bullet_multi_widths[k] = new float[new_bullet_multi_widths[0].length];
					new_bullet_multi_rotations[k] = new float[new_bullet_multi_rotations[0].length];
				}
				bullet_multi_position = new_bullet_multi_position;
				bullet_multi_widths= new_bullet_multi_widths;
				bullet_multi_rotations= new_bullet_multi_rotations;
				multiEnsure(index); //just do it again.
			}
			 */
			public void bulletHellGame_GamePart() throws XExpression, StageEndException{
				/**
				 * Mark all the bullet patterns as unvisited
				 */
				for(int i = 0; i < bhip.firables.length; i++){
					bhip.firables[i].visited = false;
				}
				bulletHellGame_GamePart$Player();
				bulletHellGame_GamePart$Boss();
				/**
				 * Does much stuff vvv
				 */
				bulletHellGame_GamePart$Bullets();
			}
			private float[] currentBossPosition = new float[3];
			//A slow way to get the accurate bossposition, at any time.
			private BulletPath bossPosition = new BulletPath(){
				public void getPosition(float[] sourcePos, float time) {
					if (bhip.currentPart.myBoss!=-1){
						BulletBoss currentBoss = bhip.bbosses[bhip.currentPart.myBoss];
						env.t = time-currentBoss.start_time;
						env.tb = bhip.getBeatFromSongTime(time) - bhip.getBeatFromSongTime(currentBoss.start_time);
						try {
							bhip.calculateXPosition(sourcePos, bhip.bossExpressionCache[bhip.currentPart.myBoss]);
						} catch (XExpression e) {
							e.printStackTrace();
						}
						pr.relativity(sourcePos, 0,0, currentBoss);
					}
				}
			};
			private float[] currentPlayerPosition = new float[3];
			//TODO: make this intelligent ... well, there's not much reason to do so.
			private BulletPath playerPosition = new StaticSourcePath(currentPlayerPosition);
			private StaticSourcePath bombPositionLatched = new StaticSourcePath(null);
			private long lastMovementCheck = -1;
			private void bulletHellGame_GamePart$Player() throws XExpression{
				BulletPlayer player = bhip.bplys[bhip.whichPlayer];

				if ((bulletMethodBegins - player.killTime) >= 0){
					reallyKillPlayer();
				}

				float[] cp = currentPlayerPosition;
				cp[0] = player.position[0];
				cp[1] = player.position[1];
				float movementDelta = 0;
				float playerDirectionAdjust = 0;

				if (lastMovementCheck==-1){
					lastMovementCheck = System.nanoTime();
				} else
				{ //Movement
					//Orient the player's position on the level (centerspot)\
					//Orient to level
					cp[0]-=centerLevelX;
					cp[1]-=centerLevelY;

					rtate(cp,-playerDirectionAdjust);
					//Try moving:
					long deltaCheck = System.nanoTime()-lastMovementCheck;
					lastMovementCheck = System.nanoTime();
					float motionSpeed = player.normalSpeed;
					if(requestFocus){
						motionSpeed = player.focusSpeed;
					}
					env.isFocusing = requestFocus ? 1 : 0;
					
					if (player.isMotionTimeAdjusted()){
						motionSpeed *= deltaCheck/1e9;
					}
					if (requestLeft){
						cp[0] -= motionSpeed;
						movementDelta = -motionSpeed;
					}
					if (requestRight){
						cp[0] += motionSpeed;
						movementDelta = motionSpeed;
					}
					if (requestDown){
						cp[1] += motionSpeed;
					}
					if (requestUp){
						cp[1] -= motionSpeed;
					}

					//Rotate in
					rtate(cp,-level_rotation);
					float wS = levelW_new/2 - (float)player.w/2;
					float hS = levelH_new/2 - (float)player.h/2;
					cp[0] = PApplet.constrain(cp[0], -wS, wS);
					cp[1] = PApplet.constrain(cp[1], -hS, hS);
					
					rtate(cp,level_rotation);
					//Unorient
					cp[0]+=centerLevelX;
					cp[1]+=centerLevelY;

					//Feed it back
					player.position[0] = cp[0];
					player.position[1] = cp[1];
					//Draw the player sprite at level_rotation rotation
				}
				
				int playerFrame = calculateAnimationFrame(player,player.animate_I,0,movementDelta);
				float alpha = 255;
				if (playerIsInvincible()){
					alpha = 200+55*g.sin(bulletMethodBegins*20);
				}
				g.fill(255,255,255,alpha);
				GL2 gl = (GL2)((PGraphicsOpenGL) g.g).gl;
				gl.glColor4f(1,1,1,alpha/256.f);
				drawGameGraphicGL(
						player.myGraphic,
						playerFrame,
						currentPlayerPosition[0],
						currentPlayerPosition[1],
						(float)player.w,
						(float)player.h,
						playerDirectionAdjust);
				gl.glColor4f(1,1,1,1);
				
				//Player accessories
				float[] xyrwh_access = new float[5];
				for(int p : bhip.accessoriesForPlayer){
					BulletAccessory ba = bhip.baccess[p];
					ba.calculateExpressions(
							bhip.accessoryExpressionCache[p],
							xyrwh_access);
					pr.relativity(xyrwh_access, currentPlayerPosition[0],currentPlayerPosition[1], ba);
					//draw the accessory. 
					//Accessories current don't implement the graphicsholder correctly, as far as motionframes go.
					int accessoryFrame = calculateAnimationFrame(ba,ba.animate_I,0,0);
					drawGameGraphicGL(
							ba.myGraphic,
							accessoryFrame,
							xyrwh_access[0],
							xyrwh_access[1],
							xyrwh_access[3],
							xyrwh_access[4],
							g.degrees(xyrwh_access[2]));
				}
			}
			private float lastBossX = 0;
			private void bulletHellGame_GamePart$Boss() throws XExpression{
				//DRAW THE BOSS
				g.fill(255,255,255,255);
				//Draw stuff
				if (bhip.currentPart.myBoss!=-1){
					BulletBoss currentBoss = bhip.bbosses[bhip.currentPart.myBoss];
					env.t = bulletMethodBegins-currentBoss.start_time;
					env.tb = bhip.getBeatFromSongTime(bulletMethodBegins) - bhip.getBeatFromSongTime(currentBoss.start_time);
					bhip.calculateXPosition(currentBossPosition, bhip.bossExpressionCache[bhip.currentPart.myBoss]);
					//accessories and bullets can source to bosses or players, but
					//bosses have no source
					pr.relativity(currentBossPosition, 0,0, currentBoss);
					//Collision with player?
					collisionBuffer[0] = currentPlayerPosition[0];
					collisionBuffer[1] = currentPlayerPosition[1];
					if (Collision$Boss(collisionBuffer, currentBoss)){
						killPlayer();
					}
					//DRAW BOSS ACCESSORIES
					float[] xyrwh_access = new float[5];
					for(int p : bhip.currentPart.accessories){
						BulletAccessory ba = bhip.baccess[p];
						ba.calculateExpressions(
								bhip.accessoryExpressionCache[p],
								xyrwh_access);
						//For these, accessories can only go on bosses
						pr.relativity(xyrwh_access, currentBossPosition[0],currentBossPosition[1], ba);
						//draw the accessory. 
						//Accessories current don't implement the graphicsholder correctly, as far as motionframes go.
						int accessoryFrame = calculateAnimationFrame(ba,ba.animate_I,0,0);
						drawGameGraphicGL(
								ba.myGraphic,
								accessoryFrame,
								xyrwh_access[0],
								xyrwh_access[1],
								xyrwh_access[3],
								xyrwh_access[4],
								xyrwh_access[2]);
					}

					int bossFrame = calculateAnimationFrame(currentBoss,currentBoss.animate_I,lastBossX, currentBossPosition[0]);
					lastBossX = currentBossPosition[0];
					drawGameGraphicGL(
							currentBoss.myGraphic,
							bossFrame,
							currentBossPosition[0],
							currentBossPosition[1],
							(float)currentBoss.w,
							(float)currentBoss.h,
							currentBossPosition[2]);
				}
			}
			private void bulletHellGame_GamePart$Bullets() throws XExpression, StageEndException{
				//Boss bullets
				BulletSpellCard currentCard = bhip.bscs[bhip.currentPart.mySpellCard];
				BulletPlayer play = bhip.bplys[bhip.whichPlayer];
				if (currentCard.isFinished(bulletMethodBegins,bhip)){
					nextSpellCard();
				} else {
					//Fire all enemy bullet patterns! this covers both boss-relative
					//and no-boss spellcards. It's surprisingly elegant ~
					for(int e : bhip.currentPart.firable){
						BulletPattern bp = bhip.firables[e];
						boolean fired = bulletHellGame_GamePart$Bullets(e,bp.entire_start_time,BulletPattern.ENEMY_VALENCE, true, bossPosition,null);
					}
				}
				//Player times?
				for(int k : bhip.bpsForPlayer){
					//Need to do this for each
					float playerStartTime = 0;
					//Fire when CTRL pressed
					boolean fire = false;
					float startingTime = 0;
					boolean hasReasonToFire;
					BulletPath firePosition = playerPosition;
					if (bhip.firables[k].isBomb){
						hasReasonToFire = requestBomb;
					} else {
						hasReasonToFire = requestFire;
					}
					if (bhip.firables[k].fireOn_Death){
						if (play.wantsDeathBomb){
							//Allow this shot exactly once, the frame after death.
							//Though, if this is a Bomb, it will last for BombDuration
							hasReasonToFire = true;
						} else {
							//Block, this bullet otherwise
							hasReasonToFire = false;
						}
					}
					if (bhip.firables[k].isBomb){
						//New bomb?
						BulletPattern bpBomb = bhip.firables[k];
						if ((bulletMethodBegins - play.bombTime) < bpBomb.bombDuration){
							fire = true; //Still firing
						} else if (hasReasonToFire && play.bombs > 0){
							//Fire new bomb?
							play.bombTime = bulletMethodBegins;
							play.timeProtect = bulletMethodBegins;
							if (!bhip.firables[k].fireOn_Death){
								//The deathbombs have already been fixed positionse'd.
								play.bombPosition = new float[]{currentPlayerPosition[0],currentPlayerPosition[1]};
							}
							if (bpBomb.bombCost>=0){
								play.bombs-=bpBomb.bombCost;
							} else {
								play.power+=bpBomb.bombCost;
							}
							//First frame.
							fire = true;
							bhip.firables[k].rootSubFirable.reset();
						}
						startingTime = play.bombTime;
						if (play.bombPosition!=null && play.bombPosition.length > 0){
							bombPositionLatched.setPos(play.bombPosition);
							firePosition = bombPositionLatched;
						}
					} else {
						startingTime = playerStartTime;
						fire = hasReasonToFire;
						/**
						 * This shouldn't be too big of a deal, considering we only need
						 * to share this bompposition for the 1st frame after you die. No way
						 * you're shooting a bomb off int ime.
						 */
						if (bhip.firables[k].fireOn_Death){
							if (play.bombPosition!=null && play.bombPosition.length > 0){
								//Probably not a performance problem, but we could cache this object.
								firePosition = new StaticSourcePath(play.bombPosition);
							}
						}
					}					
					bulletHellGame_GamePart$Bullets(k, startingTime, BulletPattern.PLAYER_VALENCE, fire, firePosition, null);
				}
				//We took care of the deathbombs.
				play.wantsDeathBomb = false;
				//Draw!
				for(int k = 0; k < bhip.firables.length; k++){
					bulletHellGame_GamePart$BulletsDraw(k);
				}
			}
			private float[] gunNozzlePosition = new float[2];
			private class StaticSourcePath implements BulletPath{
				public StaticSourcePath(float[] position){
					setPos(position);
				}
				public void setPos(float[] position){
					this.position = position;
				}
				private float[] position;
				public void getPosition(float[] sourcePos, float time) {
					sourcePos[0] = position[0];
					sourcePos[1] = position[1];
				}
			}
			/**
			 * Allows us to define the relativity behavior HERE, while allowing the BulletPattern
			 * to determine the times at which we should evaluate.
			 * This is a CALLBACK.
			 */
			private class Internal_BulletSourcePath implements BulletPath{
				public void setupCallback(int e, BulletPath sub){
					bp = bhip.firables[e];
					this.sub = sub;
					lastTime = -1;
				}
				private BulletPattern bp;
				private BulletPath sub;
				private float lastTime;
				private float[] subHolder = new float[2];
				/**
				 * Slow method for getting a bullet's initial position. This is only used for 
				 * firing new bullets. Bullet flight trajectories are controlled by the
				 * BHMTP1 and BHMTP2 methods, which have much higher throughput.
				 */
				public void getPosition(float[] sourcePos, float time) {
					if (time!=lastTime){
						getPosition0(time);
					}
					sourcePos[0] = subHolder[0];
					sourcePos[1] = subHolder[1];
				}
				private void getPosition0(float time){
					subHolder[0] = 0;
					subHolder[1] = 0;
					switch(bp.getRelativeMode()){
					case BulletRelativitable.SOURCE: case BulletRelativitable.SOURCELOCK:
						//Only in this case will we take the subpath
						sub.getPosition(subHolder, time);
						return;
					case BulletRelativitable.BOTTOM_LEFT_CORNER:
						subHolder[1] = 1;
						break;
					case BulletRelativitable.BOTTOM_RIGHT_CORNER:
						subHolder[0] = 1;
						subHolder[1] = 1;
						break;
					case BulletRelativitable.TOP_RIGHT_CORNER:
						subHolder[0] = 1;
						break;
					case BulletRelativitable.CENTER_SPOT:
						subHolder[0] = .5f;
						subHolder[1] = .5f;
						break;
					default:
						break;
					}
					pr.relativity(subHolder, 0, 0, bp);					
				}
			};
			private Internal_BulletSourcePath bulletHellGame_GamePart$Bullets_RelativeToSource = new Internal_BulletSourcePath();

			public final TaiBenchmark bulletHellGame_GamePart$Bullets = new TaiBenchmark();
			private final boolean bulletHellGame_GamePart$Bullets(int e, float start_time, boolean valence, boolean fireEnabled, BulletPath source,  BulletPatternFirableDescriptor bpfd) {
				BulletPattern bp = bhip.firables[e];
				//recursively fire subbullets
				if (bhip.subfirables[e]!=null && !bp.visited){
					bp.visited = true;
					//In this case, the lines above just (may have) spawned shooters
					//This one is special, we need to impart the firing time upon the individulal bullets...
					//Get the bulletpattern these shooters contain.
					for(int wo = 0; wo < bp.states_indexCt.length; wo++){
						if (bp.states_indexCt[wo]>=0 && bp.states_delta_net[wo]>0){ //alive, and have been for > 1 frame.
							float start_time_sub = bp.states_time[wo];
							if (bp.bulletSubFirables[wo]==null){
								bp.bulletSubFirables[wo] = bp.new BulletPatternFirableDescriptor(wo);
							}
							for(int subPattern : bhip.subfirables[e]){ //Get the sub children of this bp
								BulletPattern sub = bhip.firables[subPattern];
								if (!sub.fireOn_Collision){
									bulletHellGame_GamePart$Bullets(subPattern,start_time_sub, valence, true, bp.bulletSubFirables[wo],bp.bulletSubFirables[wo]);	
								}
							}
						}
					}
				}
				
				//Now fire the main. (Sub bullets always fire.)

				boolean fired = false;

				bulletHellGame_GamePart$Bullets.mark();
				bulletHellGame_GamePart$Bullets_RelativeToSource.setupCallback(e, source);
				
				float offset_bearing = level_rotation;
				if (valence==BulletPattern.PLAYER_VALENCE){
					offset_bearing = 0;
				}
				//This is the actual "fire" routine.
				//We pass in the relative-ized path, 
				fired = bp.handleFirings(bulletMethodBegins,start_time,gunNozzlePosition,bulletHellGame_GamePart$Bullets_RelativeToSource,currentPlayerPosition,offset_bearing,valence,bhip,bpfd,fireEnabled);
				if (fired){
					handleFiredEvent(bp);
				}
				//Done.
				bulletHellGame_GamePart$Bullets.markDone();
				return fired;
			}
			/**
			 * Yeah, this is a dud for now. But it may contain more later?
			 */
			private void handleFiredEvent(BulletPattern bp){
				triggerSound(bp); 
			}
			private void bulletHellGame_GamePart$BulletsDraw(int e) throws XExpression{
				BulletPattern bp = bhip.firables[e];

				//NEW: split into two parts.
				//PART ONE:
				//Multithread boost:
				//Parallell processed.
				bulletHellGame_GamePart_MT_bp = bp;
				bulletHellGame_GamePart_MT_bpi = e;
				bulletHellGame_GamePart_MT_toMov = bhip.bulletExpressionCache[e];
				bulletHellGame_GamePart_MT_gfxE = bhip.bulletGfxExpressionCache[e];
				bulletHellGame_GamePart_MT_colorE = bhip.bulletColorExpressionCache[e];
				bulletHellGame_GamePart_MT_edgeC = bhip.bulletEdgeExpressionCache[e];
				//Multithread:
				bhgEXCEPT[0] = null;
				bhgMTProcess = -1;
				/** Position calculations (EXPRESSIONS) **/
				new bhgMTP1().run();
				//It turns out that multithread was a LOSE here.
				/** Collisions, etc, and Rendering (GL) **/
				if (true){
					bhgMTP2_thread_run();
				} else { //"Join"
					for(int k = 0; k < bp.states_indexCt.length; k++){
						while(k > bhgMTProcess){
							try {
								Thread.sleep(0, 200);
							} catch (InterruptedException f) {
								f.printStackTrace();
							}
						}
					}
				}
				/** Neighbor calculations **/
				bhgMTP3();
				if (bhgEXCEPT[0]!=null){
					throw bhgEXCEPT[0];
				}
			}
			private static final boolean DBG_DISABLE_MT1 = false;
			private static final boolean DBG_DISABLE_MT2 = false;
			private int bhgMTProcess;
			private XExpression[] bhgEXCEPT = new XExpression[1];
			private class bhgMTP1 extends Thread{
				public void run(){
					BulletPattern bp = bulletHellGame_GamePart_MT_bp;
					//multiEnsure(bp.states_indexCt.length-1);
					try {
						int bufferRead = bp.states_indexCt.length/64, bufferCount = 0;

						for(bulletHellGame_GamePart_MT_1_k = 0,bulletHellGame_GamePart_MT_1_k2 = 0; bulletHellGame_GamePart_MT_1_k < bp.states_indexCt.length && bulletHellGame_GamePart_MT_1_k2 < bp.states_delta_x.length; bulletHellGame_GamePart_MT_1_k++, bulletHellGame_GamePart_MT_1_k2+=bp.states_delta_numFrames){
							if (bp.states_indexCt[bulletHellGame_GamePart_MT_1_k]>=0){
								if (!DBG_DISABLE_MT1){
									bulletHellGame_GamePart_MT_1();
								}
							}	
							if (bufferCount++>=bufferRead){
								bhgMTProcess = bulletHellGame_GamePart_MT_1_k; //Allow MT
								bufferCount = 0;
							}
						}
						bhgMTProcess = bulletHellGame_GamePart_MT_1_k; //done part1
					} catch (XExpression e) {
						bhgEXCEPT[0] = e;
					}
				}
			}
			/*
			private void drawMotionTrails(){
				if (false){
					for(int backwardsInTime = bp.states_delta_numFrames-1; backwardsInTime >= 1; backwardsInTime--){
						for(int k = 0; k < bp.states_indexCt.length; k++){
							while(k > bhgMTProcess){
								System.out.println("Waiting...");
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							int val = bp.states_indexCt[k];
							int deltaPos = bp.states_delta_pos[k];
							if (val>=0){
								if(backwardsInTime >= bp.states_delta_net[k]){
									continue;
								}
								int deltaPosShift = deltaPos-backwardsInTime;
								if (deltaPosShift < 0){
									deltaPosShift += bp.states_delta_numFrames;
								}
								if (needsTextureUpdate){
									needsTextureUpdate = false;
									GameGraphic bullet_gameGraphic = getGameGraphicG(bp.myBulletType,bulletFrame);
									bullet_img = getGameGraphic(bullet_gameGraphic);
									int[] bullet_r  = getGameGraphicPixelsGL(bullet_gameGraphic, bullet_img);
									tex_data = predrawGameGraphicGL(bullet_img,bullet_r);
								}
								gl.glColor4f(1,1,1,.2f);
								drawSingleBullet(bp, tex_data, bullet_img, 
										bp.states_delta_widths[k][deltaPosShift], 
										bp.states_delta_heights[k][deltaPosShift], 
										k, 
										deltaPosShift);
							}
						}
					}
				}
			}
			 */
			public void bhgMTP2_thread_run(){
				BulletPattern bp = bulletHellGame_GamePart_MT_bp;
				GL2 gl = ((PGraphicsOpenGL)g.g).gl;
				gl.glDisable(GL.GL_DEPTH_TEST);
				//cache the image lookup:
				boolean oneTexture = true;
				if (bp.animate_I.length!=1){
					oneTexture = false;
				}
				int bulletFrame = calculateAnimationFrame(bp,bp.animate_I,0,0);
				boolean needsTextureUpdate = true;
				PImage bullet_img = null;
				float[] tex_data = null;
				//Motion trails?
				if (false){
					//drawMotionTrails();
				}
				gl.glColor4f(1,1,1,1);
				for(bulletHellGame_GamePart_MT_2_k = 0,
						bulletHellGame_GamePart_MT_2_k2 = 0; 
				bulletHellGame_GamePart_MT_2_k < bp.states_indexCt.length 
				&& bulletHellGame_GamePart_MT_2_k2 < bp.states_delta_x.length;
				bulletHellGame_GamePart_MT_2_k++, 
				bulletHellGame_GamePart_MT_2_k2+=bp.states_delta_numFrames){
					int k = bulletHellGame_GamePart_MT_2_k;
					while(k > bhgMTProcess){
						//Check that the other thread didn't bug out
						if (bhgEXCEPT[0]!=null){
							return; 
						}
						try {
							Thread.sleep(1);
						} catch (Throwable e){

						}
					}
					if (DBG_DISABLE_MT2) continue;


					if (!oneTexture){
						int deltaPos = bp.states_delta_pos[k];
						float statesDx = X2(bp.states_delta_x,k,deltaPos);
						deltaPos--;
						if (deltaPos < 0){
							deltaPos += bp.states_delta_numFrames;
						}
						float statesLast = X2(bp.states_delta_x,k,deltaPos);
						statesDx = statesDx-statesLast;
						int newFrame = calculateAnimationFrame(
								bp,
								bp.animate_I,
								0,
								statesDx);
						if (newFrame!=bulletFrame){
							needsTextureUpdate = true;
						}
					}
					if (needsTextureUpdate){
						needsTextureUpdate = false;
						GameGraphic bullet_gameGraphic = getGameGraphicG(bp.myBulletType,bulletFrame);
						bullet_img = getGameGraphic(bullet_gameGraphic);
						int[] bullet_r  = getGameGraphicPixelsGL(bullet_gameGraphic, bullet_img);
						tex_data = predrawGameGraphicGL(bullet_img,bullet_r);
					}
					bulletHellGame_GamePart_MT_2(tex_data);
				}
				gl.glEnable(GL.GL_DEPTH_TEST);
				gl.glDisable(GL.GL_TEXTURE_2D);
			}
			public void bhgMTP3(){
				try {
					BulletPattern bp = bulletHellGame_GamePart_MT_bp;
					if (bp.closest_num>0){
						//Run "Closest Neighbors" algorithm
						//At the moment, this runs every frame.
						NearestNeighbor.ANN(bp.closest_num,bp,bulletMethodBegins);
					}

					//Calculate edgeweights.
					bp.calculateEdges(env, bulletMethodBegins, bulletHellGame_GamePart_MT_edgeC);
				} catch (XExpression e){
					bhgEXCEPT[0] = e;
				}
			}
			/**
			 * MT TRANSFER:
			 */
			private BulletPattern bulletHellGame_GamePart_MT_bp;
			private int bulletHellGame_GamePart_MT_bpi;
			private MultiExpression[] bulletHellGame_GamePart_MT_toMov;
			private MultiExpression[] bulletHellGame_GamePart_MT_gfxE;
			private MultiExpression[] bulletHellGame_GamePart_MT_colorE;
			private MultiExpression[] bulletHellGame_GamePart_MT_edgeC;
			private float[] twoBuffer = new float[2];
			private float[] fourBuffer = new float[4];

			public final TaiBenchmark bulletHellGame_GamePart_MT_1 = new TaiBenchmark();
			private int bulletHellGame_GamePart_MT_1_k, bulletHellGame_GamePart_MT_1_k2;
			private int bulletHellGame_GamePart_MT_2_k, bulletHellGame_GamePart_MT_2_k2;

			private final void bulletHellGame_GamePart_MT_1() throws XExpression {bulletHellGame_GamePart_MT_1.mark();

			BulletPattern bp = bulletHellGame_GamePart_MT_bp;
			MultiExpression[] toMov = bulletHellGame_GamePart_MT_toMov;
			MultiExpression[] gfxE = bulletHellGame_GamePart_MT_gfxE;
			MultiExpression[] colorE = bulletHellGame_GamePart_MT_colorE;

			int k = bulletHellGame_GamePart_MT_1_k;
			int k2 = bulletHellGame_GamePart_MT_1_k2;
			int sdnMyIndex = bp.states_delta_net[k]+1;
			int sdpMyIndexMin1 = bp.states_delta_pos[k];
			int sdpMyIndex = bp.states_delta_pos[k]+1;
			if (sdpMyIndex>bp.states_delta_numFrames-1){
				sdpMyIndex = 0;
			}
			bp.states_delta_pos[k] = sdpMyIndex;
			bp.states_delta_net[k] = sdnMyIndex;
			int lk2 = k2+sdpMyIndexMin1;
			k2 = k2+sdpMyIndex;

			env.count = bp.states_indexCt[k];
			env.alive = bp.states_stillAlive;
			env.fired = bp.states_fired[k];
			env.measure = bp.states_measure[k];
			env.bulletk = k;
			env.angleToGirl0 = bp.states_angleToGirl0[k];
			env.t = bulletMethodBegins - bp.states_time[k];
			env.store = bp.states_store[k];
			env.sow = 0f;
			env.random0 = bp.states_random0[k];
			if (bp.states_neighbor_EW[k]!=null){
				for(float[] n : bp.states_neighbor_EW[k]){
					env.sow+=n[0];
				}
			}

			if (bp.doBeatPositions){
				env.tb = bhip.getBeatFromSongTime(bulletMethodBegins) - bhip.getBeatFromSongTime(bp.states_time[k]);
			}
			if (bp.doAngleToGirl){
				if (sdnMyIndex==1){
					env.angleToGirl = env.angleToGirl0;
				} else {
					env.angleToGirl = bp.calculateAngle2P(X3(bp.states_delta_x,lk2),X3(bp.states_delta_y,lk2),currentPlayerPosition)
					-bp.states_coord_system_theta[k];
				}
			}

			numBulletsRendered++;
			float bulletX, bulletY;
			bp.calculatePosition(BulletGame$1Engine$L4$1$BulletGameBulletSimulation.this, 
					env, bulletMethodBegins, toMov, 
					//OUTPUT TO:
					twoBuffer,
					k,
					bhip);
			X3(bp.states_delta_x,k2,bulletX = twoBuffer[0]);
			X3(bp.states_delta_y,k2,bulletY = twoBuffer[1]);

			bp.calculateRotations(toMov,
					//OUTPUT TO:
					twoBuffer,
					k);
			X3(bp.states_delta_rotations,k2,twoBuffer[0]);

			bp.calculateGraphicScales(gfxE,
					//OUTPUT TO:
					twoBuffer,
					k);
			X3(bp.states_delta_widths,k2,twoBuffer[0]);
			X3(bp.states_delta_heights,k2,twoBuffer[1]);

			bp.calculateColors(colorE, fourBuffer);
			X3(bp.states_delta_tintr,k2,fourBuffer[0]);
			X3(bp.states_delta_tintg,k2,fourBuffer[1]);
			X3(bp.states_delta_tintb,k2,fourBuffer[2]);
			X3(bp.states_delta_tinta,k2,fourBuffer[3]);

			if (bp.isDead(env.t, 
					bulletX,
					bulletY,
					//READ FROM OUTPUT:
					corners_polygon, corners_polygon_res,k)){
				bp.killBullet(k); //kill it off
				return;
			}

			bulletHellGame_GamePart_MT_1.markDone();}

			public final TaiBenchmark bulletHellGame_GamePart_MT_2 = new TaiBenchmark();
			private final void bulletHellGame_GamePart_MT_2(float[] tex_data) {bulletHellGame_GamePart_MT_2.mark();
			BulletPattern bp = bulletHellGame_GamePart_MT_bp;
			int k = bulletHellGame_GamePart_MT_2_k;
			int k2 = bulletHellGame_GamePart_MT_2_k2+bp.states_delta_pos[k];

			int val = bp.states_indexCt[k];
			if (val>=0){
				//Death? could also do collision detection here.
				float bulletX = X3(bp.states_delta_x,k2);
				float bulletY = X3(bp.states_delta_y,k2);

				//Collision?
				//bhip.player.position;
				float bW = X3(bp.states_delta_widths,k2);
				float bH = X3(bp.states_delta_heights,k2);
				boolean thisValence = bp.states_valence[k];
				if (Collision(bp,k)){
					//Bullet sweeping (by bomb) has already occurred. 
					bp.killBullet(k); //kill it off, to be sure.
					
					float[] deadPosition = new float[]{bulletX,bulletY};
					bW*=5;
					bH*=5;
					//Does the Collision cause an effect?
					int[] subs = bhip.subfirables[bulletHellGame_GamePart_MT_bpi];
					if (subs!=null){
					for(int subbullet : subs){
						BulletPattern bpOnEvent = bhip.firables[subbullet];
						if (bpOnEvent.fireOn_Collision){
							BulletPatternFirableDescriptor bpfd = bp.new BulletPatternFirableDescriptor(); //Can't be null, that means something else.
							boolean fired = bpOnEvent.handleFirings(
									bulletMethodBegins,
									bulletMethodBegins,
									deadPosition,
									null,
									currentPlayerPosition,
									level_rotation,
									thisValence,
									bhip,
									bpfd,
									true);
							if (!fired){
								System.err.println("Something strange, Collision Effect didn't fire (?)");
							}
							handleFiredEvent(bp);
						}
					}
					}
				} else {
					//Coloring:
					GL2 gl = ((PGraphicsOpenGL) g.g).gl;
					gl.glColor4f(
							g.constrain(X3(bp.states_delta_tintr,k2),0,1),
							g.constrain(X3(bp.states_delta_tintg,k2),0,1),
							g.constrain(X3(bp.states_delta_tintb,k2),0,1),
							g.constrain(X3(bp.states_delta_tinta,k2),0,1)
					);
					drawSingleBullet(bp,tex_data,bulletX, bulletY, bW,bH, X3(bp.states_delta_rotations,k2));

					/** TEST: NEIGHBORS **/
					int[] neighbs = bp.states_neighbors[k];
					if (neighbs!=null)
						for(int n = 0; n < neighbs.length; n++){
							int which = neighbs[n];
							float x2 = X2(bp.states_delta_x,which,bp.states_delta_pos[which]);
							float y2 = X2(bp.states_delta_y,which,bp.states_delta_pos[which]);
							float rotationNeighb = PApplet.HALF_PI+PApplet.atan2(bulletY-y2,bulletX-x2);
							duringDrawGameGraphicGL(
									//CONSTANT:
									tex_data, 
									//NONCONSTANT:
									(bulletX+x2)/2,
									(bulletY+y2)/2,
									.01f,
									(float)(Math.sqrt(Math.pow(bulletX-x2,2)+Math.pow(bulletY-y2,2))),
									rotationNeighb,
									0);
						}
				}
			}
			bulletHellGame_GamePart_MT_2.markDone();}
			private final void drawSingleBullet(BulletPattern bp, float[] tex_data, float x, float y, float bw, float bh, float rotation){
				duringDrawGameGraphicGL(
						//CONSTANT:
						tex_data, 
						//NONCONSTANT:
						x,
						y,
						bw,
						bh,
						rotation,
						0);
			}
			public void triggerSound(BulletPattern bp){
				if (bp.soundIndex != -1){
					triggerSound(audioSamples[bp.soundIndex]);
				}
			}
			public void triggerSound(AudioSample k){
				if (k!=null){
					k.trigger();
				}
			}
			/**
			 * Actually, kill the player a split second later.
			 */
			private void killPlayer(){
				if (playerIsInvincible()){
					//Alright, we invincified this guy. Getrid
					BulletPlayer player = bhip.bplys[bhip.whichPlayer];
					player.killTime = Float.MAX_VALUE / 2;
					return;
				}
				bhip.bplys[bhip.whichPlayer].killTime = Math.min(bhip.bplys[bhip.whichPlayer].killTime,bulletMethodBegins+bhip.bGlob.BorderOfLife);
			}
			private boolean playerIsInvincible(){
				return (bulletMethodBegins - bhip.bplys[bhip.whichPlayer].timeProtect) < bhip.bGlob.InvincibilityTime; 
			}
			private void reallyKillPlayer(){
				if (playerIsInvincible()){
					//Alright, we invincified this guy. Getrid
					BulletPlayer player = bhip.bplys[bhip.whichPlayer];
					player.killTime = Float.MAX_VALUE / 2;
					return;
				}
				//bhip.bplys[bhip.whichPlayer].SignName Kill it?
				triggerSound(audioSamples[bhip.bGlob.PLAYERDEATHSOUND]);
				//Sweep bullets, in an onDeath way
				/*Looked ugly.
				for(int k : bhip.currentPart.firable){
					cleanupBullets(k, true);
				}
				*/
				BulletPlayer player = bhip.bplys[bhip.whichPlayer];
				player.wantsDeathBomb = true; //Mark that we died.
				player.bombPosition = new float[]{currentPlayerPosition[0],currentPlayerPosition[1]};

				//Move player
				player.killTime = Float.MAX_VALUE/2;
				if (player.lives < 0){
					wantsBack = true;
				} else {
					player.lives--;
					bhip.newPlayerLife(true);
					player.timeProtect = bulletMethodBegins; //Invincibility on respawn
				}
			}
			private float[] collisionBuffer = new float[2];
			/**
			 * Evaluates a player collision
			 */
			private boolean Collision(BulletPattern bp, int k){
				if (!bp.isPhysical){
					return false;
				}
				if (bp.states_valence[k]==BulletPattern.ENEMY_VALENCE){
					collisionBuffer[0] = currentPlayerPosition[0];
					collisionBuffer[1] = currentPlayerPosition[1];
					//Collide player against the texgrid of the bullet.
					if (Collision$Sub(collisionBuffer,bp,k)){
						//Harm the player?
						if (bp.isHarmful){
							killPlayer();
						} else {
							//Happy bullets make a sound:
							triggerSound(audioSamples[bhip.bGlob.POWERUPSOUND]);
						}
						return true;
					}
					//Bomb elimination of harmful bullets
					if (bp.isHarmful){
						for(int bombInd : bhip.bpsForPlayer){
							if (bhip.firables[bombInd].isBomb){
								BulletPattern bomb = bhip.firables[bombInd];
								for(int subBomb = 0; subBomb < bomb.states_indexCt.length; subBomb++ ){
									if (bomb.states_indexCt[subBomb]>=0){ //alive
										//Bomb clear
										int deltaPos = bp.states_delta_pos[k];
										collisionBuffer[1] = X2(bp.states_delta_y,k,deltaPos);
										collisionBuffer[0] = X2(bp.states_delta_x,k,deltaPos);
										if (Collision$Sub(collisionBuffer,bomb,subBomb)){
											//Clear the bullet
											sweepBullet(bp, k, false);
											return true;
										}
									}
								}
							}
						}
					}
				} else if (bp.states_valence[k]==BulletPattern.PLAYER_VALENCE){
					boolean killOnCollide = true;
					if (bp.isBomb){
						killOnCollide = false; //Bombshots don't die on collision
					}
					for(int shootable : bhip.currentPart.firable){
						BulletPattern bpEnemy = bhip.firables[shootable];
						if (bpEnemy.isShootable()){
							for(int wo = 0; wo < bpEnemy.states_indexCt.length; wo++){
								if (bpEnemy.states_indexCt[wo]>=0){ //alive
									int iEnemy = bpEnemy.states_delta_pos[wo];
									collisionBuffer[0] = X2(bpEnemy.states_delta_x,wo,iEnemy);
									collisionBuffer[1] = X2(bpEnemy.states_delta_y,wo,iEnemy);
									if (Collision$Sub(collisionBuffer,bp,k)){
										//Damage the enemy:
										bpEnemy.doDamageAgainst(bp,wo);
										return killOnCollide;
									}
								}
							}
						}
					}
					if (bhip.currentPart.myBoss!=-1){
						BulletBoss bb = bhip.bbosses[bhip.currentPart.myBoss];
						//Boss damage:
						collisionBuffer[0] = currentBossPosition[0];
						collisionBuffer[1] = currentBossPosition[1];
						boolean bulletHitsBoss = Collision$Sub(collisionBuffer,bp,k);
						int deltaPos = bp.states_delta_pos[k];
						collisionBuffer[1] = X2(bp.states_delta_y,k,deltaPos);
						collisionBuffer[0] = X2(bp.states_delta_x,k,deltaPos);
						boolean BossHitsBullet = Collision$Boss(collisionBuffer,bb);
						if (bulletHitsBoss || BossHitsBullet){
							//Damage the enemy:
							bhip.bscs[bhip.currentPart.mySpellCard].doDamageAgainst(bp);
							return killOnCollide;
						}
					}
				}
				return false;
			}

			private boolean Collision$Boss(float[] collisionBuffer2, BulletBoss bp){
				collisionBuffer[1] -= currentBossPosition[1];
				collisionBuffer[0] -= currentBossPosition[0];

				//Heuristic, quick throw out
				float MaxBoundingHeight = (float) Math.abs(bp.h);
				float MaxBoundingWidth = (float) Math.abs(bp.w);
				if (
						collisionBuffer[1] < -MaxBoundingHeight || 
						collisionBuffer[1] > MaxBoundingHeight ||
						collisionBuffer[0] < -MaxBoundingWidth || 
						collisionBuffer[0] > MaxBoundingWidth){
					return false;
				}
				//Alright, now deal with sign.
				MaxBoundingHeight = (float) bp.h;
				MaxBoundingWidth =  (float) bp.w;

				rtate(collisionBuffer, currentBossPosition[2]);
				//TODO work on this.
				//collisionBuffer[1] *= -1;

				int tw = bp.w_tex;
				int th = bp.h_tex;

				float xCoordF = collisionBuffer[0] / MaxBoundingWidth * tw;
				float yCoordF = collisionBuffer[1] / MaxBoundingHeight * th;
				int xCoord = PApplet.round(xCoordF);
				int yCoord = PApplet.round(yCoordF);

				if (xCoord < -tw/2f || xCoord > tw/2f || yCoord < -th/2f || yCoord > th/2f){
					//outside
				} else {
					//We're inside
					int index = (xCoord + tw/2)+(yCoord+th/2)*tw;
					//bp.tex_grid;
					if (bp.tex_grid[index]==1){
						return true;
					}
				}
				return false;
			}
			private boolean Collision$Sub(float[] collisionBuffer2, BulletPattern bp, int k){
				int deltaPos = bp.states_delta_pos[k];
				int lastDeltaPos = deltaPos-1;
				if (lastDeltaPos < 0 ){ 
					lastDeltaPos += bp.states_delta_numFrames; 
				} 
				float lastY = X2(bp.states_delta_y,k,lastDeltaPos);
				float lastX = X2(bp.states_delta_x,k,lastDeltaPos);
				float delY = X2(bp.states_delta_y,k,deltaPos)-lastY;
				float delX = X2(bp.states_delta_x,k,deltaPos)-lastX;
				float dist = g.sqrt(delY*delY+delX*delX);
				int len = Math.max((int)(dist*640),1);
				if (bp.states_delta_net[k]<2){
					len = 1;
				}
				dist = 1f / len;
				float storeC1 = collisionBuffer2[1];
				float storeC0 = collisionBuffer2[0];
				for(int i = len; i > 0; i--){
					collisionBuffer2[1] = lastY += delY*dist;
					collisionBuffer2[0] = lastX += delX*dist;
					collisionBuffer2[1] = storeC1 - collisionBuffer2[1];
					collisionBuffer2[0] = storeC0 - collisionBuffer2[0];
					if (Collision$Sub0(collisionBuffer2, bp, k)){
						return true;
					}
				}
				return false;
			}
			private boolean Collision$Sub0(float[] collisionBuffer2, BulletPattern bp, int k){
				int deltaPos = bp.states_delta_pos[k];
				
				//Heuristic, quick throw out
				float MaxBoundingHeight = Math.abs(X2(bp.states_delta_heights,k,deltaPos));
				float MaxBoundingWidth = Math.abs(X2(bp.states_delta_widths,k,deltaPos));
				if (
						collisionBuffer2[1] < -MaxBoundingHeight || 
						collisionBuffer2[1] > MaxBoundingHeight ||
						collisionBuffer2[0] < -MaxBoundingWidth || 
						collisionBuffer2[0] > MaxBoundingWidth){
					return false;
				}
				//Alright, now deal with sign.
				MaxBoundingHeight = X2(bp.states_delta_heights,k,deltaPos);
				MaxBoundingWidth = X2(bp.states_delta_widths,k,deltaPos);

				rtate(collisionBuffer2, X2(bp.states_delta_rotations,k,deltaPos));
				//collisionBuffer[1] *= -1;

				int tw = bp.w_tex;
				int th = bp.h_tex;

				float xCoordF = collisionBuffer2[0] / MaxBoundingWidth * tw;
				float yCoordF = collisionBuffer2[1] / MaxBoundingHeight * th;
				int xCoord = PApplet.round(xCoordF);
				int yCoord = PApplet.round(yCoordF);

				if (xCoord < -tw/2f || xCoord > tw/2f || yCoord < -th/2f || yCoord > th/2f){
					//outside
				} else {
					//We're inside
					int index = (xCoord + tw/2)+(yCoord+th/2)*tw;
					//bp.tex_grid;
					if (bp.tex_grid[index]==1){
						return true;
					}
				}
				return false;
			}
			/**
			 * Calculate which frame based on motion
			 * @param f 
			 * @param beforeMoveX 
			 * @param is 
			 * @param g22 
			 */
			public int calculateAnimationFrame(GraphicsHolder g22, int[][] is, float beforeMoveX, float f){
				float frameRate = g22.getAnimFps();
				int[] isA;
				if (is.length==1){
					isA = is[0];
				} else {
					if (beforeMoveX < f-1e-5f){
						isA = is[GraphicsHolderParser.RIGHT_ANIMATION];
					} else if (beforeMoveX > f+1e-5f){
						isA = is[GraphicsHolderParser.LEFT_ANIMATION];
					} else {
						isA = is[GraphicsHolderParser.NORM_ANIMATION];
					}
				}
				//AHAHA! smart compiler.
				int index = (int)((frameRate * bulletMethodBegins)%isA.length);
				return isA[index];
			}
			public class BulletHellPositionRelativitator implements PositionRelativitator{
				public void relativity(float[] position, float sourcex, float sourcey, BulletRelativitable br) {
					int relativeMode = br.getRelativeMode();
					//SUBTRACT "TRUE" COORD:
					switch(relativeMode){
					case BulletRelativitable.TOP_LEFT_CORNER:
						break;
					case BulletRelativitable.TOP_RIGHT_CORNER:
						position[0]-=1;
						break;
					case BulletRelativitable.BOTTOM_RIGHT_CORNER:
						position[0]-=1;
						position[1]-=1;
						break;
					case BulletRelativitable.BOTTOM_LEFT_CORNER:
						position[1]-=1;
						break;
					case BulletRelativitable.CENTER_SPOT:
						position[0]-=.5;
						position[1]-=.5;
						break;
					default:
						//everything else uses the raw position.
						break;
					}
					//Now, if the vector is 0,0, then we have the source position
					//This means that we're ready to do rotation:
					rtate(position, level_rotation);
					//Ok, now add to the actual source position:
					switch(relativeMode){
					case BulletRelativitable.TOP_LEFT_CORNER:
						position[0] += corners[0][0];
						position[1] += corners[0][1];
						break;
					case BulletRelativitable.TOP_RIGHT_CORNER:
						position[0] += corners[1][0];
						position[1] += corners[1][1];
						break;
					case BulletRelativitable.BOTTOM_RIGHT_CORNER:
						position[0] += corners[2][0];
						position[1] += corners[2][1];
						break;
					case BulletRelativitable.BOTTOM_LEFT_CORNER:
						position[0] += corners[3][0];
						position[1] += corners[3][1];
						break;
					case BulletRelativitable.CENTER_SPOT:
						position[0] += centerLevelX;
						position[1] += centerLevelY;
						break;
					case BulletRelativitable.PLAYER: 
						//Sort of a "global" source, independent of the firer.
						position[0] += currentPlayerPosition[0];
						position[1] += currentPlayerPosition[1];
						break;
					case BulletRelativitable.SOURCE: case BulletRelativitable.SOURCELOCK:
						//SourceLock is not actually handled correctly here.
						//In order to get the sourcelock effect, we need to write some
						//bullet specific code
						position[0] += sourcex;
						position[1] += sourcey;
						break;
					}
				}
			}
			private Rectangle2D.Float HEALTH_BAR = new Rectangle2D.Float(.01f,.01f,.9f,.05f);
			public void bulletHellGame_GamePart$SkinExtras(){
				float prog = bhip.bscs[bhip.currentPart.mySpellCard].getHealthDuration(bulletMethodBegins,bhip);
				if (prog >= 0){
					g.fill(0);
					g.stroke(255);
					rect(HEALTH_BAR);
					g.fill(255,0,0);
					g.noStroke();
					g.rect(HEALTH_BAR.x+1f/ARCorrectedWidth,
							HEALTH_BAR.y+1f/currentViewPortHeight,
							HEALTH_BAR.width * prog,
							HEALTH_BAR.height-2f/currentViewPortHeight);
				}
				
				Rectangle2D.Float texPromptArea = new Rectangle2D.Float(.8f,.1f,.2f,.4f);
				scaleRect(texPromptArea,gameAreaDim);
				SkinText.setArea(texPromptArea);
				String numFormat = "-9";
				int lines = 0;
				SkinText.setTextRow("Player:",lines++);
				SkinText.setTextRow(spaceOut(
						bhip.bplys[bhip.whichPlayer].lives
						,10
						,"*"
						),lines++);
				SkinText.setTextRow("Bombs:",lines++);
				SkinText.setTextRow(spaceOut(
						bhip.bplys[bhip.whichPlayer].bombs
						,10
						,"*"
						),lines++);
				SkinText.setTextRow("Graze:",lines++);
				SkinText.setTextRow(String.format("%"+numFormat+"d",new Object[]{
						bhip.bplys[bhip.whichPlayer].graze}),lines++);
				SkinText.setTextRow("Power:",lines++);
				SkinText.setTextRow(String.format("%"+numFormat+"f",new Object[]{
						bhip.bplys[bhip.whichPlayer].power}),lines++);
				SkinText.setTextRow("Score:",lines++);
				SkinText.setTextRow(String.format("%"+numFormat+"d",new Object[]{
						bhip.bplys[bhip.whichPlayer].score}),lines++);
				SkinText.draw();
			}
			private String spaceOut(int j, int chars, String string){
				char[] toRet = new char[chars];
				Arrays.fill(toRet,' ');
				int size = (chars/(j+2)+1);
				for(int i = 1; i < j+1; i++){
					try {
						toRet[i*size] = string.charAt(0);
					} catch (Throwable e){
						//Meh.
					}
				}
				return new String(toRet);
			}
			//State lines
			private boolean requestFire, requestUp, requestDown, requestRight, requestLeft, requestBomb, requestFocus;
			public void handleInputAtBeginningOfFrame() {
				requestFire = truth(keyboard.get(KeyEvent.VK_Z));
				requestBomb = truth(keyboard.get(KeyEvent.VK_X));
				requestFocus = truth(keyboard.get(KeyEvent.VK_SHIFT));
				requestUp = truth(keyboard.get(KeyEvent.VK_UP));
				requestDown = truth(keyboard.get(KeyEvent.VK_DOWN));
				requestRight = truth(keyboard.get(KeyEvent.VK_RIGHT));
				requestLeft = truth(keyboard.get(KeyEvent.VK_LEFT));
			}
			/**
			 * Guaranteed to not mess up the rendering portion of the frame.
			 * Just nulls out the logic parts of the level, and should still
			 * promote a safe return to the editor.
			 */
			public void quickMemoryFree(){
				for(BulletPattern bp : bhip.firables){
					bp.stopBackgroundThread();
				}
				bhip = null;
			}
			public void cleanup(){
				if (bhip!=null && bhip.firables!=null){
					for(BulletPattern bp : bhip.firables){
						bp.stopBackgroundThread();
					}
				}
				bhip = null;
				if (SkinText != null){
					SkinText.cleanup();
					SkinText = null;
				}
				if (music!=null){
					music.pause();
					music.close();
				}
				if (audioSamples!=null){
					for(AudioSample k : audioSamples){
						if (k!=null){
							k.close();
						}
					}
				}
				audioSamples = null;
				music = null;
				stopRenderLoadProgress();
				endVideos();
				unloadResources();
			}
		}
		//Instantiate on first frame; for extension reasons.
		public GameScreen(){
			g.registerDraw(this);
			addSubKeyListener(this);
			timeFrames = new TimeRenderer();
		}
		public void keyPressed(KeyEvent e){
			if (!isInputBlocked() && myPlay != null){
				if (e.getKeyCode()==KeyEvent.VK_ESCAPE){
					myPlay.wantsBack = true;
				}
			}
		}
		private boolean requestedBackAlready = false;
		public void backScreen_0(){
			if (!requestedBackAlready){
				if (backScreen()){
					requestedBackAlready = true;
				}
			}
		}


		public BulletGameGamePlay myPlay;
		private TimeRenderer timeFrames;
		public void cleanup() {
			if (myPlay!=null){
				myPlay.cleanup();
			}
			g.unregisterDraw(myPlay);
			g.unregisterDraw(this);
			removeSubKeyListener(this);
		}
		public void drawScreen() {
			if (myPlay==null){
				restartGame();
			}
			//Dequeue keypresses
			myPlay.handleInputAtBeginningOfFrame();
			myPlay.draw();
			if(true){
				timeFrames.draw();
			}
		}

		/** To be overridden by subclasses */
		public boolean backScreen(){
			return SceneChange(0, BulletHell.BulletGame$1Engine$GROUND.MAINSCREEN);
		}
		public void restartGame(){
			//First frame, no-custom flag.
			myPlay = new BulletGameGamePlay(true);
		}
	}
	/**
	 * TODO: skip ahead to a point in the level.
	 */
	public class GameScreenFromEditor extends BulletGame$1Engine$L4$1$BulletGameBulletSimulation.GameScreen{
		public void restartGame(){
			myPlay = new BulletGameGamePlay(false);
		}
		public boolean backScreen(){
			return SceneChange(0, BulletHell.BulletGame$1Engine$GROUND.EDITORSCREEN);
		}
	}
	public class GameScreenFromBenchmarks extends BulletGame$1Engine$L4$1$BulletGameBulletSimulation.GameScreen{
		public void restartGame(){
			myPlay = new BulletGameGamePlay(true);
		}
		public boolean backScreen(){
			//Ok, was run, put on clipboard
			new SaveGameDialog(this,new Runnable(){
				public void run(){
					SceneChange(0, BulletHell.BulletGame$1Engine$GROUND.BENCHMARK_EDITOR);
				}
			},bgbs_loaded,"EditorScreen/ScrollArrowRed.png");
			return true;
		}
	}
}
