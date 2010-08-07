package BulletGame$1;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.swing.JFrame;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;
import TaiGameCore.GameDataBase;
import TaiGameCore.PressTypeThreshold;
import TaiGameCore.TaiImgMap;
import TaiGameCore.TaiShaders;
import TaiGameCore.TaiVBO;
import TaiScript.parsing.TaiScriptEditor;
import TaiScript.parsing.TaiScriptTxtInfo;

public abstract class BulletGame$1Engine$L1$1$OpenglTextRenderer extends
		BulletGame$1Engine$ABasicEngine {
	public BulletGame$1Engine$L1$1$OpenglTextRenderer(JFrame holder,
			PApplet hold) {
		super(holder, hold);
	}

	public class EditorTextSheet extends KeyAdapter {
		private Rectangle2D.Float textR;

		public EditorTextSheet(Rectangle2D.Float usableSpace, int linesToShow) {
			this(usableSpace, linesToShow, true, true, true, 1024, 16);
		}

		public EditorTextSheet(Rectangle2D.Float usableSpace, int linesToShow,
				boolean b, boolean c, boolean d) {
			this(usableSpace, linesToShow, b, c, d, 1024, 16);
		}

		public EditorTextSheet(Rectangle2D.Float usableSpace, int linesToShow,
				boolean drawBG, boolean editable, boolean showsemicolon,
				int FontStorageSize, int numberOfGlyphsSqrt) {
			RowHeight = 1.f / linesToShow; // .05f for 20 lines works pretty
			this.drawBG = drawBG;
			this.editable = editable;
			this.dontDrawSemicolons = !showsemicolon;
			// well.
			textR = usableSpace;
			tse = new TaiScriptEditor("");
			this.linesToShow = linesToShow;
			renderedGlyphLocations = new ArrayList[linesToShow];
			for (int k = 0; k < renderedGlyphLocations.length; renderedGlyphLocations[k] = new ArrayList(), k++)
				;
			rowNoise = new float[linesToShow];
			long myNoiseSeed = 109243;
			g.noiseSeed(myNoiseSeed);
			float noiseScale = 20;
			g.noiseDetail(10, .5f);
			for (int k = 0; k < rowNoise.length; k++) {
				rowNoise[k] = 50 * g.noise(k / 20f * noiseScale);
			}
			//Shader initialization
			GL2 gl = ((PGraphicsOpenGL) g.g).gl;
			myShaders = FILE_SYSTEM.loadShader("Shader2", gl);
			FontGlyphStorage = new TaiImgMap(numberOfGlyphsSqrt
					* numberOfGlyphsSqrt, FontStorageSize);
			TextBuffer = new TaiVBO(gl);
			for (Shader1VertShader attrib : Shader1VertShader.values()) {
				TextBuffer.registerAttrib(attrib, attrib.Type,
						attrib.attribNum, attrib.attribOff);
			}
		}

		public void useTSE(TaiScriptEditor target) {
			this.tse = target;
			WindowLine = Math.max(0, Math.min(tse.CaretLine, tse.Editing.size()
					- linesToShow));
			isTextModified = true;
			textWasModifiedThisFrame = true;
		}

		private PFont TextFont;
		public String defaultToFont = "SansSerif";
		public int defaultToFontSize = 24;
		private boolean selectable = true;
		private float TXTSCL = 1.0f;
		private boolean editable = true;
		private boolean drawBG = true;
		private final float RowHeight;
		private float LEFT_TXT_INDENT = .01f;
		private int linesToShow; // Less may actually be "on screen".
		private float[] rowNoise;
		private boolean dontDrawSemicolons = false;
		private float GL_X = 0, GL_Y = 0; // UPDATE ON ALL TRANSLATIONS.

		private boolean cleanedUp = false;

		public void cleanup() {
			cleanedUp = true;
			// FontGlyphStorage.cleanup(); PImages are cleanedup via finalize.
			FontGlyphStorage.cleanup((PGraphicsOpenGL) g.g);
			//Don't clean up the myShaders! Those are cached.
			FontGlyphStorage = null;
			TextBuffer = null;
		}

		public void finalize() {
			if (!cleanedUp) {
				throw new RuntimeException(
						"I was not cleaned up (EditTextScreeN)!!!" + " "
								+ tse.Editing);
			}
		}

		TaiShaders myShaders;
		TaiImgMap FontGlyphStorage;
		TaiVBO TextBuffer;

		public boolean hasMouseFocus = true;

		public void draw() {
			mouseLoc = new float[] {
					(g.mouseX / (float) g.width - textR.x)
							/ (float) textR.width,
					(g.mouseY / (float) g.height - textR.y)
							/ (float) textR.height };

			GL2 gl = ((PGraphicsOpenGL) g.g).gl;
			// isTextModified |= g.mousePressed;
			isTextModified |= isResized; // The coordinates change.
			if (tse.CaretLine > WindowLine + linesToShow - 1) {
				isTextModified = true; // New perspectives!
				WindowLine += tse.CaretLine - (WindowLine + linesToShow - 1);
			}
			if (tse.CaretLine < WindowLine) {
				isTextModified = true;// New perspectives!
				WindowLine -= WindowLine - tse.CaretLine;
			}
			float textDistance = 4; // Sadly, my text renderer doesn't support
			// perspective yet.
			if (drawBG) {
				g.pushMatrix();
				for (int k = WindowLine; k < WindowLine + linesToShow; k++) {
					float oldH = -textDistance;// sin(k/5f)*2-4;
					float newH = -textDistance * 1;// sin((k+1)/5f)*2-4;
					drawPaperBackgroundRow(k, oldH, newH);
					g.translate(0, RowHeight);
				}
				g.popMatrix();
			}
			if (false) { // Show the text map.
				g.tint(255, 255, 255, 255);
				g.image(FontGlyphStorage.getCombinedImage(), 0, 0, 1, 1);
			}
			g.g.flush();
			gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

			screen2D4GL(640, 480);
			GL_X = 0;
			GL_Y = 0;
			// ?
			if (isTextModified) {
				TextBuffer.reset();
			}
			int k = WindowLine;
			boolean showScrollUpArrow = k > 0;
			boolean showScrollDownArrow = (tse.Editing.size() - k) * RowHeight > 1;
			if (isTextModified || !tse.Selection.isEmpty() || true) {
				gl.glPushMatrix();
				for (; k < WindowLine + linesToShow && k < tse.Editing.size(); k++) {
					float oldH = -textDistance;// sin(k/5f)*2-4;
					float newH = -textDistance * 1;// sin((k+1)/5f)*2-4;
					GL_X += LEFT_TXT_INDENT; // A little offset.
					textRow(k, oldH, newH, isTextModified, new float[] { 1, 1,
							1, 1 });
					GL_X = 0;
					gl.glTranslatef(0, -GL_Y + (GL_Y += RowHeight), 0);
				}
				gl.glPopMatrix();
			}
			textWasModifiedThisFrame = isTextModified;
			isTextModified = false;
			gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

			// So, draw the TEXT on top of the other stuff:
			myShaders.switchToShader(myShaders);
			boolean oldVal = ((PGraphicsOpenGL) g.g).MAKE_MIPMAPS;
			((PGraphicsOpenGL) g.g).MAKE_MIPMAPS = false;
			(((PGraphicsOpenGL) g.g)).bindTexture(FontGlyphStorage
					.getCombinedImage());
			((PGraphicsOpenGL) g.g).MAKE_MIPMAPS = oldVal;
			// int id =
			// ((PGraphicsOpenGL.ImageCache)smiley.getCache(g.g)).tindex;
			gl.glEnable(GL.GL_TEXTURE_2D);
			TextBuffer.CurrentColor[0] = g.red(g.g.fillColor) / 255.f;
			TextBuffer.CurrentColor[1] = g.green(g.g.fillColor) / 255.f;
			TextBuffer.CurrentColor[2] = g.blue(g.g.fillColor) / 255.f;
			TextBuffer.drawQueuedElements();
			gl.glDisable(GL.GL_TEXTURE_2D);
			myShaders.switchToShader(null);
			screen2D();
			if (arrowScroll != null) {
				float rh = Math.max(RowHeight, .11f);
				float arrowWidth = rh, arrowHeight = rh * 1.8f;
				if (showScrollUpArrow) {
					g.pushMatrix();
					g.translate(1 - arrowWidth, arrowHeight);
					g.scale(1, -1);
					g.image(FILE_SYSTEM.getImg(arrowScroll), 0f, 0f,
							arrowWidth, arrowHeight);
					g.popMatrix();
				}
				if (showScrollDownArrow) {
					g.pushMatrix();
					g.translate(1 - arrowWidth, 1 - arrowHeight);
					g.image(FILE_SYSTEM.getImg(arrowScroll), 0f, 0f,
							arrowWidth, arrowHeight);
					g.popMatrix();
				}
			}
		}

		private float[] mouseLoc;
		public String arrowScroll = null;

		public void setArrowScrollGraphic(String arrow) {
			arrowScroll = arrow;
		}

		/**
		 * Pooled float arrays for ngonAddition
		 */
		private float[] textRow0 = new float[4];
		private float[] textRow1 = new float[4];
		private float[] textRow2 = new float[4];
		private float[] textRow3 = new float[4];
		private float[] textRow4 = new float[4];

		private float[] writeFloats(float[] arr, float... values) {
			for (int k = 0; k < values.length; k++) {
				arr[k] = values[k];
			}
			return arr;
		}

		private void textRow(final int k, final float oldH, final float newH,
				final boolean addToShader, final float[] Txtcolor) {
			long now = System.nanoTime();

			GL2 gl = ((PGraphicsOpenGL) g.g).gl;
			int bestIndex = -1;
			float[] highlightColor = new float[] { 31 / 255f, 79 / 255f,
					179 / 255f };
			if (addToShader) {
				// Clear location cache
				clearXcoordinates(k);
			}
			if (tse.Editing.size() <= k)
				return;
			int charIndex = 0;
			char lastChar = 0;

			for (char c : tse.Editing.get(k).toCharArray()) {
				boolean addToShaderTmp = addToShader;
				boolean lineEnd = false;
				boolean skipDraw = false;
				if (GL_X > 1) {
					lineEnd = true;
					skipDraw = true;
				}
				if (tse.CaretLine == k && tse.CaretPosition == charIndex
						&& selectable) {
					gl.glColor3f(255, 0, 0);
					gl.glBegin(gl.GL_LINES);
					gl.glVertex2f(0 + GL_X, 0);
					gl.glVertex2f(0 + GL_X, RowHeight);
					gl.glEnd();
				}
				if (c == TaiScriptEditor.LINE_END_SUBCHAR) {
					c = ';';
					lineEnd = true;
					//Alright, did this line end with {, or }?
					if (lastChar == '{' || lastChar == '}') {
						skipDraw = true;
					}
					if (dontDrawSemicolons) {
						skipDraw = true;
					}
				}
				float leftBound = GL_X;
				if (!skipDraw) {
					PFont TextFontTmp = TextFont;
					if (TextFontTmp == null) {
						/**
						 * IF it can't find it, it returns a font that only
						 * contains '?'
						 **/
						char lookup = c;
						if (Character.isWhitespace(c)) {
							lookup = 'a';
						}
						TextFontTmp = FILE_SYSTEM.getPFontFor(defaultToFont,
								lookup, defaultToFontSize);
					}

					int index = -1;
					int multiplyLetterWidth = 1;
					if (Character.isWhitespace(c)) {
						index = TextFontTmp.index('a');
						addToShaderTmp = false;
						//Use the size of the letter 'a'
						if (c == '\t') {
							//Workaround for tab
							multiplyLetterWidth = 4;
						}
					} else {
						index = TextFontTmp.index(c);
						if (index == -1) {
							index = TextFontTmp.index('?');
						}
					}
					if (index == -1) {
						index = 0;
					}
					float high = (float) TextFontTmp.height[index];
					float bwidth = (float) TextFontTmp.width[index];
					float lextent = (float) TextFontTmp.leftExtent[index];
					float textent = (float) TextFontTmp.topExtent[index];

					/**
					 * Performance needs work.
					 */
					float baseLine = TextFontTmp.size + 1;
					float yscl = RowHeight * TextFontTmp.size / 725 * TXTSCL;
					float charW = .06f / (textR.width) * TextFontTmp.size / 725
							* TXTSCL / NSPH;
					// System.out.println(charW);
					// System.out.println(xscl+" "+yscl);

					// Careful, charWToMove must aliased
					float charWToMove = (TextFontTmp.setWidth[index]) * charW;
					charWToMove = ((int) (charWToMove * currentViewPortWidth))
							/ (float) currentViewPortWidth
							* multiplyLetterWidth;

					if (selectable
							&& tse.Selection.isInsideRegion(k, charIndex)) {
						// Single-character highlights are slow.
						if (tse.Selection.LineBegin == k
								|| tse.Selection.LineEnd == k) {
							gl.glColor3f(highlightColor[0], highlightColor[1],
									highlightColor[2]);
							gl.glBegin(gl.GL_QUADS);
							gl.glVertex3f(0 + GL_X, 0, oldH);
							gl.glVertex3f(charWToMove + GL_X, 0, oldH);
							gl.glVertex3f(charWToMove + GL_X, RowHeight, newH);
							gl.glVertex3f(0 + GL_X, RowHeight, newH);
							gl.glEnd();
						}
					}
					markXcoordinate(k, charIndex, GL_X);
					if (addToShaderTmp) {
						float charLeft = charW * (lextent);
						float charRight = charW * (lextent + bwidth);
						float dispHRatio = (baseLine - textent) * yscl;
						float bottomHRatio = dispHRatio + (high) * yscl;

						PImage img = TextFontTmp.images[index];
						// Calculate the image positiosn
						float imgWidth = ((float) bwidth)
								/ FontGlyphStorage.width;
						float imgHeight = ((float) high)
								/ FontGlyphStorage.height;

						//PERFORMANCE!
						float[] position = textRow0;
						FontGlyphStorage.addImage(img, position);
						float[] toMapTo = writeFloats(textRow1, charLeft,
								dispHRatio, charRight, bottomHRatio);
						float[] center = writeFloats(textRow2,
								(toMapTo[0] + toMapTo[2]) / 2,
								(toMapTo[1] + toMapTo[3]) / 2);
						float[] sizes = writeFloats(textRow3, toMapTo[2]
								- toMapTo[0], toMapTo[3] - toMapTo[1]);
						// Texture info.
						float[] chunk = writeFloats(textRow4, position[0],
								position[1], imgWidth, imgHeight);

						//ADD!
						TextBuffer.addNgon(4, Shader1VertShader.Center_X, GL_X
								+ center[0], Shader1VertShader.Center_Y, GL_Y
								+ center[1], Shader1VertShader.RectWidth,
								sizes[0], Shader1VertShader.RectHeight,
								sizes[1], Shader1VertShader.Rotation, 0f,
								Shader1VertShader.X_TexOffset, chunk[0],
								Shader1VertShader.Y_TexOffset, chunk[1],
								Shader1VertShader.TexScaleX, chunk[2],
								Shader1VertShader.TexScaleY, chunk[3]
						// Shader1VertShader.tint, Txtcolor
								);
					}
					GL_X += charWToMove; // Getting rid of matrix
					// operations in these
					// loops!!!
					// Next char:
					if (!Character.isWhitespace(c)) {
						lastChar = c;
					}
				}
				if (lineEnd) { // Fill in the lines.
					float offsetLeft = GL_X;
					if (!(tse.Selection.LineBegin == k || tse.Selection.LineEnd == k)) {
						// Fill in whole line.
						offsetLeft = LEFT_TXT_INDENT;
					}
					if (selectable
							&& tse.Selection.isInsideRegion(k, charIndex)) { // Rest-of-line
						// highlight
						gl.glColor3f(highlightColor[0], highlightColor[1],
								highlightColor[2]);
						gl.glBegin(gl.GL_QUADS);
						gl.glVertex3f(0 + offsetLeft, 0, oldH);
						gl.glVertex3f(1 + offsetLeft, 0, oldH); // Just
						// very
						// very
						// long,
						// please.
						gl.glVertex3f(1 + offsetLeft, RowHeight, newH);
						gl.glVertex3f(0 + offsetLeft, RowHeight, newH);
						gl.glEnd();
					}
				}
				float[] topBotBound = new float[] { GL_Y, GL_Y + RowHeight };
				// Mouse over?
				if (mouseLoc[0] >= leftBound && mouseLoc[1] >= topBotBound[0]
						&& mouseLoc[1] < topBotBound[1]) {
					// System.out.println(charIndex+" "+GL_Y+" "+GL_X+" "+Arrays.toString(mouseLoc)+" "+Arrays.toString(topBotBound));
					bestIndex = charIndex;
				}
				charIndex++;
			}

			if (bestIndex != -1) {
				if (g.mousePressed && hasMouseFocus) {
					if (draggingSelection < 0) {
						if (truth(keyboard.get(KeyEvent.VK_SHIFT))) {
							draggingSelection = 1;
						} else {
							tse.CaretLine = k;
							tse.CaretPosition = bestIndex;
							draggingSelection = 0.1;
							beginDraggingSelection = System.nanoTime();
							tse.Selection = new TaiScriptTxtInfo(k, bestIndex,
									k, bestIndex);
							tse.Selection.TemporaryDisable = true;
						}
					}
					if (draggingSelection < 1) {
						draggingSelection = (System.nanoTime() - beginDraggingSelection) / 1e9 / .1; // So,
						// .1
						// second
					} else {
						if (tse.Selection.TemporaryDisable) {
							if (k != tse.Selection.LineBegin
									|| bestIndex != tse.Selection.CharBegin) {
								tse.Selection.TemporaryDisable = false;
							}
						}
						if (!tse.Selection.TemporaryDisable) {
							tse.Selection = new TaiScriptTxtInfo(tse.CaretLine,
									tse.CaretPosition, k, bestIndex);
						}
					}
				} else {
					draggingSelection = -1;
				}
			}

			// System.out.println((System.nanoTime()-now)/1e9);
		}

		private long beginDraggingSelection;
		private double draggingSelection = -1;

		private void drawPaperBackgroundRow(int k, float oldH, float newH) {
			g.noStroke();
			g.fill(rowNoise[k % rowNoise.length]);
			g.beginShape();
			g.vertex(0, 0, oldH);
			g.vertex(1, 0, oldH);
			g.vertex(1, RowHeight, newH);
			g.vertex(0, RowHeight, newH);
			g.endShape();
		}

		public boolean isTextModified = true;
		public boolean textWasModifiedThisFrame = true;

		public void keyPressed(KeyEvent e) {
			if (!editable && !selectable) {
				return;
			}
			if (e.getID() == KeyEvent.KEY_RELEASED) {
				arrowKeysText.release();
				return;
			}
			if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
				if (e.isControlDown()) {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_C:
						tse.copy();
						break;
					case KeyEvent.VK_X:
						if (!tse.Selection.isEmpty()) {
							tse.copy();
							tse.backspace();
							isTextModified = true;
						}
						break;
					case KeyEvent.VK_V:
						if (editable) {
							tse.paste();
							isTextModified = true;
						}
						break;
					case KeyEvent.VK_A:
						tse.selectAll();
						break;
					}
				} else if (e.getID() == KeyEvent.KEY_TYPED && editable) {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_BACK_SPACE:
						tse.backspace();
						break;
					case KeyEvent.VK_ENTER:
						tse.line();
						break;
					case KeyEvent.VK_TAB:
						tse.tab();
						break;
					case KeyEvent.VK_DELETE:
						if (tse.Selection.TemporaryDisable) {
							int old1 = tse.CaretLine, old2 = tse.CaretPosition;
							tse.caretRight();
							// If we actually moved
							if (old1 != tse.CaretLine
									|| old2 != tse.CaretPosition)
								tse.backspace();
						} else {
							tse.backspace();
						}
						break;
					case KeyEvent.VK_CONTROL:
						break;
					case KeyEvent.VK_ESCAPE:
						break;
					default:
						tse.insert(e.getKeyChar());
					}
					isTextModified = true;
				} else {
					int beforeL = tse.CaretLine, beforeC = tse.CaretPosition; // May
					// not
					// be
					// used.
					switch (e.getKeyCode()) {
					case KeyEvent.VK_END:
						tse.CaretPosition = tse.Editing.get(tse.CaretLine)
								.length() - 1;
						break;
					case KeyEvent.VK_HOME:
						tse.CaretPosition = 0;
						break;
					case KeyEvent.VK_LEFT:
						if (arrowKeysText.isTypeTime(KeyEvent.VK_LEFT))
							tse.caretLeft();
						break;
					case KeyEvent.VK_RIGHT:
						if (arrowKeysText.isTypeTime(KeyEvent.VK_RIGHT))
							tse.caretRight();
						break;
					case KeyEvent.VK_DOWN:
						if (arrowKeysText.isTypeTime(KeyEvent.VK_DOWN))
							tse.caretDown();
						break;
					case KeyEvent.VK_UP:
						if (arrowKeysText.isTypeTime(KeyEvent.VK_UP))
							tse.caretUp();
						break;
					case KeyEvent.VK_PAGE_DOWN:
						if (arrowKeysText.isTypeTime(KeyEvent.VK_PAGE_DOWN)) {
							for (int k = 0; k < linesToShow; k++)
								tse.caretDown();
						}
						break;
					case KeyEvent.VK_PAGE_UP:
						if (arrowKeysText.isTypeTime(KeyEvent.VK_PAGE_UP)) {
							for (int k = 0; k < linesToShow; k++)
								tse.caretUp();
						}
						break;
					default:

					}
					// If we moved somewhere, affect selection.
					if (tse.CaretLine != beforeL
							|| tse.CaretPosition != beforeC) {
						if (truth(keyboard.get(KeyEvent.VK_SHIFT))) {
							if (tse.Selection.TemporaryDisable) {
								tse.Selection = new TaiScriptTxtInfo(beforeL,
										beforeC, beforeL, beforeC);
							}
							// Set the end of the selection to be the "new"
							// cursor.
							tse.Selection = new TaiScriptTxtInfo(
									tse.Selection.LineBegin,
									tse.Selection.CharBegin, tse.CaretLine,
									tse.CaretPosition);
						} else {
							singleWidthSelect();
						}
					}
				}
			} else {
			}
		}

		/**
		 * Sets the selection to be the empty selection, at the current
		 * tse.CaretLine, tse.CaretPosition
		 */
		public void singleWidthSelect() {
			tse.Selection = new TaiScriptTxtInfo(tse.CaretLine,
					tse.CaretPosition, tse.CaretLine, tse.CaretPosition);
			tse.Selection.TemporaryDisable = true;
		}

		private PressTypeThreshold arrowKeysText = new PressTypeThreshold(.2,
				.03);
		private int WindowLine = 0;

		public int getLinesToShow() {
			return linesToShow;
		}

		public int getWindowLine() {
			return WindowLine;
		}

		public TaiScriptEditor tse;

		/**
		 * 0 means unlimited.
		 * 
		 * Restricts the number of lines to 'i' and the number of columns to 'j'.
		 */
		public void setTextRestrictions(int i, int j) {
			tse.sizeRestrictions(i, j);
		}

		public void setDisplaysLineEnd(boolean displayLineEnd) {
			dontDrawSemicolons = !displayLineEnd;
		}

		public String getRowText(int i) {
			//Hmm, should I not return the line end char?
			String value = tse.Editing.get(i);
			value = value.substring(0, value.length() - 1); //LINE_END is a character
			return value;
		}

		private void markXcoordinate(int rowNumber, int glyphId, float value) {
			int k = rowNumber - WindowLine;
			while (glyphId >= renderedGlyphLocations[k].size()) {
				renderedGlyphLocations[k].add(0f);
			}
			renderedGlyphLocations[k].set(glyphId, value);
		}

		private void clearXcoordinates(int rowNumber) {
			int k = rowNumber - WindowLine;
			renderedGlyphLocations[k].clear();
		}

		/**
		 * Gets the x-coordinate of glypy <i>glyphId</i> of the text row
		 * <i>rowNumber</i>.
		 */
		public float getXcoordinate(int rowNumber, int glyphId) {
			if (rowNumber < WindowLine || rowNumber >= WindowLine + linesToShow) {
				throw new ArrayIndexOutOfBoundsException("Text row "
						+ rowNumber
						+ " was not rendered this frame! call draw() first.");
			}
			if (glyphId < 0 || glyphId >= tse.Editing.get(rowNumber).length()) {
				throw new ArrayIndexOutOfBoundsException("Invalid glyphId: "
						+ glyphId);
			}
			int k = rowNumber - WindowLine;
			if (glyphId >= renderedGlyphLocations[k].size()) {
				return 1;
			}
			return renderedGlyphLocations[k].get(glyphId);
		}

		private ArrayList<Float>[] renderedGlyphLocations;// linesToShow

		public void setTextFont(PFont font) {
			TextFont = font;
		}

		public void scaleText(float scale) {
			TXTSCL = scale;
		}

		public void setSelectable(boolean b) {
			selectable = b;
		}
	}

	public class TaiTextBox {
		public EditorTextSheet ets;

		public EditorTextSheet getText() {
			return ets;
		}

		public Rectangle2D.Float area;

		public TaiTextBox(double x, double y, double w, double h, int numLines) {
			this(new Rectangle2D.Float((float) x, (float) y, (float) w,
					(float) h), numLines);
		}

		public TaiTextBox(Rectangle2D.Float area, int numLines) {
			this(area, numLines, false);
		}

		public TaiTextBox(Rectangle2D.Float area, int numLines, boolean editable) {
			this(area, numLines, editable, 256, 8);
		}

		public TaiTextBox(Rectangle2D.Float area, int numLines,
				boolean editable, int size, int numGlypsSqrt) {
			this.area = area;
			if (!editable) {
				ets = new EditorTextSheet(area, numLines, false, false, false,
						size, numGlypsSqrt);
				ets.setSelectable(false);
			} else {
				ets = new EditorTextSheet(area, numLines, false, true, false,
						size, numGlypsSqrt);
				ets.setSelectable(true);
			}

			ets.setArrowScrollGraphic(null);
			ets.setTextRestrictions(numLines, 0);
			addSubKeyListener(ets);
			this.editable = editable;
			postConstructor();
		}

		public void setArea(Rectangle2D.Float area) {
			this.area = area;
			ets.textR = area;
		}

		private boolean editable;

		public boolean isEditable() {
			return editable;
		}

		public void postConstructor() {

		}

		public void useFont(PFont arcadeFont) {
			ets.setTextFont(arcadeFont);
		}

		private float txtScl = 1;

		public void setTextScale(float scl) {
			txtScl = scl;
		}

		private String lastString = "";

		public void setText(String string) {
			if (lastString.equals(string)) {
				return;
			}
			//else
			lastString = string;
			ets.isTextModified = true;
			ets.tse.newFile();
			ets.tse.insertText(string);
		}

		public void setTextRow(String row, int rowNum) {
			if (ets.getRowText(rowNum).equals(row)) { //Can't do equals; line ending char
				return;
			}
			//else
			ets.isTextModified = true;
			ets.tse.setLine(row, rowNum);
		}

		public void draw() {
			viewport(area.x, area.y, area.width, area.height);
			if (false) {
				outlineViewport();
			}
			ets.scaleText(txtScl);
			ets.draw();
			viewport(0, 0, 1, 1);
		}

		public void cleanup() {
			ets.cleanup();
			removeSubKeyListener(ets);
		}
	}

	public class SaveGameDialog extends ModalDialog {
		public SaveGameDialog(BulletGameScreen parent, final Runnable doAfter,
				GameDataBase saveObj, String arrowToShow) {
			super(parent, new ModalDialogCallback<SaveGameDialog>() {
				public void dialogFinished(SaveGameDialog self) {
					self.cleanup();
					doAfter.run();
				}
			});
			hash = saveObj.hashToString();
			StringBuffer neoHash = new StringBuffer();
			int count = 0;
			for (char k : hash.toCharArray()) {
				neoHash.append(k);
				if (count++ % 48 == 47) {
					neoHash.append("\n");
				}
			}
			hash = neoHash.toString();
			//Certain number of 
			area = new Rectangle2D.Float(.1f, .05f, .8f, .8f);
			innerArea = new Rectangle2D.Float(0f, .2f, 1f, .79f);
			scaleRect(innerArea, area);
			ets = new TaiTextBox(innerArea, 24);
			ets.setTextScale(.4f);
			ets.ets.setSelectable(true);
			ets.ets.setTextRestrictions(0, 0);
			ets.setText(hash);
			ets.ets.setArrowScrollGraphic(arrowToShow);
			ets.ets.tse.selectAll();

			Rectangle2D.Float topArea = new Rectangle2D.Float(0f, 0f, 1f, .2f);
			scaleRect(topArea, area);
			display = new TaiTextBox(topArea, 2);
			display.setTextScale(.6f);
			display
					.setText("Your save game is below. Copy it to your clipboard (make sure \nyou select all of it, try CTRL+A), and press ESCAPE to return.");
		}

		public void cleanup() {
			display.cleanup();
			ets.cleanup();
		}

		private Rectangle2D.Float area;
		private Rectangle2D.Float innerArea;
		private String hash;
		private TaiTextBox ets;
		private TaiTextBox display;

		public boolean drawDialog() {
			viewport(area);
			g.fill(0);
			g.rect(0, 0, 1, 1);
			g.fill(255);
			display.draw();
			ets.draw();
			return false;
		}
	}

	/**
	 * Maps a rectangle (0,0,- 1, 1) onto another.
	 * TexPlace is (first parameter) adjusted
	 */
	public void scaleRect(Rectangle2D.Float texPlace,
			Rectangle2D.Float dialogPlace) {
		texPlace.x = dialogPlace.width * texPlace.x + dialogPlace.x;
		texPlace.y = dialogPlace.height * texPlace.y + dialogPlace.y;
		texPlace.width *= dialogPlace.width;
		texPlace.height *= dialogPlace.height;
	}

	/**
	 * UnMaps a rectangle (0,0,- 1, 1) onto another.
	 * TexPlace is (first parameter) adjusted
	 */
	public void unScaleRect(Rectangle2D.Float texPlace,
			Rectangle2D.Float dialogPlace) {
		texPlace.x = (texPlace.x - dialogPlace.x) / dialogPlace.width;
		texPlace.y = (texPlace.y - dialogPlace.y) / dialogPlace.height;
		texPlace.width /= dialogPlace.width;
		texPlace.height /= dialogPlace.height;
	}
}
