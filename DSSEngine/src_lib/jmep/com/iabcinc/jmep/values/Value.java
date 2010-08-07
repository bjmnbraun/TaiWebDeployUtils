/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.iabcinc.jmep.values;

/**
 *
 * @author jd3714
 */
public abstract class Value<T> {
	// value should be immutable, so dont define setters!
	final private T value;

	public Value(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

}
