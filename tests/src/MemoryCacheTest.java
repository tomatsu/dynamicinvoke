/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
import junit.framework.TestCase;
import dynamicinvoke.util.*;

public class MemoryCacheTest {

	public void testLargeMap(){
		MemoryCache cache = new MemoryCache();
		for (int i = 0; i < 100000000; i++){
			cache.put(new Object(), new Object());
		}
	}

	public static void main(String[] args){
		MemoryCacheTest t = new MemoryCacheTest();
		t.testLargeMap();
	}
}