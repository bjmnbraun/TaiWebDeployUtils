package TaiGameCore;

import java.awt.Font;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import javax.media.opengl.GL2;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import ddf.minim.AudioPlayer;
import ddf.minim.AudioSample;
import ddf.minim.Minim;

/**
 * Simple filesystem, to get my files.
 * @author Benjamin
 */
public class GameVirtualFS {
	private PApplet g;

	public GameVirtualFS(PApplet on) {
		g = on;
	}

	public String InputStreamTxtRead(InputStream is, String startAt)
			throws IOException {
		BufferedReader brv = new BufferedReader(new InputStreamReader(is));
		String vsrc = "";
		String line;
		boolean inAppend = false;
		while ((line = brv.readLine()) != null) {
			if (startAt == null || line.startsWith(startAt))
				inAppend = true;
			if (inAppend)
				vsrc += line + "\n";
		}
		return vsrc;
	}

	/*
	 * Opengl natives are at /data/glBin
	 * 
	 * NOTE: native libraries can't be read inside of jars, so "class-resource" is useless.

	public static void fixOpenglNativePath(){
		com.sun.gluegen.runtime.NativeLibLoader.disableLoading();
		com.sun.opengl.impl.NativeLibLoader.setLoadingAction(new LoaderAction(){
			{
				//Force the gluegen to load...
				loadLibrary("gluegen-rt", null, false, false);
			}
			public void loadLibrary(String arg0, String[] arg1, boolean arg2,
					boolean arg3) {
				String dir = new File("").getAbsolutePath()+String.format("%1$sdata%1$sglBin%1$s",File.separator);
				dir+=System.mapLibraryName(arg0);
				System.out.println(dir);		
				if (!new File(dir).exists()){
					System.err.println("WARNING: no library "+dir);
				} else
					System.load(dir);
			}
		});
	}
	 */
	public String getBaseCreatedFilesDirectory() {
		String userDir = System.getProperty("user.home") + File.separator
				+ ".bullethell1";
		if (!new File(userDir).exists()) {
			new File(userDir).mkdirs();
		}
		return userDir;
	}

	public String getBaseJarUrl() {
		String whole = GameVirtualFS.class.getResource("").toString();
		int remove = 2;
		if (!whole.startsWith("jar")) {
			remove = 3;
		}
		for (int i = 0; i < remove; i++)
			whole = whole.substring(0, whole.lastIndexOf("/"));
		whole += "/";
		//Ok, now we're at the base of THIS jar... but...
		if (whole.startsWith("jar")) {
			int the_1 = whole.lastIndexOf("_1");
			whole = whole.substring(0, the_1 + 1) + "2"
					+ whole.substring(the_1 + 2);
		}
		;
		System.out.println(whole);
		return whole;
	}

	/**
	 * For VLW fonts
	 */
	private HashMap<String, PFont> fonts = new HashMap();

