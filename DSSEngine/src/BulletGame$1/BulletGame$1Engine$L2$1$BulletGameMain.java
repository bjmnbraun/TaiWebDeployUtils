package BulletGame$1;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.util.Scanner;

import javax.swing.JFrame;

import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import BulletGame$2.BulletHellInstance;
import Deployments.BulletHell;
import TaiGameCore.RelativelyTimed;
import TaiGameCore.ScreenSelector;
import TaiGameCore.TaiShaders;
import TaiGameCore.TaiVBO;
import TaiGameCore.RelativelyTimed.RelativeTimeNode;
import TaiGameCore.RelativelyTimed.TimeSource;
import TaiGameCore.ScreenSelector.TMenuItem;
import TaiScript.parsing.TaiScriptEditor;
import ddf.minim.AudioPlayer;

/**
 * SCREEN:
 * MAIN SCREEN.
 * @author Benjamin
 *
 */
public abstract class BulletGame$1Engine$L2$1$BulletGameMain extends BulletGame$1Engine$L1$1$OpenglTextRenderer{
	public BulletGame$1Engine$L2$1$BulletGameMain(JFrame holder, PApplet hold) {
		super(holder, hold);
		VERSION_NUMBER = FILE_SYSTEM.getVersion();
		System.out.println("VERSION # "+VERSION_NUMBER);
	}
	public final int VERSION_NUMBER;

	public BulletHellInstance bhi;
	private void initGlobals(){
		FILE_SYSTEM.set404Image("img404.png");
		if (bhi==null){
			bhi = new BulletHellInstance("");
		}
	}

	public class SoundPulser{
		private AudioPlayer ap;
		public SoundPulser(AudioPlayer ap){
			this.ap = ap;
		}
		TaiShaders ts;
		TaiVBO randomTest;
		public void draw(){
			if (randomTest==null){
				randomTest = new TaiVBO(((PGraphicsOpenGL)g.g).gl);
				ts = FILE_SYSTEM.loadShader("Shader2", ((PGraphicsOpenGL)g.g).gl);
				for(Shader1VertShader d : Shader1VertShader.values()){
					randomTest.registerAttrib(d, d.Type, d.attribNum, d.attribOff);
				}
			}
			randomTest.reset();
			view3D(640,480);
			randomTest.addNgon(4,
					Shader1VertShader.Center_X, .5f,
					Shader1VertShader.Center_Y, .5f, 
					Shader1VertShader.RectWidth, .25f,
					Shader1VertShader.RectHeight, .25f,
					Shader1VertShader.Rotation, 0f,
					Shader1VertShader.X_TexOffset, 0f,
					Shader1VertShader.Y_TexOffset, 0f,
					Shader1VertShader.TexScaleX, 1f,
					Shader1VertShader.TexScaleY, 1f
			);
			ts.switchToShader(ts);
			randomTest.drawQueuedElements();
			ts.switchToShader(null);
			//Shader1VertShader.tint, Txtcolor
		}
		public void cleanup(){
			
		}
	}

