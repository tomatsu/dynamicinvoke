/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
import junit.framework.TestCase;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import dynamicinvoke.*;

public class MethodSelectionTest extends TestCase {
	static DynamicRuntime  rt = new CodeGenerationRuntime();
	static DynamicRuntime rt2 = new ReflectionRuntime();

	public static class A {
		public String sig;
		public A(){}
		public A(int i){ this.sig = "<init>(I)";	}
		public A(long i){ this.sig = "<init>(J)";	}
		public A(short i){ this.sig = "<init>(S)";	}
		public A(byte i){ this.sig = "<init>(B)";	}
		public A(char i){ this.sig = "<init>(C)";	}
		public A(float i){ this.sig = "<init>(F)";	}
		public A(double i){ this.sig = "<init>(D)";	}
		public A(boolean i){ this.sig = "<init>(Z)";	}
		public A(A a){ this.sig = "<init>(LA;)";	}

		public String test1(int i){	    return "A.test1(I)";	}
		public String test1(long i){	    return "A.test1(J)";	}
		public String test1(short i){	    return "A.test1(S)";	}
		public String test1(byte i){	    return "A.test1(B)";	}
		public String test1(char i){	    return "A.test1(C)";	}
		public String test1(float i){	    return "A.test1(F)";	}
		public String test1(double i){	    return "A.test1(D)";	}
		public String test1(boolean i){	    return "A.test1(Z)";	}
		public String test2(int i){	    return "A.test2(I)";	}
		public String test3(long i){	    return "A.test3(J)";	}
		public String test4(short i){	    return "A.test4(S)";	}
		public String test5(byte i){	    return "A.test5(B)";	}
		public String test6(char i){	    return "A.test6(C)";	}
		public String test7(float i){	    return "A.test7(F)";	}
		public String test8(double i){	    return "A.test8(D)";	}
		public String test9(boolean i){	    return "A.test9(Z)";	}
		public String test10(A a){	    return "A.test10(LA;)";	}

		public String test11(int[] i){	    return "A.test11([I)";	}
		public String test12(long[] i){     return "A.test12([J)";	}
		public String test13(short[] i){    return "A.test13([S)";	}
		public String test14(byte[] i){     return "A.test14([B)";	}
		public String test15(char[] i){     return "A.test15([C)";	}
		public String test16(float[] i){    return "A.test16([F)";	}
		public String test17(double[] i){   return "A.test17([D)";	}
		public String test18(boolean[] i){  return "A.test18([Z)";	}
		public String test19(Object[] i){   return "A.test19([Ljava/lang/Object;)";	}

		public String test20(Object arg){   return "A.test20(Ljava/lang/Object;)"; }
		public String test20(HashMap arg){  return "A.test20(Ljava/util/HashMap;)"; }
		public String test20(Map arg){      return "A.test20(Ljava/util/Map;)"; }
		public String test20(AbstractMap arg){   return "A.test20(Ljava/util/AbstractMap;)"; }
		public String test20(Cloneable arg){     return "A.test20(Ljava/lang/Cloneable;)"; }
		public String test20(Serializable arg){  return "A.test20(Ljava/io/Serializable;)"; }
	}

	public static class B extends A {
		public B(){}
		public B(Object arg){   this.sig = "<init>(Ljava/lang/Object;)"; }
		public B(HashMap arg){  this.sig = "<init>(Ljava/util/HashMap;)"; }
		public B(Map arg){      this.sig = "<init>(Ljava/util/Map;)"; }
		public B(AbstractMap arg){   this.sig = "<init>(Ljava/util/AbstractMap;)"; }
		public B(Cloneable arg){     this.sig = "<init>(Ljava/lang/Cloneable;)"; }
		public B(Serializable arg){  this.sig = "<init>(Ljava/io/Serializable;)"; }
	}

	void verifyMethodSelection(Object target, String methodName, Object arg, String result) {
		verifyMethodSelection(target, methodName, arg, null, result);
	}

	void verifyMethodSelection(Object target, String methodName, Object arg, Class cast, String result) {
		verifyMethodSelection(false, target, methodName, new Object[]{arg},
				      cast != null ? new Class[]{cast} : null,
				      result);
		verifyMethodSelection(true, target, methodName, new Object[]{arg},
				      cast != null ? new Class[]{cast} : null,
				      result);
	}

