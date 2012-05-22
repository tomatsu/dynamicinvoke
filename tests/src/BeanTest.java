/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
import junit.framework.TestCase;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import dynamicinvoke.*;
import java.util.*;
import java.awt.*;

public class BeanTest extends TestCase {
	static DynamicRuntime rt = new CodeGenerationRuntime();
	static DynamicRuntime rt2 = new ReflectionRuntime();

	public void testRead_1(){
		testRead_1(false);
		testRead_1(true);
	}

	void testRead_1(boolean reflection){
		DynamicRuntime r = reflection ? rt2 : rt;
		Component component = new Button();
		component.setName("ok");
		try {
			Object value = r.getBeanProperty(component, "name");
			assertEquals(value, "ok");
		} catch (Exception e){
			e.printStackTrace();
			fail();
		}
	}

	public static void main(String[] args){
		junit.textui.TestRunner.main(new String[]{"BeanTest"});
	}

}