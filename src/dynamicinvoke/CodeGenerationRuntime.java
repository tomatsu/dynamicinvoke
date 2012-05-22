/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.*;
import java.io.*;
import java.lang.reflect.Field;
import dynamicinvoke.util.*;
import dynamicinvoke.impl.*;

/**
 * This class provides API for method call, field access, and bean property access, which
 * are implemented by byte code generation.
 */
public class CodeGenerationRuntime extends ReflectionRuntime {

	private final static boolean DEBUG = false;

	static CodeLoader createCodeLoader() {
		return createCodeLoader(Thread.currentThread().getContextClassLoader());
	}

	static CodeLoader createCodeLoader(ClassLoader parent) {
		return new CodeLoader(parent);
	}

	/**
	 * Call a method
	 * 
	 * @param target
	 *            the target object
	 * @param c
	 *            the class of the target object
	 * @param name
	 *            the method name
	 * @param args
	 *            the arguments
	 * @param types
	 *            optional: the types of the arguments, which is used to select
	 *            the method.
	 * @return the returned object. If primitive type value is returned, returns
	 *         a boxed object.
	 */
	public Object callMethod(Object target, Class<?> c, String name,
				 Object args[], Class types[]) throws Exception

	{
		if (DEBUG) {
			System.err.println("callMethod " + target + ", " + c + ", " + name);
			System.err.print("args.length="+args.length);
			if (types != null && types.length > 0){
				System.err.println("types.length=" + types.length);
				System.err.print(types[0]);
				for (int i = 1; i < types.length; i++){
					System.err.print(",");
					System.err.print(types[i]);
				}
				System.err.println();
			}
		}
		MethodCallEnv env = getMethodCallEnv();

		CodeLoader codeLoader = env.codeLoader;
		ArrayStack<ProxyCache> methods = env.methodProxyBuffer;
		methods.removeAllElements();

		ProxyCache methodCache = null;
		boolean cacheHit = false;

		ProxyCache first = null;
		int count = 0;
		int min = Integer.MAX_VALUE;
		int nargs = args.length;
		int nc = 0;
		boolean gotClass = false;
		Class[] argTypes = null;
		MethodCallPattern pattern = null;

		if (nargs > 0 && nargs <= 64) {
			argTypes = env.argBuffer;
			if (types != null){
				for (int i = 0; i < nargs; i++) {
					if (types[i] != null){
						argTypes[i] = types[i];
					} else {
						Object a = args[i];
						if (a != null) {
							argTypes[i] = args[i].getClass();
						} else {
							argTypes[i] = null;
						}
					}
				}
			} else {
				for (int i = 0; i < nargs; i++) {
					Object a = args[i];
					if (a != null) {
						argTypes[i] = args[i].getClass();
					} else {
						argTypes[i] = null;
					}					
				}
			}
			pattern = env.methodCallPattern;
			pattern.initialize(c, name, argTypes, nargs);
			Map<MethodCallPattern,ProxyCache> pcm = env.methodCallProxyCache;
			methodCache = pcm.get(pattern);
			cacheHit = (methodCache != null);
			gotClass = true;
		}
		if (!cacheHit) {
			ProxyCache[] m = getMethodProxyCache(c, name, env);
			if (m == null) {
				return super.callMethod(target, c, name, args, types);
			}
			int mlen = m.length;
			cand: for (int i = 0; i < mlen; i++) {
				ProxyCache mi = m[i];
				Class p[] = mi.paramTypes;
				if (p.length != nargs) {
					continue;
				}
				count = 0;
				int plen = p.length;
				for (int j = 0; j < plen; j++) {
					Class<?> pj = p[j];
					Class<?> tj;
					if (types != null) {
						tj = types[j];
						if (tj != null){
							if (pj != tj && !pj.isAssignableFrom(tj)) {
								continue cand;
							}
						} else {
							if (gotClass){
								tj = argTypes[j];
							} else {
								tj = (args[j] != null ? args[j].getClass() : null);
							}
						}
					} else {
						if (gotClass){
							tj = argTypes[j];
						} else {
							tj = (args[j] != null ? args[j].getClass() : null);
						}						
					}
					int t  = matchType(pj, args[j], tj);
					if (DEBUG) {
						System.err.println("t=" + t);
					}
					if (t < 0) {
						continue cand;
					}
					count += t;
				}
				if (count > min) {
					continue;
				}
				if ((mi.type == ProxyCache.STATIC) != (target == null)) {
					continue;
				}
				if (nc == 0) {
					methods.push(first = mi);
					if (DEBUG) {
						System.err.println("push " + mi);
					}
					min = count;
					nc++;
				} else {
					if (count < min) {
						methods.removeAllElements();
						methods.push(first = mi);
						if (DEBUG) {
							System.err.println("push " + mi);
						}
						nc = 1;
						min = count;
					} else if (count == min) {
						methods.push(mi);
						first = null;
						nc++;
						if (DEBUG) {
							System.err.println("push " + mi);
						}
					}
				}
			}
			if (first == null) {
				methodCache = null;
				Class<?> clazz = c;
				out: while (clazz != null) {
					int size = methods.size();
					for (int i = 0; i < size; i++) {
						methodCache = methods.pop();
						if (methodCache.declaringClass == clazz) {
							break out;
						}
					}
					clazz = clazz.getSuperclass();
				}
			} else {
				methodCache = first;
				if (DEBUG){
					System.err.println("took first one");
				}
			}
			if (pattern != null) {
				if (methodCache != null) {
					env.methodCallProxyCache.put(pattern.clone(), methodCache);
				}
			}

		}

		if (methodCache != null) {
			boolean retry = false;
			while (true) {
				try {
					if (methodCache.proxy == null || retry) {
						methodCache.proxy = 
							DynamicProxyFactory.makeProxy(name,
										      methodCache.targetClass,
										      methodCache.returnType, methodCache.paramTypes,
										      methodCache.type, codeLoader);
					}
					if (nargs == 0) {
						if (DEBUG) System.err.println("invoke " + target);
						return methodCache.invoke(target);
					} else {
						if (DEBUG) System.err.println("invoke " + target);
						return methodCache.invoke(target, args);
					}
				} catch (ClassCastException cce) {
					if (DEBUG) {
						cce.printStackTrace();
					}
					if (!retry) {
						codeLoader = createCodeLoader(getClassLoader(c));
						retry = true;
						continue;
					}
					if (DEBUG) {
						System.err.println("use reflection");
					}
					return super.callMethod(target, c, name, args, types);
				} catch (LinkageError err) {
					if (DEBUG) {
						err.printStackTrace();
					}
					if (!retry) {
						codeLoader = createCodeLoader(getClassLoader(c));
						retry = true;
						continue;
					}
					if (DEBUG) {
						System.err.println("use reflection");
					}
					return super.callMethod(target, c, name, args, types);
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Call constructor
	 *
	 * @param c the class
	 * @param args the arguments
	 * @param types optional type information of the arguments, which is used to select
	 *        the method
	 * @return the instance
	 */
	public Object callConstructor(Class<?> c, Object args[], Class types[]) throws Exception {
		if (DEBUG) {
			System.err.println("callConstructor " + c);
			System.err.print("args.length="+args.length);
			if (types != null && types.length > 0){
				System.err.println("types.length=" + types.length);
				System.err.print(types[0]);
				for (int i = 1; i < types.length; i++){
					System.err.print(",");
					System.err.print(types[i]);
				}
				System.err.println();
			}
		}
		MethodCallEnv env = getMethodCallEnv();
		CodeLoader codeLoader = env.codeLoader;

		ProxyCache cons = null;
		boolean gotClass = false;
		int nargs = args.length;
		Class[] argTypes = null;
		MethodCallPattern pattern = null;
		boolean cacheHit = false;

		if (nargs > 0 && nargs <= 64) {
			argTypes = env.argBuffer;
			if (types != null){
				for (int i = 0; i < nargs; i++) {
					if (types[i] != null){
						argTypes[i] = types[i];
					} else {
						Object a = args[i];
						if (a != null) {
							argTypes[i] = args[i].getClass();
						} else {
							argTypes[i] = null;
						}
					}
				}				
			} else {
				for (int i = 0; i < nargs; i++) {
					Object a = args[i];
					if (a != null) {
						argTypes[i] = args[i].getClass();
					} else {
						argTypes[i] = null;
					}
				}
			}
			pattern = env.methodCallPattern;
			pattern.initialize(c, "<init>", argTypes, nargs);
			Map<MethodCallPattern,ProxyCache> pcm = env.methodCallProxyCache;
			cons = pcm.get(pattern);
			cacheHit = (cons != null);
			gotClass = true;
		}

		if (!cacheHit) {
			Map<Class<?>,ProxyCache[]> constructorProxyCache = env.constructorProxyCache;
			ProxyCache cs[] = constructorProxyCache.get(c);
			if (cs == null) {
				Constructor con[] = c.getConstructors();
				ProxyCache px[] = new ProxyCache[con.length];
				for (int i = 0; i < con.length; i++) {
					px[i] = new ProxyCache(con[i]);
				}
				constructorProxyCache.put(c, px);
				cs = px;
			}

			int count = 0;
			int min = Integer.MAX_VALUE;
			cand: for (int i = 0; i < cs.length; i++) {
				Class p[] = cs[i].paramTypes;
				if (p.length != args.length) {
					continue;
				}
				count = 0;
				for (int j = 0; j < p.length; j++) {
					Class<?> pj = p[j];
					Class<?> tj;
					if (types != null) {
						tj = types[j];
						if (tj != null){
							if (pj != tj && !pj.isAssignableFrom(tj)) {
								continue cand;
							}
						} else {
							if (gotClass){
								tj = argTypes[j];
							} else {
								tj = (args[j] != null ? args[j].getClass() : null);
							}
						}
					} else {
						if (gotClass){
							tj = argTypes[j];
						} else {
							tj = (args[j] != null ? args[j].getClass() : null);
						}
					}
					int t = matchType(pj, args[j], tj);
					if (DEBUG) {
						System.err.println("t=" + t);
					}
					if (t < 0) {
						continue cand;
					}
					count += t;
				}
				if (count < min) {
					min = count;
					cons = cs[i];
				}
			}
			if (pattern != null) {
				if (cons != null) {
					env.methodCallProxyCache.put(pattern.clone(), cons);
				}
			}
		}

		if (cons != null) {
			boolean retry = false;
			while (true) {
				if (cons.proxy == null || retry) {
					cons.proxy = DynamicProxyFactory
						.makeProxy("<init>", cons.declaringClass,
							   cons.returnType, cons.paramTypes,
							   ProxyCache.CONSTRUCTOR, codeLoader);
				}
				try {
					if (DEBUG) System.err.println("invoke " + cons);					
					if (args.length == 0) {
						return cons.invoke(null);
					} else {
						return cons.invoke(null, args);
					}
				} catch (ClassCastException cce) {
					if (DEBUG) {
						System.err.println(cce);
					}
					if (!retry) {
						codeLoader = createCodeLoader(getClassLoader(c));
						retry = true;
						continue;
					}
					if (DEBUG) {
						System.err.println("use reflection");
					}
					return super.callConstructor(c, args, types);
				} catch (LinkageError err) {
					if (!retry) {
						codeLoader = createCodeLoader(getClassLoader(c));
						retry = true;
						continue;
					}
					if (DEBUG) {
						System.err.println(err);
					}
					return super.callConstructor(c, args, types);
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
	}


	static ClassLoader getClassLoader(final Class<?> clazz) {
		return clazz.getClassLoader();
	}

	ProxyCache[] getMethodProxyCache(Class<?> cls, String name, MethodCallEnv env) {
		if (env.lastClass == cls && env.lastName == name) {
			return env.lastValue;
		}
		MemberAccessPattern pattern = env.memberAccessPattern;
		pattern.initialize(cls, name);
		Map<MemberAccessPattern,ProxyCache[]> methodProxyCache = env.methodProxyCache;
		ProxyCache[] cache = methodProxyCache.get(pattern);
		if (cache != null) {
			env.lastClass = cls;
			env.lastName = name;
			env.lastValue = cache;
		} else {
			Method m[] = cls.getMethods();
			if (m == null) { // for Bug:4137722
				throw new NoClassDefFoundError("" + cls);
			}
			int j = 0;
			for (int i = 0; i < m.length; i++) {
				String m_name = m[i].getName();
				if (m_name.equals(name) && i >= j) {
					m[j] = m[i];
					j++;
				}
			}
			ProxyCache px[] = new ProxyCache[j];
			for (int i = 0; i < j; i++) {
				Method mi = findCallableMethod(cls, m[i].getName(), m[i].getParameterTypes());
				if (mi != null) {
					px[i] = new ProxyCache(mi, cls);
				} else {
					px[i] = new ProxyCache(m[i], cls);
				}
			}
			methodProxyCache.put(pattern.clone(), cache = px);
			env.lastClass = cls;
			env.lastName = name;
			env.lastValue = cache;
		}
		return cache;
	}

	/**
	 * Get object field
	 *
	 * @param target the object that has the instance field to be accessed, or null for static field
	 * @param c the class of the object, or the class that has the static field to be accessed
	 * @param name the field name
	 * @return value the field value
	 */
	public Object getObjectField(Object target, Class<?> c, String name)
		throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
	{
		MethodCallEnv env = getMethodCallEnv();
		CodeLoader codeLoader = env.codeLoader;
		Map<MemberAccessPattern,FieldAccessor> cache = env.fieldProxyCache;
		MemberAccessPattern pattern = env.memberAccessPattern;
		pattern.initialize(c, name);
		FieldAccessor fa = cache.get(pattern);
		try {
			if (fa == null) {
				if (target == null) {
					fa = FieldAccessorGenerator.generate(name, c, codeLoader, true);
				} else {
					fa = FieldAccessorGenerator
						.generate(name, c, codeLoader, false);
				}
				cache.put(pattern.clone(), fa);
			}
			return fa.get(target);
		} catch (InstantiationException e1){
			throw new IllegalArgumentException();
		} catch (IOException e2){
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Set object field
	 *
	 * @param target the object that has the instance field to be accessed, or null for static field
	 * @param c the class of the object, or the class that has the static field to be accessed
	 * @param name the field name
	 * @param value the new value
	 */
	public void setObjectField(Object target, Class<?> c, String name, Object value)
		throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException 
	{
		MethodCallEnv env = getMethodCallEnv();
		CodeLoader codeLoader = env.codeLoader;
		Map<MemberAccessPattern,FieldAccessor> cache = env.fieldProxyCache;
		MemberAccessPattern pattern = env.memberAccessPattern;
		pattern.initialize(c, name);
		FieldAccessor fa = cache.get(pattern);
		try {
			if (fa == null) {
				if (target == null) {
					fa = FieldAccessorGenerator.generate(name, c, codeLoader, true);
				} else {
					fa = FieldAccessorGenerator
						.generate(name, c, codeLoader, false);
				}
				cache.put(pattern.clone(), fa);
			}
			fa.set(target, value);
		} catch (InstantiationException e1){
			throw new IllegalArgumentException();
		} catch (IOException e2){
			throw new IllegalArgumentException();
		}
	}

	/* Bean Access */

	DynamicBeanAccessor getDynamicBeanAccessor(Class<?> cls) {
		MethodCallEnv env = getMethodCallEnv();
		Map<Class,DynamicBeanAccessor> beanProxyCache = env.beanProxyCache;
		DynamicBeanAccessor a = beanProxyCache.get(cls);
		if (a == null) {
			a = createDynamicBeanAccessor(cls);
			beanProxyCache.put(cls, a);
		}
		return a;
	}

	/**
	 * Gets a Bean property of the specified bean.
	 * 
	 * @param target
	 *            the target bean
	 * @param name
	 *            the Bean property name
	 */
	public Object getBeanProperty(Object target, String name)
		throws IllegalAccessException 
	{
		DynamicBeanAccessor a = getDynamicBeanAccessor(target.getClass());
		ProxyCache readMethodProxy = a.findReadMethodProxy(name);
		if (readMethodProxy != null) {
			return readMethodProxy.invoke(target);
		}
		throw new IllegalArgumentException("not readable property: target="
						   + target + ", fieldName=" + name);
	}

	/**
	 * Sets a Bean property of the specified bean.
	 * 
	 * @param target
	 *            the target bean
	 * @param name
	 *            the Bean property name
	 * @param value
	 *            the new property value
	 */
	public void setBeanProperty(Object target, String name, Object value)
		throws IllegalAccessException, IllegalArgumentException
	{
		DynamicBeanAccessor a = getDynamicBeanAccessor(target.getClass());
		ProxyCache writeMethodProxy = a.findWriteMethodProxy(name);
		if (writeMethodProxy != null) {
			try {
				writeMethodProxy.invoke(target, value);
				return;
			} catch (ClassCastException cce) {
				try {
					throw new RuntimeException("todo");
				} catch (Exception e) {
					throw new IllegalArgumentException(/* msg */);
				}
			}
		}
		throw new IllegalArgumentException("not writable property: target="
						   + target + ", fieldName=" + name);
	}

	DynamicBeanAccessor createDynamicBeanAccessor(Class<?> cls) {
		return new DynamicBeanAccessor(cls);
	}

	public Object getIndexedBeanProperty(Object target, String name, int idx)
		throws IllegalAccessException, IllegalArgumentException
	{
		DynamicBeanAccessor a = getDynamicBeanAccessor(target.getClass());
		ProxyCache readMethodProxy = a.findReadMethodProxy(name);
		if (readMethodProxy != null) {
			if (readMethodProxy.paramTypes.length == 1 &&
			    readMethodProxy.paramTypes[0] == int.class){
				return readMethodProxy.invoke(target, new Integer(idx));
			}
		}
		throw new IllegalArgumentException("not readable property: target="
						   + target + ", fieldName=" + name);
	}

	public Object setIndexedBeanProperty(Object target, String name, int idx, Object value)
		throws IllegalAccessException, IllegalArgumentException
	{
		DynamicBeanAccessor a = getDynamicBeanAccessor(target.getClass());
		ProxyCache writeMethodProxy = a.findWriteMethodProxy(name);
		if (writeMethodProxy != null) {
			if (writeMethodProxy.paramTypes.length == 2 && 
			    writeMethodProxy.paramTypes[0] == int.class){
				return writeMethodProxy.invoke(target, 
							       new Object[]{new Integer(idx), value});
			}
		}
		throw new IllegalArgumentException("not readable property: target="
						   + target + ", fieldName=" + name);
	}

}
