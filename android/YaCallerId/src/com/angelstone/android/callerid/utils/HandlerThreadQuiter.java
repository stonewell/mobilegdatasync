package com.angelstone.android.callerid.utils;

import android.os.HandlerThread;

public class HandlerThreadQuiter {

	public static boolean quit(HandlerThread t) {
		return t.quit();
	}
}
