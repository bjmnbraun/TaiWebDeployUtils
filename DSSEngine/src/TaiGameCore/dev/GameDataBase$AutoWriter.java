package TaiGameCore.dev;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import TaiGameCore.GameDataBase;
import TaiGameCore.TaiDAWG;
import TaiGameCore.GameDataBase.StringBase.ExtendsData;
import TaiGameCore.GameDataBase.StringBase.MakeGetters;
import TaiGameCore.GameDataBase.StringBase.ScopePreValidate;
import TaiGameCore.GameDataBase.StringBase.ValidationException;

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
		ArrayList<Field> fields = new ArrayList();
		for(Field q : got.getDeclaredFields()){
			fields.add(q);
		}
		if (GameDataBase.StringBase.class.isAssignableFrom(got)){
			handleExtendedTemplates(got,fields);
			//We have a string parsable class. Write its parser.
			System.out.println("public ArrayList<Exception> parseFromStrings(TaiDAWG<TaiScriptStatementAndContext> data, Validator ... valid) {");
			System.out.println("\tArrayList<Exception> toRet = new ArrayList();");
			System.out.println("\tWordByRef<TaiScriptStatementAndContext> word;");
			ArrayList<String> scriptables = new ArrayList();

			String scopeAndValidate = null;
			try {
				GameDataBase.StringBase.ScopePreValidate a = (ScopePreValidate) got.getAnnotation(GameDataBase.StringBase.ScopePreValidate.class);
				if (a!=null){
					scopeAndValidate = a.method();
				}
			} catch (NullPointerException e){
				//None.
			}
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
				boolean skipScopeAndValidate = false;
				try {
					GameDataBase.StringBase.IsUnscoped a = k.getAnnotation(GameDataBase.StringBase.IsUnscoped.class);
					if (a!=null){
						skipScopeAndValidate = true;
					}
				} catch (NullPointerException e){
					//None.
				}
				if (isScriptable){
					System.out.println("\tword = data.get(\""+k.getName()+"\");");
					System.out.println("\tif (word!=null){String val = word.getContentData().rawString;");
					
					System.out.println("try {");
					{//Validating exceptions! If it does not validate, don't write it.
						System.out.println("if (false){throw new ValidationException(null, null);}//Workaround for unreachable catch block");
						if (scopeAndValidate!=null && !skipScopeAndValidate){
							System.out.println("val = "+scopeAndValidate+"(word.getContentData(),"+k.getType().getSimpleName()+".class,\""+k.getName()+"\");");
						}
						if (valid!=-1){
							System.out.println("\tvalid["+valid+"].validate(\""+k.getName()+"\",val);");
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

						//Validate error? add exception, don't store value.
						System.out.println("} catch (ValidationException e){");
						System.out.println("toRet.add(e);}");
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
			System.out.println("StringTreeIterator<WordByRef<TaiScriptStatementAndContext>> iterator = data.iterator();");
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

		System.out.println("public void autoWrittenDeSerializeCode(){");
		for(Field k : fields){
			printDeserializerForField(k);
		}
		System.out.println("}");

		System.out.flush();

		//OK, now the serialization code:

		System.out.println("public void autoWrittenSerializeCode(){");
		for(Field k : fields){
			printSerializerForField(k);
		}		
		System.out.println("}");
	}
	/**
	 * Adds to outFields any serializable fields added by the extended class.
	 */
	private static void handleExtendedTemplates(Class got, List<Field> outFields) {
		try {
			GameDataBase.StringBase.ExtendsData ex = (ExtendsData) got.getAnnotation(GameDataBase.StringBase.ExtendsData.class);
			if (ex!=null){
				for(Class q : ex.parents()){
					System.out.println("//Template "+q.toString());
					for(Field e : q.getDeclaredFields()){
						outFields.add(e);
						for(Annotation a : e.getAnnotations()){
							Class<? extends Annotation> aType = a.annotationType();
							System.out.print("@"+aType.getSimpleName()+"(");
							for (Method aT : aType.getDeclaredMethods()){
								Object valObj = aT.invoke(a, null);
								String valStr = null;
								if (valObj.getClass()==String.class){
									valStr = "\""+valObj+"\"";
								} else {
									valStr = ""+valObj;
								}
								System.out.print(aT.getName()+" = "+valStr);
							}
							System.out.println(")");
						}
						System.out.print("public "+e.getType().getSimpleName()+" "+e.getName()+";");
						System.out.println();
					}
					try {
						GameDataBase.StringBase.MakeGetters m = (MakeGetters) q.getAnnotation(GameDataBase.StringBase.MakeGetters.class);
						if (m!=null){
							//Make the getters.
							for(Field e : q.getDeclaredFields()){
								String name = e.getName();
								System.out.print("public "+e.getType().getSimpleName()+" get");
								System.out.print(Character.toUpperCase(name.charAt(0)));
								if (name.length()>1){
									System.out.print(name.substring(1));
								}
								System.out.println("() {");
								System.out.println("\treturn "+name+";");
								System.out.println("}");
							}
						}
					} catch (NullPointerException e){
						
					}
				}
			}
		} catch (NullPointerException e){
			//None.
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	private static void printDeserializerForField(Field k) {

		if (Modifier.isStatic(k.getModifiers())){
			return;
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
	private static void printSerializerForField(Field k) {
		if (Modifier.isStatic(k.getModifiers())){
			return;
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
	private static Class getInArray(Class[] declaredClasses, String string) {
		for(Class stuff : declaredClasses){
			if (stuff.getSimpleName().equals(string)){
				return stuff;
			}
		}
		return null;
	}
}
