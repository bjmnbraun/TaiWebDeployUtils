package TaiGameCore;

import java.awt.geom.Rectangle2D;

import processing.core.PApplet;
import processing.core.PImage;

public class GameSprite extends GameDataBase {

	public GameSprite(String hash) {
		super(hash);
		if (frame == null) {
			frame = new GameGraphic[0];
		}
	}

	@DefaultValue(value = "(int)(1e9/60)")
	//1 60/fps frame.
	public int frameResolution; //in Nanos
	@DefaultValue(value = "")
	public int[] keyFrames;
	@DefaultValue(value = "-1")
	//-1 means no loop
	public int loopAfter;
	public GameGraphic[] frame;
	public GameAudio sound;

	public void autoWrittenDeSerializeCode() {
		frameResolution = ((IntEntry) readField("frameResolution",
				new IntEntry((int) (1e9 / 60)))).getInt();
		keyFrames = ((IntArrayEntry) readField("keyFrames", new IntArrayEntry(
				new int[] {}))).getIntArray();
		loopAfter = ((IntEntry) readField("loopAfter", new IntEntry(-1)))
				.getInt();
		String frame_strTmp = ((StringEntry) readField("frame",
				new StringEntry(""))).getString();
		if (frame_strTmp.length() > 0) {
			String[] parts123456 = frame_strTmp.split(",");
			frame = new GameGraphic[parts123456.length];
			for (int qqq = 0; qqq < parts123456.length; qqq++) {
				frame[qqq] = new GameGraphic(parts123456[qqq]);
			}
		}
		String sound_strTmp = ((StringEntry) readField("sound",
				new StringEntry(""))).getString();
		if (sound_strTmp.length() > 0) {
			sound = new GameAudio(sound_strTmp);
		}
	}

	public void autoWrittenSerializeCode() {
		writeField("frameResolution", new IntEntry(frameResolution));
		writeField("keyFrames", new IntArrayEntry(keyFrames));
		writeField("loopAfter", new IntEntry(loopAfter));
		writeField("frame", new StringEntry(
				frame != null ? hashAllToString(frame) : ""));
		writeField("sound", new StringEntry(sound != null ? sound
				.hashToString() : ""));
	}

	public static class GameGraphic extends GameDataBase {
		public GameGraphic(String hash) {
			super(hash);
		}

		public String filename;
		/**
		 * Negative values mean PERCENTS! so, -1 means 0% and -101 means 100%
		 * 
		 * It is also in X,Y,W,H form so don't get confused
		 */
		@DefaultValue(value = "-1,-1,-101,-101")
		public int[] rect;

		public void autoWrittenDeSerializeCode() {
			filename = ((StringEntry) readField("filename", new StringEntry(
					"[default]"))).getString();
			rect = ((IntArrayEntry) readField("rect", new IntArrayEntry(
					new int[] { -1, -1, -101, -101 }))).getIntArray();
		}

		public void autoWrittenSerializeCode() {
			writeField("filename", new StringEntry(filename));
			writeField("rect", new IntArrayEntry(rect));
		}

		public void setRect(Rectangle2D.Float evalRectOrig, PImage img,
				int wGridSize, int hGridSize) {
			rect[0] = PApplet.round(PApplet.constrain(evalRectOrig.x, 0, 1)
					* img.width / wGridSize)
					* wGridSize;
			rect[1] = PApplet.round(PApplet.constrain(evalRectOrig.y, 0, 1)
					* img.height / hGridSize)
					* hGridSize;
			rect[2] = PApplet.round(PApplet.constrain(evalRectOrig.x
					+ evalRectOrig.width, 0, 1)
					* img.width / wGridSize)
					* wGridSize - rect[0];
			rect[3] = PApplet.round(PApplet.constrain(evalRectOrig.y
					+ evalRectOrig.height, 0, 1)
					* img.height / hGridSize)
					* hGridSize - rect[1];
		}

		public Rectangle2D.Float evalRect(PImage img) {
			float[] values = new float[4];
			for (int k = 0; k < 4; k++) {
				if (rect[k] < 0) {
					values[k] = (-rect[k] - 1) / 100f;
				} else {
					float dim = 0;
					if (k % 2 == 0) {
						dim = img.width;
					} else {
						dim = img.height;
					}
					values[k] = rect[k] / dim;
				}
			}
			Rectangle2D.Float toRet = new Rectangle2D.Float(values[0],
					values[1], values[2], values[3]);
			return toRet;
		}
	}

	public static class GameAudio extends GameDataBase {
		public GameAudio(String hash) {
			super(hash);
		}

		public String filename;

		public void autoWrittenDeSerializeCode() {
			filename = ((StringEntry) readField("filename", new StringEntry(
					"[default]"))).getString();
		}

		public void autoWrittenSerializeCode() {
			writeField("filename", new StringEntry(filename));
		}
	}
}
