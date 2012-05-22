/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.classfile;

public class ClassFileException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ClassFileException() {
	}

	public ClassFileException(String msg) {
		super(msg);
	}
}
