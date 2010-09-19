package com.angelstone.sync.contact.gcontact;

import com.angelstone.sync.gclient.GDataException;

public class GDataFeedsException extends GDataException {

	private static final long serialVersionUID = 6750139381363217751L;

	public GDataFeedsException(String message, Throwable cause) {
		super(message, cause);
	}

	public GDataFeedsException(String className, String methodName,
			Throwable cause) {
		this("Error at " + className + "." + methodName, cause);
	}

	public GDataFeedsException(Class<?> sourceClass, String methodName,
			Throwable cause) {
		this("Error at " + sourceClass.getName() + "." + methodName, cause);
	}

	public GDataFeedsException(String message, String className,
			String methodName, Throwable cause) {
		this(message + " at " + className + "." + methodName, cause);
	}

	public GDataFeedsException(String message) {
		this(message, (Throwable) null);
	}

	public GDataFeedsException(String className, String methodName) {
		this(className, methodName, (Throwable) null);
	}

	public GDataFeedsException(String message, String className,
			String methodName) {
		this(message, className, methodName, null);
	}
}
