package com.angelstone.android.callerid.ui;

import com.angelstone.android.callerid.R;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;

public class CallerListViewAdapter extends ResourceCursorAdapter {

	public CallerListViewAdapter(Context context, Cursor c) {
		super(context, R.layout.caller_list_item, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		
	}

}
