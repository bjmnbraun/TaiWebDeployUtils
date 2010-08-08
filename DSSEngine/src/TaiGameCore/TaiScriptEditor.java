package TaiGameCore;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JTextArea;


public class TaiScriptEditor extends GameDataBase{
	public TaiScriptEditor(){
		this("");
	}
	public TaiScriptEditor(String hash) {
		super(hash);
		if (Selection==null){
			Selection = new TaiScriptTxtInfo("");
		}
	}
	public static char LINE_END_SUBCHAR = (char)5;
	public ArrayList<String> Editing;
	public int CaretLine;
	public int CaretPosition;
	public TaiScriptTxtInfo Selection;
	
	public void insert(char c) {
		if (!Selection.TemporaryDisable){
			deleteSelection();
		}
		String line = Editing.get(CaretLine);
		if (BoundedMaxColumns>0 && line.length() >= BoundedMaxColumns){
			return;
		}
		String before = line.substring(0,CaretPosition);
		String after = line.substring(CaretPosition);
		Editing.set(CaretLine, before+c+after);
		CaretPosition++;
		hasUnsavedChanges = true;
	}
	public void line() {
		line(true); //Autotab
	}
	public void line(boolean autoTab) {
		if (!Selection.TemporaryDisable){
			deleteSelection();
		}
		if (BoundedMaxLines>0 && Editing.size() >= BoundedMaxLines){
			return; //Don't do it!
		}
		String line = Editing.get(CaretLine);
		//Detect the indentation:
		String cloneWhitespace = "";
		if (autoTab){
			for(char k : line.toCharArray()){
				if (k!='\t'){
					break;
				}
				cloneWhitespace+='\t'; //Copy the number of tabs.
			}
		}
		String before = line.substring(0,CaretPosition);
		String after = cloneWhitespace + line.substring(CaretPosition,line.length()-1);
		//Split the line, inserting the second half before the next.
		Editing.set(CaretLine, before+LINE_END_SUBCHAR);
		Editing.add(CaretLine+1,after+LINE_END_SUBCHAR);
		CaretPosition = cloneWhitespace.length(); //put cursor at same level as line
		CaretLine++;
		hasUnsavedChanges = true;
	}
	/**
	 * This is sort of a hack, it sets a full line of text. Note that the selection, 
	 * the caret line, etc etc are all cleared.
	 */
	public void setLine(String text, int line){
		//resetCaret();
		CaretPosition = Math.min(CaretPosition,text.length()-1);
		Editing.set(line,text+LINE_END_SUBCHAR);
		hasUnsavedChanges = true;
	}
	public void backspace() {
		if (!Selection.TemporaryDisable){
			deleteSelection();
		} else {
			if (CaretPosition>0){
				Selection = new TaiScriptTxtInfo(CaretLine,CaretPosition,CaretLine,CaretPosition-1);
			} else if (CaretLine>0){
				Selection = new TaiScriptTxtInfo(CaretLine,CaretPosition,CaretLine-1,
						Editing.get(CaretLine-1).length()-1);
			} else {
				return; //Uhh... Can't do a backspace here!
			}
			deleteSelection();
		}
	}
	public void deleteSelection(){
		boolean recurseBackwards = Selection.isBackwards();
		if (!recurseBackwards){
			recurseBackwards=true;
			Selection.flip();
		}
		//Meh, This one can always go backwards.
		if (recurseBackwards){
			int indexOnEnd = -1;
			if (Selection.LineEnd==Selection.LineBegin){
				indexOnEnd = Selection.CharBegin;
			}
			removeOnLine(Selection.LineEnd, Selection.CharEnd, indexOnEnd);
			if (Selection.LineEnd!=Selection.LineBegin){
				Editing.set(Selection.LineEnd, getContentLine(Selection.LineEnd)+getContentLine(Selection.LineBegin).substring(Selection.CharBegin)+LINE_END_SUBCHAR);
			}
			int numRemoved = 0;
			for(int line = Selection.LineBegin; line > Selection.LineEnd; line--){
				Editing.remove(line);
				numRemoved++;
			}
			Selection.LineBegin = CaretLine = Selection.LineEnd;
			Selection.CharBegin = CaretPosition = Selection.CharEnd;
		}
		//At the end, make an empty selection.
		Selection.TemporaryDisable = true;
	}
	public String getSelection(){
		StringWriter sr = new StringWriter();
		PrintWriter out = new PrintWriter(sr);
		boolean selectionWasBackwards = Selection.isBackwards();
		if (selectionWasBackwards){ //So, make it forwards.
			Selection.flip();
		}; 
		int charLoc = Selection.CharBegin;
		boolean isOneLiner = (Selection.LineEnd - Selection.LineBegin == 0);
		for(int line = Selection.LineBegin; line <= Selection.LineEnd; line++){
			if (line==Selection.LineEnd){
				out.print(getContentLine(line).substring(charLoc,Selection.CharEnd));
			} else {
				out.println(getContentLine(line).substring(charLoc));
				charLoc=0; //For next line.
			}
		}
		if (selectionWasBackwards){ //Flip it back.
			Selection.flip();
		};
		if (!isOneLiner){
			out.println(); //Peel this off when pasting.
		}
		return sr.toString();
	}
	/**
	 * Returns the line without the lineSubChar Ending...
	 */
	private String getContentLine(int lineEnd) {
		return Editing.get(lineEnd).substring(0,Editing.get(lineEnd).length()-1);
	}
	/**
	 * TODO: may Crash!
	 */
	private void removeOnLine(int line, int start, int end) {
		String after = "";
		if (end!=-1){
			after = Editing.get(line).substring(end);
		}
		String result = Editing.get(line).substring(0,start)+after;
		if (!result.endsWith(""+LINE_END_SUBCHAR)){
			result+=LINE_END_SUBCHAR;
		}
		Editing.set(line,result);
		hasUnsavedChanges = true;
	}
	public void caretLeft() {
		if (CaretPosition==0){
			if (CaretLine==0){
				return; //Nothing to do.
			}
			CaretLine--;
			CaretPosition = Editing.get(CaretLine).length()-1;
		} else {
			CaretPosition--;
		}
	}
	public void caretRight() {
		if (CaretPosition==Editing.get(CaretLine).length()-1){
			if (CaretLine==Editing.size()-1){
				return;//Nothing to do.
			}
			CaretLine++;
			CaretPosition = 0;
		} else {
			CaretPosition++;
		}
	}
	public void tab() {
		insert('\t');
	}
	public void caretDown() {
		int toShiftRight = CaretPosition;
		if (CaretLine==Editing.size()-1){
			return; //Can't.
		}
		CaretLine++;
		CaretPosition = Math.min(toShiftRight,Editing.get(CaretLine).length()-1);
	}
	public void caretUp() {
		int toShiftRight = CaretPosition;
		if (CaretLine==0){
			return; //Can't;
		}
		CaretLine--;
		CaretPosition = Math.min(toShiftRight,Editing.get(CaretLine).length()-1);
	}
	public static String getClipboardContents(){
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		//odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		boolean hasTransferableText =
			(contents != null) &&
			contents.isDataFlavorSupported(DataFlavor.stringFlavor)
			;
		if ( hasTransferableText ) {
			try {
				result = (String)contents.getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException ex){
				//highly unlikely since we are using a standard DataFlavor
				System.out.println(ex);
				ex.printStackTrace();
			}
			catch (IOException ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}
		return result;
	}
	public void copy() {
		setClipboardContents(getSelection());
	}
	public void insertText(String[] lines){
		for(String sr : lines){
			for(char c : sr.toCharArray()){
				insert(c);
			}
		}
	}
	/**
	 * Splits val on lines.
	 * @param val
	 */
	public void insertText(String val) {
		Scanner sr = new Scanner(val);
		while(sr.hasNextLine()){
			String line = sr.nextLine();
			for(char c : line.toCharArray()){
				insert(c);
			}
			if (sr.hasNextLine()){
				line(false); //No autotab here.
			}
		}
	}
	private void setClipboardContents(String aString){
		StringSelection stringSelection = new StringSelection( aString );
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, new ClipboardOwner(){
			public void lostOwnership(Clipboard clipboard, Transferable contents) {
			}
		});
	}
	public void paste() {
		String val = getClipboardContents();
		insertText(val);
	}
	private boolean hasUnsavedChanges;
	private int BoundedMaxLines = 0;
	private int BoundedMaxColumns = 0;
	public void setSaved(boolean b) {
		hasUnsavedChanges = !b;
	}
	public boolean hasUnsavedChanges(){
		return hasUnsavedChanges;
	}
	public void selectAll() {
		CaretLine = 0;
		CaretPosition = 0;
		Selection = new TaiScriptTxtInfo(0,0,Editing.size()-1,Editing.get(Editing.size()-1).length()-1);
	}
	public void selectLine(int lineNum){
		if (lineNum>=Editing.size() || lineNum<0){
			throw new ArrayIndexOutOfBoundsException("index: "+lineNum+" size: "+Editing.size());
		}
		CaretLine = lineNum;
		CaretPosition = 0;
		Selection = new TaiScriptTxtInfo(CaretLine,0,CaretLine,Editing.get(CaretLine).length()-1);
	}
	/**
	 * 0 means unlimited.
	 * 
	 * Restricts the number of lines to 'i' and the number of columns to 'j'.
	 */
	public void sizeRestrictions(int i, int j) {
		BoundedMaxLines = i;
		BoundedMaxColumns = j;	
	}
	public void resetCaret(){
		CaretLine = 0; CaretPosition = 0;
		Selection = new TaiScriptTxtInfo(0,0,0,0);
		Selection.TemporaryDisable = true;
	}
	public void clear() {
		Editing.clear();
		resetCaret();
		hasUnsavedChanges = false;
	}
	public void clearBlanks(){
		if (BoundedMaxLines==0){
			throw new RuntimeException("ClearBlanks can only be used on editors that have locked # of lines");
		}
		clear();
		for(int k = 0; k < BoundedMaxLines; k++){
			Editing.add(""+LINE_END_SUBCHAR);
		}
	}
	/**
	 * Calls clear, then puts a blank line in Editing (most like a new file.)
	 * IF you are calling this to prepare for loading an input file, use clear() instead.
	 */
	public void newFile(){
		clear();
		Editing.add(""+LINE_END_SUBCHAR);
	}
	
