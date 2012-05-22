/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
import junit.framework.TestCase;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import dynamicinvoke.*;

public class FieldTest extends TestCase {
	static DynamicRuntime rt = new CodeGenerationRuntime();
	static DynamicRuntime rt2 = new ReflectionRuntime();

	public void testRead_1(){
		testRead_1(false);
		testRead_1(true);
	}

	void testRead_1(boolean reflection){
		DynamicRuntime r = reflection ? rt2 : rt;
		Object target = new java.awt.Point(100, 200);
		try {
			if (((Integer)r.getObjectField(target, java.awt.Point.class, "x")).intValue() != 100){
				fail();
			}
			if (((Integer)r.getObjectField(target, java.awt.Point.class, "y")).intValue() != 200){
				fail();
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
	}

	public void testRead_2(){
		testRead_2(false);
		testRead_2(true);
	}

	void testRead_2(boolean reflection){
		DynamicRuntime r = reflection ? rt2 : rt;
		Class cls = System.class;
		try {
			if (r.getObjectField(null, cls, "out") != System.out){
				fail();
			}
			if (r.getObjectField(null, cls, "in") != System.in){
				fail();
			}
			if (r.getObjectField(null, cls, "err") != System.err){
				fail();
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
	}

	public void testWrite_1(){
		testWrite_1(false);
		testWrite_1(true);
	}

	void testWrite_1(boolean reflection){
		DynamicRuntime r = reflection ? rt2 : rt;
		Object target = new java.awt.Point(0, 0);
		try {
			Integer newX = new Integer(100);
			r.setObjectField(target, java.awt.Point.class, "x", newX);
			if (!r.getObjectField(target, java.awt.Point.class, "x").equals(newX)){
				fail();
			}
			Integer newY = new Integer(200);
			r.setObjectField(target, java.awt.Point.class, "y", newY);
			if (!r.getObjectField(target, java.awt.Point.class, "y").equals(newY)){
				fail();
			}
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
	}

	public static void main(String[] args){
		junit.textui.TestRunner.main(new String[]{"FieldTest"});
	}
}