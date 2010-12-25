package com.angelstone.android.callerid.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.callerid.R;
import com.angelstone.android.callerid.store.CallerId;

public class CallerListViewAdapter extends ResourceCursorAdapter {
	private int mIdxNumber = 0;
	private int mIdxData = 0;
	
	public CallerListViewAdapter(Context context, Cursor c) {
		super(context, R.layout.caller_list_item, c);
		
		mIdxNumber = c.getColumnIndex(CallerId.COL_NUMBER);
		mIdxData = c.getColumnIndex(CallerId.COL_DATA);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView iv = (ImageView)view.findViewById(R.id.list_item_img);
		TextView tv = (TextView)view.findViewById(R.id.list_item_text);
		
		tv.setText(cursor.getString(mIdxNumber));
	}

}
