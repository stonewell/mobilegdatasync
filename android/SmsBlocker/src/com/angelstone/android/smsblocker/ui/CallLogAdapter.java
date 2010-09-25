package com.angelstone.android.smsblocker.ui;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class CallLogAdapter extends ResourceCursorAdapter implements
		ContentListViewAdapter {
	private int mNumberColIndex;
	private int mNameColIndex;
	private int mDateColIndex;
	private int mCallTypeIndex;
	private Cursor mCursor;

	private Map<String, Integer> mCheckState = null;
	private Context mContext;

	public CallLogAdapter(Context context, Cursor c,
			Map<String, Integer> checkState) {
		super(context, R.layout.call_record_list_row, c);
		mContext = context;
		mCursor = c;
		mNameColIndex = c.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME);
		mNumberColIndex = c.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
		mDateColIndex = c.getColumnIndexOrThrow(CallLog.Calls.DATE);
		mCallTypeIndex = c.getColumnIndexOrThrow(CallLog.Calls.TYPE);

		mCheckState = checkState;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		int type = mCursor.getInt(mCallTypeIndex);

		ImageView iv = (ImageView) view.findViewById(R.id.call_record_img);

		if (type == CallLog.Calls.INCOMING_TYPE) {
			iv.setImageResource(android.R.drawable.sym_call_incoming);
		} else if (type == CallLog.Calls.OUTGOING_TYPE) {
			iv.setImageResource(android.R.drawable.sym_call_outgoing);
		} else if (type == CallLog.Calls.MISSED_TYPE) {
			iv.setImageResource(android.R.drawable.sym_call_missed);
		}

		String name = mCursor.getString(mNameColIndex);
		TextView tv = (TextView) view.findViewById(R.id.call_record_name);
		tv.setText(name);

		String number = PhoneNumberUtils.formatNumber(mCursor
				.getString(mNumberColIndex));
		tv = (TextView) view.findViewById(R.id.call_record_phonenumber);
		tv.setText(number);

		long time = mCursor.getLong(mDateColIndex);
		tv = (TextView) view.findViewById(R.id.call_record_date);
		tv.setText(DateUtils.formatDateTime(mContext, time,
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME
						| DateUtils.FORMAT_ABBREV_ALL));

		iv = (ImageView) view
				.findViewById(R.id.call_record_list_item_check_img);

		number = PhoneNumberHelpers.delete86String(number);
		number = PhoneNumberHelpers.removeNonNumbericChar(number);

		if (mCheckState.containsKey(number)
				&& mCheckState.get(number) == UIConstants.CHECK_ON) {
			iv.setImageResource(R.drawable.btn_check_on);
		} else {
			iv.setImageResource(R.drawable.btn_check_off);
		}
	}

	@Override
	public String getNumber(int position) {
		Cursor c = (Cursor) getItem(position);
		String number = c.getString(mNumberColIndex);

		number = PhoneNumberHelpers.delete86String(number);
		number = PhoneNumberHelpers.removeNonNumbericChar(number);
		return number;
	}
}
