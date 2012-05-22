/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

import dynamicinvoke.*;
import dynamicinvoke.util.*;
import java.util.*;
import java.lang.reflect.*;

public class DefaultCacheSetFactory implements CacheSetFactory {
	public Map<MethodCallPattern,ProxyCache> createMethodCallProxyCache(){
		return new LRUCache<MethodCallPattern,ProxyCache>();
	}
	public Map<MemberAccessPattern,ProxyCache[]> createMethodProxyCache(){
		return new LRUCache<MemberAccessPattern,ProxyCache[]>();
	}
	public Map<Class<?>,ProxyCache[]> createConstructorProxyCache(){
		return new LRUCache<Class<?>,ProxyCache[]>();
	}
	public Map<MemberAccessPattern,FieldAccessor> createFieldProxyCache(){
		return new LRUCache<MemberAccessPattern,FieldAccessor>();
	}
	public Map<MemberAccessPattern,Method[]> createMethodCache(){
		return new LRUCache<MemberAccessPattern,Method[]>();
	}
	public Map<MemberAccessPattern,Field> createFieldCache(){
		return new LRUCache<MemberAccessPattern,Field>();
	}
	public Map<Class,BeanAccessor> createBeanAccessors(){
		return new LRUCache<Class,BeanAccessor>();
	}
	public Map<Class,DynamicBeanAccessor> createDynamicBeanAccessors(){
		return new LRUCache<Class,DynamicBeanAccessor>();
	}
}

