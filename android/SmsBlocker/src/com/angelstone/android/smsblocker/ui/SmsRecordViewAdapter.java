package com.angelstone.android.smsblocker.ui;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class SmsRecordViewAdapter extends ResourceCursorAdapter implements
		ContentListViewAdapter {
	private static final String COL_PERSON = "person";
	private static final String COL_SUBJECT = "subject";
	// private static final String COL_DATE = "date";
	private static final String COL_BODY = "body";
	private static final String COL_ADDRESS = "address";

	private int mColPerson = 0;
	private int mColSubject = 0;
	private int mColBody = 0;
	private int mColAddress = 0;

	private Map<String, Integer> mCheckStates = null;

	public SmsRecordViewAdapter(Context context, Cursor c,
			Map<String, Integer> checkStates) {
		super(context, R.layout.sms_record_list_row, c, false);

		mCheckStates = checkStates;
		mColPerson = c.getColumnIndex(COL_PERSON);
		mColSubject = c.getColumnIndex(COL_SUBJECT);
		mColBody = c.getColumnIndex(COL_BODY);
		mColAddress = c.getColumnIndex(COL_ADDRESS);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView img = (ImageView) view.findViewById(R.id.sms_log_check_img);
		TextView vNumber = (TextView) view.findViewById(R.id.sms_record_number);
		TextView vName = (TextView) view.findViewById(R.id.sms_record_name);
		TextView vBody = (TextView) view.findViewById(R.id.sms_record_body);

		String number = cursor.getString(mColAddress);
		
		if (number == null) {
			for(int i=0;i < cursor.getColumnCount(); i++) {
				Log.e("@@@@@@@@@@@@", cursor.getColumnName(i) + "," + cursor.getString(i));
			}
		}

		number = PhoneNumberHelpers.delete86String(number);
		number = PhoneNumberHelpers.removeNonNumbericChar(number);

		vNumber.setText(number);
		String name = cursor.getString(mColPerson);

		if (!TextUtils.isEmpty(name)) {
			vName.setText("<"
					+ PhoneNumberHelpers.getContactNameById(context,
							Integer.parseInt(name)) + ">");
		} else {
			vName.setText("");
		}

		String body = cursor.getString(mColBody);
		String subject = cursor.getString(mColSubject);

		if (TextUtils.isEmpty(subject)) {
			vBody.setText(body);
		} else {
			vBody.setText(subject);
		}

		if (mCheckStates.containsKey(number)
				&& mCheckStates.get(number) == UIConstants.CHECK_ON) {
			img.setImageResource(R.drawable.btn_check_on);
		} else {
			img.setImageResource(R.drawable.btn_check_off);
		}

	}

	public String getNumber(int position) {
		Cursor c = (Cursor) getItem(position);
		String number = c.getString(mColAddress);

		number = PhoneNumberHelpers.delete86String(number);
		number = PhoneNumberHelpers.removeNonNumbericChar(number);
		return number;
	}

}
