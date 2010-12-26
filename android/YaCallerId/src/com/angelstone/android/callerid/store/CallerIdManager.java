package com.angelstone.android.callerid.store;

import android.content.ContentUris;
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

	private String mAuthority = null;
	private Uri mContentUri = null;

	public CallerIdManager(String authority) {
		mAuthority = authority;
		mContentUri = new CallerId().getContentUri(mAuthority);
	}

	public Cursor getCallerIds(Context context) {
		return context.getContentResolver()
				.query(
						mContentUri,
						new String[] { CallerId.COL_ID, CallerId.COL_NUMBER,
								CallerId.COL_DATA }, null, null, null);
	}

	public Uri addCallerId(Context context, String number, byte[] data) {
		Cursor cur = null;

		try {
			String where_str = CallerId.COL_NUMBER + "=?";

			cur = context.getContentResolver().query(mContentUri,
					new String[] { CallerId.COL_ID }, where_str, new String[] { number },
					null);

			if (cur != null && cur.getCount() != 0) {
				return null;
			} // number already existed

			ContentValues args = new ContentValues();
			args.put(CallerId.COL_NUMBER, number);

			if (data != null)
				args.put(CallerId.COL_DATA, data);

			return context.getContentResolver().insert(mContentUri, args);

		} catch (Exception e) {
			ActivityLog.logError(context, context.getString(R.string.app_name),
					e.getLocalizedMessage());
			Log.e(context.getString(R.string.app_name), "addCallerId", e);
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

		return null;
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

	public int updateCallerId(Context context, long id, String number, byte[] data) {
		ContentValues args = new ContentValues();
		args.put(CallerId.COL_DATA, data);
		args.put(CallerId.COL_NUMBER, number);

		Uri uri = ContentUris.withAppendedId(mContentUri, id);

		return context.getContentResolver().update(uri, args, null, null);
	}

	public Uri getContentUri() {
		return mContentUri;
	}

	public void setContentUri(Uri contentUri) {
		mContentUri = contentUri;
	}

	public void deleteCallerId(Context context, long id) {
		Uri uri = ContentUris.withAppendedId(mContentUri, id);

		context.getContentResolver().delete(uri, null, null);
	}

	public Cursor getCallerId(Context context, long id) {
		Uri uri = ContentUris.withAppendedId(mContentUri, id);

		return context.getContentResolver().query(uri, null, null, null, null);
	}

	public long findOtherCaller(Context context, String number,
			long id) {
		Cursor c =
			context.getContentResolver().query(mContentUri, 
					new String[] {CallerId.COL_ID}, 
					CallerId.COL_NUMBER + "=? AND " + CallerId.COL_ID + " != ?", 
					new String[] {number, String.valueOf(id)}, null);
		
		if (c.moveToFirst())
			return c.getLong(0);
		return -1;
	}
}
