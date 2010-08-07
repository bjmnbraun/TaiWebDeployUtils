/* * JMEP - Java Mathematical Expression Parser. * Copyright (C) 1999  Jo Desmet *  * This library is free software; you can redistribute it and/or * modify it under the terms of the GNU Lesser General Public * License as published by the Free Software Foundation; either * version 2.1 of the License, or any later version. *  * This library is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU * Lesser General Public License for more details. *  * You should have received a copy of the GNU Lesser General Public * License along with this library; if not, write to the Free Software * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA *  * You can contact the Original submitter of this library by * email at: Jo_Desmet@yahoo.com. *  */package com.iabcinc.jmep.tokens;import com.iabcinc.jmep.hooks.Variable;import com.iabcinc.jmep.XUndefinedVariable;public class VARToken extends Token {	private Variable variable;	//  private Object value;	private String name;	public VARToken(String name, int position) {		super(Token.VAR, position);		this.variable = null;		//    this.value = null;		this.name = name;	}	public VARToken(String sName, Variable variable, int position) {		super(Token.VAR, position);		this.variable = variable;		//    value = null;		name = sName;	}	//  public VARToken(String sName,int dValue,int position) {	//    super(Token.VAR,position);	//    variable = null;	//    value = new Integer(dValue);	//    name = sName;	//  }	//	//  public VARToken(String sName,boolean bValue,int position) {	//    super(Token.VAR,position);	//    variable = null;	//    value = new Integer(bValue?1:0);	//    name = sName;	//  }	//	//  public VARToken(String sName,double fValue,int position) {	//    super(Token.VAR,position);	//    variable = null;	//    value = new Double(fValue);	//    name = sName;	//  }	//	//  public VARToken(String sName,String sValue,int position) {	//    super(Token.VAR,position);	//    variable = null;	//    value = new String(sValue);	//    name = sName;	//  }	/**	 * Uses contextID 0	 */	public static Object getValue(Variable v) {		int[] ifInt = new int[1];		float[] ifFloat = new float[1];		Object[] ifObj = new Object[1];		int which = v.getValue(ifInt, ifFloat, ifObj, 0);		if (which == 0) {			return ifFloat[0];		}		if (which == 1) {			return ifInt[0];		}		return ifObj[0];	}	public Object evaluate(int[][] intArrays, float[][] floatArrays,			Object[][] objectArrays, int i, int contextID)			throws XUndefinedVariable {		//Object oValue = value;		//if (oValue != null) return oValue;		//if (variable != null) return variable.getValue();		if (variable == null)			throw new XUndefinedVariable(getPosition(), name);		//return oValue;		int whichType = variable.getValue(intArrays[i], floatArrays[i],				objectArrays[i], contextID);		if (whichType == 0) {			return floatArrays[i];		} else if (whichType == 1) {			return intArrays[i];		} else {			return objectArrays[i];		}	}	//  boolean isConstant() {	//    return value != null;	//  }}