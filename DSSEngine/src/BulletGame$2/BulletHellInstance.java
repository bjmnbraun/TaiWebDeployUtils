package BulletGame$2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import TaiGameCore.GameDataBase;
import TaiGameCore.GameSprite;
import TaiGameCore.TaiDAWG;
import TaiGameCore.GameSprite.GameGraphic;
import TaiGameCore.TaiDAWG.WordByRef;
import TaiGameCore.TaiTrees.StringTreeIterator;
import TaiScript.parsing.TaiScriptEditor;

public class BulletHellInstance extends GameDataBase{
	public BulletHellInstance(String hash) {
		super(hash);
		if (extraTextularData==null){
			extraTextularData = new GameSprite[0];
		}
	}

	@DefaultValue(value="A3O1GJqJqLAJIJAJIJAJEPMBGN0PAJAJAJAJAJAJAJAJAJAJAJAJCJAJAJAJC1sDq32541ErA1AL2DMF0xCPCx4N2FsLIx2t4NOrw1K5CHKBCxAJu1KBMDANuFqLI5GvK30PEBwPCx0N4tCrwPGtEPA3ArqJOD0r4r0v4r0HKDwtKt6B6F6LM527AL23ADGtEFAD0LwB4tK7I3CxAPwvCvKBwBKFM3KJ6J6J0PqDqv0DCDC3qr01C565MB2r2LEv6JG50rGtw3s701OD43GFK1wLMv23E7Mx27IDENE32FODO5IrE32NC70rsPItMNOx2HIPK1Mx0vA7AvKPsHIvMB4NC54tKNsBKDM36Bw1GxGNItKxqFAB07MHsN6BuJqLC3uNELG52NEFCLq5w3O3AD41AtEvCJ21IrsB2LqtOvwFMBKPIDO7IFOvGN63KB4HwxAP6JCt0BCNE7wDAHG3KFGPAL2LutMPMFwtI30PA5IDO3IJCxqBwHCJ6v0LuHOJOtsrMHO3OtwPAtCJAPONqtM1AFwPIDEFur0BC3OD2xuLEtCB6rIJwDMFwLCJuvKLsxCrGDC72PIvI1Ev0NE7sJqHuB45GN67MN2PMrKFON2H0Hur6Pq5AxC70Hs147OJ63CBuvOD2L07ENKHKDwHwNIx0J4x0LItCDqru36x65s52twB4JOvsxILM7IJ636LCHOJAvOHAv4Hq10rqJu70NKxKHw1ONAvE7Ct25qr61Mv47w3O703Iv47uDE5OHE3wPEJ6L6HCHwPG5Ax654v63sBwPEr67O7u1416PON6xOFwH67u341CDEvOxIxKFGxqD27stsD6H61uvu1w7qHO5OLu7CvCHMBIH4P6NM7CxsvML6t65634Js70FOD6v6B0PsLO3I3s7GB6J6DqD63A3O1wJIJA32t2t25OFCJAJAJutqJAJAJA3O1CJEJqLAJqLAJIJAJIJAJEPMBGN0PA32t2t25OFCJAJAJutqJAJAJCJAJAJAJAJAJAJAJAJAJAJAJAJAJAJAJAJAJC1A3O1sJuJAJAJAJAJCJAJCJAJ6NAJAJAJMtCJAJAJAJAJ")
	public TaiScriptEditor script;
	public TaiDAWG<String> textureURLs;
	/**
	 * Haha, I like this variable name.
	 */
	public TaiDAWG<Integer> extraTextularIndeces;
	public GameSprite[] extraTextularData;