	void verifyMethodSelection(boolean reflection, Object target, String methodName, Object[] args, Class[] cast, String result) {
		Object v;
		if (result == null){
			try {
				if (reflection){
					v = rt2.callMethod(target, target.getClass(), methodName, args, cast);
				} else {
					v = rt.callMethod(target, target.getClass(), methodName, args, cast);
				}
				fail(String.valueOf(v));
			} catch (IllegalArgumentException e){
			} catch (Throwable t){
				t.printStackTrace();
				fail();
			}

		} else {
			try {
				if (reflection){
					v = rt2.callMethod(target, target.getClass(), methodName, args, cast);
				} else {
					v = rt.callMethod(target, target.getClass(), methodName, args, cast);
				}
				if (!(v instanceof String))	fail();
				assertEquals((String)v, result);
			} catch (Exception e){
				e.printStackTrace();
				fail();
			}
		}
	}

	void verifyConstructorSelection(Class cls, Object arg, String result) {
		verifyConstructorSelection(cls, arg, null, result);
	}

	void verifyConstructorSelection(Class cls, Object arg, Class cast, String result) {
		verifyConstructorSelection(false, cls, new Object[]{arg}, cast != null ? new Class[]{cast} : null,
					   result);
		verifyConstructorSelection(true, cls, new Object[]{arg}, cast != null ? new Class[]{cast} : null,
					   result);
	}

	void verifyConstructorSelection(boolean reflection, Class cls, Object[] args, Class[] types, String result) {
		if (result == null){
			try {
				Object a;
				if (reflection){
					a = rt2.callConstructor(cls, args, types);
				} else {
					a = rt.callConstructor(cls, args, types);
				}
				fail();
			} catch (IllegalArgumentException e){
			} catch (Throwable t){
				t.printStackTrace();
				fail();
			}
		} else {
			try {
				Object a;
				if (reflection){
					a = rt2.callConstructor(cls, args, types);
				} else {
					a = rt.callConstructor(cls, args, types);
				}
				assertTrue(a != null);
				Field f = a.getClass().getField("sig");
				String sig = (String)f.get(a);
				assertEquals(sig, result);
			} catch (Exception e){
				e.printStackTrace();
				fail();
			}
		}
	}

	public void testPrimitive_1(){
		testPrimitive_1(new A());
		testPrimitive_1(createObjectWithDifferentClassLoader(A.class));
	}

	void testPrimitive_1(Object target){
		verifyMethodSelection(target, "test1", new Integer(0), "A.test1(I)");
		verifyMethodSelection(target, "test1", new Long(0), "A.test1(J)");
		verifyMethodSelection(target, "test1", new Short((short)0), "A.test1(S)");
		verifyMethodSelection(target, "test1", new Byte((byte)0), "A.test1(B)");
		verifyMethodSelection(target, "test1", new Character((char)0), "A.test1(C)");
		verifyMethodSelection(target, "test1", new Float((float)0), "A.test1(F)");
		verifyMethodSelection(target, "test1", new Double((double)0), "A.test1(D)");
		verifyMethodSelection(target, "test1", new Boolean(false), "A.test1(Z)");
		verifyMethodSelection(target, "test1", new Object(), null);
		verifyMethodSelection(target, "test1", null, null);
	}

	public void testPrimitive_1_2(){
		testPrimitive_1(new B());
		testPrimitive_1(createObjectWithDifferentClassLoader(B.class));
	}

	public void testPrimitive_2(){
		testPrimitive_2(new A());
		testPrimitive_2(createObjectWithDifferentClassLoader(A.class));
	}

	void testPrimitive_2(Object target){
		verifyMethodSelection(target, "test2", new Integer(0), "A.test2(I)");
		verifyMethodSelection(target, "test2", new Long(0), null);
		verifyMethodSelection(target, "test2", new Short((short)0), "A.test2(I)");
		verifyMethodSelection(target, "test2", new Byte((byte)0), "A.test2(I)");
		verifyMethodSelection(target, "test2", new Character((char)0), "A.test2(I)");
		verifyMethodSelection(target, "test2", new Float((float)0), null);
		verifyMethodSelection(target, "test2", new Double((double)0), null);
		verifyMethodSelection(target, "test2", new Boolean(false), null);
		verifyMethodSelection(target, "test2", new Object(), null);
		verifyMethodSelection(target, "test2", null, null);
	}

