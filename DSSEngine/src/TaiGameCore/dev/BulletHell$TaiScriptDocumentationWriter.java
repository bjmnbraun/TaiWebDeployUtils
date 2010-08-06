package TaiGameCore.dev;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.parser.ParserDelegator;

import BulletGame$2.BulletGlobals.AspectRatioValidator;
import BulletGame$2.BulletGlobals.SoundValidator;
import BulletGame$2.BulletLevel.BPMValidator;
import BulletGame$2.BulletPattern.ExpressionEvaluator;
import BulletGame$2.BulletPattern.FireOnValidator;
import BulletGame$2.BulletPattern.ModeValidator;
import BulletGame$2.BulletPattern.NeighborAlgorithmValidator;
import BulletGame$2.BulletPattern.PositiveValidator;
import BulletGame$2.BulletPattern.RelativeCoordsValidator;
import BulletGame$2.BulletPlayer.YesNoValidator;
import BulletGame$2.GraphicsHolderParser.AnimationValidator;
import TaiGameCore.GameDataBase;
import TaiGameCore.GameDataBase.DefaultValue;
import TaiGameCore.GameDataBase.StringBase;
import TaiGameCore.GameDataBase.StringBase.CriticalScriptField;
import TaiGameCore.GameDataBase.StringBase.FromScript;
import TaiGameCore.GameDataBase.StringBase.HasValidator;
import TaiScript.parsing.TaiScriptLanguage$Constants;
import TaiScript.parsing.TaiScriptLanguage$Constants.AliasRefersTo;

public class BulletHell$TaiScriptDocumentationWriter {
	static String srcFolder = "C:\\Users\\Benjamin\\PROGRAMMING\\Personal\\OSBullets\\BulletHell3\\src";
	static String resFolder = "C:\\Users\\Benjamin\\PROGRAMMING\\Personal\\OSBullets\\BulletHell3\\res_BulletHell\\";
	static String mainFolder = "C:\\Users\\Benjamin\\PROGRAMMING\\Personal\\OSBullets\\BulletHell3\\build_BulletHell\\docs\\";
	
