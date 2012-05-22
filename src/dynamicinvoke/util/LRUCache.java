/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.util;
import java.util.*;

public class LRUCache<K,V> extends LinkedHashMap<K,V> {
	private static final long serialVersionUID = 1L;
	int max;

	public LRUCache() {
		this(256);
	}

	public LRUCache(int max) {
		this.max = max;
	}

	protected boolean removeEldestEntry(Map.Entry eldest) {
		return size() > max;
	}
}

