/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke;

/*
 * ObjectDescFactory.getDefault().create(className)
 */
public abstract class ObjectDescFactory {

	public final static String DEFAULT_OBJECT_DESC_FACTORY_NAME = "dynamicinvoke.beans.BeanObjectDescFactory";
	public final static String PROPERTY_OBJECT_DESC_FACTORY = "dynamicinvoke.defaultObjectDescFactoryName";

	static ObjectDescFactory defaultFactory = getDefaultObjectDescFactory();

	static ObjectDescFactory instantiateObjectDescFactory(String className) {
		try {
			Class<?> cls = Class.forName(className);
			return (ObjectDescFactory) cls.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static ObjectDescFactory getDefaultObjectDescFactory() {
		String prop = System.getProperty(PROPERTY_OBJECT_DESC_FACTORY);
		if (prop != null) {
			return instantiateObjectDescFactory(prop);
		}
		return instantiateObjectDescFactory(DEFAULT_OBJECT_DESC_FACTORY_NAME);
	}

	public static ObjectDescFactory getDefault() {
		return defaultFactory;
	}

	public abstract ObjectDesc create(Class<?> cls);

	public ObjectDesc create(Class<?> cls, Class<?> stopClass) {
		return create(cls);
	}
}
