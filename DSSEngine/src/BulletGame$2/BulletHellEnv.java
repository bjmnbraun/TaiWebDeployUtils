package BulletGame$2;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import processing.core.PApplet;
import BulletGame$1.BulletGame$1Engine$ABasicEngine;

import com.iabcinc.jmep.Environment;
import com.iabcinc.jmep.hooks.Function;
import com.iabcinc.jmep.hooks.Variable;

public class BulletHellEnv {
	public Environment env;
	private PApplet g2;
	public BulletGame$1Engine$ABasicEngine g;

	public static void main(String[] args){
		//This class writes itself:
		class MethodObject { 
			public MethodObject(String func, int numArgs){
				this.func = func;
				this.numArgs = numArgs;
			}
			private String func;
			private int numArgs;
			public boolean equals(Object other){
				MethodObject oth = (MethodObject)other;
				return oth.func.equals(func)&&oth.numArgs==numArgs;
			}
		}
		String[] banList = new String[]{
				"random",
				"IEEEremainder",
				"sin",
				"cos",
		};
		ArrayList<MethodObject> found = new ArrayList();
		methodloop: for(Method k : Math.class.getDeclaredMethods()){
			int numArgs = k.getParameterTypes().length;
			String name = k.getName();
			//Check the banlist
			for(String q : banList){
				if (name.equals(q)){
					continue methodloop;
				}
			}
			if (Modifier.isPublic(k.getModifiers())){
				MethodObject made = new MethodObject(name, numArgs);
				if (!found.contains(made)){
					//This is technically incorrect, I don't know how to
					//handle function overloading correctly now.
					//Though it's probably possible.
					found.add(made);
				}
			}
		}
		boolean checkNumberInstance = false;
		Method[] pappmethods = PApplet.class.getMethods();
		for(MethodObject q : found){
			System.out.println("\t\tenv.addFunction(\""+q.func+"\", new Function(){public Object call(Object[] oPars) {");
			if (q.numArgs>0){
				System.out.println("if (oPars == null) throw new RuntimeException(\"null args\");");
				System.out.println("if (oPars.length != "+q.numArgs+") throw new RuntimeException(\""+q.func+" takes "+q.numArgs+" args\");");
			} else {
				System.out.println("if (oPars != null && oPars.length!=0) throw new RuntimeException(\""+q.func+" takes "+q.numArgs+" args\");");
			}
			if (checkNumberInstance){
				for(int k = 0; k < q.numArgs; k++){
					System.out.println("if (oPars["+k+"] instanceof Number){");
				}
			}
			//Does PApplet have this method?
			boolean isPAPP = false;
			for(Method o : pappmethods){
				if (o.getName().equals(q.func)){
					isPAPP = true;
				}
			}
			if (isPAPP){
				System.out.print("return g2."+q.func+"(");
				for(int k = 0; k < q.numArgs; k++){
					System.out.print("((Number)oPars["+k+"]).floatValue()");
					if (k+1<q.numArgs){
						System.out.print(",");
					}
				}
			} else {
				System.out.print("return Math."+q.func+"(");
				for(int k = 0; k < q.numArgs; k++){
					System.out.print("((Number)oPars["+k+"]).doubleValue()");
					if (k+1<q.numArgs){
						System.out.print(",");
					}
				}
			}
			System.out.print(");");
			if (checkNumberInstance){
				for(int k = 0; k < q.numArgs; k++){
					System.out.println("}");
				}
				if (q.numArgs>0){
					System.out.println("\t\tthrow new RuntimeException(\"invalid input\");");
				}
			}
			System.out.println("\t\t}});");
		}
	}

