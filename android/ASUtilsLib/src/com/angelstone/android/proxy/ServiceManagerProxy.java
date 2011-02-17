package com.angelstone.android.proxy;

import java.lang.reflect.Method;

import android.os.IBinder;
import android.util.Log;

public class ServiceManagerProxy {
	public static IBinder getService(String name) {
		try {
			Class<?> mClass = Class.forName("android.os.ServiceManager");
			Method mGetService = mClass.getMethod("getService", String.class);

			if (mGetService == null) {
				Log.e("ServiceManager", "service not found:" + name);
				return null;
			}

			return (IBinder) mGetService.invoke(null, name);

		} catch (Throwable t) {
			Log.e("ServiceManager", "getService fail:" + name, t);
		}
		return null;
	}

}