	public void testPrimitive_3(){
		testPrimitive_3(new A());
		testPrimitive_3(createObjectWithDifferentClassLoader(A.class));
	}

	void testPrimitive_3(Object target){
		verifyMethodSelection(target, "test3", new Integer(0), "A.test3(J)");
		verifyMethodSelection(target, "test3", new Long(0), "A.test3(J)");
		verifyMethodSelection(target, "test3", new Short((short)0), "A.test3(J)");
		verifyMethodSelection(target, "test3", new Byte((byte)0), "A.test3(J)");
		verifyMethodSelection(target, "test3", new Character((char)0), null);
		verifyMethodSelection(target, "test3", new Float((float)0), null);
		verifyMethodSelection(target, "test3", new Double((double)0), null);
		verifyMethodSelection(target, "test3", new Boolean(false), null);
		verifyMethodSelection(target, "test3", new Object(), null);
		verifyMethodSelection(target, "test3", null, null);
	}


	public void testPrimitive_4(){
		testPrimitive_4(new A());
		testPrimitive_4(createObjectWithDifferentClassLoader(A.class));
	}

	void testPrimitive_4(Object target){
		verifyMethodSelection(target, "test4", new Integer(0), null);
		verifyMethodSelection(target, "test4", new Long(0), null);
		verifyMethodSelection(target, "test4", new Short((short)0), "A.test4(S)");
		verifyMethodSelection(target, "test4", new Byte((byte)0), "A.test4(S)");
		verifyMethodSelection(target, "test4", new Character((char)0), null);
		verifyMethodSelection(target, "test4", new Float((float)0), null);
		verifyMethodSelection(target, "test4", new Double((double)0), null);
		verifyMethodSelection(target, "test4", new Boolean(false), null);
		verifyMethodSelection(target, "test4", null, null);
	}


	public void testPrimitive_5(){
		testPrimitive_5(new A());
		testPrimitive_5(createObjectWithDifferentClassLoader(A.class));
	}

	void testPrimitive_5(Object target){
		verifyMethodSelection(target, "test5", new Integer(0), null);
		verifyMethodSelection(target, "test5", new Long(0), null);
		verifyMethodSelection(target, "test5", new Short((short)0), null);
		verifyMethodSelection(target, "test5", new Byte((byte)0), "A.test5(B)");
		verifyMethodSelection(target, "test5", new Character((char)0), null);
		verifyMethodSelection(target, "test5", new Float((float)0), null);
		verifyMethodSelection(target, "test5", new Double((double)0), null);
		verifyMethodSelection(target, "test5", new Boolean(false), null);
		verifyMethodSelection(target, "test5", new Object(), null);
		verifyMethodSelection(target, "test5", null, null);
	}

	public void testPrimitive_6(){
		testPrimitive_6(new A());
		testPrimitive_6(createObjectWithDifferentClassLoader(A.class));
	}

	void testPrimitive_6(Object target){
		verifyMethodSelection(target, "test6", new Integer(0), null);
		verifyMethodSelection(target, "test6", new Long(0), null);
		verifyMethodSelection(target, "test6", new Short((short)0), null);
		verifyMethodSelection(target, "test6", new Byte((byte)0), null);
		verifyMethodSelection(target, "test6", new Character((char)0), "A.test6(C)");
		verifyMethodSelection(target, "test6", new Float((float)0), null);
		verifyMethodSelection(target, "test6", new Double((double)0), null);
		verifyMethodSelection(target, "test6", new Boolean(false), null);
		verifyMethodSelection(target, "test6", new Object(), null);
		verifyMethodSelection(target, "test6", null, null);
	}

	public void testPrimitive_7(){
		testPrimitive_7(new A());
		testPrimitive_7(createObjectWithDifferentClassLoader(A.class));
	}

	void testPrimitive_7(Object target){
		verifyMethodSelection(target, "test7", new Integer(0), "A.test7(F)");
		verifyMethodSelection(target, "test7", new Long(0), "A.test7(F)");
		verifyMethodSelection(target, "test7", new Short((short)0), "A.test7(F)");
		verifyMethodSelection(target, "test7", new Byte((byte)0), "A.test7(F)");
		verifyMethodSelection(target, "test7", new Character((char)0), "A.test7(F)");
		verifyMethodSelection(target, "test7", new Float((float)0), "A.test7(F)");
		verifyMethodSelection(target, "test7", new Double((double)0), null);
		verifyMethodSelection(target, "test7", new Boolean(false), null);
		verifyMethodSelection(target, "test7", new Object(), null);
		verifyMethodSelection(target, "test7", null, null);
	}

