package com.angelstone.android.callerid.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.angelstone.android.callerid.R;
import com.angelstone.android.utils.ActivityLog;

public class CallerIdManager {
	public static final int INSERT_ERROR_NONE = 0;
	public static final int INSERT_ERROR_AREADY_EXIST = 2;
	public static final int INSERT_ERROR_EXCEPTION_OCCUR = -1;

	public static enum BlockListAction {
		NO_NUMBER, BLOCK, NOT_BLOCK,
	};

	private String mAuthority = null;
	private Uri mContentUri = null;

	public CallerIdManager(String authority) {
		mAuthority = authority;
		mContentUri = new CallerId().getContentUri(mAuthority);
	}

	public Cursor getCallerIds(Context context) {
		return context.getContentResolver().query(
				mContentUri,
				new String[] { CallerId.COL_ID, CallerId.COL_NUMBER,
						CallerId.COL_DATA }, null, null, null);
	}

	public int addCallerId(Context context, String number, byte[] data) {
		int err = INSERT_ERROR_NONE;
		Cursor cur = null;

		try {
			String where_str = CallerId.COL_NUMBER + "=?";

			cur = context.getContentResolver().query(mContentUri,
					new String[] { CallerId.COL_ID }, where_str,
					new String[] { number }, null);

			if (cur != null && cur.getCount() != 0) {
				return INSERT_ERROR_AREADY_EXIST;
			} // number already existed

			ContentValues args = new ContentValues();
			args.put(CallerId.COL_NUMBER, number);
			args.put(CallerId.COL_DATA, data);
			context.getContentResolver().insert(mContentUri, args);

		} catch (Exception e) {
			ActivityLog.logError(context, context.getString(R.string.app_name), e.getLocalizedMessage());
			Log.e(context.getString(R.string.app_name), "addCallerId", e);
			err = INSERT_ERROR_EXCEPTION_OCCUR;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

		return err;
	}

	public int clearCallerIds(Context context) {
		int err = 0;

		context.getContentResolver().delete(mContentUri, null, null);

		return err;
	}

	public int deleteCallerId(Context context, String number) {
		int err = 0;

		String where_str = CallerId.COL_NUMBER + "=?";

		context.getContentResolver().delete(mContentUri, where_str,
				new String[] { number });

		return err;
	}

	public int updateCallerId(Context context, String number,
			byte[] data) {
		return updateCallerId(context, number, number, data);
	}

	public int updateCallerId(Context context, String oldNumber,
			String number, byte[] data) {
		int err = 0;
		ContentValues args = new ContentValues();
		args.put(CallerId.COL_DATA, data);
		if (!oldNumber.equals(number)) {
			args.put(CallerId.COL_NUMBER, number);
		}
		context.getContentResolver().update(mContentUri, args,
				CallerId.COL_NUMBER + "=?", new String[] { oldNumber });
		return err;
	}

	public Uri getContentUri() {
		return mContentUri;
	}

	public void setContentUri(Uri contentUri) {
		mContentUri = contentUri;
	}
}
