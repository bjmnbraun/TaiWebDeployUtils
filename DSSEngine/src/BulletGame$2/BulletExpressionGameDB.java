package BulletGame$2;

import TaiGameCore.GameDataBase;
import TaiGameCore.MultiExpression;

import com.iabcinc.jmep.Environment;
import com.iabcinc.jmep.XExpression;

public abstract class BulletExpressionGameDB extends GameDataBase{
	public BulletExpressionGameDB(String hash) {
		super(hash);
	}

	public MultiExpression expr(String expression, Environment env, BulletGlobals defines) throws XExpression{
		return new MultiExpression(expression,env, defines);
	}
}
