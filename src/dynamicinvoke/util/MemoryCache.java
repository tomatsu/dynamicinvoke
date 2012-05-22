/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.util;

import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

public class MemoryCache<K,V> extends AbstractMap<K,V> {
	private Map<K,Pair<K,V>> map;
        private ReferenceQueue<Object> queue = new ReferenceQueue<Object>();

	public MemoryCache() {
		this(new ConcurrentHashMap<K,Pair<K,V>>());
	}

	public MemoryCache(Map<K,Pair<K,V>> map) {
		this.map = map;
	}

	public V get(Object key) {
	        processQueue();
		Pair<K,V> value = map.get(key);
		if (value != null) {
			return value.getValue();
		}
		return null;

	}

	public V put(K key, V value) {
	        processQueue();
	        Pair<K,V> old = map.put(key, new Pair<K,V>(key, value, queue));
		if (old != null) {
			return old.getValue();
		}
		return null;
	}

	public V remove(Object key) {
	        processQueue();
	        Pair<K,V> old = map.remove(key);
		return old.getValue();
	}

	private static boolean valEquals(Object o1, Object o2) {
		return (o1 == null) ? (o2 == null) : o1.equals(o2);
	}

        private class Pair<K,V> extends SoftReference<V> {
	        K key;

	        Pair(K key, V value, ReferenceQueue<Object> queue){
		        super(value, queue);
		        this.key = key;
		}

		public V getValue(){
			return get();
		}
        }

        private void processQueue(){
	        Pair p;
	        while ((p = (Pair)queue.poll()) != null) {
		        map.remove(p.key);
		}
	}

	private class Entry<K,V> implements Map.Entry<K,V> {
		private Map.Entry<K,Pair<K,V>> ent;
		private V value;

		Entry(Map.Entry<K,Pair<K,V>> ent, V value) {
			this.ent = ent;
			this.value = value;
		}

		public K getKey() {
			return ent.getKey();
		}

		public V getValue() {
			return value;
		}

		public V setValue(V value) {
		        Pair<K,V> oldPair = ent.setValue(new Pair<K,V>(ent.getKey(), value, queue));
			return oldPair.getValue();
		}

		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry e = (Map.Entry) o;
			return (valEquals(ent.getKey(), e.getKey()) && valEquals(value,
					e.getValue()));
		}

		public int hashCode() {
			Object k;
			return ((((k = getKey()) == null) ? 0 : k.hashCode()) ^ ((value == null) ? 0
					: value.hashCode()));
		}
	}

	private class EntrySet extends AbstractSet<Map.Entry<K,V>> {
		Set<Map.Entry<K,Pair<K,V>>> hashEntries = map.entrySet();

		public Iterator<Map.Entry<K,V>> iterator() {
		        processQueue();
			return new Iterator<Map.Entry<K,V>>() {
				Iterator<Map.Entry<K,Pair<K,V>>> hashIterator = hashEntries.iterator();
				Entry<K,V> next = null;

				public boolean hasNext() {
					while (hashIterator.hasNext()) {
 						Map.Entry<K,Pair<K,V>> ent = hashIterator.next();
						Pair<K,V> vc = ent.getValue();
						V v = null;
						if ((vc != null) && ((v = vc.get()) == null)) {
							continue;
						}
						next = new Entry<K,V>(ent, v);
						return true;
					}
					return false;
				}

				public Map.Entry<K,V> next() {
					if (next == null && !hasNext()) {
						throw new NoSuchElementException();
					}
					Entry<K,V> e = next;
					next = null;
					return e;
				}

				public void remove() {
					hashIterator.remove();
				}
			};
		}

		public boolean isEmpty() {
			return !iterator().hasNext();
		}

		public int size() {
			int j = 0;
			for (Iterator i = iterator(); i.hasNext(); i.next()) {
				j++;
			}
			return j;
		}

		public boolean remove(Object o) {
			if (o instanceof Entry) {
				return hashEntries.remove(((Entry) o).ent);
			} else {
				return false;
			}
		}
	}


	public Set<Map.Entry<K,V>> entrySet() {
		return new EntrySet();
	}
}