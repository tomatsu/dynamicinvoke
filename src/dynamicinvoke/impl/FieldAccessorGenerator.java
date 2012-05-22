/*
 * This software is released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/. 
 */
package dynamicinvoke.impl;

import dynamicinvoke.classfile.ClassFile;
import dynamicinvoke.classfile.Opcode;
import dynamicinvoke.classfile.Constants;
import java.lang.reflect.Field;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FieldAccessorGenerator {

	static String makeSignature(Class<?> type) {
		if (type == int.class) {
			return "I";
		} else if (type == byte.class) {
			return "B";
		} else if (type == long.class) {
			return "J";
		} else if (type == char.class) {
			return "C";
		} else if (type == short.class) {
			return "S";
		} else if (type == float.class) {
			return "F";
		} else if (type == double.class) {
			return "D";
		} else if (type == boolean.class) {
			return "Z";
		} else if (type == void.class) {
			return "V";
		} else {
			if (type.isArray()) {
				return "[" + makeSignature(type.getComponentType());
			} else {
				return "L" + type.getName().replace('.', '/') + ";";
			}
		}
	}

	public static FieldAccessor generate(String fieldName, Class<?> cls,
			CodeLoader loader, boolean isStatic) throws NoSuchFieldException,
			InstantiationException, IOException, IllegalAccessException {
		String tempClassName = "_" + (loader.nextCount() & 0x7fffffff);
		ClassFile cf = new ClassFile(tempClassName,
				"dynamicinvoke.impl.FieldAccessor", null,
				(short) (Constants.ACC_PUBLIC | Constants.ACC_FINAL));
		Field field = cls.getField(fieldName);
		Class<?> type = field.getType();
		String param = makeSignature(type);

		cf.openMethod("<init>", "()V", Constants.ACC_PUBLIC);
		cf.add(Opcode.ALOAD_0);
		cf.add(Opcode.INVOKESPECIAL, "dynamicinvoke.impl.FieldAccessor", "<init>",
				"()", "V");
		cf.add(Opcode.RETURN);
		cf.closeMethod();

		cf.openMethod("get", "(Ljava/lang/Object;)Ljava/lang/Object;",
				Constants.ACC_PUBLIC);
		if (type.isPrimitive()) {
			cf.add(Opcode.NEW, DynamicProxyFactory.wrapperClass(type));
			cf.add(Opcode.DUP);
			if (isStatic) {
				cf.add(Opcode.GETSTATIC, cls.getName(), fieldName, param);
			} else {
				cf.add(Opcode.ALOAD_1);
				if (!cls.equals(Object.class)) {
					cf.add(Opcode.CHECKCAST, cls.getName());
				}
				cf.add(Opcode.GETFIELD, cls.getName(), fieldName, param);
			}
			cf.add(Opcode.INVOKESPECIAL,
					DynamicProxyFactory.wrapperClass(type), "<init>", "("
							+ ClassFile.signature(type) + ")", "V");
		} else {
			if (isStatic) {
				cf.add(Opcode.GETSTATIC, cls.getName(), fieldName, param);
			} else {
				cf.add(Opcode.ALOAD_1);
				if (!cls.equals(Object.class)) {
					cf.add(Opcode.CHECKCAST, cls.getName());
				}
				cf.add(Opcode.GETFIELD, cls.getName(), fieldName, param);
			}
		}
		cf.add(Opcode.ARETURN);
		cf.closeMethod();

		cf.openMethod("set", "(Ljava/lang/Object;Ljava/lang/Object;)V",
				Constants.ACC_PUBLIC);

		if (isStatic) {
			cf.add(Opcode.ALOAD_1);
			if (type.isPrimitive()) {
				DynamicProxyFactory.castParam(cf, type);
			} else {
				cf.add(Opcode.CHECKCAST, type.getName());
			}
			cf.add(Opcode.PUTSTATIC, cls.getName(), fieldName, param);
		} else {
			cf.add(Opcode.ALOAD_1);
			if (!cls.equals(Object.class)) {
				cf.add(Opcode.CHECKCAST, cls.getName());
			}
			cf.add(Opcode.ALOAD_2);
			if (type.isPrimitive()) {
				DynamicProxyFactory.castParam(cf, type);
			} else {
				cf.add(Opcode.CHECKCAST, type.getName());
			}
			cf.add(Opcode.PUTFIELD, cls.getName(), fieldName, param);
		}
		cf.add(Opcode.RETURN);
		cf.closeMethod();

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);
		cf.write(dout);

		/* debug(cf); */

		Class<?> clazz = loader.define(tempClassName, bout.toByteArray(), 0,
				bout.size());
		loader.resolve(clazz);
		return (FieldAccessor) clazz.newInstance();
	}

	/**
	 * static void debug(ClassFile file) { try { String fileName = "/tmp/" +
	 * file.getClassName() + ".class"; System.out.println(fileName);
	 * FileOutputStream fout = new FileOutputStream(fileName); DataOutputStream
	 * dout = new DataOutputStream(fout); file.write(dout); fout.close(); }
	 * catch (IOException e) { e.printStackTrace(); } }
	 **/
}
