/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import dynamicinvoke.*;

public class ProxyCache {
	public final static int DEFAULT = 0;
	public final static int STATIC = 1;
	public final static int CONSTRUCTOR = 2;

	public Class[] paramTypes;
	public Class<?> declaringClass;
	public Class<?> targetClass;
	public Class<?> returnType;
	public int type;
	public DynamicProxy proxy;
	public boolean hasArrayParam;

	public ProxyCache(Method method, Class<?> clazz) {
		this.paramTypes = method.getParameterTypes();
		this.declaringClass = method.getDeclaringClass();
		this.returnType = method.getReturnType();
		this.type = Modifier.isStatic(method.getModifiers()) ? STATIC : DEFAULT;
		this.targetClass = clazz;
		init();
	}

	public ProxyCache(Constructor cons) {
		this.paramTypes = cons.getParameterTypes();
		this.declaringClass = cons.getDeclaringClass();
		this.targetClass = cons.getDeclaringClass();
		this.returnType = void.class;
		this.type = CONSTRUCTOR;
		init();
	}

	void init() {
		Class[] types = this.paramTypes;
		for (int i = 0; i < types.length; i++) {
			if (types[i].isArray()) {
				hasArrayParam = true;
				break;
			}
		}
	}

	public Object invoke(Object target) {
		return proxy.invoke(target);
	}

	public Object invoke(Object target, Object arg) {
		return proxy.invoke(target, arg);
	}

	public Object invoke(Object target, Object[] args) {
		int arity = args.length;
		if (hasArrayParam) {
			for (int i = 0; i < arity; i++) {
				Class<?> type = paramTypes[i];
				Object arg = args[i];
				if (arg != null && type.isArray() && !type.isInstance(arg)) {
					args[i] = DynamicRuntime.transform(type, arg);
				}
			}
		}
		if (arity == 1){
			return proxy.invoke(target, args[0]);
		}
		return proxy.invoke(target, args);
	}
}
