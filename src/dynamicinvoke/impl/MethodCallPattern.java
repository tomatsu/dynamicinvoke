/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

import dynamicinvoke.util.*;

public class MethodCallPattern implements Cloneable {
	Class<?> targetClass;
	String name;
	ArrayStack argPattern = new ArrayStack();

	public void initialize(Class<?> targetClass, String name, Class[] argTypes,
			int nargs) {
		this.targetClass = targetClass;
		this.name = name;
		this.argPattern.initialize(argTypes, nargs);
	}

	public int hashCode() {
		return targetClass.hashCode() + name.hashCode()
			+ argPattern.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj instanceof MethodCallPattern) {
			MethodCallPattern that = (MethodCallPattern) obj;
			return (that.targetClass == this.targetClass
				&& that.name == this.name && that.argPattern
				.equals(this.argPattern));
		}
		return false;
	}

	public MethodCallPattern clone(){
		try {
			MethodCallPattern clone = (MethodCallPattern)super.clone();
			clone.argPattern = (ArrayStack)argPattern.clone();
			return clone;
		} catch (CloneNotSupportedException e){
			throw new InternalError();
		}
	}

	public String toString(){
		return getClass().getName() + "(" + targetClass + "@" + targetClass.hashCode() + ", " + name + "," + argPattern + ")";
	}
}