	public BulletHellEnv(BulletGame$1Engine$ABasicEngine g){
		env = new Environment();
		if (g!=null){
			this.g = g;
			g2 = g.g;
		}
		setup();
	}
	public void setup(){
		//Add default functions and global variables
		env.addVariable(BIGTSTR, BIGT);
		env.addVariable(BIGTBSTR, BIGTB);
		env.addVariable(BIGANGLETOGIRLSTR, BIGANGLETOGIRL);
		env.addVariable(BIGANGLETOGIRL_INITIALSTR, BIGANGLETOGIRL_INITIAL);
		env.addVariable(BIGCOUNTSTR, BIGCOUNT);
		env.addVariable(BIGVSTR, BIGV);
		env.addVariable(BIGTHETASTR, BIGTHETA);
		env.addVariable(BIGMEASUREDSTR, BIGMEASURE);
		env.addVariable(BIGFIREDSTR, BIGFIRED);
		env.addVariable(BIGSTORESTR, BIGSTORE);
		env.addVariable(BIGSTORE2STR, BIGSTORE2);
		env.addVariable(BIGSOWSTR, BIGSOW);
		env.addVariable(BIGEDGEWSTR, BIGEDGEW);
		env.addVariable(BIGDOTESTR, BIGDOTE);
		env.addVariable(BIGALIVESTR, BIGALIVE);
		env.addVariable(BIGRANDOM0STR, BIGRANDOM0);
		env.addVariable(BIGISFOCUSINGSTR, BIGISFOCUSING);

		env.addFunction("angleAvg", new Function(){public Object call(Object[] oPars){
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 2 && oPars.length!=3) throw new RuntimeException("avg takes 2,3 args");
			if (oPars[0] instanceof Number){
				if (oPars[1] instanceof Number){
					float a1 = ((Number)oPars[0]).floatValue();
					float a2 = ((Number)oPars[1]).floatValue();
					float ra = .5f;
					if (oPars.length==3){
						ra = ((Number)oPars[2]).floatValue();;
					}
					float dy = g.SinLUT(a1)*ra+g.SinLUT(a2)*(1-ra);
					float dx = g.CosLUT(a1)*ra+g.CosLUT(a2)*(1-ra);
					return PApplet.atan2(dy,dx);
				}
			}
			throw new RuntimeException("invalid input");
		}});
		env.addFunction("mod", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 2) throw new RuntimeException("mod takes 2 args");
			if (oPars[0] instanceof Number){
				if (oPars[1] instanceof Number){
					return
					((Number)oPars[0]).floatValue() 
					% 
					((Number)oPars[1]).floatValue() 
					;
				}
			}
			throw new RuntimeException("invalid input");
		}});

		env.addFunction("algS", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("algS takes 1 arg");
			if (oPars[0] instanceof Integer){
					return (
							((Integer)oPars[0]).intValue()
					*
					((Integer)oPars[0]).intValue() + 1
					) / 2
				;
			}
			throw new RuntimeException("invalid input");
		}});
		
		env.addFunction("invPolygonal", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 2) throw new RuntimeException("ivP takes 2 args");
			if (oPars[0] instanceof Integer && oPars[1] instanceof Integer){
				int target = ((Integer)oPars[0]).intValue();
				int s = ((Integer)oPars[1]).intValue();
				int toRet = 0;
				int sum = 0;
				while(true){
					sum += (s/2+1)*toRet*toRet-(s/2-2)*toRet;
					toRet++;
					if (sum > target){
						break;
					}
				}
				return toRet;
			}
			throw new RuntimeException("invalid input");
		}});
		
		env.addFunction("invPolygonal", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 2) throw new RuntimeException("ivP takes 2 args");
			if (oPars[0] instanceof Integer && oPars[1] instanceof Integer){
				int target = ((Integer)oPars[0]).intValue();
				int s = ((Integer)oPars[1]).intValue();
				int toRet = 0;
				int sum = 0;
				while(true){
					sum += (s/2+1)*toRet*toRet-(s/2-2)*toRet;
					toRet+=2;
					if (sum > target){
						break;
					}
				}
				return toRet;
			}
			throw new RuntimeException("invalid input");
		}});
		
		env.addFunction("polygonSum", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 2) throw new RuntimeException("poSum takes 2 args");
			if (oPars[0] instanceof Integer && oPars[1] instanceof Integer){
				int i = ((Integer)oPars[0]).intValue();
				int s = ((Integer)oPars[1]).intValue();
				int sum = 0;
				for(int k = 0; k < i; k+=2){
					sum += (s/2+1)*k*k-(s/2-2)*k;
				}
				return sum;
			}
			throw new RuntimeException("invalid input");
		}});
		
		env.addFunction("random", new Function(){public Object call(Object[] oPars) {
			if (oPars==null || oPars.length==0){
				return g2.random(1);
			}
			//oPars !=null && oPars.length!=0
			if (oPars.length==1){
				if (oPars[0] instanceof Number){
					return g2.random(((Number)oPars[0]).floatValue());
				}
				throw new RuntimeException("invalid input");
			} else if (oPars.length==2){
				if (oPars[0] instanceof Number){
					if (oPars[1] instanceof Number){
						float low = ((Number)oPars[0]).floatValue();
						float hi = ((Number)oPars[1]).floatValue();
						return g2.random(low,hi);
					}
				}
				throw new RuntimeException("invalid input");
			} else {
				throw new RuntimeException("random takes 0,1, or 2 args");
			}
		}});

		env.addFunction("sin", new Function(){public Object call(Object[] oPars) {
			//oPars !=null && oPars.length!=0
			if (oPars!=null && oPars.length==1){
				if (oPars[0] instanceof Number){
					return g.SinLUT(((Number)oPars[0]).floatValue());
				}
				throw new RuntimeException("invalid input");
			} else {
				throw new RuntimeException("sin takes 1 arg");
			}
		}});
		

		env.addFunction("cos", new Function(){public Object call(Object[] oPars) {
			//oPars !=null && oPars.length!=0
			if (oPars!=null && oPars.length==1){
				if (oPars[0] instanceof Number){
					return g.CosLUT(((Number)oPars[0]).floatValue());
				}
				throw new RuntimeException("invalid input");
			} else {
				throw new RuntimeException("cos takes 1 arg");
			}
		}});


		env.addConstant("PI", PApplet.PI);
		env.addConstant("HALF_PI", PApplet.HALF_PI);
		env.addConstant("TWO_PI", PApplet.TWO_PI);
		env.addConstant("E", (float)Math.E);

		//AUTOWRITTEN

		env.addFunction("tan", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("tan takes 1 args");
			return g2.tan(((Number)oPars[0]).floatValue());		
		}});
					env.addFunction("atan2", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 2) throw new RuntimeException("atan2 takes 2 args");
			return g2.atan2(((Number)oPars[0]).floatValue(),((Number)oPars[1]).floatValue());		}});
					env.addFunction("sqrt", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("sqrt takes 1 args");
			return g2.sqrt(((Number)oPars[0]).floatValue());		}});
					env.addFunction("pow", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 2) throw new RuntimeException("pow takes 2 args");
			return g2.pow(((Number)oPars[0]).floatValue(),((Number)oPars[1]).floatValue());		}});
					env.addFunction("min", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 2) throw new RuntimeException("min takes 2 args");
			return g2.min(((Number)oPars[0]).floatValue(),((Number)oPars[1]).floatValue());		}});
					env.addFunction("max", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 2) throw new RuntimeException("max takes 2 args");
			return g2.max(((Number)oPars[0]).floatValue(),((Number)oPars[1]).floatValue());		}});
					env.addFunction("abs", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("abs takes 1 args");
			return g2.abs(((Number)oPars[0]).floatValue());		}});
					env.addFunction("signum", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("signum takes 1 args");
			return Math.signum(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("acos", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("acos takes 1 args");
			return g2.acos(((Number)oPars[0]).floatValue());		}});
					env.addFunction("asin", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("asin takes 1 args");
			return g2.asin(((Number)oPars[0]).floatValue());		}});
					env.addFunction("atan", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("atan takes 1 args");
			return g2.atan(((Number)oPars[0]).floatValue());		}});
					env.addFunction("cbrt", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("cbrt takes 1 args");
			return Math.cbrt(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("ceil", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("ceil takes 1 args");
			return g2.ceil(((Number)oPars[0]).floatValue());		}});
					env.addFunction("cosh", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("cosh takes 1 args");
			return Math.cosh(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("exp", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("exp takes 1 args");
			return g2.exp(((Number)oPars[0]).floatValue());		}});
					env.addFunction("expm1", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("expm1 takes 1 args");
			return Math.expm1(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("floor", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("floor takes 1 args");
			return g2.floor(((Number)oPars[0]).floatValue());		}});
					env.addFunction("hypot", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 2) throw new RuntimeException("hypot takes 2 args");
			return Math.hypot(((Number)oPars[0]).doubleValue(),((Number)oPars[1]).doubleValue());		}});
					env.addFunction("log", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("log takes 1 args");
			return g2.log(((Number)oPars[0]).floatValue());		}});
					env.addFunction("log10", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("log10 takes 1 args");
			return Math.log10(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("log1p", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("log1p takes 1 args");
			return Math.log1p(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("rint", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("rint takes 1 args");
			return Math.rint(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("round", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("round takes 1 args");
			return g2.round(((Number)oPars[0]).floatValue());		}});
					env.addFunction("sinh", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("sinh takes 1 args");
			return Math.sinh(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("tanh", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("tanh takes 1 args");
			return Math.tanh(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("toDegrees", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("toDegrees takes 1 args");
			return Math.toDegrees(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("toRadians", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("toRadians takes 1 args");
			return Math.toRadians(((Number)oPars[0]).doubleValue());		}});
					env.addFunction("ulp", new Function(){public Object call(Object[] oPars) {
			if (oPars == null) throw new RuntimeException("null args");
			if (oPars.length != 1) throw new RuntimeException("ulp takes 1 args");
			return Math.ulp(((Number)oPars[0]).doubleValue());		}});
	}
	public float t = 0f;
	public float tb = 0f;
	public float angleToGirl = 0f;
	public float angleToGirl0 = 0f;
	public float theta = 0f;
	public float v = 0f;
	public float fired = 0f;
	public float store = 0f;
	public float store2 = 0f;
	public float sow = 0f;
	public float random0 = 0f;

	public float dotE = 0f;
	public float edgew = 0f;

	public int measure = 0;
	public int count = 0;
	public int alive = 0;
	public int isFocusing = 0;
	
	/**
	 * The index in the bulletpattern of the current bullet being computed
	 */
	public int bulletk = 0;
	
	public String BIGTSTR = "t";
	private Variable BIGT = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = t;
			return 0;
		}
	};
	public String BIGTBSTR = "tb";
	private Variable BIGTB = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = tb;
			return 0;
		}
	};
	public String BIGANGLETOGIRLSTR = "angle2P";
	private Variable BIGANGLETOGIRL = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = angleToGirl;
			return 0;
		}
	};
	public String BIGANGLETOGIRL_INITIALSTR = "angle2P0";
	private Variable BIGANGLETOGIRL_INITIAL = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = angleToGirl0;
			return 0;
		}
	};
	public String BIGCOUNTSTR = "count";
	private Variable BIGCOUNT = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifInt[0] = count;
			return 1;
		}
	};
	/**
	 * We can create difference equations by setting how many times per second
	 * the vector is changed.
	 */
	public String BIGTHETASTR = "theta";
	private Variable BIGTHETA = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = theta;
			return 0;
		}
	};
	public String BIGVSTR = "v";
	private Variable BIGV = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = v;
			return 0;
		}
	};
	public String BIGFIREDSTR = "fired";
	private Variable BIGFIRED = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = fired;
			return 0;
		}
	};

	public String BIGMEASUREDSTR = "measure";
	private Variable BIGMEASURE = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifInt[0] = measure;
			return 1;
		}
	};
	

	public String BIGSOWSTR = "sow";
	private Variable BIGSOW = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = sow;
			return 0;
		}
	};

	public String BIGSTORESTR = "store";
	private Variable BIGSTORE = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = store;
			return 0;
		}
	};

	public String BIGSTORE2STR = "store2";
	private Variable BIGSTORE2 = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = store2;
			return 0;
		}
	};


	public String BIGDOTESTR = "dotE";
	private Variable BIGDOTE = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = dotE;
			return 0;
		}
	};


	public String BIGEDGEWSTR = "edgew";
	private Variable BIGEDGEW = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = edgew;
			return 0;
		}
	};

	public String BIGALIVESTR = "alive";
	private Variable BIGALIVE = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifInt[0] = alive;
			return 1;
		}
	};
	

	public String BIGRANDOM0STR = "random0";
	private Variable BIGRANDOM0 = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifFloat[0] = random0;
			return 0;
		}
	};
	
	public String BIGISFOCUSINGSTR = "isFocusing";
	private Variable BIGISFOCUSING = new Variable(){
		public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject) {
			ifInt[0] = isFocusing;
			return 1;
		}
	};
}