	public void testPrimitive_8(){
		testPrimitive_8(new A());
		testPrimitive_8(createObjectWithDifferentClassLoader(A.class));
	}

	void testPrimitive_8(Object target){
		verifyMethodSelection(target, "test8", new Integer(0), "A.test8(D)");
		verifyMethodSelection(target, "test8", new Long(0), "A.test8(D)");
		verifyMethodSelection(target, "test8", new Short((short)0), "A.test8(D)");
		verifyMethodSelection(target, "test8", new Byte((byte)0), "A.test8(D)");
		verifyMethodSelection(target, "test8", new Character((char)0), "A.test8(D)");
		verifyMethodSelection(target, "test8", new Float((float)0), "A.test8(D)");
		verifyMethodSelection(target, "test8", new Double((double)0), "A.test8(D)");
		verifyMethodSelection(target, "test8", new Boolean(false), null);
		verifyMethodSelection(target, "test8", new Object(), null);
		verifyMethodSelection(target, "test8", null, null);
	}

	public void testPrimitive_9(){
		testPrimitive_9(new A());
		testPrimitive_9(createObjectWithDifferentClassLoader(A.class));
	}

	void testPrimitive_9(Object target){
		verifyMethodSelection(target, "test9", new Integer(0), null);
		verifyMethodSelection(target, "test9", new Long(0), null);
		verifyMethodSelection(target, "test9", new Short((short)0), null);
		verifyMethodSelection(target, "test9", new Byte((byte)0), null);
		verifyMethodSelection(target, "test9", new Character((char)0), null);
		verifyMethodSelection(target, "test9", new Float((float)0), null);
		verifyMethodSelection(target, "test9", new Double((double)0), null);
		verifyMethodSelection(target, "test9", new Boolean(false), "A.test9(Z)");
		verifyMethodSelection(target, "test9", new Object(), null);
		verifyMethodSelection(target, "test9", null, null);
	}

	public void testMethodArgOfSameType(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		verifyMethodSelection(a, "test10", a, "A.test10(LA;)");
		verifyMethodSelection(a, "test10", b, null);
		verifyMethodSelection(b, "test10", b, "A.test10(LA;)");
		verifyMethodSelection(b, "test10", a, null);
	}

	public void testConstructor_1(){
		testConstructor_1(A.class);
		testConstructor_1(getClassWithDifferentClassLoader(A.class));
	}

	void testConstructor_1(Class cls){
		verifyConstructorSelection(cls, new Integer(0), "<init>(I)");
		verifyConstructorSelection(cls, new Long(0), "<init>(J)");
		verifyConstructorSelection(cls, new Short((short)0), "<init>(S)");
		verifyConstructorSelection(cls, new Byte((byte)0), "<init>(B)");
		verifyConstructorSelection(cls, new Character((char)0), "<init>(C)");
		verifyConstructorSelection(cls, new Float((float)0), "<init>(F)");
		verifyConstructorSelection(cls, new Double((double)0), "<init>(D)");
		verifyConstructorSelection(cls, new Boolean(false), "<init>(Z)");
		verifyConstructorSelection(cls, new Object(), null);
		verifyConstructorSelection(cls, null, "<init>(LA;)");
	}

	public void testConstructorArgOfSameType(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		verifyConstructorSelection(a.getClass(), a, "<init>(LA;)");
		verifyConstructorSelection(b.getClass(), b, "<init>(LA;)");
		verifyConstructorSelection(a.getClass(), b, null);
		verifyConstructorSelection(b.getClass(), a, null);
	}

	public void testArray_11(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		testArray_11(a);
		testArray_11(b);
	}

