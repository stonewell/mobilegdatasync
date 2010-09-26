package com.angelstone.android.smsblocker.ui;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListAdapter;

import com.angelstone.android.platform.SysCompat;

public class AddFromContactView extends AddFromListBaseView {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected ListAdapter createAdapter(Context context, Cursor c,
			Map<String, Integer> checkStates) {
		return new ContactsViewAdapter(context, c, checkStates);
	}

	@Override
	protected Cursor getListCursor() {
		SysCompat sc = SysCompat.register(this);

		Cursor cur = getContentResolver().query(sc.PHONE_URI, null, sc.COLUMN_PHONE_NUMBER + " is not null", null,
				"UPPER(" + sc.COLUMN_PHONE_NAME + ") ASC");

		return cur;
	}

	@Override
	protected int getResultCode() {
		return 3;
	}

}
