package com.angelstone.android.utils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class SmsHelper {
	public static void sendToSmsInbox(Context context, String number,
			String body, long date) {
		ContentValues values = new ContentValues();
		values.put("address", number);
		values.put("body", body);
		values.put("date", new Long(date));
		values.put("read", 0);
		context.getContentResolver().insert(Uri.parse("content://sms/inbox"),
				values);
	}
}
