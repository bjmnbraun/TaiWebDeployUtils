package TaiGameCore;


/**
 * Represents a position in the TaiScript.
 */
public class TaiScriptTxtInfo extends GameDataBase{
	public TaiScriptTxtInfo(String hash){
		super(hash);
	}
	/**
	 * Does NOT include CharEnd!!!
	 */
	public TaiScriptTxtInfo(int LineStart, int CharBegin, int LineEnd, int CharEnd) {
		super("");
		this.LineBegin = LineStart;
		this.LineEnd = LineEnd;
		this.CharBegin = CharBegin;
		this.CharEnd = CharEnd;
	}
	public boolean isBackwards(){
		boolean needsSwap = false;
		if (LineEnd == LineBegin){
			if (CharEnd < CharBegin)
				needsSwap = true;
		} else if (LineEnd < LineBegin){
			needsSwap = true;
		}
		return needsSwap;
	}
	public boolean isInsideRegion(int line, int column){
		if (TemporaryDisable) return false;
		boolean needsSwap = isBackwards();
		
		int ActualLineEnd = needsSwap?LineBegin:LineEnd;
		int ActualLineBegin = !needsSwap?LineBegin:LineEnd;
		int ActualCharEnd = needsSwap?CharBegin:CharEnd;
		int ActualCharBegin = !needsSwap?CharBegin:CharEnd;
		
		int DirectionalCharEnd = ActualCharEnd;
		if (needsSwap){
			DirectionalCharEnd--;
		}
		//LineBegin / CharBegin represent the CARAT position.

		if (line < ActualLineBegin){
			return false;
		} else if (line==ActualLineBegin){
			if (ActualLineBegin==ActualLineEnd){
				return column >= ActualCharBegin && column < ActualCharEnd;
			} else {
				return column >= ActualCharBegin;
			}
		} else if (line < ActualLineEnd){
			return true;
		} else if (line == ActualLineEnd){
			return column < ActualCharEnd;
		}
		return false;
	}
	public int LineBegin;
	public int LineEnd;
	public int CharBegin;
	public int CharEnd;
	public boolean TemporaryDisable = false;
	public void flip() {
		int tempLine = LineBegin;
		int tempChar = CharBegin;
		LineBegin = LineEnd;
		CharBegin = CharEnd;
		LineEnd = tempLine;
		CharEnd = tempChar;
	}
	public boolean isEmpty() {
		return TemporaryDisable;
	}
	public void autoWrittenDeSerializeCode(){
		LineBegin = ((IntEntry)readField("LineBegin", new IntEntry())).getInt();
		LineEnd = ((IntEntry)readField("LineEnd", new IntEntry())).getInt();
		CharBegin = ((IntEntry)readField("CharBegin", new IntEntry())).getInt();
		CharEnd = ((IntEntry)readField("CharEnd", new IntEntry())).getInt();
		TemporaryDisable = ((IntEntry)readField("TemporaryDisable", new IntEntry())).getInt()==1;
	}
	public void autoWrittenSerializeCode(){
		writeField("LineBegin", new IntEntry(LineBegin));
		writeField("LineEnd", new IntEntry(LineEnd));
		writeField("CharBegin", new IntEntry(CharBegin));
		writeField("CharEnd", new IntEntry(CharEnd));
		writeField("TemporaryDisable", new IntEntry(TemporaryDisable?1:0));
	}
	
}
