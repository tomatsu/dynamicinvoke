/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import dynamicinvoke.*;

public class DynamicBeanAccessor extends BeanAccessor {
	static Object[] noarg = new Object[0];
	private Map<String,ProxyCache> readMethodProxies;
	private Map<String,ProxyCache> writeMethodProxies;

	public DynamicBeanAccessor(Class<?> cls) {
		super(cls);
	}

	protected void init(Class<?> cls) {
		this.readMethodProxies = new HashMap<String,ProxyCache>();
		this.writeMethodProxies = new HashMap<String,ProxyCache>();
		super.init(cls);
	}

	protected void addReadMethod(String name, Method m) {
		Method m0 = m;
		if (!isPublic) {
			String methodName = m.getName();
			Class[] types = m.getParameterTypes();
			m = DynamicRuntime.findCallableMethod(beanClass, methodName, types);
		}
		if (m != null) {
			CodeLoader loader = new CodeLoader(m.getDeclaringClass()
					.getClassLoader());
			DynamicProxy px = DynamicProxyFactory.makeProxy(m, loader);
			ProxyCache pc = new ProxyCache(m, beanClass);
			pc.proxy = px;
			readMethodProxies.put(name, pc);
		} else {
			DynamicProxy px = createReflectionProxy(beanClass, name);
			ProxyCache pc = new ProxyCache(m0, beanClass);
			pc.proxy = px;
			readMethodProxies.put(name, pc);
			writeMethodProxies.put(name, pc);
		}
	}

	protected void addWriteMethod(String name, Method m) {
		Method m0 = m;
		if (!isPublic) {
			String methodName = m.getName();
			Class[] types = m.getParameterTypes();
			m = DynamicRuntime.findCallableMethod(beanClass, methodName, types);
		}
		if (m != null) {
			CodeLoader loader = new CodeLoader(m.getDeclaringClass()
					.getClassLoader());
			DynamicProxy px = DynamicProxyFactory.makeProxy(m, loader);
			ProxyCache pc = new ProxyCache(m, beanClass);
			pc.proxy = px;
			writeMethodProxies.put(name, pc);
		} else {
			DynamicProxy px = createReflectionProxy(beanClass, name);
			ProxyCache pc = new ProxyCache(m0, beanClass);
			pc.proxy = px;
			readMethodProxies.put(name, pc);
			writeMethodProxies.put(name, pc);
		}
	}

	public ProxyCache findReadMethodProxy(String name) {
		return readMethodProxies.get(name);
	}

	public ProxyCache findWriteMethodProxy(String name) {
		return writeMethodProxies.get(name);
	}

	static void createMethodMap(ObjectDesc d,
				    final Map<String,Method> readMethods,
				    final Map<String,Method> writeMethods) 
	{
		d.handleProperties(new PropertyHandler() {
			public void handle(String propertyName, Class<?> type,
					Method readMethod, Method writeMethod) {
				if (readMethod != null) {
					readMethods.put(propertyName, readMethod);
				}
				if (writeMethod != null) {
					writeMethods.put(propertyName, writeMethod);
				}
			}
		});
	}

	static DynamicProxy createReflectionProxy(Class<?> cls, String name) {
		ObjectDesc od = ObjectDescFactory.getDefault().create(cls);
		Map<String,Method> rmap = new HashMap<String,Method>();
		Map<String,Method> wmap = new HashMap<String,Method>();
		createMethodMap(od, rmap, wmap);
		Method w = rmap.get(name);
		Method r = wmap.get(name);
		if (r != null) {
			r.setAccessible(true);
		}
		if (w != null) {
			w.setAccessible(true);
		}
		final Method readMethod = r;
		final Method writeMethod = w;
		DynamicProxy px = new DynamicProxy() {
			public Object invoke(Object target) {
				try {
					return readMethod.invoke(target, noarg);
				} catch (IllegalAccessException iae) {
				} catch (InvocationTargetException ite) {
				}
				return null;
			}

			public Object invoke(Object target, Object[] args) {
				try {
					writeMethod.invoke(target, args);
				} catch (IllegalAccessException iae) {
				} catch (InvocationTargetException ite) {
				}
				return null;
			}
		};
		return px;
	}
}