	public class Background {
		float fRotate = 2f;
		RelativelyTimed clock;
		public Background(TimeSource ts) {
			clock = new RelativeTimeNode(ts);
			// TODO Auto-generated constructor stub
		}
		private PImage smiley = new PImage(64,64,g.ARGB);
		public void draw(){
			viewport(.1,.1,.3,.85);
			//view3D(640,480); //Weirdo, 3d... pshaw
			screen2D4GL(640,480);
		}
	}
	public class Stage2Screen extends BulletGameScreen implements KeyListener{
		private RelativeTimeNode clock;
		private ScreenSelector sll;
		private class MainScreenMenuItem extends TMenuItem {
			public MainScreenMenuItem(int ScreenTarget, String image, int i){
				target = ScreenTarget;
				img = image;
				IndexInSelection= i;
				Size = IndexInSelection==0?maxSize:minSize;
			}
			private int IndexInSelection;
			private String img;
			private int target;
			public void act() {
				SceneChange(1,target);
			}
			private RelativeTimeNode trans;
			private float deltaGrowth = 0;
			private float maxSize = .002f, minSize = .0015f;
			private float Size;
			private PImage ItemImg;
			public void draw(){
				if (trans==null){
					trans = new RelativeTimeNode(clock);
				}
				if (ItemImg==null){
					ItemImg = FILE_SYSTEM.getImg(img);
				}
				g.pushMatrix();
				g.translate(.8f,.7f+IndexInSelection*.13f);
				g.scale(Size,Size*NSPH);
				g.fill(255,255,0);
				g.image(ItemImg,0,-ItemImg.height/2);
				g.popMatrix();
				Size += deltaGrowth * trans.time();
				trans.reset();
				Size = g.constrain(Size, minSize, maxSize);
			}
			public void onSelect(){
				deltaGrowth = .001f;
			}
			public void onDeselect(){
				deltaGrowth = -.005f;
			}
		}
		private final TMenuItem[] screenOptionTexts = new TMenuItem[]{
				new MainScreenMenuItem(BulletHell.BulletGame$1Engine$GROUND.GAMESCREEN,"MainScreen/PlayGame.png",0),
				new MainScreenMenuItem(BulletHell.BulletGame$1Engine$GROUND.EDITORSCREEN,"MainScreen/Editor.png",1),
		};
		public void drawScreen(){
			viewport(0,0,1,1);
			screen2D(); //Also used for all remaining items on the stage2 screen.
			drawBg();
			for(TMenuItem k : screenOptionTexts){
				k.draw();	
			}
		}
		public void drawBg(){
			g.background(255,255,255);
		}
		public Stage2Screen(){
			initGlobals();
			clock = new RelativeTimeNode(GAME_TIME);
			g.registerDraw(bg = new Background(GAME_TIME)); //DRAWN FIRST: USES VIEW3D!!!
			g.registerDraw(this); //Reverts the 3d caused by the bg
			g.registerDraw(fps = new TimeRenderer());
			//TODO: foreground!
			sll = new ScreenSelector(ScreenSelector.MODE.KEYBOARD){
				public TMenuItem[] getItems() {
					return screenOptionTexts;
				}
			};
			addSubKeyListener(this);

			if (false){
				backgroundMusic = FILE_SYSTEM.loadAudioFile("20090312_Karoshi_2009_Theme_Master1.mp3",AUDIO);
				backgroundMusic.play();
			}

			g.registerDraw(sp = new SoundPulser(backgroundMusic));
		}
		private AudioPlayer backgroundMusic;
		private Background bg;
		private TimeRenderer fps;
		private SoundPulser sp;
		public void cleanup() {
			if (this!=null)	g.unregisterDraw(this);
			if (bg!=null)	g.unregisterDraw(bg);
			if (fps!=null)	g.unregisterDraw(fps);
			if (sp!=null){	g.unregisterDraw(sp); sp.cleanup(); }
			removeSubKeyListener(this);
			if (backgroundMusic!=null){
				backgroundMusic.pause();
				backgroundMusic.close();
				backgroundMusic = null;
			}
		}
		public void keyPressed(KeyEvent e) {
			if (!isInputBlocked()){
				sll.keyPressed(e);
				if (e.getKeyCode()==KeyEvent.VK_F5){
					SceneChange(Transitioner.FADE_WITH_BLACK, BulletHell.BulletGame$1Engine$GROUND.BENCHMARK_EDITOR);
				}
			}
			if (e.getKeyCode()==KeyEvent.VK_ESCAPE){
				//REMOVE FOR ONLINE!!!!
				//System.exit(0);
			}
		}
		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub

		}
		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub

		}
	}

	/**
	 * Overwrites the current bhi.
	 */
	public class LoadBulletDialog extends ModalDialog {
		public LoadBulletDialog(BulletGameScreen parent, final Runnable runnable) {
			super(parent, new ModalDialogCallback<LoadBulletDialog>(){
				public void dialogFinished(LoadBulletDialog self) {
					self.cleanup();
					if (self.gottit!=null){
						bhi = self.gottit;
					}
					runnable.run();
				}
			});
			area = new Rectangle2D.Float(.02f,.35f,.94f,.25f);
			bht = new BulletHellText(area, 2);
			bht.setTextScale(.2f);
			bht.ets.tse.clearBlanks();
			basics = System.nanoTime();
		}
		private Rectangle2D.Float area;
		private BulletHellInstance gottit;
		private BulletHellText bht;
		private int stringShow = 1;
		private String[] toRead = new String[]{
				"Copy a save-state, then click here.",
				"Error loading script. Copy it again, and try again."
		};
		public void cleanup(){
			bht.cleanup();
			super.cleanup();
		}
		private long basics;
		public boolean drawDialog() {
			viewport(area);
			g.fill(0);
			g.rect(0,0,1,1);
			g.fill(255);
			bht.draw();
			for(int k = 0; k < stringShow; k++){
				bht.setTextRow(toRead[k], k);
			}
			long diff = System.nanoTime()-basics;
			if (g.mousePressed && diff > .3e9){
				try {
					String str = TaiScriptEditor.getClipboardContents();
					StringBuffer sb = new StringBuffer();
					Scanner in = new Scanner(str);
					while(in.hasNextLine()){
						sb.append(in.nextLine());
					}
					gottit = new BulletHellInstance(sb.toString());
					return true;
				} catch (Throwable e){
					System.out.println("Could not load save file. Invalid hash.");
					stringShow = 2;
				}
				gottit = null;
			}
			return false;
		}
	}
	public class BulletHellText extends TaiTextBox{
		public BulletHellText(Float area, int numLines) {
			this(area,numLines,"Comfortaa-Thin-48.vlw");
			setTextScale(.16f);
		}
		public BulletHellText(Float area, int numLines, String string) {
			super(area, numLines);
			ets.tse.clearBlanks();
			useFont(FILE_SYSTEM.getFont(string));
		}
	}
}
