package com.angelstone.android.smsblocker.ui;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.widget.ListAdapter;

public class AddFromCallLogView extends AddFromListBaseView {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected ListAdapter createAdapter(Context context, Cursor c,
			Map<String, Integer> checkStates) {
		return new CallLogAdapter(context, c, checkStates);
	}

	@Override
	protected Cursor getListCursor() {
		return getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
				CallLog.Calls.NUMBER + " is not null", null, CallLog.Calls.DEFAULT_SORT_ORDER);
	}

	@Override
	protected int getResultCode() {
		return 4;
	}

}
