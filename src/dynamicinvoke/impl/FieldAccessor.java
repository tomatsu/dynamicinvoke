/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

/**
 * Proxy interface for field access
 */
public abstract class FieldAccessor {

	/**
	 * Get the field value
	 * 
	 * @param target
	 *            the target object
	 * @return the field value
	 */
	public abstract Object get(Object target);

	/**
	 * Set the field value
	 * 
	 * @param target
	 *            the target object
	 * @param value
	 *            a new value for the field
	 */
	public abstract void set(Object target, Object value);
}
