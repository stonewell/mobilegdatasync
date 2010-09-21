package com.angelstone.android.smsblocker.ui;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class CallLogAdapter extends BaseAdapter {
	public static class CallLogItem {
		public CallLogItem(int p, int c) {
			Position = p;
			CheckState = c;
		}

		public int Position;
		public int CheckState;
	};

	private int mNumberColIndex;
	private int mNameColIndex;
	private int mDateColIndex;
	private int mCallTypeIndex;
	private Cursor mCursor;
	private int mRowIDColumn;

	private ArrayList<CallLogItem> mCheckState = null;
	private Context mContext;

	private int mLayout = R.layout.call_record_list_row;
	private LayoutInflater mInflater = null;

	public CallLogAdapter(Context context, Cursor c,
			ArrayList<CallLogItem> checkState) {
		mContext = context;
		mCursor = c;
		mRowIDColumn = c != null ? c.getColumnIndexOrThrow("_id") : -1;
		mNameColIndex = c.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME);
		mNumberColIndex = c.getColumnIndexOrThrow(CallLog.Calls.NUMBER);
		mDateColIndex = c.getColumnIndexOrThrow(CallLog.Calls.DATE);
		mCallTypeIndex = c.getColumnIndexOrThrow(CallLog.Calls.TYPE);

		mCheckState = checkState;

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void bindView(View view, Context context, int position) {
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

		CallLogItem item = mCheckState.get(position);
		if (item.CheckState == PhoneNumberHelpers.CHECK_OFF) {
			iv.setImageResource(R.drawable.btn_check_off);
		} else if (item.CheckState == PhoneNumberHelpers.CHECK_ON) {
			iv.setImageResource(R.drawable.btn_check_on);
		} else if (item.CheckState == PhoneNumberHelpers.CHECK_DISABLE) {
			iv.setImageResource(R.drawable.btn_check_off_disable);
		}

	}

	@Override
	public int getCount() {
		return mCheckState.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= 0 && position < mCheckState.size()) {
			CallLogItem item = mCheckState.get(position);
			if (mCursor.moveToPosition(item.Position)) {
				return mCursor;
			}
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		Object o = getItem(position);

		if (o != null) {
			return mCursor.getLong(mRowIDColumn);
		}

		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int p = mCheckState.get(position).Position;

		if (!mCursor.moveToPosition(p)) {
			throw new IllegalStateException("couldn't move cursor to position "
					+ position);
		}

		View v;
		if (convertView == null) {
			v = newView(mContext, mCursor, parent);
		} else {
			v = convertView;
		}
		bindView(v, mContext, position);
		return v;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		int p = mCheckState.get(position).Position;
		mCursor.moveToPosition(p);
		View v;
		if (convertView == null) {
			v = newDropDownView(mContext, mCursor, parent);
		} else {
			v = convertView;
		}
		bindView(v, mContext, position);
		return v;
	}

	private View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(mLayout, parent, false);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	private View newDropDownView(Context context, Cursor cursor,
			ViewGroup parent) {
		return mInflater.inflate(mLayout, parent, false);
	}
}