	void testArray_11(Object target){
		verifyMethodSelection(target, "test11", new int[]{0}, "A.test11([I)");
		verifyMethodSelection(target, "test11", new long[]{0}, null);
		verifyMethodSelection(target, "test11", new short[]{(short)0}, "A.test11([I)");
		verifyMethodSelection(target, "test11", new byte[]{(byte)0}, "A.test11([I)");
		verifyMethodSelection(target, "test11", new char[]{(char)0}, "A.test11([I)");
		verifyMethodSelection(target, "test11", new float[]{(float)0}, null);
		verifyMethodSelection(target, "test11", new double[]{(double)0}, null);
		verifyMethodSelection(target, "test11", new boolean[]{false}, null);

		verifyMethodSelection(target, "test11", new Object[]{new Integer(1)}, "A.test11([I)");
		verifyMethodSelection(target, "test11", new Object[]{new Long(1)}, null);
		verifyMethodSelection(target, "test11", new Object[]{new Short((short)1)}, "A.test11([I)");
		verifyMethodSelection(target, "test11", new Object[]{new Byte((byte)1)}, "A.test11([I)");
		verifyMethodSelection(target, "test11", new Object[]{new Character((char)1)}, "A.test11([I)");
		verifyMethodSelection(target, "test11", new Object[]{new Float((float)1)}, null);
		verifyMethodSelection(target, "test11", new Object[]{new Double((double)1)}, null);

		verifyMethodSelection(target, "test11", new Object[]{new Boolean(false)}, null);
		verifyMethodSelection(target, "test11", new Object[]{new Object()}, null);
		verifyMethodSelection(target, "test11", new Object[]{null}, null);
	}

	public void testArray_12(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		testArray_12(a);
		testArray_12(b);
	}

	void testArray_12(Object target){
		verifyMethodSelection(target, "test12", new int[]{0}, "A.test12([J)");
		verifyMethodSelection(target, "test12", new long[]{0}, "A.test12([J)");
		verifyMethodSelection(target, "test12", new short[]{(short)0}, "A.test12([J)");
		verifyMethodSelection(target, "test12", new byte[]{(byte)0}, "A.test12([J)");
		verifyMethodSelection(target, "test12", new char[]{(char)0}, null);
		verifyMethodSelection(target, "test12", new float[]{(float)0}, null);
		verifyMethodSelection(target, "test12", new double[]{(double)0}, null);
		verifyMethodSelection(target, "test12", new boolean[]{false}, null);

		verifyMethodSelection(target, "test12", new Object[]{new Integer(1)}, "A.test12([J)");
		verifyMethodSelection(target, "test12", new Object[]{new Long(1)}, "A.test12([J)");
		verifyMethodSelection(target, "test12", new Object[]{new Short((short)1)}, "A.test12([J)");
		verifyMethodSelection(target, "test12", new Object[]{new Byte((byte)1)}, "A.test12([J)");
		verifyMethodSelection(target, "test12", new Object[]{new Character((char)1)}, null);
		verifyMethodSelection(target, "test12", new Object[]{new Float((float)1)}, null);
		verifyMethodSelection(target, "test12", new Object[]{new Double((double)1)}, null);

		verifyMethodSelection(target, "test12", new Object[]{new Boolean(false)}, null);
		verifyMethodSelection(target, "test12", new Object[]{new Object()}, null);
		verifyMethodSelection(target, "test12", new Object[]{null}, null);
	}

	public void testArray_13(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		testArray_13(a);
		testArray_13(b);
	}

	void testArray_13(Object target){
		verifyMethodSelection(target, "test13", new int[]{0}, null);
		verifyMethodSelection(target, "test13", new long[]{0}, null);
		verifyMethodSelection(target, "test13", new short[]{(short)0}, "A.test13([S)");
		verifyMethodSelection(target, "test13", new byte[]{(byte)0}, "A.test13([S)");
		verifyMethodSelection(target, "test13", new char[]{(char)0}, null);
		verifyMethodSelection(target, "test13", new float[]{(float)0}, null);
		verifyMethodSelection(target, "test13", new double[]{(double)0}, null);
		verifyMethodSelection(target, "test13", new boolean[]{false}, null);

		verifyMethodSelection(target, "test13", new Object[]{new Integer(1)}, null);
		verifyMethodSelection(target, "test13", new Object[]{new Long(1)}, null);
		verifyMethodSelection(target, "test13", new Object[]{new Short((short)1)}, "A.test13([S)");
		verifyMethodSelection(target, "test13", new Object[]{new Byte((byte)1)}, "A.test13([S)");
		verifyMethodSelection(target, "test13", new Object[]{new Character((char)1)}, null);
		verifyMethodSelection(target, "test13", new Object[]{new Float((float)1)}, null);
		verifyMethodSelection(target, "test13", new Object[]{new Double((double)1)}, null);

		verifyMethodSelection(target, "test13", new Object[]{new Boolean(false)}, null);
		verifyMethodSelection(target, "test13", new Object[]{new Object()}, null);
		verifyMethodSelection(target, "test13", new Object[]{null}, null);
	}

