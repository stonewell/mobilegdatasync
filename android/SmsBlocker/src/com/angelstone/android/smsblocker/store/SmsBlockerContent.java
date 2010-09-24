package com.angelstone.android.smsblocker.store;

import android.net.Uri;

public interface SmsBlockerContent {
	public static final String AUTHORITY = "com.angelstone.anroid.smsblocker";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
}
