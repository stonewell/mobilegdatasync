package com.angelstone.android.callfirewall.ui;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.phonetools.R;
import com.angelstone.android.phonetools.store.EventLog;

public class RejectedCallLogAdapter extends ResourceCursorAdapter {

	private int mNumberColumnId;
	private int mTimeColumnId;
	private java.text.DateFormat mDateFormat;
	private java.text.DateFormat mTimeFormat;

	public RejectedCallLogAdapter(Context context, int layout, Cursor c) {
		super(context, layout, c, true);
		mNumberColumnId = c.getColumnIndexOrThrow(EventLog.COL_NUMBER);
		mTimeColumnId = c.getColumnIndexOrThrow(EventLog.COL_TIME);
		mDateFormat = DateFormat.getDateFormat(context);
		mTimeFormat = DateFormat.getTimeFormat(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView iv = (ImageView) view
				.findViewById(R.id.call_reject_record_item_img);
		iv.setImageResource(R.drawable.block);

		String number = cursor.getString(mNumberColumnId);
		TextView tv = (TextView) view
				.findViewById(R.id.call_reject_record_item_text_2);
		tv.setText(number);

		long time = cursor.getLong(mTimeColumnId);
		tv = (TextView) view.findViewById(R.id.call_reject_record_item_text_3);

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);

		tv.setText(mDateFormat.format(c.getTime()) + " "
				+ mTimeFormat.format(c.getTime()));
	}

}