	public static void main(String[] args) throws Throwable{
		final Process p = Runtime.getRuntime().exec("javadoc -d "+mainFolder+" BulletGame$2",null,new File(srcFolder));
		//Just generate the javadocs for the package containing the GameDataBase.StringBase implementations
		new Thread(){public void run(){
		Scanner s = new Scanner(p.getInputStream());
		while(s.hasNextLine()){
			System.out.println(s.nextLine());
		}
		}}.start();
		new Thread(){public void run(){
			Scanner s = new Scanner(p.getErrorStream());
			while(s.hasNextLine()){
				System.out.println("err:"+s.nextLine());
			}
		}}.start();
		p.waitFor(); //Waitfor needs the buffers to be flushed. Phew!
		
		PrintStream os = new PrintStream(new FileOutputStream(new File(resFolder+"DSSdoc.txt")));
		
		TreeMap<Integer, Field> classesToDocument = new TreeMap();
		//For each of the scriptables, 
		Field[] ScriptablesFields = TaiScriptLanguage$Constants.class.getFields();
		Field v; //Temporary;
		for(Field ScriptablesFields_i : ScriptablesFields){
			//Validate
			v = ScriptablesFields_i;
			if (v.getType()==String.class && v.getAnnotation(TaiScript.parsing.TaiScriptLanguage$Constants.AliasRefersTo.class)!=null){
				AliasRefersTo alias = v.getAnnotation(TaiScript.parsing.TaiScriptLanguage$Constants.AliasRefersTo.class);
				int order = alias.displayOrder();
				classesToDocument.put(order, v);
			}
		}
		
		//Document
		Scanner header = new Scanner(new File(resFolder+"Documentation_Header.txt"));
		while(header.hasNextLine()){
			String line = header.nextLine();
			line = line.replace("Version XX", "Version 1");
			line = line.replace("Updated XX", "Updated "+new Date());
			os.println(line);
		}
		header.close();
		for(Field scriptable : classesToDocument.values()){
			AliasRefersTo alias = scriptable.getAnnotation(TaiScript.parsing.TaiScriptLanguage$Constants.AliasRefersTo.class);
			Class<? extends StringBase> gameDatabaseClass = alias.GameDatabaseClass();
			String aliasStr = (String)scriptable.get(null);
			document(os,aliasStr,gameDatabaseClass);
		}
		
		os.close();
	}
	public static void document(PrintStream out, String alias, Class<? extends StringBase> docClass) throws Throwable{
		final TreeMap<String, String> variableComments = new TreeMap();
		HTMLEditorKit.ParserCallback callback = 
			new HTMLEditorKit.ParserCallback () {
			//Field Summary, 
			int[] markers = new int[5];
			
			private String variableName = null, variableComment = "";
			public void handleText(char[] data, int pos) {
				if (s.peek()==Tag.B && new String(data).equals("Field Detail")){
					markers[0] = 6;
					variableName = null;
					return;
				}
				if (s.peek()==Tag.B && new String(data).equals("Constructor Detail")){
					markers[0] = 0;
				}
				if (markers[0] > 0){
					Tag g = s.peek();
					//System.out.println(g+" "+new String(data));
					
					if (g==Tag.B){
						if (new String(data).contains("See Also")){
							return;
						}
						variableName = new String(data);
						variableComment = "";
					}
					if (g==Tag.DD){
						variableComment = new String(data);
					}
					if (g==Tag.H3 && variableName != null){
						//Ok, dedicate it.
						variableComments.put(variableName, variableComment);	
					}
				}
			}
			Stack<Tag> s = new Stack();
			public void handleStartTag(Tag arg0, MutableAttributeSet arg1, int arg2) {
				s.push(arg0);
				for(int k = 0; k < markers.length; k++){
					if(markers[k]!=0)markers[k]++;
				}
			}
			public void handleEndTag(Tag t, int pos) {
				if (!(s.pop().equals(t))){
					System.out.println("Warn! UnevenStackoff"+pos);
				}
				for(int k = 0; k < markers.length; k++){
					if(markers[k]!=0)markers[k]--;
				}
			}
		};
		Reader reader = new FileReader(new File(mainFolder+"BulletGame$2\\"+docClass.getSimpleName()+".html"));
		ParserDelegator parserDelegator = new ParserDelegator();
		parserDelegator.parse(reader, callback, false);
		reader.close();
		
		String[] ValidatorDescriptions = new String[]{
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
				"parameter is a cow",
		};
		ValidatorDescriptions[AnimationValidator.ANIMATION_VALIDATOR] = "parameter represents a correct animation command.";
		ValidatorDescriptions[AspectRatioValidator.ASPECT_RATIO_VALIDATOR] = "parameter represents an aspect ratio, in floating point or w:h form.";
		ValidatorDescriptions[BPMValidator.BPM_VALIDATOR] = "parameter is a list of BPM changes, per the tried-and-true Stepmania.com format.";
		ValidatorDescriptions[ExpressionEvaluator.EXPRESSION_VALIDATOR] = "parameter represents a computable expression, using only defined variables,\n which may be defined piecewise or iteratively.";
		ValidatorDescriptions[FireOnValidator.FIRE_ON_VALIDATOR] = "parameter correctly obeys the syntax for the fire_on parameter.";
		ValidatorDescriptions[ModeValidator.MODE_VALIDATOR] = "parameter == RECT or POLAR";
		ValidatorDescriptions[NeighborAlgorithmValidator.NEIGHBOR_VALIDATOR] = "parameter correctly represents one of the ways to filter out a bullet's \"neighbors\".";
		ValidatorDescriptions[PositiveValidator.POSITIVE_VALIDATOR] = "parameter > 0 (strictly)";
		ArrayList<String> possibleRelativeModes = new ArrayList();
		for(Field q : RelativeCoordsValidator.class.getFields()){
			if (q.getType().equals(String.class)){
				possibleRelativeModes.add((String)q.get(null));
			}
		}
		String relModes = "parameter is one of ";
		for(int k = 0; k < possibleRelativeModes.size() - 1; k++){
			relModes += possibleRelativeModes.get(k)+", ";
		}
		relModes += "or "+possibleRelativeModes.get(possibleRelativeModes.size()-1);
		ValidatorDescriptions[RelativeCoordsValidator.RELATIVE_COORDS_VALIDATOR] = relModes;
		ValidatorDescriptions[YesNoValidator.YES_NO_VALIDATOR] = "parameter can be evaluated to a Yes or No. Specifically, the following characters are looked for: [yt1] = yes, [nf0] = no";
		ValidatorDescriptions[SoundValidator.SOUND_VALIDATOR] = "parameter is a URL to a mp3 or ogg file. Warning: Not validated until play-time!";
		
		out.println("=======");
		out.println(alias);
		out.println("=======");
		out.println("   *** ALL KEYS ARE CASE SeNsItIvE ***   ");
		out.println("");
		for(Entry<String, String> k : variableComments.entrySet()){
			Field got = docClass.getField(k.getKey());
			FromScript fromScript = got.getAnnotation(GameDataBase.StringBase.FromScript.class);
			if (fromScript==null){
				continue;
			}
			HasValidator hasValid = got.getAnnotation(GameDataBase.StringBase.HasValidator.class);
			CriticalScriptField Critical = got.getAnnotation(GameDataBase.StringBase.CriticalScriptField.class);
			DefaultValue defaultVal = got.getAnnotation(GameDataBase.DefaultValue.class);
			
			out.println(got.getName());
			out.println("---------");
			out.println();
			
			String comment= k.getValue();
			String[] wordWrap = wordWrap(comment,80);
			for(String word : wordWrap){
				out.println(word);
			}
			if (Critical!=null){
				out.println("+ REQUIRED in order to compile "+alias);
			}
			if (hasValid!=null){
				out.println("+ Valid only if "+ValidatorDescriptions[hasValid.num()]);
			}
			if (defaultVal!=null){
				String gotStr = defaultVal.value();
				if (gotStr==null || gotStr.equals("null")){
					out.println("If not specified, it is Disabled.");
				} else {
					out.println("Defaults to (without quotes), '"+gotStr+"'");
				}
			}
			out.println();
		}
	}
	
	public static String[] wordWrap(String str, int len){
		Pattern wrapRE = Pattern.compile("(\\S\\S{" + len + ",}|.{1," + len + "})(\\s+|$)");
		
		List list = new LinkedList();

		Matcher m = wrapRE.matcher(str);

		while (m.find()) list.add(m.group());

		return (String[]) list.toArray(new String[list.size()]);
	}
}
