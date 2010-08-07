package TaiGameCore;

import java.lang.reflect.Field;

import BulletGame$1.BulletGame$1Engine$L3$1$BulletGameEditorScreen.EditorScreen;
import BulletGame$1.BulletGame$1Engine$L4$1$BulletGameBulletSimulation.GameScreen.BulletGameGamePlay;

public class TaiBenchmark {

	public void mark() {
	}

	public void markDone() {
	}

	public static void bench(BulletGameGamePlay bggp) {
	}

	public static void bench(EditorScreen bggp) {
	}

	private static int firstBench = 0;

	/*
	public static void bench(EditorScreen obj){
		if (firstBench==0){ firstBench=1; return; }
		if (firstBench==1){firstBench=2;
		for (int p = 0; p < obj.bulletEditorScreenDraw_2.maxStackDepth; p++){
		System.out.print("bulletEditorScreenDraw_2_"+p+" ");
		System.out.print("bulletEditorScreenDraw_2_"+p+" ");
		}
		for (int p = 0; p < obj.bulletEditorScreenDraw_1.maxStackDepth; p++){
		System.out.print("bulletEditorScreenDraw_1_"+p+" ");
		System.out.print("bulletEditorScreenDraw_1_"+p+" ");
		}
		System.out.println();
		}
		for (int p = 0; p < obj.bulletEditorScreenDraw_2.maxStackDepth; p++){
		System.out.print(obj.bulletEditorScreenDraw_2.ops[p]+" ");
		System.out.print(obj.bulletEditorScreenDraw_2.avgNanos[p]+" ");
		}
		obj.bulletEditorScreenDraw_2.reset();
		for (int p = 0; p < obj.bulletEditorScreenDraw_1.maxStackDepth; p++){
		System.out.print(obj.bulletEditorScreenDraw_1.ops[p]+" ");
		System.out.print(obj.bulletEditorScreenDraw_1.avgNanos[p]+" ");
		}
		obj.bulletEditorScreenDraw_1.reset();
		System.out.println();
	}


	private static final int MAXSTACK = 8;
	public int[] ops = new int[MAXSTACK];
	public int stackDepth;
	public int maxStackDepth = 0;
	public long[] cTime = new long[MAXSTACK];
	public float[] avgNanos = new float[MAXSTACK];
	public void mark(){
		ops[stackDepth]++;
		cTime[stackDepth] = System.nanoTime();
		stackDepth++;
	}
	public void markDone(){
		maxStackDepth = Math.max(maxStackDepth,stackDepth);
		stackDepth--;
		long delta = System.nanoTime()-cTime[stackDepth];
		avgNanos[stackDepth] = (avgNanos[stackDepth] * ops[stackDepth] + delta)/(ops[stackDepth]+1);
	}
	public void reset(){
		for(int k = 0; k < MAXSTACK; k++){
			ops[k] = 0;
			avgNanos[k] = 0;
		}
	}
	public static void bench(BulletGameGamePlay obj) {
		if (firstBench==0){ firstBench=1; return; }
		if (firstBench==1){firstBench=2;
		for (int p = 0; p < obj.bulletHellGame_GamePart$Bullets.maxStackDepth; p++){
			System.out.print("bulletHellGame_GamePart$Bullets_"+p+" ");
			System.out.print("bulletHellGame_GamePart$Bullets_"+p+" ");
		}
		for (int p = 0; p < obj.bulletHellGame_GamePart_MT_1.maxStackDepth; p++){
			System.out.print("bulletHellGame_GamePart_MT_1_"+p+" ");
			System.out.print("bulletHellGame_GamePart_MT_1_"+p+" ");
		}
		for (int p = 0; p < obj.bulletHellGame_GamePart_MT_2.maxStackDepth; p++){
			System.out.print("bulletHellGame_GamePart_MT_2_"+p+" ");
			System.out.print("bulletHellGame_GamePart_MT_2_"+p+" ");
		}
		System.out.println();
		}
		for (int p = 0; p < obj.bulletHellGame_GamePart$Bullets.maxStackDepth; p++){
			System.out.print(obj.bulletHellGame_GamePart$Bullets.ops[p]+" ");
			System.out.print(obj.bulletHellGame_GamePart$Bullets.avgNanos[p]+" ");
		}
		obj.bulletHellGame_GamePart$Bullets.reset();
		for (int p = 0; p < obj.bulletHellGame_GamePart_MT_1.maxStackDepth; p++){
			System.out.print(obj.bulletHellGame_GamePart_MT_1.ops[p]+" ");
			System.out.print(obj.bulletHellGame_GamePart_MT_1.avgNanos[p]+" ");
		}
		obj.bulletHellGame_GamePart_MT_1.reset();
		for (int p = 0; p < obj.bulletHellGame_GamePart_MT_2.maxStackDepth; p++){
			System.out.print(obj.bulletHellGame_GamePart_MT_2.ops[p]+" ");
			System.out.print(obj.bulletHellGame_GamePart_MT_2.avgNanos[p]+" ");
		}
		obj.bulletHellGame_GamePart_MT_2.reset();
		System.out.println();
	}
	*/

	public static void main(String[] args) throws ClassNotFoundException {
		Class test;
		test = BulletGame$1.BulletGame$1Engine$L4$1$BulletGameBulletSimulation.GameScreen.BulletGameGamePlay.class;
		test = BulletGame$1.BulletGame$1Engine$L3$1$BulletGameEditorScreen.EditorScreen.class;
		Class q = test;

		System.out.println("if (firstBench==0){ firstBench=1; return; }");
		System.out.println("if (firstBench==1){firstBench=2;");
		for (Field k : q.getFields()) {
			if (k.getType().equals(TaiBenchmark.class)) {
				System.out.println("for (int p = 0; p < obj." + k.getName()
						+ ".maxStackDepth; p++){");
				for (int i = 0; i < 2; i++) {
					System.out.println("System.out.print(\"" + k.getName()
							+ "_\"+p+\" \");");
				}
				System.out.println("}");
			}
		}
		System.out.println("System.out.println();");
		System.out.println("}");
		for (Field k : q.getFields()) {
			if (k.getType().equals(TaiBenchmark.class)) {
				System.out.println("for (int p = 0; p < obj." + k.getName()
						+ ".maxStackDepth; p++){");
				System.out.println("System.out.print(obj." + k.getName()
						+ ".ops[p]+\" \");");
				System.out.println("System.out.print(obj." + k.getName()
						+ ".avgNanos[p]+\" \");");
				//Reset the stuff:
				System.out.println("}");
				System.out.println("obj." + k.getName() + ".reset();");
			}
		}
		System.out.println("System.out.println();");
	}

}
