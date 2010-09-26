package com.angelstone.android.smsblocker.ui;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListAdapter;

public class AddFromSmsRecordView extends AddFromListBaseView {
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
		return getContentResolver().query(
				Uri.parse("content://mms-sms/conversations"), null, "address is not null", null,
				"date desc");
	}

	@Override
	protected int getResultCode() {
		// TODO Auto-generated method stub
		return 5;
	}
}
