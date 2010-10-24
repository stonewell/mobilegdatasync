package com.angelstone.android.smsblocker.ui;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.BlackList;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class BlackListViewAdapter extends ResourceCursorAdapter {
	private int mNumberColumnId;
	private int mBlockSmsColumnId;
	
	public BlackListViewAdapter(Context context, Cursor c) {
		super(context, R.layout.black_list_item_layout, c, true);
		
		mNumberColumnId = c.getColumnIndex(BlackList.NUMBER);
		mBlockSmsColumnId = c.getColumnIndex(BlackList.BLOCK_SMS);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView iv = (ImageView) view
				.findViewById(R.id.black_list_item_sb_img);
		
		int block_sms = cursor.getInt(mBlockSmsColumnId);
		
		if (block_sms == 1)
			iv.setImageResource(R.drawable.sms_reject);
		else
			iv.setImageResource(R.drawable.sms);

		String number = cursor.getString(mNumberColumnId);
		TextView tv = (TextView) view
				.findViewById(R.id.black_list_item_text);
		tv.setText(number);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

}
