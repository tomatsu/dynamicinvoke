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
 * are implemented by reflection.
 */
public class ReflectionRuntime extends DynamicRuntime {

	private final static boolean DEBUG = false;

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
				 Object args[], Class types[]) throws Exception {
		MethodCallEnv env = getMethodCallEnv();
		ArrayStack<Method> methods = env.methodBuffer;

		Method method = null;
		boolean _static = (target == null);
		Method m[] = getMethods(c, name, env);

		try {
			int count = 0;
			int min = Integer.MAX_VALUE;
			cand: for (int i = 0; i < m.length; i++) {
				Method mi = m[i];
				Class p[] = mi.getParameterTypes();
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
							tj = (args[j] != null ? args[j].getClass() : null);
						}
					} else {
						tj = (args[j] != null ? args[j].getClass() : null);
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
				if (count > min) {
					continue;
				}

				boolean st = Modifier.isStatic(mi.getModifiers());
				if (st != _static) {
					continue;
				}

				if (count < min) {
					methods.removeAllElements();
					methods.push(mi);
					min = count;
				} else if (count == min) {
					methods.push(mi);
				}
			}
			Class<?> clazz = c;
			out: while (clazz != null) {
				int size = methods.size();
				for (int i = 0; i < size; i++) {
					method = methods.pop();
					if (!_static && method.getDeclaringClass() == clazz) {
						break out;
					}
				}
				clazz = clazz.getSuperclass();
			}
			if (method != null) {
				if (args.length > 0) {
					Class p[] = method.getParameterTypes();
					for (int j = 0; j < p.length; j++) {
						Class<?> pj = p[j];
						if (args[j] != null
						    && isArray(args[j])
						    && (pj.isArray() || List.class
							.isAssignableFrom(pj))) {
							if (!pj.isInstance(args[j])) {
								args[j] = transform(pj, args[j]);
							}
						}
					}
				}
				try {
					if (DEBUG) {
						System.err.println("call " + method);
					}
					return method.invoke(target, args);
				} catch (InvocationTargetException ita) {
					Throwable t = ita.getTargetException();
					if (t instanceof Exception){
						throw (Exception)t;
					} else {
						throw (Error)t;
					}
				}
			} else {
				throw new IllegalArgumentException();
			}
		} catch (IllegalAccessException pe) {
			Class<?> cls = method.getDeclaringClass();
			try {
				if (!Modifier.isPublic(cls.getModifiers())) {
					Method _m = findCallableMethod(cls, name,
								       method.getParameterTypes());
					if (DEBUG){
						System.err.println("findCallableMethod => "+  _m);
					}
					if (_m != null) {
						for (int i = 0; i < m.length; i++) {
							if (m[i] == method) {
								if (DEBUG) {
									System.err.println(_m + " <- " + method);
								}
								m[i] = _m;
								break;
							}
						}
						return _m.invoke(target, args);
					}
				}
				return reInvoke(pe, method, target, args);
			} catch (IllegalAccessException iae) {
				throw iae;
			} catch (InvocationTargetException ita) {
				Throwable t = ita.getTargetException();
				if (t instanceof Exception){
					throw (Exception)t;
				} else {
					throw (Error)t;
				}
			}
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
	public Object callConstructor(Class<?> c, Object args[],
				      Class types[]) throws Exception
	{
		if (DEBUG) {
			System.err.println("callConstructorByReflection " + c);
		}

		Constructor cs[] = c.getConstructors();

		Constructor cons = null;
		int count = 0;
		int min = Integer.MAX_VALUE;
		cand: for (int i = 0; i < cs.length; i++) {
			Class p[] = cs[i].getParameterTypes();
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
						tj = (args[j] != null ? args[j].getClass() : null);
					}
				} else {
					tj = (args[j] != null ? args[j].getClass() : null);
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
		if (cons != null) {
			Class p[] = cons.getParameterTypes();
			for (int j = 0; j < p.length; j++) {
				Class<?> pj = p[j];
				if (args[j] != null
				    && isArray(args[j])
				    && (pj.isArray() || List.class.isAssignableFrom(pj))) {
					if (!pj.isInstance(args[j])) {
						args[j] = transform(pj, args[j]);
					}
				}
			}
			try {
				return cons.newInstance(args);
			} catch (InvocationTargetException ita) {
				Throwable t = ita.getTargetException();
				if (t instanceof Exception){
					throw (Exception)t;
				} else {
					throw (Error)t;
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
	}


	Method[] getMethods(Class<?> cls, String name, MethodCallEnv env) {
		MemberAccessPattern pattern = env.memberAccessPattern;
		pattern.initialize(cls, name);
		Map<MemberAccessPattern,Method[]> methodCache = env.methodCache;
		Method[] v = methodCache.get(pattern);
		if (v != null) {
			return v;
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
			Method m2[] = new Method[j];
			System.arraycopy(m, 0, m2, 0, j);
			methodCache.put(pattern.clone(), m2);
			return m2;
		}
	}

	Object reInvoke(IllegalAccessException t, final Method method,
			Object target, Object[] args) throws IllegalAccessException,
							     InvocationTargetException {
		if (DEBUG) {
			System.err.println("setAccessible " + method.getName());
		}
		method.setAccessible(true);
		return method.invoke(target, args);
	}

	protected Field getField(final Class<?> cls, final String name) throws NoSuchFieldException {
		MethodCallEnv env = getMethodCallEnv();
		Map<MemberAccessPattern,Field> fieldCache = env.fieldCache;
		MemberAccessPattern pattern = env.memberAccessPattern;
		pattern.initialize(cls, name);
		Field field = fieldCache.get(pattern);
		if (field == null) {
			field = cls.getField(name);
			fieldCache.put(pattern.clone(), field);
		}
		return field;
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
		Field f = getField(c, name);
		if (f == null){
			throw new IllegalArgumentException();
		}
		return f.get(target);
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
		Field f = getField(c, name);
		if (f == null){
			throw new IllegalArgumentException();
		}
		f.set(target, value);
	}

	BeanAccessor getBeanAccessor(Class<?> cls) {
		MethodCallEnv env = getMethodCallEnv();
		Map<Class,BeanAccessor> beanAccessors = env.beanAccessors;
		BeanAccessor a = beanAccessors.get(cls);
		if (a == null) {
			a = createBeanAccessor(cls);
			beanAccessors.put(cls, a);
		}
		return a;
	}

	BeanAccessor createBeanAccessor(Class<?> cls) {
		return new BeanAccessor(cls);
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
			throws IllegalAccessException, IllegalArgumentException
	{
		BeanAccessor a = getBeanAccessor(target.getClass());
		Method readMethod = (Method) a.findReadMethod(name);
		try {
			if (readMethod != null && readMethod.getParameterTypes().length == 0) {
				return readMethod.invoke(target, new Object[]{});
			}
		} catch (InvocationTargetException e){
		}
		throw new IllegalArgumentException();
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
	public void setBeanProperty(Object target, String name, Object value) throws IllegalAccessException, IllegalArgumentException
	{
		BeanAccessor a = getBeanAccessor(target.getClass());
		Method writeMethod = (Method) a.findWriteMethod(name);
		if (writeMethod != null && writeMethod.getParameterTypes().length == 1) {
			Class<?> type = writeMethod.getParameterTypes()[0];
			Object[] arg = null;
			try {
				if (type.isArray() && !type.isInstance(value)) {
					value = transform(type, value);
//				} else if (type.isPrimitive()){
//					value = primitive(type, value, false, 10);
				} else if (List.class.isAssignableFrom(type)){
					value = transform(type, value);
				}
				arg = new Object[] { value };
				writeMethod.invoke(target, arg);
				return;
			} catch (InvocationTargetException ite){
			}
		}
		throw new IllegalArgumentException();
	}

	public Object getIndexedBeanProperty(Object target, String name, int idx)
		throws IllegalAccessException, IllegalArgumentException
	{
		BeanAccessor a = getBeanAccessor(target.getClass());
		Method readMethod = (Method) a.findReadMethod(name);
		try {
			if (readMethod != null){
				Class[] types = readMethod.getParameterTypes();
				if (types.length == 1 && types[0] == int.class){
					return readMethod.invoke(target, new Object[]{new Integer(idx)});
				}
			}
		} catch (InvocationTargetException e){
		}
		throw new IllegalArgumentException();
	}

	public Object setIndexedBeanProperty(Object target, String name, int idx, Object value)
		throws IllegalAccessException, IllegalArgumentException
	{
		BeanAccessor a = getBeanAccessor(target.getClass());
		Method writeMethod = (Method) a.findWriteMethod(name);
		try {
			if (writeMethod != null){
				Class[] types = writeMethod.getParameterTypes();
				if (types.length == 2 && types[0] == int.class){
					return writeMethod.invoke(target,
								  new Object[]{new Integer(idx), value});
				}
			}
		} catch (InvocationTargetException e){
		}
		throw new IllegalArgumentException();
	}

	public boolean isIndexedBeanProperty(Object target, String name, boolean read)
	{
        	BeanAccessor a = getBeanAccessor(target.getClass());
        	Method method = read ? (Method) a.findReadMethod(name) : (Method) a.findWriteMethod(name);
        	Class [] params = null;
        	if(method != null)
        	    params = method.getParameterTypes();
        	    
		int nbParam = read ? 1 : 2;
		return method != null && params != null && params.length == nbParam && params[0] == int.class;
	}


}
