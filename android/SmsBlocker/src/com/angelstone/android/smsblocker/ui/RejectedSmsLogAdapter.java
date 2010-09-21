package com.angelstone.android.smsblocker.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.smsblocker.R;

public class RejectedSmsLogAdapter extends ResourceCursorAdapter {

		private int mNumberColumnId;
		private int mNameColumnId;
		private int mSmsBodyColumnId;

		public RejectedSmsLogAdapter(Context context, int layout, Cursor c) {
			super(context, layout, c, true);
			// mTimeColumnId = c.getColumnIndexOrThrow("time");
			mNumberColumnId = c.getColumnIndexOrThrow("number");
			mNameColumnId = c.getColumnIndexOrThrow("contact_name");
			mSmsBodyColumnId = c.getColumnIndexOrThrow("sms_text");
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ImageView iv = (ImageView) view
					.findViewById(R.id.call_reject_record_item_img);
			iv.setImageResource(R.drawable.sms_reject);

			String name = cursor.getString(mNameColumnId);
			TextView tv = (TextView) view
					.findViewById(R.id.call_reject_record_item_text_1);
			tv.setText(name);

			String number = cursor.getString(mNumberColumnId);
			tv = (TextView) view
					.findViewById(R.id.call_reject_record_item_text_2);
			tv.setText(number);

			String replySmsBody = cursor.getString(mSmsBodyColumnId);
			tv = (TextView) view
					.findViewById(R.id.call_reject_record_item_text_3);
			tv.setText(replySmsBody);
		}

}
