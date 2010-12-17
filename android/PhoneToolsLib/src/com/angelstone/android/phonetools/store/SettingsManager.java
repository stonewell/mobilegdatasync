package com.angelstone.android.phonetools.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SettingsManager {
	private String mAuthority = null;
	private Uri mContentUri = null;

	public SettingsManager(String authority) {
		mAuthority = authority;
		mContentUri = new Setting().getContentUri(mAuthority);
	}

	public boolean writeSettingString(Context context, String option_name,
			String option_value) {
		Cursor cur = null;

		try {
			String where_str = Setting.COL_OPTION + "=?";
			cur = context.getContentResolver().query(mContentUri,
					new String[] { Setting.COL_ID }, where_str,
					new String[] { option_name }, null);

			if (cur == null) {
				return false;
			}

			ContentValues args = new ContentValues();

			if (cur.getCount() == 0) {
				args.put(Setting.COL_OPTION, option_name);
				args.put(Setting.COL_VALUE, option_value);
				context.getContentResolver().insert(mContentUri, args);
			} else {
				args.put(Setting.COL_VALUE, option_value);
				context.getContentResolver().update(mContentUri, args,
						where_str, new String[] { option_name });

			}
		} catch (Exception e) {
			return false;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

		return true;
	}

	public boolean writeSetting(Context context, String option_name,
			boolean option_value) {
		return writeSettingString(context, option_name, option_value ? "1"
				: "0");
	}

	public String readSettingString(Context context, String option_name) {
		Cursor cur = null;

		try {
			String where_str = Setting.COL_OPTION + "=?";
			cur = context.getContentResolver().query(mContentUri,
					new String[] { Setting.COL_VALUE }, where_str,
					new String[] { option_name }, null);

			if (cur == null) {
				return null;
			}

			if (!cur.moveToFirst())
				return null;

			int idx = cur.getColumnIndex(Setting.COL_VALUE);

			String value = cur.getString(idx);

			return value;
		} catch (Exception e) {
			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return null;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public boolean readSetting(Context context, String option_name) {
		return "1".equals(readSettingString(context, option_name));
	}

}
