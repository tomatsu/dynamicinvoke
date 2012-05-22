/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.beans;

import java.beans.BeanInfo;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;
import dynamicinvoke.*;

public class BeanObjectDesc implements ObjectDesc {
	private Class<?> cls;
	private int flag;
	private PropertyDescriptor[] pd;

	public BeanObjectDesc(Class<?> cls)
			throws IntrospectionException {
		this(cls, Introspector.IGNORE_ALL_BEANINFO);
	}

	public BeanObjectDesc(Class<?> cls, int flag)
			throws IntrospectionException {
		this.cls = cls;
		this.flag = flag;
		BeanInfo beanInfo = getBeanInfo(cls);
		this.pd = beanInfo.getPropertyDescriptors();
	}

	BeanInfo getBeanInfo(Class<?> targetClass)
			throws IntrospectionException {
		return Introspector.getBeanInfo(targetClass, flag);
	}

	public Method[] getMethods() {
		try {
			BeanInfo beanInfo;
			beanInfo = Introspector.getBeanInfo(cls, flag);
			MethodDescriptor[] methodDesc = beanInfo.getMethodDescriptors();
			Method[] m = new Method[methodDesc.length];
			for (int i = 0; i < methodDesc.length; i++) {
				m[i] = methodDesc[i].getMethod();
			}
			return m;
		} catch (IntrospectionException e) {
			return new Method[] {};
		}
	}

	public void handleProperties(PropertyHandler handler) {
		for (int i = 0; i < pd.length; i++) {
			PropertyDescriptor p = pd[i];
			Class<?> propertyType = null;
			Method readMethod = null;
			Method writeMethod = null;
			if (p instanceof IndexedPropertyDescriptor) {
				IndexedPropertyDescriptor ip = (IndexedPropertyDescriptor) p;
				propertyType = ip.getIndexedPropertyType();
				readMethod = ip.getIndexedReadMethod();
				writeMethod = ip.getIndexedWriteMethod();
			} else {
				propertyType = p.getPropertyType();
				readMethod = p.getReadMethod();
				writeMethod = p.getWriteMethod();
			}
			handler.handle(p.getName(), propertyType, readMethod, writeMethod);
		}
	}
}
