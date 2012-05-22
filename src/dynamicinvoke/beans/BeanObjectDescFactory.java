/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.beans;

import java.beans.Introspector;
import java.beans.IntrospectionException;
import dynamicinvoke.*;

public class BeanObjectDescFactory extends ObjectDescFactory {

	private int flag;

	public BeanObjectDescFactory() {
		if (Boolean.getBoolean("dynamicinvoke.respect_bean_info")) {
			this.flag = Introspector.USE_ALL_BEANINFO;
		} else {
			this.flag = Introspector.IGNORE_ALL_BEANINFO;
		}
	}

	public ObjectDesc create(Class<?> cls) {
		try {
			return new BeanObjectDesc(cls, flag);
		} catch (IntrospectionException e) {
			return null;
		}
	}
}
