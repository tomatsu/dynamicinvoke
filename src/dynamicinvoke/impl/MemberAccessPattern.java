/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

public class MemberAccessPattern implements Cloneable {
	Class<?> targetClass;
	String name;

	public void initialize(Class<?> targetClass, String name) {
		this.targetClass = targetClass;
		this.name = name;
	}

	public int hashCode() {
		return targetClass.hashCode() + name.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj instanceof MemberAccessPattern) {
			MemberAccessPattern that = (MemberAccessPattern) obj;
			return (that.targetClass == this.targetClass && that.name.equals(this.name));
		}
		return false;
	}

	public MemberAccessPattern clone(){
		try {
			return (MemberAccessPattern)super.clone();
		} catch (CloneNotSupportedException e){
			throw new InternalError();
		}
	}

	public String toString(){
		return getClass().getName() + "(" + targetClass + "@" + targetClass.hashCode() + ", " + name + ")";
	}
		
}