	public void testArray_14(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		testArray_14(a);
		testArray_14(b);
	}

	void testArray_14(Object target){
		verifyMethodSelection(target, "test14", new int[]{0}, null);
		verifyMethodSelection(target, "test14", new long[]{0}, null);
		verifyMethodSelection(target, "test14", new short[]{(short)0}, null);
		verifyMethodSelection(target, "test14", new byte[]{(byte)0}, "A.test14([B)");
		verifyMethodSelection(target, "test14", new char[]{(char)0}, null);
		verifyMethodSelection(target, "test14", new float[]{(float)0}, null);
		verifyMethodSelection(target, "test14", new double[]{(double)0}, null);
		verifyMethodSelection(target, "test14", new boolean[]{false}, null);

		verifyMethodSelection(target, "test14", new Object[]{new Integer(1)}, null);
		verifyMethodSelection(target, "test14", new Object[]{new Long(1)}, null);
		verifyMethodSelection(target, "test14", new Object[]{new Short((short)1)}, null);
		verifyMethodSelection(target, "test14", new Object[]{new Byte((byte)1)}, "A.test14([B)");
		verifyMethodSelection(target, "test14", new Object[]{new Character((char)1)}, null);
		verifyMethodSelection(target, "test14", new Object[]{new Float((float)1)}, null);
		verifyMethodSelection(target, "test14", new Object[]{new Double((double)1)}, null);

		verifyMethodSelection(target, "test14", new Object[]{new Boolean(false)}, null);
		verifyMethodSelection(target, "test14", new Object[]{new Object()}, null);
		verifyMethodSelection(target, "test14", new Object[]{null}, null);
	}

	public void testArray_15(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		testArray_15(a);
		testArray_15(b);
	}

	void testArray_15(Object target){
		verifyMethodSelection(target, "test15", new int[]{0}, null);
		verifyMethodSelection(target, "test15", new long[]{0}, null);
		verifyMethodSelection(target, "test15", new short[]{(short)0}, null);
		verifyMethodSelection(target, "test15", new byte[]{(byte)0}, null);
		verifyMethodSelection(target, "test15", new char[]{(char)0}, "A.test15([C)");
		verifyMethodSelection(target, "test15", new float[]{(float)0}, null);
		verifyMethodSelection(target, "test15", new double[]{(double)0}, null);
		verifyMethodSelection(target, "test15", new boolean[]{false}, null);

		verifyMethodSelection(target, "test15", new Object[]{new Integer(1)}, null);
		verifyMethodSelection(target, "test15", new Object[]{new Long(1)}, null);
		verifyMethodSelection(target, "test15", new Object[]{new Short((short)1)}, null);
		verifyMethodSelection(target, "test15", new Object[]{new Byte((byte)1)}, null);
		verifyMethodSelection(target, "test15", new Object[]{new Character((char)1)}, "A.test15([C)");
		verifyMethodSelection(target, "test15", new Object[]{new Float((float)1)}, null);
		verifyMethodSelection(target, "test15", new Object[]{new Double((double)1)}, null);

		verifyMethodSelection(target, "test15", new Object[]{new Boolean(false)}, null);
		verifyMethodSelection(target, "test15", new Object[]{new Object()}, null);
		verifyMethodSelection(target, "test15", new Object[]{null}, null);
	}

	public void testArray_16(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		testArray_16(a);
		testArray_16(b);
	}

