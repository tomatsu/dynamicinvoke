/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
import junit.framework.TestCase;
import dynamicinvoke.*;
import java.io.*;

public class ExceptionTest extends TestCase {
	static DynamicRuntime rt = new CodeGenerationRuntime();
	
	public static class A {	
		public void test1() throws IOException {
			throw new IOException();
		}
		public void test2() {
			throw new ClassCastException();
		}
	}
	public static class B extends A {
		public B() throws IOException {
			throw new IOException();
		}
	}

	public void testException_1(){
		testException_1(false);
		testException_1(true);
	}

	void testException_1(boolean reflection){
		DynamicRuntime r = reflection ? new ReflectionRuntime() : new CodeGenerationRuntime();
		try {
			r.callMethod(new A(), A.class, "test1", new Object[]{}, null);
		} catch (IOException e1){
			return;
		} catch (Exception e2){
			e2.printStackTrace();
		}
		fail();
	}

	public void testException_2(){
		testException_2(false);
		testException_2(true);
	}

	void testException_2(boolean reflection){
		DynamicRuntime r = reflection ? new ReflectionRuntime() : new CodeGenerationRuntime();
		try {
			r.callMethod(new A(), A.class, "test2", new Object[]{}, null);
		} catch (ClassCastException e1){
			return;
		} catch (Exception e2){
			e2.printStackTrace();
		}
		fail();
	}

	public void testException_3(){
		testException_3(false);
		testException_3(true);
	}

	void testException_3(boolean reflection){
		DynamicRuntime r = reflection ? new ReflectionRuntime() : new CodeGenerationRuntime();
		try {
			r.callConstructor(B.class, new Object[]{}, null);
		} catch (IOException e1){
			return;
		} catch (Exception e2){
			e2.printStackTrace();
		}
		fail();
	}


	public static void main(String args[]) throws Throwable {
		junit.textui.TestRunner.main(new String[]{"ExceptionTest"});
	}
}