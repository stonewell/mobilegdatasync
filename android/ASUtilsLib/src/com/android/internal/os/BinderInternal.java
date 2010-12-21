package com.android.internal.os;

import android.util.Log;
import android.os.IBinder;

public class BinderInternal
{
	static
	{
		try
		{
			System.loadLibrary("android_runtime");
		}
		catch (Exception e)
		{
			Log.d("fw", e.getClass().toString());
		}
	}
	
	public static final native IBinder getContextObject();
}
