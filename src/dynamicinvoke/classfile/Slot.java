/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.classfile;

class Slot {

	Object key;
	Object value;
	Slot chain;

	Slot() {
	}

	Slot(Object key, Object value) {
		this.key = key;
		this.value = value;
	}

	public void set(Object value) {
		this.value = value;
	}

	public Object get() {
		return value;
	}
}
