/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.iabcinc.jmep.hooks;

/**
 *
 * @author jd3714
 */
final public class Constant implements Variable {
	final private Object value;

	public Constant(Number value) {
		this.value = value;
	}

	public Constant(String value) {
		this.value = value;
	}

	public int getValue(int[] ifInt, float[] ifFloat, Object[] ifObject, int thread) {
		ifObject[0] = value;
		return 2;
	}
}
