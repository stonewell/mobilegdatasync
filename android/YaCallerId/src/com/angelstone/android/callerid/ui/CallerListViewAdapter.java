package com.angelstone.android.callerid.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.callerid.R;
import com.angelstone.android.callerid.store.CallerId;
import com.angelstone.android.callerid.utils.PhotoLoader;

public class CallerListViewAdapter extends ResourceCursorAdapter {
	private int mIdxNumber = 0;
	private int mIdxId = 0;
	private PhotoLoader mPhotoLoader = null;

	public CallerListViewAdapter(Context context, Cursor c,
			PhotoLoader photoLoader) {
		super(context, R.layout.caller_list_item, c);

		mIdxNumber = c.getColumnIndex(CallerId.COL_NUMBER);
		mIdxId = c.getColumnIndex(CallerId.COL_ID);
		mPhotoLoader = photoLoader;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView iv = (ImageView) view.findViewById(R.id.list_item_img);
		TextView tv = (TextView) view.findViewById(R.id.list_item_text);

		tv.setText(cursor.getString(mIdxNumber));
		mPhotoLoader.loadPhoto(iv, cursor.getLong(mIdxId));
	}

}
