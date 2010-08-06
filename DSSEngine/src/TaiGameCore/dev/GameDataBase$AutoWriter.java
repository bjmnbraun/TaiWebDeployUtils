package TaiGameCore.dev;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Scanner;

import TaiGameCore.GameDataBase;
import TaiGameCore.TaiDAWG;

/**
 * Fills in the appropriate automatic serialization / deserialization of a given gamedatabase
 */
public class GameDataBase$AutoWriter {
	public static void main(String[] args) throws Throwable{
		Scanner in = new Scanner(System.in);
		while(in.hasNextLine()){
			try {
				main(in.nextLine());
			} catch (Throwable e){
				e.printStackTrace();
			}
		}
	}
	public static void main(String className) throws ClassNotFoundException{
		String[] parts = className.split("/");
		Class got = GameDataBase$AutoWriter.class.getClassLoader().loadClass(parts[0]);
		for(int k = 1; k < parts.length; k++){
			got = getInArray(got.getDeclaredClasses(),parts[k]);
		}
		//System.out.println(got);
		Field[] fields = got.getDeclaredFields();

		if (GameDataBase.StringBase.class.isAssignableFrom(got)){
			System.out.println("public ArrayList<Exception> parseFromStrings(TaiDAWG<String> data, Validator ... valid) {");
			System.out.println("\tArrayList<Exception> toRet = new ArrayList();");
			System.out.println("\tWordByRef<String> word;");
			ArrayList<String> scriptables = new ArrayList();
			for(Field k : fields){
				boolean isCritical = false;
				try {
					GameDataBase.StringBase.CriticalScriptField a = k.getAnnotation(GameDataBase.StringBase.CriticalScriptField.class);
					if (a!=null){
						isCritical = true;
					}
				} catch (NullPointerException e){
					//None.
				}
				boolean isScriptable = false;
				try {
					GameDataBase.StringBase.FromScript a = k.getAnnotation(GameDataBase.StringBase.FromScript.class);
					if (a!=null){
						isScriptable = true;
						scriptables.add(k.getName());
					}
				} catch (NullPointerException e){
					//None.
				}
				int valid = -1;
				try {
					GameDataBase.StringBase.HasValidator a = k.getAnnotation(GameDataBase.StringBase.HasValidator.class);
					if (a!=null){
						valid = a.num();
					}
				} catch (NullPointerException e){
					//None.
				}
				if (isScriptable){
					System.out.println("\tword = data.get(\""+k.getName()+"\");");
					System.out.println("\tif (word!=null){String val = word.getContentData();");
					if (valid!=-1){
						System.out.println("try {");
						System.out.println("\tvalid["+valid+"].validate(\""+k.getName()+"\",val);");
						System.out.println("} catch (ValidationException e){");
						System.out.println("toRet.add(e);}");
					}
					if (k.getType().equals(String.class)){
						System.out.println("\t"+k.getName()+"= val;");
					} else if (k.getType().equals(Integer.TYPE)){
						System.out.println("\t"+k.getName()+"= new Integer(val.trim());");
					} else if (k.getType().equals(int[].class)){
						System.out.println("\tString[] spli = val.split(\",\");");
						System.out.println("\t"+k.getName()+"= new int[spli.length];");	
						System.out.println("\tfor(int k = 0; k < spli.length; k++){");
						System.out.println("\t\t"+k.getName()+"[k]=new Integer(spli[k].trim());");
						System.out.println("\t}");
					} else if (k.getType().equals(double[].class)){
						System.out.println("\tString[] spli = val.split(\",\");");
						System.out.println("\t"+k.getName()+"= new double[spli.length];");	
						System.out.println("\tfor(int k = 0; k < spli.length; k++){");
						System.out.println("\t\t"+k.getName()+"[k]=new Double(spli[k].trim());");
						System.out.println("\t}");
					} else if (k.getType().equals(float[].class)){
						System.out.println("\tString[] spli = val.split(\",\");");
						System.out.println("\t"+k.getName()+"= new float[spli.length];");	
						System.out.println("\tfor(int k = 0; k < spli.length; k++){");
						System.out.println("\t\t"+k.getName()+"[k]=new Float(spli[k].trim());");
						System.out.println("\t}");
					} else if (k.getType().equals(Double.TYPE)){
						System.out.println("\t"+k.getName()+"= new Double(val.trim());");
					} else if (k.getType().equals(Float.TYPE)){
						System.out.println("\t"+k.getName()+"= new Float(val.trim());");
					} else {
						System.out.println("Unknown type : "+k.getName());
					}
					System.out.println("\t}");
					if (isCritical){
						System.out.println("\t else {");
						System.out.println("\t\ttoRet.add(new FieldRequiredException(\""+k.getName()+" required.\"));");
						System.out.println("\t}");
					}
				}
			}
			//Do we have any unrecognized fields?
			System.out.println("StringTreeIterator<WordByRef<String>> iterator = data.iterator();");
			System.out.println("while(iterator.hasNext()){");
			System.out.println("	String key = iterator.next();");
			System.out.print("if (");
			for(int p = 0; p < scriptables.size(); p++){
				String k = scriptables.get(p);
				System.out.print("!key.equals(\""+k+"\")");
				if (p+1<scriptables.size()){
					System.out.print("&&");
				}
			}
			System.out.println("){");
			System.out.println("\t\ttoRet.add(new ValidationException(\"Unrecognized var: \"+key+\".\",key));");
			System.out.println("}");
			System.out.println("	iterator.tryNext();");
			System.out.println("}");
			System.out.println("\treturn toRet;");
			System.out.println("}");
		}

		//XXX
		System.out.println("public void autoWrittenDeSerializeCode(){");
		for(Field k : fields){
			if (Modifier.isStatic(k.getModifiers())){
				continue;
			}	

			String annotatedDefault = null;
			try {
				GameDataBase.DefaultValue a = k.getAnnotation(GameDataBase.DefaultValue.class);
				if (a!=null){
					annotatedDefault = a.value();
					if(annotatedDefault.length()==0){
						annotatedDefault = null;
					}
				}
			} catch (NullPointerException e){
				//None.
			}
			if (annotatedDefault==null){
				annotatedDefault = "";
			}
			System.out.print("\t");
			if (GameDataBase.class.isAssignableFrom(k.getType())){
				String stringTempForm = k.getName()+"_strTmp";
				System.out.print("String "+stringTempForm);
				System.out.print("= ((StringEntry)readField(\""+k.getName()+"\", ");
				System.out.print("new StringEntry(\"\"))).getString();");
				System.out.println();
				System.out.println("\tif ("+stringTempForm+".length()>0){"); //We actually have a value.
				System.out.println("\t\t"+k.getName()+" = new "+k.getType().getSimpleName()+"("+stringTempForm+");");
				System.out.println("\t}");
				if (annotatedDefault.length()>0){
					System.out.println("\t else {");
					System.out.println("\t\t"+k.getName()+" = new "+k.getType().getSimpleName()+"(\""+annotatedDefault+"\");");
					System.out.println("\t }");
				}
			} else if (k.getType().isArray() && GameDataBase.class.isAssignableFrom(k.getType().getComponentType())){
				String stringTempForm = k.getName()+"_strTmp";
				System.out.print("String "+stringTempForm + " = ");
				System.out.print("((StringEntry)readField(\""+k.getName()+"\", ");
				String defaultStr = annotatedDefault!=null?annotatedDefault:"";
				System.out.print("new StringEntry(\""+defaultStr+"\"))).getString();");
				System.out.println();
				System.out.println("\tif ("+stringTempForm+".length()>0){"); //We actually have a value.
				System.out.println("\t\tString[] parts123456 = "+stringTempForm+".split(\",\");");
				System.out.println("\t\t"+k.getName()+" = new "+k.getType().getComponentType().getSimpleName()+"[parts123456.length];");
				System.out.println("\t\tfor(int qqq = 0; qqq < parts123456.length; qqq++){");
				System.out.println("\t\t\t"+k.getName()+"[qqq]=new "+k.getType().getComponentType().getSimpleName()+"(parts123456[qqq]);");
				System.out.println("\t}}");		
			} else if (TaiDAWG.class.isAssignableFrom(k.getType())){
				String StringEntryName = "got"+System.nanoTime();
				System.out.println("\tStringEntry "+StringEntryName+" = (StringEntry)readField(\""+k.getName()+"\", null);");
				System.out.println("\t"+k.getName()+" = new TaiDAWG();");
				System.out.println("\t\tif ("+StringEntryName+"!=null){");
				System.out.println("\t\t\ttry{");
				System.out.println("\t\t\tByteArrayInputStream bais = new ByteArrayInputStream(decodeString("+StringEntryName+".getString()));");
				System.out.println("\t\t\tObjectInputStream ois = new ObjectInputStream(bais);");
				System.out.println("\t\t\t"+k.getName()+".readInTree(ois,null);");
				System.out.println("\t\t} catch (Throwable e){ System.err.println(\"Error in Deserialization of TAIDAWG\"); }");
				System.out.println("\t}");
			} else if (k.getType().equals(String.class)){
				System.out.print(k.getName()+" = ");
				System.out.print("((StringEntry)readField(\""+k.getName()+"\", ");
				String defaultStr = annotatedDefault!=null?annotatedDefault:"[default]";
				System.out.print("new StringEntry(\""+defaultStr+"\"))).getString();");
				System.out.println();
			} else if (k.getType().equals(Boolean.TYPE)){
				System.out.print(k.getName()+" = ");
				System.out.print("((IntEntry)readField(\""+k.getName()+"\", ");
				String defaultStr = annotatedDefault!=null?annotatedDefault:"0";
				System.out.print("new IntEntry("+defaultStr+"))).getInt()==1;");
				System.out.println();
			} else if (k.getType().equals(Integer.TYPE)){
				System.out.print(k.getName()+" = ");
				System.out.print("((IntEntry)readField(\""+k.getName()+"\", ");
				String defaultStr = annotatedDefault!=null?annotatedDefault:"0";
				System.out.print("new IntEntry("+defaultStr+"))).getInt();");
				System.out.println();
			} else if (k.getType().equals(Double.TYPE)){
				System.out.print(k.getName()+" = ");
				System.out.print("((DoubleEntry)readField(\""+k.getName()+"\", ");
				String defaultStr = annotatedDefault!=null?annotatedDefault:"0";
				System.out.print("new DoubleEntry("+defaultStr+"))).getDouble();");
				System.out.println();
			} else if (k.getType().equals(Float.TYPE)){
				System.out.print(k.getName()+" = (float)");
				System.out.print("((DoubleEntry)readField(\""+k.getName()+"\", ");
				String defaultStr = annotatedDefault!=null?annotatedDefault:"0";
				System.out.print("new DoubleEntry("+defaultStr+"))).getDouble();");
				System.out.println();
			} else if (k.getType().equals(int[].class)){
				System.out.print(k.getName()+" = ");
				System.out.print("((IntArrayEntry)readField(\""+k.getName()+"\", ");
				String defaultStr = annotatedDefault!=null?annotatedDefault:"";
				System.out.print("new IntArrayEntry(new int[]{"+defaultStr+"}))).getIntArray();");
				System.out.println();
			} else if (k.getType().equals(byte[].class)){
				System.out.print(k.getName()+" = ");
				System.out.print("((ByteArrayEntry)readField(\""+k.getName()+"\", ");
				String defaultStr = annotatedDefault!=null?annotatedDefault:"";
				System.out.print("new ByteArrayEntry(new byte[]{"+defaultStr+"}))).getByteArray();");
				System.out.println();
			} else if (k.getType().equals(double[].class)){
				System.out.print(k.getName()+" = ");
				System.out.print("((DoubleArrayEntry)readField(\""+k.getName()+"\", ");
				String defaultStr = annotatedDefault!=null?annotatedDefault:"";
				System.out.print("new DoubleArrayEntry(new double[]{"+defaultStr+"}))).getDoubleArray();");
				System.out.println();
			} else if (k.getType().equals(float[].class)){
				System.out.print(k.getName()+" = ");
				System.out.print("((FloatArrayEntry)readField(\""+k.getName()+"\", ");
				String defaultStr = annotatedDefault!=null?annotatedDefault:"";
				System.out.print("new FloatArrayEntry(new float[]{"+defaultStr+"}))).getFloatArray();");
				System.out.println();
			} else {
				System.out.println("customDeserialize"+k.getName()+"();");
				//throw new RuntimeException("Invalid entrytype: "+k);
			}
		}
		System.out.println("}");

		System.out.flush();

		//XXX

		//OK, now the serialization code:

		/*
		writeField("name", new StringEntry(name));
		writeField("EvolvedForm", new StringEntry(EvolvedForm.hashToString()));
		writeField("baseStats", new IntArrayEntry(new int[]{}));
		 */
		System.out.println("public void autoWrittenSerializeCode(){");
		for(Field k : fields){
			if (Modifier.isStatic(k.getModifiers())){
				continue;
			}	

			System.out.print("\t");
			if (GameDataBase.class.isAssignableFrom(k.getType())){
				System.out.print("writeField(\""+k.getName()+"\", ");
				System.out.print("new StringEntry("+k.getName()+"!=null?"+k.getName()+".hashToString():\"\"));");
				System.out.println();
			} else if (k.getType().isArray() && GameDataBase.class.isAssignableFrom(k.getType().getComponentType())){
				System.out.print("writeField(\""+k.getName()+"\", ");
				System.out.print("new StringEntry("+k.getName()+"!=null?hashAllToString("+k.getName()+"):\"\"));");
				System.out.println();
			} else if (TaiDAWG.class.isAssignableFrom(k.getType())){
				System.out.println("\tif ("+k.getName()+"!=null){");
				System.out.println("\t\ttry {");
				System.out.println("\t\t\tByteArrayOutputStream baos = new ByteArrayOutputStream();");
				System.out.println("\t\t\tObjectOutputStream oow = new ObjectOutputStream(baos);");
				System.out.println("\t\t\t"+k.getName()+".writeOutTree(oow);");
				System.out.println("\t\t\toow.close();");
				System.out.println("\t\t\tbaos.flush();");
				System.out.println("\t\t\tString encodeString = encodeString(baos.toByteArray());");
				System.out.println("\t\t\twriteField(\""+k.getName()+"\", new StringEntry(encodeString));");
				System.out.println("\t\t} catch (Throwable e){;");
				System.out.println("\t\t\tSystem.err.println(\"Error in taiDAWG serializations\");");
				System.out.println("\t\t}");
				System.out.println("\t}");
			} else if (k.getType().equals(String.class)){
				System.out.print("writeField(\""+k.getName()+"\", ");
				System.out.print("new StringEntry("+k.getName()+"));");
				System.out.println();
			} else if (k.getType().equals(Integer.TYPE)){
				System.out.print("writeField(\""+k.getName()+"\", ");
				System.out.print("new IntEntry("+k.getName()+"));");
				System.out.println();
			} else if (k.getType().equals(Boolean.TYPE)){
				System.out.print("writeField(\""+k.getName()+"\", ");
				System.out.print("new IntEntry("+k.getName()+"?1:0));");
				System.out.println();
			} else if (k.getType().equals(Double.TYPE) || k.getType().equals(Float.TYPE)){
				System.out.print("writeField(\""+k.getName()+"\", ");
				System.out.print("new DoubleEntry("+k.getName()+"));");
				System.out.println();
			} else if (k.getType().equals(int[].class)){
				System.out.print("writeField(\""+k.getName()+"\", ");
				System.out.print("new IntArrayEntry("+k.getName()+"));");
				System.out.println();
			} else if (k.getType().equals(byte[].class)){
				System.out.print("writeField(\""+k.getName()+"\", ");
				System.out.print("new ByteArrayEntry("+k.getName()+"));");
				System.out.println();
			} else if (k.getType().equals(double[].class)){
				System.out.print("writeField(\""+k.getName()+"\", ");
				System.out.print("new DoubleArrayEntry("+k.getName()+"));");
				System.out.println();
			} else if (k.getType().equals(float[].class)){
				System.out.print("writeField(\""+k.getName()+"\", ");
				System.out.print("new FloatArrayEntry("+k.getName()+"));");
				System.out.println();
			} else {
				System.out.println("customSerialize"+k.getName()+"();");
				//throw new RuntimeException("Invalid entrytype: "+k);
			}
		}		
		System.out.println("}");
	}

	private static Class getInArray(Class[] declaredClasses, String string) {
		for(Class stuff : declaredClasses){
			if (stuff.getSimpleName().equals(string)){
				return stuff;
			}
		}
		return null;
	}
}
