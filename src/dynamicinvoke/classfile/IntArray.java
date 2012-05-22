/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.classfile;

class IntArray {
	int array[] = new int[2];
	int count = 0;

	public void add(int i) {
		if (count >= array.length - 1) {
			int[] newarray = new int[array.length * 2];
			System.arraycopy(array, 0, newarray, 0, array.length);
			array = newarray;
		}
		array[count++] = i;
	}

	public int size() {
		return count;
	}

	public int[] getArray() {
		return array;
	}
}
