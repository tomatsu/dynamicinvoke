/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
import junit.framework.TestCase;
import java.lang.reflect.*;
import dynamicinvoke.*;

public class BenchmarkTest extends TestCase {
	static Object[] noargs = new Object[]{};

	public static Object staticField;

	static void callCurrentTimeMillis(DynamicRuntime rt, int iteration) {
		System.out.println("Calling System.currentTimeMillis() " + iteration + " times with " + rt);
		long s = System.currentTimeMillis();
		try {
			for (int i = 0; i < iteration; i++){
				rt.callMethod(null, System.class, "currentTimeMillis", noargs, null);
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	static void callCurrentThread(DynamicRuntime rt, int iteration) {
		System.out.println("Calling Thread.currentThread() " + iteration + " times with " + rt);
		long s = System.currentTimeMillis();
		try {
			for (int i = 0; i < iteration; i++){
				rt.callMethod(null, Thread.class, "currentThread", noargs, null);
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	static void callHashCode(DynamicRuntime rt, int iteration)  {
		System.out.println("Calling Object.hashCode() " + iteration + " times with " + rt);
		long s = System.currentTimeMillis();
		try {
			Object target = new Object();
			for (int i = 0; i < iteration; i++){
				rt.callMethod(target, target.getClass(), "hashCode", noargs, null);
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	static void callValueOf(DynamicRuntime rt, int iteration)  {
		System.out.println("Calling String.valueOf() " + iteration + " times with " + rt);
		long s = System.currentTimeMillis();
		try {
			Object target = new Object();
//			Object[] arg = new Object[]{new Integer(0)};
			Object[] arg = new Object[]{"hello"};
			for (int i = 0; i < iteration; i++){
				rt.callMethod(null, String.class, "valueOf", arg, null);
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	static void callNew(DynamicRuntime rt, int iteration)  {
		System.out.println("Calling new Object() " + iteration + " times with " + rt);
		long s = System.currentTimeMillis();
		try {
			for (int i = 0; i < iteration; i++){
				rt.callConstructor(Object.class, noargs, null);
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	static void callNewString(DynamicRuntime rt, int iteration)  {
		System.out.println("Calling new String() " + iteration + " times with " + rt);
		long s = System.currentTimeMillis();
		try {
			Object[] args = new Object[]{"hello"};
			for (int i = 0; i < iteration; i++){
				rt.callConstructor(String.class, args, null);
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	public void testGetStaticField(DynamicRuntime rt, int iteration){
		System.out.println("Getting static field System.out " + iteration + " times with " + rt);
		Class cls = System.class;
		long s = System.currentTimeMillis();
		try {
			for (int i = 0; i < iteration; i++){
				rt.getObjectField(null, cls, "out");
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	public void testSetStaticField(DynamicRuntime rt, int iteration){
		System.out.println("Setting static field System.out " + iteration + " times with " + rt);
		Object obj = new Object();
		Class cls = getClass();
		long s = System.currentTimeMillis();
		try {
			for (int i = 0; i < iteration; i++){
				rt.setObjectField(null, cls, "staticField", obj);
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	public void testGetObjectField(DynamicRuntime rt, int iteration){
		System.out.println("Getting Object field Point.x " + iteration + " times...");
		java.awt.Point pt = new java.awt.Point(1,1);
		Class cls = pt.getClass();
		long s = System.currentTimeMillis();
		try {
			for (int i = 0; i < iteration; i++){
				rt.getObjectField(pt, cls, "x");
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

 	public void testSetObjectField(DynamicRuntime rt, int iteration){
		System.out.println("Setting Object field Point.x " + iteration + " times with " + rt);
		java.awt.Point pt = new java.awt.Point(1,1);
		Class cls = pt.getClass();
		Object value = new Integer(1);
		long s = System.currentTimeMillis();
		try {
			for (int i = 0; i < iteration; i++){
				rt.setObjectField(pt, cls, "x", value);
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}



	public void testGetBeanProperty(DynamicRuntime rt, int iteration){
		System.out.println("Getting Bean Property Button.name " + iteration + " times with " + rt);
		Object target = new java.awt.Button();
		String name = "name";
		long s = System.currentTimeMillis();
		try {
			for (int i = 0; i < iteration; i++){
				Object property = rt.getBeanProperty(target, name);
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	public void testSetBeanProperty(DynamicRuntime rt, int iteration){
		System.out.println("Setting Bean Property Button.name " + iteration + " times with " + rt);
		Object target = new java.awt.Button();
		String name = "name";
		long s = System.currentTimeMillis();
		try {
			for (int i = 0; i < iteration; i++){
				rt.setBeanProperty(target, name, "hello");
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		System.out.println(System.currentTimeMillis() - s);
	}

	public void testKnownMethods(){
		testKnownMethods(new CodeGenerationRuntime());
		testKnownMethods(new ReflectionRuntime());
	}

	void testKnownMethods(DynamicRuntime rt){
		int n = 100000000;
//		callCurrentTimeMillis(rt, n);
//		callCurrentThread(rt, n);
//		callHashCode(rt, n);
		callValueOf(rt, n);
//		callNew(rt, n);
//		callNewString(rt, n);
//		testGetStaticField(rt, n);
//		testSetStaticField(rt, n);
//		testGetObjectField(rt, n);
//		testSetObjectField(rt, n);
//		testGetBeanProperty(rt, n);
//		testSetBeanProperty(rt, n);

		System.out.println("ok");
	}

	public static void main(String args[]) throws Throwable {
		junit.textui.TestRunner.main(new String[]{"BenchmarkTest"});
	}
}