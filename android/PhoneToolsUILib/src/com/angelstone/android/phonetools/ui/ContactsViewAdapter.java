package com.angelstone.android.phonetools.ui;

import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.platform.SysCompat;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class ContactsViewAdapter extends ResourceCursorAdapter implements
		ContentListViewAdapter {
	private int mColName = 0;
	private int mColNumber = 0;

	private Map<String, Integer> mCheckStates = null;

	public ContactsViewAdapter(Context context, Cursor c,
			Map<String, Integer> checkStates) {
		super(context, R.layout.contacts_row, c, false);

		mCheckStates = checkStates;
		SysCompat sc = SysCompat.register(context);
		mColNumber = c.getColumnIndex(sc.COLUMN_PHONE_NUMBER);
		mColName = c.getColumnIndex(sc.COLUMN_PHONE_NAME);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView img = (ImageView) view.findViewById(R.id.contact_row_check_img);
		TextView vNumber = (TextView) view.findViewById(R.id.phonenumber);
		TextView vName = (TextView) view.findViewById(R.id.name);

		String number = cursor.getString(mColNumber);

		number = PhoneNumberHelpers.delete86String(number);
		number = PhoneNumberHelpers.removeNonNumbericChar(number);

		vNumber.setText(number);
		
		String name = cursor.getString(mColName);

		vName.setText(name);

		if (mCheckStates.containsKey(number)
				&& mCheckStates.get(number) == UIConstants.CHECK_ON) {
			img.setImageResource(R.drawable.btn_check_on);
		} else {
			img.setImageResource(R.drawable.btn_check_off);
		}

	}

	public String getNumber(Context context, int position) {
		Cursor c = (Cursor) getItem(position);
		String number = c.getString(mColNumber);

		number = PhoneNumberHelpers.delete86String(number);
		number = PhoneNumberHelpers.removeNonNumbericChar(number);
		return number;
	}

}
