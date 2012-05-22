/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke;

import dynamicinvoke.util.*;
import dynamicinvoke.impl.*;
import java.util.*;
import java.security.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

class MethodCallEnv {
	Class[] argBuffer;
	MethodCallPattern methodCallPattern;
	MemberAccessPattern memberAccessPattern;
	ArrayStack<Method> methodBuffer;
	ArrayStack<ProxyCache> methodProxyBuffer;
	CodeLoader codeLoader;

	Map<MethodCallPattern,ProxyCache> methodCallProxyCache;
	Map<MemberAccessPattern,ProxyCache[]> methodProxyCache;
	Map<Class<?>,ProxyCache[]> constructorProxyCache;
	Map<MemberAccessPattern,FieldAccessor> fieldProxyCache;
	Map<MemberAccessPattern,Method[]> methodCache;
	Map<MemberAccessPattern,Field> fieldCache;
	Map<Class,BeanAccessor> beanAccessors;
	Map<Class,DynamicBeanAccessor> beanProxyCache;

	Class<?> lastClass;
	String lastName;
	ProxyCache[] lastValue;

	static CacheSetFactory getDefaultCacheSetFactory(){
		try {
			String prop = AccessController.doPrivileged(new PrivilegedAction<String>(){
					public String run(){
						return System.getProperty("dynamicinvoke.cacheSetFactory");
					}
				});
			if (prop != null){
				Class<?> cls = Class.forName(prop);
				return (CacheSetFactory) cls.newInstance();
			}
			URL resource = MethodCallEnv.class.getResource("/META-INF/dynamicinvoke/cacheSetFactory");
			if (resource != null){
				BufferedReader r = new BufferedReader(new InputStreamReader(resource.openStream(), "UTF-8"));
				String line = r.readLine();
				if (line != null){
					Class<?> cls = Class.forName(line);
					return (CacheSetFactory) cls.newInstance();
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return new DefaultCacheSetFactory();
	}

	MethodCallEnv() {
		this(getDefaultCacheSetFactory());
	}

	MethodCallEnv(CacheSetFactory factory) {
		this.argBuffer = new Class[64];
		this.methodCallPattern = new MethodCallPattern();
		this.memberAccessPattern = new MemberAccessPattern();
		this.methodBuffer = new ArrayStack<Method>();
		this.methodProxyBuffer = new ArrayStack<ProxyCache>();
		this.codeLoader = CodeGenerationRuntime.createCodeLoader();

		this.methodCallProxyCache = factory.createMethodCallProxyCache();
		this.methodProxyCache = factory.createMethodProxyCache();
		this.constructorProxyCache = factory.createConstructorProxyCache();
		this.fieldProxyCache = factory.createFieldProxyCache();
		this.methodCache = factory.createMethodCache();
		this.fieldCache = factory.createFieldCache();
		this.beanAccessors = factory.createBeanAccessors();
		this.beanProxyCache = factory.createDynamicBeanAccessors();
	}
}
