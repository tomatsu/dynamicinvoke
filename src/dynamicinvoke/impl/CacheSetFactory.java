/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

import java.util.*;
import java.lang.reflect.*;

public interface CacheSetFactory {
	Map<MethodCallPattern,ProxyCache> createMethodCallProxyCache();
	Map<MemberAccessPattern,ProxyCache[]> createMethodProxyCache();
	Map<Class<?>,ProxyCache[]> createConstructorProxyCache();
	Map<MemberAccessPattern,FieldAccessor> createFieldProxyCache();
	Map<MemberAccessPattern,Method[]> createMethodCache();
	Map<MemberAccessPattern,Field> createFieldCache();
	Map<Class,BeanAccessor> createBeanAccessors();
	Map<Class,DynamicBeanAccessor> createDynamicBeanAccessors();
}

