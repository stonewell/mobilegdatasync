package com.angelstone.android.phonetools.store;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class EventLogManager {
	private String mAuthority = null;
	private Uri mContentUri = null;

	public EventLogManager(String authority) {
		mAuthority = authority;
		mContentUri = new EventLog("").getContentUri(mAuthority);
	}

	public boolean writeLog(Context context, EventLog log) {
		ContentValues args = new ContentValues();
		args.put(EventLog.COL_TIME, Long.valueOf(System.currentTimeMillis()));
		args.put(EventLog.COL_NUMBER, log.getNumber());
		args.put(EventLog.COL_CONTENT, log.getContent());
		context.getContentResolver().insert(mContentUri, args);
		return true;
	}

	public Cursor getEventLogs(Context context) {
		return getEventLogs(context, null, null);
	}

	public Cursor getEventLogs(Context context, String where,
			String[] where_args) {
		return context.getContentResolver().query(
				mContentUri,
				new String[] { EventLog.COL_ID, EventLog.COL_TIME,
						EventLog.COL_NUMBER, EventLog.COL_CONTENT }, where,
				where_args, null);
	}

	public Cursor getEventLog(Context context, int id) {
		Uri queryUri = ContentUris.withAppendedId(mContentUri, id);

		return context.getContentResolver().query(
				queryUri,
				new String[] { EventLog.COL_ID, EventLog.COL_TIME,
						EventLog.COL_NUMBER, EventLog.COL_CONTENT }, null, null,
				null);
	}

	public boolean deleteLog(Context context, int _id) {
		Uri deleteUri = ContentUris.withAppendedId(mContentUri, _id);

		context.getContentResolver().delete(deleteUri, null, null);
		return true;
	}

	public void deleteLogs(Context context) {
		deleteLogs(context, null, null);
	}

	public void deleteLogs(Context context, String where, String[] where_args) {
		context.getContentResolver().delete(mContentUri, where, where_args);
	}
}
