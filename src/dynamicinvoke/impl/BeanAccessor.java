/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

import dynamicinvoke.*;
import java.util.*;
import java.lang.reflect.*;

public class BeanAccessor {
	protected Class<?> beanClass;
	protected boolean isPublic;
	protected Map<String,Method> readMethods;
	protected Map<String,Method> writeMethods;
	protected Map<String,Class> types;

	public BeanAccessor(Class<?> cls) {
		init(cls);
	}

	protected void init(Class<?> cls) {
		this.isPublic = Modifier.isPublic(cls.getModifiers());
		this.beanClass = cls;
		this.readMethods = new HashMap<String,Method>();
		this.writeMethods = new HashMap<String,Method>();
		this.types = new HashMap<String,Class>();

		ObjectDescFactory.getDefault().create(cls)
				.handleProperties(new PropertyHandler() {
					public void handle(String propertyName, Class type,
							Method readMethod, Method writeMethod) {
						if (readMethod != null) {
							addReadMethod(propertyName, readMethod);
						}
						if (writeMethod != null) {
							addWriteMethod(propertyName, writeMethod);
						}
						types.put(propertyName, type);
					}
				});
	}

	protected void addReadMethod(String name, Method method) {
		readMethods.put(name, method);
	}

	protected void addWriteMethod(String name, Method method) {
		writeMethods.put(name, method);
	}

	public Method findReadMethod(String name) {
		return readMethods.get(name);
	}

	public Object findWriteMethod(String name) {
		return writeMethods.get(name);
	}

	public Class<?> getType(String name) {
		return types.get(name);
	}
}