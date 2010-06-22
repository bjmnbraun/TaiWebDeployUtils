package TaiScript.parsing;

public class TaiScriptLanguage$Rules {
	public static char[] reservedPunctuation = ".,{}!@#%^&*()+=-~`'\";:<>?/\\|".toCharArray();
	public static boolean isValidInstanceName(String name) {
		for(char k : name.toCharArray()){
			for(char b : reservedPunctuation){
				if (b==k) return false;
			}
		}
		return true;
	}

}
