/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke;

import java.lang.reflect.Method;

public interface PropertyHandler {

	/**
	 * Called by ObjectDesc.handleProperties()
	 * 
	 * @param propertyName
	 *            a property name
	 * @param type
	 *            the type of the property
	 * @param readMethod
	 *            the read method for the property
	 * @param writeMethod
	 *            the write method for the property
	 */
	void handle(String propertyName, Class<?> type, Method readMethod,
			Method writeMethod);
}