	public void setTextureFile(int which, String url){
		if (which < textureURLs.size()){
			WordByRef got = getTextureFile(which);
			got.setValue(url);
		} else {
			textureURLs.insert("gfx"+which, url);
		}
	}
	/**
	 * Creates a gamegraphic if none is there.
	 */
	public GameSprite getGameGraphic(String whichObj, String type){
		String key = whichObj+"_"+type;
		WordByRef<Integer> got = extraTextularIndeces.get(key);
		if (got==null){
			//Put it there!
			return assignGameGraphic(key);
		}
		int index = got.getContentData();
		return extraTextularData[index];
	}
	private GameSprite assignGameGraphic(String key){
		int newKey = extraTextularData.length;
		GameSprite[] newArray = new GameSprite[newKey+1];
		for(int k = 0; k < newKey; k++){
			newArray[k] = extraTextularData[k]; //copy
		}
		GameSprite newElement = new GameSprite("");
		newArray[newKey] = newElement;
		extraTextularIndeces.insert(key,newKey);
		extraTextularData = newArray;
		newElement.frame = new GameGraphic[0];
		return newElement;
	}
	/**
	 * Iterate through the tree, get the ith one.
	 */
	public WordByRef<String> getTextureFile(int i) {
		StringTreeIterator<WordByRef<String>> iterator = textureURLs.iterator();
		while(iterator.hasNext()){
			WordByRef<String> currentNode = ((WordByRef)iterator.getCurrentNode());
			if (i==0){
				return currentNode;
			}
			iterator.tryNext();
			i--;
		}
		return null;
	}
	public void autoWrittenDeSerializeCode(){
		String script_strTmp= ((StringEntry)readField("script", new StringEntry(""))).getString();
		if (script_strTmp.length()>0){
			script = new TaiScriptEditor(script_strTmp);
		}
		 else {
			script = new TaiScriptEditor("A3O1GJqJqLAJIJAJIJAJEPMBGN0PAJAJAJAJAJAJAJAJAJAJAJAJCJAJAJAJC1sDq32541ErA1AL2DMF0xCPCx4N2FsLIx2t4NOrw1K5CHKBCxAJu1KBMDANuFqLI5GvK30PEBwPCx0N4tCrwPGtEPA3ArqJOD0r4r0v4r0HKDwtKt6B6F6LM527AL23ADGtEFAD0LwB4tK7I3CxAPwvCvKBwBKFM3KJ6J6J0PqDqv0DCDC3qr01C565MB2r2LEv6JG50rGtw3s701OD43GFK1wLMv23E7Mx27IDENE32FODO5IrE32NC70rsPItMNOx2HIPK1Mx0vA7AvKPsHIvMB4NC54tKNsBKDM36Bw1GxGNItKxqFAB07MHsN6BuJqLC3uNELG52NEFCLq5w3O3AD41AtEvCJ21IrsB2LqtOvwFMBKPIDO7IFOvGN63KB4HwxAP6JCt0BCNE7wDAHG3KFGPAL2LutMPMFwtI30PA5IDO3IJCxqBwHCJ6v0LuHOJOtsrMHO3OtwPAtCJAPONqtM1AFwPIDEFur0BC3OD2xuLEtCB6rIJwDMFwLCJuvKLsxCrGDC72PIvI1Ev0NE7sJqHuB45GN67MN2PMrKFON2H0Hur6Pq5AxC70Hs147OJ63CBuvOD2L07ENKHKDwHwNIx0J4x0LItCDqru36x65s52twB4JOvsxILM7IJ636LCHOJAvOHAv4Hq10rqJu70NKxKHw1ONAvE7Ct25qr61Mv47w3O703Iv47uDE5OHE3wPEJ6L6HCHwPG5Ax654v63sBwPEr67O7u1416PON6xOFwH67u341CDEvOxIxKFGxqD27stsD6H61uvu1w7qHO5OLu7CvCHMBIH4P6NM7CxsvML6t65634Js70FOD6v6B0PsLO3I3s7GB6J6DqD63A3O1wJIJA32t2t25OFCJAJAJutqJAJAJA3O1CJEJqLAJqLAJIJAJIJAJEPMBGN0PA32t2t25OFCJAJAJutqJAJAJCJAJAJAJAJAJAJAJAJAJAJAJAJAJAJAJAJAJC1A3O1sJuJAJAJAJAJCJAJCJAJ6NAJAJAJMtCJAJAJAJAJ");
		 }
			StringEntry got254070401969733 = (StringEntry)readField("textureURLs", null);
		textureURLs = new TaiDAWG();
			if (got254070401969733!=null){
				try{
				ByteArrayInputStream bais = new ByteArrayInputStream(decodeString(got254070401969733.getString()));
				ObjectInputStream ois = new ObjectInputStream(bais);
				textureURLs.readInTree(ois,null);
			} catch (Throwable e){ System.err.println("Error in Deserialization of TAIDAWG"); }
		}
			StringEntry got254070411179842 = (StringEntry)readField("extraTextularIndeces", null);
		extraTextularIndeces = new TaiDAWG();
			if (got254070411179842!=null){
				try{
				ByteArrayInputStream bais = new ByteArrayInputStream(decodeString(got254070411179842.getString()));
				ObjectInputStream ois = new ObjectInputStream(bais);
				extraTextularIndeces.readInTree(ois,null);
			} catch (Throwable e){ System.err.println("Error in Deserialization of TAIDAWG"); }
		}
		String extraTextularData_strTmp = ((StringEntry)readField("extraTextularData", new StringEntry(""))).getString();
		if (extraTextularData_strTmp.length()>0){
			String[] parts123456 = extraTextularData_strTmp.split(",");
			extraTextularData = new GameSprite[parts123456.length];
			for(int qqq = 0; qqq < parts123456.length; qqq++){
				extraTextularData[qqq]=new GameSprite(parts123456[qqq]);
		}}
	}
	public void autoWrittenSerializeCode(){
		writeField("script", new StringEntry(script!=null?script.hashToString():""));
			if (textureURLs!=null){
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oow = new ObjectOutputStream(baos);
				textureURLs.writeOutTree(oow);
				oow.close();
				baos.flush();
				String encodeString = encodeString(baos.toByteArray());
				writeField("textureURLs", new StringEntry(encodeString));
			} catch (Throwable e){;
				System.err.println("Error in taiDAWG serializations");
			}
		}
			if (extraTextularIndeces!=null){
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oow = new ObjectOutputStream(baos);
				extraTextularIndeces.writeOutTree(oow);
				oow.close();
				baos.flush();
				String encodeString = encodeString(baos.toByteArray());
				writeField("extraTextularIndeces", new StringEntry(encodeString));
			} catch (Throwable e){;
				System.err.println("Error in taiDAWG serializations");
			}
		}
		writeField("extraTextularData", new StringEntry(extraTextularData!=null?hashAllToString(extraTextularData):""));
	}

}
