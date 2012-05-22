/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

/**
 * Stack implemented by an array
 */
public class ArrayStack<E> implements Cloneable {
	int count = 0;
	int capacity;
	Object[] values;

	public ArrayStack() {
		this(64);
	}

	public ArrayStack(int size) {
		this.values = new Object[size];
		this.capacity = size;
	}

	public ArrayStack(Object[] values, int size) {
		this.values = values;
		this.capacity = values.length;
		this.count = size;
	}

	public void initialize(Object[] values, int count) {
		this.values = values;
		this.capacity = values.length;
		this.count = count;
	}

	public void push(E object) {
		if (object == null) {
			throw new RuntimeException();
		}
		if (count >= capacity) {
			Object[] newValues = new Object[(count + 1) * 2];
			System.arraycopy(values, 0, newValues, 0, count);
			this.values = newValues;
		}
		this.values[count++] = object;
	}

	@SuppressWarnings("unchecked")
	public E pop() {
		try {
			return (E)this.values[--count];
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int size() {
		return count;
	}

	@SuppressWarnings("unchecked")
	public E peek() {
		return (E)values[count - 1];
	}

	public void removeAllElements() {
		this.count = 0;
	}

	public void copyInto(Object[] array) {
		throw new UnsupportedOperationException();
	}

	public int hashCode() {
		int c = 0;
		for (int i = 0; i < count; i++) {
			Object obj = values[i];
			if (obj != null) {
				c += obj.hashCode();
			}
		}
		return c;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ArrayStack) {
			ArrayStack that = (ArrayStack) obj;
			int count = this.count;
			if (that.count != count) {
				return false;
			}
			for (int i = 0; i < count; i++) {
				if (this.values[i] != that.values[i]) { 
					/*
					 * compare by object identity
					 */
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public Object clone(){
		try {
			ArrayStack clone = (ArrayStack)super.clone();
			clone.values = (Object[])values.clone();
			return clone;
		} catch (CloneNotSupportedException e){
			throw new InternalError();
		}
	}
}