	public void autoWrittenDeSerializeCode(){
		customDeserializeEditing();
		CaretLine = ((IntEntry)readField("CaretLine", new IntEntry())).getInt();
		CaretPosition = ((IntEntry)readField("CaretPosition", new IntEntry())).getInt();
		String Selection_strTmp= ((StringEntry)readField("Selection", new StringEntry(""))).getString();
		if (Selection_strTmp.length()>0){
			Selection = new TaiScriptTxtInfo(Selection_strTmp);
		}
		hasUnsavedChanges = ((IntEntry)readField("hasUnsavedChanges", new IntEntry())).getInt()==1;
		BoundedMaxLines = ((IntEntry)readField("BoundedMaxLines", new IntEntry())).getInt();
		BoundedMaxColumns = ((IntEntry)readField("BoundedMaxColumns", new IntEntry())).getInt();
	}
	private void customDeserializeEditing() {
		String got = ((StringEntry)readField("Editing", new StringEntry(""+LINE_END_SUBCHAR))).getString();
		Scanner in = new Scanner(got);
		Editing = new ArrayList<String>();
		while(in.hasNextLine()){
			Editing.add(in.nextLine());
		}
		if (got.length()==0){
			newFile();
		}
	}
	public void autoWrittenSerializeCode(){
		customSerializeEditing();
		writeField("CaretLine", new IntEntry(CaretLine));
		writeField("CaretPosition", new IntEntry(CaretPosition));
		writeField("Selection", new StringEntry(Selection!=null?Selection.hashToString():""));
		writeField("hasUnsavedChanges", new IntEntry(hasUnsavedChanges?1:0));
		writeField("BoundedMaxLines", new IntEntry(BoundedMaxLines));
		writeField("BoundedMaxColumns", new IntEntry(BoundedMaxColumns));
	}
	private void customSerializeEditing() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for(String k : Editing){
			pw.println(k);
		}
		writeField("Editing", new StringEntry(sw.toString()));
	}
}
