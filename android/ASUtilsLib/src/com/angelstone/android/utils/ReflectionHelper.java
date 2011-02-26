package com.angelstone.android.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionHelper {
	public static Object getField(Class<?> cls, String fieldName,
			Object instance) throws ClassNotFoundException, SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {

		Field f = cls.getField(fieldName);

		return f.get(instance);
	}

	public static Object getField(String clsName, String fieldName,
			Object instance) throws ClassNotFoundException, SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		Class<?> cls = Class.forName(clsName);

		return getField(cls, fieldName, instance);
	}

	public static Object getField(Object instance, String fieldName)
			throws ClassNotFoundException, SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		return getField(instance.getClass(), fieldName, instance);
	}

	public static Object getStaticField(String clsName, String fieldName)
			throws ClassNotFoundException, SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		return getField(clsName, fieldName, null);
	}

	public static Object callMethod(Class<?> cls, String methodName,
			Object instance, Class<?>[] paramsTypes, Object... params)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Method m = cls.getMethod(methodName, paramsTypes);

		return m.invoke(instance, params);
	}

	public static Object newInstance(Class<?> cls, Class<?>[] paramsTypes,
			Object... params) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Constructor<?> c = cls.getConstructor(paramsTypes);

		return c.newInstance(params);
	}

	public static Object callMethod(Class<?> cls, String methodName,
			Object instance) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		return callMethod(cls, methodName, instance, (Class<?>[]) null,
				(Object[]) null);
	}

	public static Object callMethod(Object instance, String methodName,
			Class<?>[] paramsTypes) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		return callMethod(instance.getClass(), methodName, instance,
				paramsTypes, (Object[]) null);
	}

	public static Object callMethod(Object instance, String methodName)
			throws SecurityException, IllegalArgumentException,
			NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		return callMethod(instance.getClass(), methodName, instance,
				(Class<?>[]) null, (Object[]) null);
	}

	public static Object getStaticField(Object headers, String fieldName)
			throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
		return getField(headers.getClass(), fieldName, null);
	}
	
	public static Object callMethod(Object instance, String methodName,
			Class<?>[] paramsTypes, Object... params) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		return callMethod(instance.getClass(), methodName, instance,
				paramsTypes, params);
	}
}
