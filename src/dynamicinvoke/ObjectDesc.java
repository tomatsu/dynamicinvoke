/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke;

import java.lang.reflect.Method;

public interface ObjectDesc {

	public Method[] getMethods();

	public void handleProperties(PropertyHandler handler);
}
