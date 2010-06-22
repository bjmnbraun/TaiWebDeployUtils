package TaiGameCore;

import BulletGame$2.BulletGlobals;
import BulletGame$2.BulletHellEnv;
import Deployments.BulletHell.BulletGame$1Engine$GROUND;

import com.iabcinc.jmep.Environment;
import com.iabcinc.jmep.Expression;
import com.iabcinc.jmep.XExpression;
import com.iabcinc.jmep.XIllegalFunctionCall;

public class MultiExpression {
	public static void main(String[] args) throws XExpression{
		MultiExpression m = new MultiExpression("((1+1)+(1+1))*4+1-2+3-5+3",new BulletHellEnv(new BulletGame$1Engine$GROUND(null,null)).env, new BulletGlobals(""));
		long now = System.nanoTime();
		int x = 0;
		double num = 5000000;
		for(int y = 0; y < num; y++){
			m.evaluatef();
			//x += y;
		}
		int rate = (int)(1e9/((System.nanoTime()-now)/num));
		System.out.println("Throughput (in 1/60th of a second):  = "+rate/60);
	}
	/**
	 * Used if you want to override default behavior
	 */
	public MultiExpression(){

	}
	public MultiExpression(String expression, Environment env, BulletGlobals defines) throws XExpression{
		try {
			expression = defines.replaceGlobalConstants(expression);
		} catch (Throwable e){
			throw new XExpression(0, e.getMessage());
		}
		expression = expression.trim();
		if (expression.startsWith("{")){
			expression = expression.substring(1);
			//Do multi parsing
			String[] cases = expression.split("[:&]");
			if (expression.endsWith(":")||expression.endsWith("&")){
				throw new XExpression(0, "Trailing "+expression.charAt(expression.length()-1));
			}
			if (cases.length%2!=0 || cases.length==0){
				throw new XExpression(0,"Invalid multiexpression");
			}
			conditions = new Expression[cases.length/2];
			for(int k = 0; k < conditions.length; k++){
				conditions[k] = new Expression(cases[k*2],env);
			}
			//Make sure these return booleans:
			int constraintCheck = 0;
			for(Expression q : conditions){
				constraintCheck++;
				if (!(q.evaluate() instanceof int[])){
					throw new XExpression(0,"Contraint #"+constraintCheck+" invalid");
				};
			}
			values = new Expression[cases.length/2];
			for(int k = 0; k < values.length; k++){
				values[k] = new Expression(cases[k*2+1]+"+0.0",env);
			}
			//Test these:
			constraintCheck = 0;
			for(Expression q : values){
				constraintCheck++;
				q.evaluate();
			}
		} else {
			conditions = null;
			values = new Expression[]{
					new Expression(expression+"+0.0",env),
			};
		};
	}
	private Expression[] conditions;
	private Expression[] values;
	private int k_cond;
	public float evaluatef() throws XExpression {
		try {
			if (conditions==null){
				Object eval = values[0].evaluate();
				if (eval instanceof float[]){
					return ((float[])eval)[0];
				} else {
					return ((int[])eval)[0];
				}
			}
			for(k_cond = 0; k_cond < conditions.length; k_cond++){
				if (((int[])conditions[k_cond].evaluate())[0]==1){
					Object eval = values[k_cond].evaluate();
					if (eval instanceof float[]){
						return ((float[])eval)[0];
					} else {
						return ((int[])eval)[0];
					}
				}
			}
			throw new XIllegalFunctionCall(0,"Constraint not closed",null);
		} catch (RuntimeException  e){
			e.printStackTrace();
			return 0;
		}
	}
	public int evaluatei() throws XExpression {
		if (conditions==null){
			return ((int[])values[0].evaluate())[0];
		}
		for(k_cond = 0; k_cond < conditions.length; k_cond++){
			if (((int[])conditions[k_cond].evaluate())[0]==1){
				return ((int[])values[k_cond].evaluate())[0];
			}
		}
		throw new XIllegalFunctionCall(0,"Constraint not closed",null);
	}
}
