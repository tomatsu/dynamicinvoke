/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

import dynamicinvoke.*;
import dynamicinvoke.util.*;
import java.util.*;
import java.lang.reflect.*;

public class MemoryCacheSetFactory implements CacheSetFactory {
	static Map<MethodCallPattern,ProxyCache> _methodCallProxyCache = new MemoryCache<MethodCallPattern,ProxyCache>();
	static Map<MemberAccessPattern,ProxyCache[]> _methodProxyCache = new MemoryCache<MemberAccessPattern,ProxyCache[]>();
	static Map<Class<?>,ProxyCache[]> _constructorProxyCache = new MemoryCache<Class<?>,ProxyCache[]>();
	static Map<MemberAccessPattern,FieldAccessor> _fieldProxyCache = new MemoryCache<MemberAccessPattern,FieldAccessor>();
	static Map<MemberAccessPattern,Method[]> _methodCache = new MemoryCache<MemberAccessPattern,Method[]>();
	static Map<MemberAccessPattern,Field> _fieldCache = new MemoryCache<MemberAccessPattern,Field>();
	static Map<Class,BeanAccessor> _beanAccessors = new MemoryCache<Class,BeanAccessor>();
	static Map<Class,DynamicBeanAccessor> _dynamicBeanAccessors = new MemoryCache<Class,DynamicBeanAccessor>();

	public MemoryCacheSetFactory(){
	}

	public Map<MethodCallPattern,ProxyCache> createMethodCallProxyCache(){
		return _methodCallProxyCache;
	}
	public Map<MemberAccessPattern,ProxyCache[]> createMethodProxyCache(){
		return _methodProxyCache;
	}
	public Map<Class<?>,ProxyCache[]> createConstructorProxyCache(){
		return _constructorProxyCache;
	}
	public Map<MemberAccessPattern,FieldAccessor> createFieldProxyCache(){
		return _fieldProxyCache;
	}
	public Map<MemberAccessPattern,Method[]> createMethodCache(){
		return _methodCache;
	}
	public Map<MemberAccessPattern,Field> createFieldCache(){
		return _fieldCache;
	}
	public Map<Class,BeanAccessor> createBeanAccessors(){
		return _beanAccessors;
	}
	public Map<Class,DynamicBeanAccessor> createDynamicBeanAccessors(){
		return _dynamicBeanAccessors;
	}
}

