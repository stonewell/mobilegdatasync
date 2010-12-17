package com.angelstone.android.phonetools.ui;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.utils.PhoneNumberHelpers;
import com.google.android.mms.pdu.CharacterSets;
import com.google.android.mms.pdu.EncodedStringValue;

public class SmsRecordViewAdapter extends ResourceCursorAdapter implements
		ContentListViewAdapter {
	private static final String COL_MESSAGE_COUNT = "message_count";
	private static final String COL_SNIPPET_CS = "snippet_cs";
	private static final String COL_SNIPPET = "snippet";
	private static final String COL_RECIPIENT_IDS = "recipient_ids";

	private static final Uri RECEIPT_URI = Uri
			.parse("content://mms-sms/canonical-addresses");

	private int mColMessageCount = 0;
	private int mColSnippetCS = 0;
	private int mColSnippet = 0;
	private int mColRecipientIds = 0;

	private Map<String, Integer> mCheckStates = null;

	private static Map<String, String> mRecipientIds = new HashMap<String, String>();

	public SmsRecordViewAdapter(Context context, Cursor c,
			Map<String, Integer> checkStates) {
		super(context, R.layout.sms_record_list_row, c, false);

		mCheckStates = checkStates;
		mColMessageCount = c.getColumnIndex(COL_MESSAGE_COUNT);
		mColSnippetCS = c.getColumnIndex(COL_SNIPPET_CS);
		mColSnippet = c.getColumnIndex(COL_SNIPPET);
		mColRecipientIds = c.getColumnIndex(COL_RECIPIENT_IDS);

		if (mRecipientIds.size() == 0)
			fill(context);
	}

	private void fill(Context context) {
		Cursor c = context.getContentResolver().query(RECEIPT_URI, null, null,
				null, null);

		int id = c.getColumnIndex("_id");
		int address = c.getColumnIndex("address");

		while (c.moveToNext()) {
			mRecipientIds.put(c.getString(id), c.getString(address));
		}
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ImageView img = (ImageView) view.findViewById(R.id.sms_log_check_img);
		TextView vNumber = (TextView) view.findViewById(R.id.sms_record_number);
		TextView vName = (TextView) view.findViewById(R.id.sms_record_name);
		TextView vBody = (TextView) view.findViewById(R.id.sms_record_body);

		String number = getRecipientNumber(context, cursor);

		number = PhoneNumberHelpers.delete86String(number);
		number = PhoneNumberHelpers.removeNonNumbericChar(number);

		vNumber.setText(number);

		String name = PhoneNumberHelpers.getContactName(context, number);
		if (!TextUtils.isEmpty(name)) {
			vName.setText(" <" + name + "> ("
					+ cursor.getString(mColMessageCount) + ")");
		} else {
			vName.setText("(" + cursor.getString(mColMessageCount) + ")");
		}

		String body = extractEncStrFromCursor(cursor, mColSnippet,
				mColSnippetCS);

		vBody.setText(body);

		if (mCheckStates.containsKey(number)
				&& mCheckStates.get(number) == UIConstants.CHECK_ON) {
			img.setImageResource(R.drawable.btn_check_on);
		} else {
			img.setImageResource(R.drawable.btn_check_off);
		}

	}

	private String getRecipientNumber(Context context, Cursor cursor) {
		String ids = cursor.getString(mColRecipientIds);

		if (TextUtils.isEmpty(ids)) {
			return "";
		}

		ids = ids.trim();

		int pos = ids.indexOf(" ");

		if (pos >= 0)
			ids = ids.substring(0, pos);

		if (mRecipientIds.containsKey(ids)) {
			return mRecipientIds.get(ids);
		}

		fill(context);

		if (mRecipientIds.containsKey(ids)) {
			return mRecipientIds.get(ids);
		}

		return "";
	}

	public String getNumber(Context context, int position) {
		Cursor c = (Cursor) getItem(position);
		String number = getRecipientNumber(context, c);

		number = PhoneNumberHelpers.delete86String(number);
		number = PhoneNumberHelpers.removeNonNumbericChar(number);
		return number;
	}

	public static String extractEncStrFromCursor(Cursor cursor,
			int columnRawBytes, int columnCharset) {
		String rawBytes = cursor.getString(columnRawBytes);
		int charset = cursor.getInt(columnCharset);

		if (TextUtils.isEmpty(rawBytes)) {
			return "";
		} else if (charset == CharacterSets.ANY_CHARSET) {
			return rawBytes;
		} else {
			return new EncodedStringValue(charset, getBytes(rawBytes))
					.getString();
		}
	}

	private static byte[] getBytes(String data) {
		try {
			return data.getBytes(CharacterSets.MIMENAME_ISO_8859_1);
		} catch (UnsupportedEncodingException e) {
			// Impossible to reach here!
			Log.e("MMS", "ISO_8859_1 must be supported!", e);
			return new byte[0];
		}
	}
}
