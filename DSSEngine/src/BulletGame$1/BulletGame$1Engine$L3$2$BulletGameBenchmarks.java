package BulletGame$1;

import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.Scanner;

import javax.swing.JFrame;

import processing.core.PApplet;
import BulletGame.Benchmarking.BulletGameBenchmark;
import BulletGame.Benchmarking.BulletGameBenchmarkSet;
import Deployments.BulletHell;
import TaiScript.parsing.TaiScriptEditor;




public abstract class BulletGame$1Engine$L3$2$BulletGameBenchmarks extends BulletGame$1Engine$L3$1$BulletGameEditorScreen {
	public BulletGame$1Engine$L3$2$BulletGameBenchmarks(JFrame holder,PApplet hold) {
		super(holder, hold);
	}
	public BulletGameBenchmarkSet bgbs_loaded;

	public class BenchmarkScreen extends BulletGameScreen {
		public BenchmarkScreen(){
			rects = new Rectangle2D.Float[]{
					new Rectangle2D.Float(.02f,.02f,.1f,.1f),
					new Rectangle2D.Float(.14f,.02f,.1f,.1f),
					new Rectangle2D.Float(.26f,.02f,.1f,.1f),
			};
			myms = new MouseChecker[rects.length];
			for(int k = 0; k < myms.length; k++){
				myms[k] = new MouseChecker(BulletGame$1Engine$L3$2$BulletGameBenchmarks.this);
			}
			g.registerDraw(this);
			addSubKeyListener(this);
			g.frameRate(200);
		}
		public void cleanup() {
			g.unregisterDraw(this);
			removeSubKeyListener(this);
		}
		public void keyPressed(KeyEvent e){
			if (!isInputBlocked()){
				if (e.getKeyCode()==KeyEvent.VK_ESCAPE){
					bgbs_loaded = null;
					SceneChange(Transitioner.FADE_WITH_BLACK, BulletHell.BulletGame$1Engine$GROUND.MAINSCREEN);
				}
			}
		}
		private Rectangle2D.Float[] rects;
		private MouseChecker[] myms;
		private Runnable[] doOnStuff = new Runnable[]{
				new Runnable(){
					public void run(){
						if (hasCurrentDialog()){
							return;
						}
						//New benchmark, using bhi
						BulletGameBenchmarkSet bgbs = new BulletGameBenchmarkSet("");
						bgbs.constantEnv = bhi;
						//put it on clipboard
						new SaveGameDialog(BenchmarkScreen.this,new Runnable(){
							public void run(){}
						},bgbs,"EditorScreen/ScrollArrowRed.png");
						bgbs_loaded = bgbs;
					}
				},
				new Runnable(){
					public void run(){
						if (hasCurrentDialog()){
							return;
						}
						//Load benchmark
						new LoadBenchmarkDialog(BenchmarkScreen.this, new Runnable(){
							public void run(){

							}
						});
					}
				},
				new Runnable(){
					public void run(){
						if (hasCurrentDialog()){
							return;
						}
						bhi = bgbs_loaded.constantEnv;
						//Add new benchmark run to loaded set
						SceneChange(Transitioner.FADE_WITH_BLACK, BulletHell.BulletGame$1Engine$GROUND.GAMESCREEN_FROMBENCHMARKS);
					}
				}
		};
		public void drawScreen() {
			g.noStroke();
			g.fill(0);
			g.rect(0,0,1,1);
			int buttonsToShow = bgbs_loaded==null?2:3;
			for(int k = 0; k < buttonsToShow; k++){
				g.fill(255,0,0);
				rect(rects[k]);
				if(myms[k].mouseChecker(rects[k])){
					doOnStuff[k].run();
				}
			}
			if (bgbs_loaded!=null){
				Rectangle2D.Float area = new Rectangle2D.Float(.1f,.2f,.8f,.6f);
				viewport(area);
				g.fill(200,200,255);
				g.rect(0,0,1,1);
				for(int k = 0; k < bgbs_loaded.benchs.length; k++){
					BulletGameBenchmark bg = bgbs_loaded.benchs[k];
					int length = bg.bulletsLiveAtPoint.length;
					float maxTime = bg.timepoints[length-1];
					float tScl = 1f/maxTime;
					g.stroke(0,255,0);
					drawLine(bg,bg.fpspoints,length,1f/200,tScl);
					g.stroke(255,0,0);
					drawLine(bg,bg.bulletsLiveAtPoint,length,1f/4000,tScl);
				}
			}
		}
		private void drawLine(BulletGameBenchmark bg, float[] stuff, int length, float scale, float tScl){
			g.beginShape(PApplet.LINES);
			float lx = 0, ly = 0;
			for(int q = 0; q < length; q++){
				if(q>1){
					g.vertex(lx, ly);	
				}
				float x = bg.timepoints[q]*tScl;
				float y = 1f-stuff[q]*scale;
				g.vertex(x, y);
				lx = x;
				ly = y;
			}
			g.endShape();
		}

		public class LoadBenchmarkDialog extends ModalDialog {
			private void loadStuff() {
				bgbs_loaded = gottit;
			}
			public LoadBenchmarkDialog(BulletGameScreen parent, final Runnable runnable) {
				super(parent, new ModalDialogCallback<LoadBenchmarkDialog>(){
					public void dialogFinished(LoadBenchmarkDialog self) {
						self.cleanup();
						self.loadStuff();
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
			private BulletGameBenchmarkSet gottit;
			private BulletHellText bht;
			private int stringShow = 1;
			private String[] toRead = new String[]{
					"Copy a benchmark set, then click here.",
					"Error loading benchmark. Copy it again, and try again."
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
						gottit = new BulletGameBenchmarkSet(sb.toString());
						return true;
					} catch (Throwable e){
						e.printStackTrace();
						stringShow = 2;
					}
					gottit = null;
				}
				return false;
			}
		}
	}
}