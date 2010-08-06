package TaiGameCore;

import processing.core.PApplet;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

/**
 * Combines PImages for 1 giant OPENGL texture
 * @author Benjamin
 */
public class TaiImgMap {
	private int TEX_SIZE;
	private int rowCount;
	private int colCount;
	private int usedImages;
	private PImage[] images;
	private float sizeEachX, sizeEachY;
	public TaiImgMap(int numImages, int size){
		TEX_SIZE = size;
		rowCount = (int) Math.floor(Math.sqrt(numImages));
		colCount = (int) Math.ceil(Math.sqrt(numImages));
		sizeEachX = 1f/colCount;
		sizeEachY = 1f/rowCount;
		images = new PImage[numImages];
		combine = new PImage(TEX_SIZE, TEX_SIZE, PApplet.ARGB);
		combine.blit_resize_smooth = true;
		width = combine.width;
		height = combine.height;
	}
	public void clear(){
		usedImages = 0;
	}
	private PImage combine;
	public void addImage(PImage img, float[] toRet) {
		getLocation(addImage0(img),toRet);
	}
	private int addImage0(PImage img){
		int alreadyhas = -1;
		for(int k = 0; k < images.length; k++){
			if (img==images[k]){
				alreadyhas = k;
				break;
			}
		}
		if (alreadyhas!=-1){
			return alreadyhas;
		}
		int ind = usedImages;
		if (ind==images.length-1){
			System.err.println("WARNING: out of textures in TaiImgMap!");
			return images.length-1;
		}
		images[ind]=img;

		float[] positions = new float[2]; 
		getLocation(ind,positions);
		int xTexDraw = (int) (positions[0]*combine.width);
		int yTexDraw = (int) (positions[1]*combine.height);
		for(int x = 0; x < img.width; x++){
			for(int y = 0; y < img.height; y++){
				combine.set(x+xTexDraw,y+yTexDraw,img.get(x,y));//img.get(x,y);
			}
		}
		combine.updatePixels();
		//GC the other pixels:
		//images[ind].pixels = null;

		usedImages++;
		return ind;
	}
	public void getLocation(int imgInd, float[] toRet){
		int row = imgInd/colCount;
		int col = imgInd%colCount;
		toRet[0] = col*sizeEachX;
		toRet[1] = row*sizeEachY;
	}
	public PImage getCombinedImage() {
		return combine;
	}
	public final int width;
	public final int height; 
	public int getTempImgHeight() {
		return combine.height;
	}

	public int getTempImgWidth() {
		return combine.width;
	}
	public void cleanup(PGraphicsOpenGL gon) {
		combine.removeCache(gon);
		combine.parent = null;
		combine = null;
		images = null;
	}
}
