package com.angelstone.android.smsblocker.ui;

import com.angelstone.android.smsblocker.R;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;

public class BlackListViewAdapter extends ResourceCursorAdapter {

	public BlackListViewAdapter(Context context, Cursor c) {
		super(context, R.layout.black_list_item_layout, c, true);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

}
