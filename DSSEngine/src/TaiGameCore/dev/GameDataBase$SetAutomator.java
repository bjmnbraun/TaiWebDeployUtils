package TaiGameCore.dev;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;

import TaiGameCore.GameDataBase;
import TaiGameCore.GameDataBase.DefaultValue;

/**
 * For any instances of game databases in a class, adds a key,value pair to a corresponding universal lookup map.
 */
public class GameDataBase$SetAutomator {
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
		Class got = GameDataBase$SetAutomator.class.getClassLoader().loadClass(parts[0]);
		for(int k = 1; k < parts.length; k++){
			got = getInArray(got.getDeclaredClasses(),parts[k]);
		}
		//System.out.println(got);
		Field[] fields = got.getDeclaredFields();
		HashMap<String, LinkedList<Field>> fieldsOfType = new HashMap();
		for(Field k : fields){
			if (!GameDataBase.class.isAssignableFrom(k.getType())){
				continue;
			}
			String kstr = k.getType().getSimpleName();
			LinkedList<Field> gotList = fieldsOfType.get(kstr);
			if (gotList==null){
				gotList = new LinkedList<Field>();
				fieldsOfType.put(kstr,gotList);
			}
			gotList.add(k);
		}
		for(String k : fieldsOfType.keySet()){
			String mapName = makeMapName(k);
			System.out.println("\tprivate HashMap<String, "+k+"> "+mapName+" = new HashMap(); ");
		}	
		System.out.println("\tpublic "+got.getSimpleName()+"(){");
		for(String k : fieldsOfType.keySet()){
			String mapName = makeMapName(k);
			for(Field g : fieldsOfType.get(k)){
				String defaultValue = "";
				DefaultValue defVal = ((DefaultValue)g.getAnnotation(DefaultValue.class));
				if (defVal!=null){
					defaultValue = defVal.value();
					if (defaultValue.length()==0){
						defaultValue = null;
					}
				}
				if (defaultValue==null){
					//Look for a generator.
					try {
						Method found = got.getMethod(g.getName()+"_generator");
						if (found!=null){
							GameDataBase created = (GameDataBase)found.invoke(null);
							if (created!=null){
								defaultValue = created.hashToString();
							}
						}
					} catch (Throwable e){
						//
					}
				}
				if (defaultValue==null){
					defaultValue = "";
				}
				System.out.println("\t\t"+mapName+".put(\""+g.getName()+"\","+g.getName()+" = new "+k+"(\""+defaultValue+"\"));");
			}
		}
		System.out.println("\t}");
	}
	public static String makeMapName(String className){
		return "all"+className+"s";
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
