/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke;

import java.util.*;
import java.lang.reflect.*;

/**
 * Subclasses of this abstract class provides API for method call, field access, and bean property access.
 */
public abstract class DynamicRuntime {
	private final static boolean DEBUG = false;

	private static final ThreadLocal<MethodCallEnv> localMethodCallEnv = 
		new ThreadLocal<MethodCallEnv>() {
			protected MethodCallEnv initialValue() {
				return new MethodCallEnv();
			}
		};

	static MethodCallEnv getMethodCallEnv(){
		return (MethodCallEnv)localMethodCallEnv.get();
	}

	static void setMethodCallEnv(MethodCallEnv env){
		localMethodCallEnv.set(env);
	}

	static boolean isArray(Object obj) {
		return (obj instanceof Object[] || obj instanceof int[]
			|| obj instanceof char[] || obj instanceof boolean[]
			|| obj instanceof byte[] || obj instanceof double[]
			|| obj instanceof long[] || obj instanceof short[] || obj instanceof float[]);
	}

	public static DynamicRuntime getDefault(){
		return new CodeGenerationRuntime();
	}

	public static Method findCallableMethod(Class<?> clazz, String name,
						Class args[]) {
		if (DEBUG){
			System.err.println("findCallableMethod " + clazz + ", name = " + name);
		}
		while (clazz != null) {
			Method method;
			if (Modifier.isPublic(clazz.getModifiers())) {
				try {
					method = clazz.getMethod(name, args);
					if (method != null
					    && Modifier.isPublic(method.getDeclaringClass()
								 .getModifiers())) {
						return method;
					}
				} catch (NoSuchMethodException nme) {
				}
			}
			Class it[] = clazz.getInterfaces();
			for (int i = 0; i < it.length; i++) {
				Method m = findCallableMethod(it[i], name, args);
				if (m != null) {
					return m;
				}
			}
			clazz = clazz.getSuperclass();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static Object transform(Class<?> type, Object obj) {
		if (DEBUG){
			System.err.println("transform: type = " + type + ", obj = " + ((obj != null) ? obj.getClass().getName() : "null"));
		}
	    
		if (type.isArray()) {
			boolean isList = (obj instanceof List);
			boolean isArray = isArray(obj);
			if (obj != null && !(isList || isArray)) {
				return obj;
			}
			Class<?> componentType = type.getComponentType();
			int len;
			if (isArray) {
				len = getArrayLength(obj);
			} else if (isList) {
				len = ((List) obj).size();
			} else {
				return obj;
			}
			Object result = Array.newInstance(componentType, len);
			for (int i = 0; i < len; i++) {
				Object elem = null;
				if (isArray) {
					elem = Array.get(obj, i);
				} else if (isList) {
					elem = ((List) obj).get(i);
				}
				Array.set(result, i, transform(componentType, elem));
			}
			return result;
		} else {
			if (type == long.class) {
				if (obj instanceof Character) throw new IllegalArgumentException();
			} else if (List.class.isAssignableFrom(type)) {
				if (isArray(obj)) {
					int len = getArrayLength(obj);
					try {
						List<Object> list;
						if (type.isInterface()) {
							list = new ArrayList<Object>();
							if (!type.isInstance(list)) {
								throw new IllegalArgumentException();
							}
						} else {
							list = (List<Object>) type.newInstance();
						}
						for (int i = 0; i < len; i++) {
							list.add(Array.get(obj, i));
						}
						return list;
					} catch (Exception e) {
						throw new IllegalArgumentException(e);
					}
				}
			}
			return obj;
		}
	}

	/*
	 *
	 */
	protected int matchType(Class<?> type, Object obj, Class<?> clazz) {
		if (DEBUG) {
			System.err.println("matchType " + type + ", " + obj + ", " + clazz);
		}
		if (clazz == type) {
			return 0;
		}
		if (type == boolean.class) {
			return (clazz == Boolean.class) ? 0 : -1;
		}

		if (type == byte.class) {
			return distance(0, clazz);
		}
		if (type == char.class) {
			return distance(6, clazz);
		}
		if (type == short.class) {
			return distance(1, clazz);
		}
		if (type == int.class) {
			return distance(2, clazz);
		}
		if (type == long.class) {
			return distance(3, clazz);
		}
		if (type == float.class) {
			return distance(4, clazz);
		}
		if (type == double.class) {
			return distance(5, clazz);
		}
		if (obj == null) {
			return 1;
		}
		if (type.isAssignableFrom(clazz)) {
			return 1;
		} else if (clazz.isArray()) {
			if (type.isArray()) {
				int j = 1;
				for (int i = 0; i < getArrayLength(obj); i++) {
					Object elem = Array.get(obj, i);
					int s = matchType(type.getComponentType(), elem,
							  elem != null ? elem.getClass() : null);
					if (s < 0) {
						return -1;
					} else {
						j += s;
					}
				}
				return j;
			} else if (List.class.isAssignableFrom(type)) {
				return 1;
			}
		} else if (type.isArray()) {
			if (obj instanceof List) {
				int j = 1;
				List list = (List) obj;
				for (int i = 0; i < list.size(); i++) {
					int s = matchType(type.getComponentType(), list.get(i),
							  list.get(i).getClass());
					if (s < 0) {
						return -1;
					} else {
						j += s;
					}
				}
				return j;
			}
		}
		return -1;
	}

	private final static int[][] distance_table = {
		{ 0, 1, 2, 3, 4, 5, -1 },
		{ -1, 0, 1, 2, 3, 4, -1 },
		{ -1, -1, 0, 1, 2, 3, -1 },
		{ -1, -1, -1, 0, 1, 2, -1 },
		{ -1, -1, -1, -1, 0, 1, -1 },
		{ -1, -1, -1, -1, -1, 0, -1 },
		{ -1, -1, 1, -1, 5, 5, 0 }, 
	};

	private static int distance(int pos, Class<?> clazz) {
		if (clazz == Double.class) {
			return distance_table[5][pos];
		} else if (clazz == Float.class) {
			return distance_table[4][pos];
		} else if (clazz == Long.class) {
			return distance_table[3][pos];
		} else if (clazz == Integer.class) {
			return distance_table[2][pos];
		} else if (clazz == Short.class) {
			return distance_table[1][pos];
		} else if (clazz == Character.class) {
			return distance_table[6][pos];
		} else if (clazz == Byte.class) {
			return distance_table[0][pos];
		} else {
			return -1;
		}
	}

	final static int getArrayLength(Object array) {
		if (array instanceof Object[]) {
			return ((Object[]) array).length;
		} else if (array instanceof int[]) {
			return ((int[]) array).length;
		} else if (array instanceof byte[]) {
			return ((byte[]) array).length;
		} else if (array instanceof char[]) {
			return ((char[]) array).length;
		} else if (array instanceof float[]) {
			return ((float[]) array).length;
		} else if (array instanceof double[]) {
			return ((double[]) array).length;
		} else if (array instanceof boolean[]) {
			return ((boolean[]) array).length;
		} else if (array instanceof long[]) {
			return ((long[]) array).length;
		} else if (array instanceof short[]) {
			return ((short[]) array).length;
		} else {
			throw new IllegalArgumentException(String.valueOf(array));
		}
	}


	public abstract Object callMethod(Object target, Class<?> c, String name,
					  Object args[], Class types[]) throws Exception;

	public abstract Object callConstructor(Class<?> c, Object args[],
					       Class types[]) throws Exception;

	public abstract Object getObjectField(Object target, Class<?> c, String name) 
		throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException;

	public abstract void setObjectField(Object target, Class<?> c, String name, Object value)
		throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException;

	public abstract Object getBeanProperty(Object target, String name)
		throws IllegalAccessException, IllegalArgumentException; 

	public abstract void setBeanProperty(Object target, String name, Object value)
		throws IllegalAccessException, IllegalArgumentException;

	public abstract Object getIndexedBeanProperty(Object target, String name, int idx)
		throws IllegalAccessException, IllegalArgumentException;

	public abstract Object setIndexedBeanProperty(Object target, String name, int idx, Object value)
		throws IllegalAccessException, IllegalArgumentException;

	public abstract boolean isIndexedBeanProperty(Object target, String name, boolean read);
}