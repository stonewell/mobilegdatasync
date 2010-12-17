package com.angelstone.android.phonetools.ui;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListAdapter;

public class AddFromSmsRecordView extends AddFromListBaseView {
	private static final Uri MMS_SMS_URI = Uri
			.parse("content://mms-sms/conversations").buildUpon()
			.appendQueryParameter("simple", "true").build();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected ListAdapter createAdapter(Context context, Cursor c,
			Map<String, Integer> checkStates) {
		return new SmsRecordViewAdapter(context, c, checkStates);
	}

	@Override
	protected Cursor getListCursor() {
		return getContentResolver().query(MMS_SMS_URI, null, null, null, null);
	}

	@Override
	protected int getResultCode() {
		return 5;
	}
}
