package com.angelstone.android.smsblocker.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.angelstone.android.smsblocker.store.BlackList;
import com.angelstone.android.smsblocker.store.DatabaseValues;
import com.angelstone.android.smsblocker.store.EventLog;
import com.angelstone.android.smsblocker.store.Setting;
import com.angelstone.android.smsblocker.store.SmsBlockerContent;

public class SmsBlockerProvider extends ContentProvider implements
		DatabaseValues {
	private static final int BLACK_LIST = 1;
	private static final int BLACK_LIST_ID = 2;
	private static final int EVENT_LOG = 3;
	private static final int EVENT_LOG_ID = 4;
	private static final int SETTINGS = 5;
	private static final int SETTINGS_ID = 6;

	private static UriMatcher sUriMatcher = null;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(SmsBlockerContent.AUTHORITY, "blacklist", BLACK_LIST);
		sUriMatcher.addURI(SmsBlockerContent.AUTHORITY, "blacklist/#",
				BLACK_LIST_ID);
		sUriMatcher.addURI(SmsBlockerContent.AUTHORITY, "eventlog", EVENT_LOG);
		sUriMatcher.addURI(SmsBlockerContent.AUTHORITY, "eventlog/#", EVENT_LOG_ID);
		sUriMatcher.addURI(SmsBlockerContent.AUTHORITY, "settings", SETTINGS);
		sUriMatcher.addURI(SmsBlockerContent.AUTHORITY, "settings/#", SETTINGS_ID);
	}

	private SmsBlockerDatabaseHelper mOpenHelper;

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case BLACK_LIST:
			count = db.delete(BLACK_LIST_TABLE, where, whereArgs);
			break;

		case BLACK_LIST_ID: {
			String id = uri.getPathSegments().get(1);
			count = db.delete(BLACK_LIST_TABLE, BlackList._ID + "=" + id
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
					whereArgs);
			break;
		}
		case EVENT_LOG:
			count = db.delete(EVENT_LOG_TABLE, where, whereArgs);
			break;

		case EVENT_LOG_ID: {
			String id = uri.getPathSegments().get(1);
			count = db.delete(EVENT_LOG_TABLE,
					EventLog._ID + "=" + id
							+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
					whereArgs);
			break;
		}
		case SETTINGS: {
			count = db.delete(SETTING_TABLE, where, whereArgs);
		}
		case SETTINGS_ID: {
			String id = uri.getPathSegments().get(1);
			count = db.delete(SETTING_TABLE,
					Setting._ID + "=" + id
							+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
					whereArgs);
			break;
		}
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case BLACK_LIST:
			return BlackList.CONTENT_TYPE;
		case BLACK_LIST_ID:
			return BlackList.CONTENT_ITEM_TYPE;
		case EVENT_LOG:
			return EventLog.CONTENT_TYPE;
		case EVENT_LOG_ID:
			return EventLog.CONTENT_ITEM_TYPE;
		case SETTINGS:
			return Setting.CONTENT_TYPE;
		case SETTINGS_ID:
			return Setting.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Validate the requested uri
		int match = sUriMatcher.match(uri);
		if (match != BLACK_LIST && match != EVENT_LOG && match != SETTINGS) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = 0;

		if (match == BLACK_LIST)
			rowId = db.insert(BLACK_LIST_TABLE, BlackList.NUMBER, values);
		else if (match == EVENT_LOG)
			rowId = db.insert(EVENT_LOG_TABLE, EventLog.NUMBER, values);
		else
			rowId = db.insert(SETTING_TABLE, Setting.OPTION, values);

		if (rowId > 0) {
			Uri notifyUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(notifyUri, null);
			return notifyUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new SmsBlockerDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String orderBy = sortOrder;
		switch (sUriMatcher.match(uri)) {
		case BLACK_LIST:
			qb.setTables(BLACK_LIST_TABLE);
			if (TextUtils.isEmpty(sortOrder))
				orderBy = BlackList.DEFAULT_SORT_ORDER;
			break;

		case BLACK_LIST_ID:
			qb.setTables(BLACK_LIST_TABLE);
			qb.appendWhere(BlackList._ID + "=" + uri.getPathSegments().get(1));
			if (TextUtils.isEmpty(sortOrder))
				orderBy = BlackList.DEFAULT_SORT_ORDER;
			break;

		case EVENT_LOG:
			qb.setTables(EVENT_LOG_TABLE);
			if (TextUtils.isEmpty(sortOrder))
				orderBy = EventLog.DEFAULT_SORT_ORDER;
			break;

		case EVENT_LOG_ID:
			qb.setTables(EVENT_LOG_TABLE);
			qb.appendWhere(EventLog._ID + "=" + uri.getPathSegments().get(1));
			if (TextUtils.isEmpty(sortOrder))
				orderBy = EventLog.DEFAULT_SORT_ORDER;
			break;

		case SETTINGS:
			qb.setTables(SETTING_TABLE);
			if (TextUtils.isEmpty(sortOrder))
				orderBy = Setting.DEFAULT_SORT_ORDER;
			break;

		case SETTINGS_ID:
			qb.setTables(SETTING_TABLE);
			qb.appendWhere(Setting._ID + "=" + uri.getPathSegments().get(1));
			if (TextUtils.isEmpty(sortOrder))
				orderBy = Setting.DEFAULT_SORT_ORDER;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null,
				orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case BLACK_LIST:
			count = db.update(BLACK_LIST_TABLE, values, where, whereArgs);
			break;

		case BLACK_LIST_ID: {
			String id = uri.getPathSegments().get(1);
			count = db.update(BLACK_LIST_TABLE, values, BlackList._ID + "=" + id
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
					whereArgs);
			break;
		}
		case EVENT_LOG:
			count = db.update(EVENT_LOG_TABLE, values, where, whereArgs);
			break;

		case EVENT_LOG_ID: {
			String id = uri.getPathSegments().get(1);
			count = db.update(EVENT_LOG_TABLE, values, EventLog._ID + "=" + id
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
					whereArgs);
			break;
		}
		case SETTINGS:
			count = db.update(SETTING_TABLE, values, where, whereArgs);
			break;

		case SETTINGS_ID: {
			String id = uri.getPathSegments().get(1);
			count = db.update(SETTING_TABLE, values, Setting._ID + "=" + id
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""),
					whereArgs);
			break;
		}
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