	public PFont getFont(String string) {
		PFont got = fonts.get(string);
		if (got != null) {
			return got;
		}
		try {
			URL srcU = new URL(getBaseJarUrl() + "embed/font/" + string);//Get under the bulletGame$1package, then out of bin.
			//System.out.println(srcU);
			got = new PFont(srcU.openStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		fonts.put(string, got);
		return got;
	}

	public URL getSrcUrlForString(String string, String base)
			throws IOException {
		URL srcU = null;
		if (string.contains("http://") || string.contains("file://")) {
			srcU = new URL(string);
		} else {
			srcU = new URL(getBaseJarUrl() + base + string);//Get under the bulletGame$1package, then out of bin.
		}
		return srcU;
	}

	public PImage getImgFresh(final String string) {
		final PImage[] got = new PImage[1];
		final boolean[] done = new boolean[1];
		Thread made = new Thread() {
			public void run() {
				//System.out.println(srcU);
				try {
					URL srcU = getSrcUrlForString(string, "embed/imgs/");
					InputStream openStream = srcU.openStream();
					openStream.close();
					got[0] = g.loadImage(srcU.toString());
				} catch (IOException e) {
					got[0] = null;
				}
				done[0] = true;
			}
		};
		made.start();
		long now = System.nanoTime();
		while (!done[0]) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (System.nanoTime() - now > 3e9) {
				made.interrupt();
				return null;
			}
		}
		return got[0];
	}

	private HashMap<String, PImage> imgs = new HashMap();

	public void set404Image(String d404Img) {
		this.d404Img = d404Img;
	}

	private String d404Img = null;

	public PImage getImg(String string) {
		PImage got = imgs.get(string);
		if (got != null) {
			if (got.getCache(got.parent.g) != null) {
				got.pixels = null;
			}
			return got;
		}
		got = getImgFresh(string);
		if (got == null && d404Img != null && !string.equals(d404Img)) {
			got = getImg(d404Img); //allow only one chance. When you leave a screen, it refreshes.
		}
		if (got != null) {
			imgs.put(string, got);
			((PGraphicsOpenGL) g.g).setUnmodifiablePImage(got);
		}
		return got;
	}

	public void memoryHack_UnmodifyPImage(PImage p) {
		((PGraphicsOpenGL) g.g).setUnmodifiablePImage(p);
	}

	private HashMap<String, TaiShaders> shaders = new HashMap();

	/**
	 * Should be called inside gl's draw action.
	 */
	public TaiShaders loadShader(String string, GL2 gl) {
		TaiShaders got = shaders.get(string);
		if (got != null) {
			return got;
		}
		String vertTxt = getStringResource("embed/gl/" + string + ".vert",
				"!!ARBvp1.0");
		got = new TaiShaders(gl);
		got.initFromStrings(vertTxt);
		shaders.put(string, got);
		return got;
	}

	public String getStringResource(String string, String startAt) {
		String vertTxt = null;
		try {
			URL srcV = new URL(getBaseJarUrl() + string);//Get under the bulletGame$1package, then out of bin.
			vertTxt = InputStreamTxtRead(srcV.openStream(), startAt);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return vertTxt;
	}

	public AudioPlayer loadAudioFile(String string, Minim m) {
		try {
			URL srcU = getSrcUrlForString(string, "embed/sound/");
			return m.loadFile(srcU.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public AudioSample loadAudioSample(String string, Minim m) {
		try {
			URL srcU = getSrcUrlForString(string, "embed/sound/");
			return m.loadSample(srcU.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void clearImages() {
		Set<Entry<String, PImage>> keySet = imgs.entrySet();
		while (!keySet.isEmpty()) {
			Entry<String, PImage> toRem = keySet.iterator().next();
			unloadResource(toRem.getValue(), toRem.getKey(),
					(PGraphicsOpenGL) toRem.getValue().parent.g);
		}
	}

	public int getVersion() {
		try {
			URL srcV = new URL(getBaseJarUrl() + "embed/version");//Get under the bulletGame$1package, then out of bin.
			String[] loadStrings = g.loadStrings(srcV.openStream());
			return new Integer(loadStrings[0]);
		} catch (Throwable e) {
			//e.printStackTrace();
			return -1;
		}
	}

	/**
	 * To be used more later.
	 */
	public void unloadResource(Object res, String filename, PGraphicsOpenGL also) {
		if (res instanceof PFont) {
			PFont f = (PFont) res;
			for (PImage c : f.images) {
				c.removeCache(also);
			}
			fonts.remove(filename);
		}
		if (res instanceof PImage) {
			PImage g = (PImage) res;
			g.pixels = null;
			g.removeCache(also);
			imgs.remove(filename);
		}
		if (res instanceof TaiShaders) {
			TaiShaders resT = (TaiShaders) res;
			shaders.remove(filename);
			resT.cleanup();
		}
	}

	private HashMap<String, AwtPfontLink> awtFonts = new HashMap(5);

	private class AwtPfontLink {
		public Font awtFont;
		public HashMap<Character, PFont> pFonts = new HashMap(1000);
	}

	//How to make it so that each pfont has > 1 character? oh well.
	/**
	 * Uses createFont
	 */
	public PFont getPFontFor(String string, char c, int size) {
		String cacheName = string + "-size-" + size;
		AwtPfontLink got = awtFonts.get(cacheName);
		if (got == null) {
			Font fon = new Font(string, Font.PLAIN, size);
			got = new AwtPfontLink();
			got.awtFont = fon;
			awtFonts.put(cacheName, got);
		}
		PFont gotFont = got.pFonts.get(c);
		if (gotFont == null) {
			gotFont = new PFont(got.awtFont, true, new char[] { c });
			got.pFonts.put(c, gotFont);
		}
		return gotFont;
	}

	public void downloadFile(String string, URL url) throws IOException {
		File target = new File(string);
		if (target.exists()) {
			target.delete();
		}
		target.createNewFile();
		BufferedInputStream bis = new BufferedInputStream(url.openStream());
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(target));
		byte[] read = new byte[1024];
		int read2 = -1;
		while ((read2 = bis.read(read)) != -1) {
			bos.write(read, 0, read2);
		}
		bis.close();
		bos.close();
	}

	public boolean isOnline() {
		return getBaseJarUrl().contains("http:");
	}
}
