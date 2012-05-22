/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

public abstract class DynamicProxy {

	public Object invoke(Object target) {
		throw new RuntimeException();
	}

	public Object invoke(Object target, Object arg) {
		throw new RuntimeException();
	}

	public Object invoke(Object target, Object[] args) {
		throw new RuntimeException();
	}
}