	void testArray_16(Object target){
		verifyMethodSelection(target, "test16", new int[]{0}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new long[]{0}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new short[]{(short)0}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new byte[]{(byte)0}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new char[]{(char)0}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new float[]{(float)0}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new double[]{(double)0}, null);
		verifyMethodSelection(target, "test16", new boolean[]{false}, null);

		verifyMethodSelection(target, "test16", new Object[]{new Integer(1)}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new Object[]{new Long(1)}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new Object[]{new Short((short)1)}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new Object[]{new Byte((byte)1)}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new Object[]{new Character((char)1)}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new Object[]{new Float((float)1)}, "A.test16([F)");
		verifyMethodSelection(target, "test16", new Object[]{new Double((double)1)}, null);

		verifyMethodSelection(target, "test16", new Object[]{new Boolean(false)}, null);
		verifyMethodSelection(target, "test16", new Object[]{new Object()}, null);
		verifyMethodSelection(target, "test16", new Object[]{null}, null);
	}

	public void testArray_17(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		testArray_17(a);
		testArray_17(b);
	}

	void testArray_17(Object target){
		verifyMethodSelection(target, "test17", new int[]{0}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new long[]{0}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new short[]{(short)0}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new byte[]{(byte)0}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new char[]{(char)0}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new float[]{(float)0}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new double[]{(double)0}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new boolean[]{false}, null);

		verifyMethodSelection(target, "test17", new Object[]{new Integer(1)}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new Object[]{new Long(1)}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new Object[]{new Short((short)1)}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new Object[]{new Byte((byte)1)}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new Object[]{new Character((char)1)}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new Object[]{new Float((float)1)}, "A.test17([D)");
		verifyMethodSelection(target, "test17", new Object[]{new Double((double)1)}, "A.test17([D)");

		verifyMethodSelection(target, "test17", new Object[]{new Boolean(false)}, null);
		verifyMethodSelection(target, "test17", new Object[]{new Object()}, null);
		verifyMethodSelection(target, "test17", new Object[]{null}, null);
	}

	public void testArray_18(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		testArray_18(a);
		testArray_18(b);
	}

	void testArray_18(Object target){
		verifyMethodSelection(target, "test18", new int[]{0}, null);
		verifyMethodSelection(target, "test18", new long[]{0}, null);
		verifyMethodSelection(target, "test18", new short[]{(short)0}, null);
		verifyMethodSelection(target, "test18", new byte[]{(byte)0}, null);
		verifyMethodSelection(target, "test18", new char[]{(char)0}, null);
		verifyMethodSelection(target, "test18", new float[]{(float)0}, null);
		verifyMethodSelection(target, "test18", new double[]{(double)0}, null);
		verifyMethodSelection(target, "test18", new boolean[]{false}, "A.test18([Z)");
		verifyMethodSelection(target, "test18", new Object[]{new Integer(1)}, null);
		verifyMethodSelection(target, "test18", new Object[]{new Long(1)}, null);
		verifyMethodSelection(target, "test18", new Object[]{new Short((short)1)}, null);
		verifyMethodSelection(target, "test18", new Object[]{new Byte((byte)1)}, null);
		verifyMethodSelection(target, "test18", new Object[]{new Character((char)1)}, null);
		verifyMethodSelection(target, "test18", new Object[]{new Float((float)1)}, null);
		verifyMethodSelection(target, "test18", new Object[]{new Double((double)1)}, null);
		verifyMethodSelection(target, "test18", new Object[]{new Boolean(false)}, "A.test18([Z)");
		verifyMethodSelection(target, "test18", new Object[]{new Object()}, null);
		verifyMethodSelection(target, "test18", new Object[]{null}, null);
	}

	public void testArray_19(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		testArray_19(a);
		testArray_19(b);
	}

	void testArray_19(Object target){
		verifyMethodSelection(target, "test19", new int[]{0}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new long[]{0}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new short[]{(short)0}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new byte[]{(byte)0}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new char[]{(char)0}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new float[]{(float)0}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new double[]{(double)0}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new boolean[]{false}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new Object[]{new Integer(1)}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new Object[]{new Long(1)}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new Object[]{new Short((short)1)}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new Object[]{new Byte((byte)1)}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new Object[]{new Character((char)1)}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new Object[]{new Float((float)1)}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new Object[]{new Double((double)1)}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new Object[]{new Boolean(false)}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new Object[]{new Object()}, "A.test19([Ljava/lang/Object;)");
		verifyMethodSelection(target, "test19", new Object[]{null}, "A.test19([Ljava/lang/Object;)");
	}

	public void testOverloadMethod_20(){
		Object a = new A();
		Object b = createObjectWithDifferentClassLoader(A.class);
		testOverloadMethod_20(a);
		testOverloadMethod_20(b);
	}

