package com.angelstone.android.smsblocker.ui;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.utils.PhoneNumberHelpers;

import android.content.Context;
import android.database.Cursor;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.ResourceCursorAdapter;

public class SmsRecordViewAdapter extends ResourceCursorAdapter{

	public SmsRecordViewAdapter(Context context, Cursor c, int[] checkStates) {
		super(context, R.layout.sms_record_list_row, c, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		String number = mCursor.getString(mCursor.getColumnIndex("address"));

		number = PhoneNumberHelpers.delete86String(number);
		number = PhoneNumberHelpers.removeNonNumbericChar(number);

		map.put("number", PhoneNumberUtils.formatNumber(number));
		String name = PhoneNumberHelpers.getContactName(this, number);

		if (name != null && !name.equals("")) {
			map.put("name", "<" + name + ">");
		} else {
			map.put("name", null);
		}

		String body = mCursor.getString(mCursor.getColumnIndex("body"));

		map.put("body", body);

		if (pnm.isInBlacklist(number)) {
			map.put("checkImg", R.drawable.btn_check_off_disable);
			mCheckState[mCursor.getPosition()] = PhoneNumberHelpers.CHECK_DISABLE;
		} else if (AddBlackListNumberView.indexOfSelectedNumber(number) != -1) {
			map.put("checkImg", R.drawable.btn_check_on);
			mCheckState[mCursor.getPosition()] = PhoneNumberHelpers.CHECK_ON;

			mCurrentCheckOnItemCount++;
			mAllCheckAllowedItemCount++;
		} else {
			map.put("checkImg", R.drawable.btn_check_off);
			mCheckState[mCursor.getPosition()] = PhoneNumberHelpers.CHECK_OFF;

			mAllCheckAllowedItemCount++;
		}
	}

}
