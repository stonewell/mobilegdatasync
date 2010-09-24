package com.angelstone.android.smsblocker.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.EventLog;

public class RejectedSmsLogAdapter extends ResourceCursorAdapter {

		private int mNumberColumnId;
		private int mSmsBodyColumnId;

		public RejectedSmsLogAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c, true);
			mNumberColumnId = c.getColumnIndexOrThrow(EventLog.NUMBER);
			mSmsBodyColumnId = c.getColumnIndexOrThrow(EventLog.SMS_TEXT);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ImageView iv = (ImageView) view
					.findViewById(R.id.call_reject_record_item_img);
			iv.setImageResource(R.drawable.sms_reject);

			String number = cursor.getString(mNumberColumnId);
			TextView tv = (TextView) view
					.findViewById(R.id.call_reject_record_item_text_2);
			tv.setText(number);

			String replySmsBody = cursor.getString(mSmsBodyColumnId);
			tv = (TextView) view
					.findViewById(R.id.call_reject_record_item_text_3);
			tv.setText(replySmsBody);
		}

}