	void testOverloadMethod_20(Object target){
		verifyMethodSelection(target, "test20", new HashMap(), Object.class, "A.test20(Ljava/lang/Object;)");
		verifyMethodSelection(target, "test20", new HashMap(), HashMap.class, "A.test20(Ljava/util/HashMap;)");
		verifyMethodSelection(target, "test20", new HashMap(), Map.class, "A.test20(Ljava/util/Map;)");
		verifyMethodSelection(target, "test20", new HashMap(), AbstractMap.class, "A.test20(Ljava/util/AbstractMap;)");
		verifyMethodSelection(target, "test20", new HashMap(), Cloneable.class, "A.test20(Ljava/lang/Cloneable;)");
		verifyMethodSelection(target, "test20", new HashMap(), Serializable.class, "A.test20(Ljava/io/Serializable;)");
		verifyMethodSelection(target, "test20", null, Object.class, "A.test20(Ljava/lang/Object;)");
		verifyMethodSelection(target, "test20", null, HashMap.class, "A.test20(Ljava/util/HashMap;)");
		verifyMethodSelection(target, "test20", null, Map.class, "A.test20(Ljava/util/Map;)");
		verifyMethodSelection(target, "test20", null, AbstractMap.class, "A.test20(Ljava/util/AbstractMap;)");
		verifyMethodSelection(target, "test20", null, Cloneable.class, "A.test20(Ljava/lang/Cloneable;)");
		verifyMethodSelection(target, "test20", null, Serializable.class, "A.test20(Ljava/io/Serializable;)");
	}

	public void testOverloadConstructor_1(){
		testOverloadConstructor_1(B.class);
		testOverloadConstructor_1(getClassWithDifferentClassLoader(B.class));
	}

	void testOverloadConstructor_1(Class cls){
		verifyConstructorSelection(cls, new HashMap(), Object.class, "<init>(Ljava/lang/Object;)");
		verifyConstructorSelection(cls, new HashMap(), HashMap.class, "<init>(Ljava/util/HashMap;)");
		verifyConstructorSelection(cls, new HashMap(), Map.class, "<init>(Ljava/util/Map;)");
		verifyConstructorSelection(cls, new HashMap(), AbstractMap.class, "<init>(Ljava/util/AbstractMap;)");
		verifyConstructorSelection(cls, new HashMap(), Cloneable.class, "<init>(Ljava/lang/Cloneable;)");
		verifyConstructorSelection(cls, new HashMap(), Serializable.class, "<init>(Ljava/io/Serializable;)");
		verifyConstructorSelection(cls, null, Object.class, "<init>(Ljava/lang/Object;)");
		verifyConstructorSelection(cls, null, HashMap.class, "<init>(Ljava/util/HashMap;)");
		verifyConstructorSelection(cls, null, Map.class, "<init>(Ljava/util/Map;)");
		verifyConstructorSelection(cls, null, AbstractMap.class, "<init>(Ljava/util/AbstractMap;)");
		verifyConstructorSelection(cls, null, Cloneable.class, "<init>(Ljava/lang/Cloneable;)");
		verifyConstructorSelection(cls, null, Serializable.class, "<init>(Ljava/io/Serializable;)");
	}

	public static Object createObjectWithDifferentClassLoader(Class type){
		Class cls = getClassWithDifferentClassLoader(type);
		try {
			return cls.newInstance();
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static Class getClassWithDifferentClassLoader(Class cls){
		try {
			String name = cls.getName();
			String resource = "/" + name.replace('.', '/') + ".class";
			InputStream in = MethodSelectionTest.class.getResourceAsStream(resource);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buf = new byte[512];
			int n;
			while ((n = in.read(buf)) != -1){
				bout.write(buf, 0, n);
			}
			final byte[] code = bout.toByteArray();
			ClassLoader cl = new ClassLoader(){
					protected Class findClass(String name) throws ClassNotFoundException {
						if ("MethodSelectionTest.A".equals(name)){
							return defineClass(code, 0, code.length);
						}
						throw new RuntimeException();
					}
				};
			return cl.loadClass("MethodSelectionTest.A");
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
		return null;
	}

	public static void main(String args[]) throws Throwable {
		junit.textui.TestRunner.main(new String[]{"MethodSelectionTest"});
	}
}
