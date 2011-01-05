package com.angelstone.android.utils;

import android.os.HandlerThread;

public class HandlerThreadQuiter {

	public static boolean quit(HandlerThread t) {
		return t.quit();
	}
}
