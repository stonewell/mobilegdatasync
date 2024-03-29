package com.angelstone.android.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public abstract class GenericContentProvider extends ContentProvider {

	public static class ContentProviderArg {
		public ContentProviderArg() {

		}

		public ContentProviderArg(String tableName, String contentType,
				String contentItemType, String sortOrder, String authority,
				String uri, String columnNullableName) {
			TableName = tableName;
			ContentType = contentType;
			ContentItemType = contentItemType;
			SortOrder = sortOrder;
			Authority = authority;
			LastPartUri = uri;
			ColumnNullableName = columnNullableName;
		}

		public String TableName;
		public String ContentType;
		public String ContentItemType;
		public String SortOrder;
		public String Authority;
		public String LastPartUri;
		public String ColumnNullableName;
	}

	private UriMatcher sUriMatcher = null;
	protected SQLiteOpenHelper mOpenHelper;
	private HashMap<Integer, ContentProviderArg> mContentCodeArgMap = new HashMap<Integer, GenericContentProvider.ContentProviderArg>();
	private HashMap<Integer, ContentProviderArg> mContentItemCodeArgMap = new HashMap<Integer, GenericContentProvider.ContentProviderArg>();

	protected void initialize(List<ContentProviderArg> args) {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		int code = 0;

		for (ContentProviderArg arg : args) {
			sUriMatcher.addURI(arg.Authority, arg.LastPartUri, code++);

			mContentCodeArgMap.put(code - 1, arg);

			if (arg.ContentItemType != null) {
				sUriMatcher.addURI(arg.Authority, arg.LastPartUri + "/#", code++);
				mContentItemCodeArgMap.put(code - 1, arg);
			}
		}
	}

	protected void initialize(String authority, List<Entity> entities) {
		List<ContentProviderArg> args = new ArrayList<ContentProviderArg>();

		Iterator<Entity> it = entities.iterator();

		while (it.hasNext()) {
			Entity entity = it.next();

			ContentProviderArg arg = new ContentProviderArg(
					entity.getTableName(), entity.getContentType(authority),
					entity.getContentItemType(authority),
					entity.getDefaultSortOrder(), authority, entity
							.getTableName(),
					entity.getNullableColumnName());

			args.add(arg);
		}

		initialize(args);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		int code = sUriMatcher.match(uri);

		if (mContentCodeArgMap.containsKey(code)) {
			count = db.delete(mContentCodeArgMap.get(code).TableName, where,
					whereArgs);
		} else if (mContentItemCodeArgMap.containsKey(code)) {
			String id = uri.getPathSegments().get(1);
			count = db.delete(mContentItemCodeArgMap.get(code).TableName,
					"_id ="
							+ id
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
		} else {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		int code = sUriMatcher.match(uri);
		if (mContentCodeArgMap.containsKey(code)) {
			return mContentCodeArgMap.get(code).ContentType;
		} else if (mContentItemCodeArgMap.containsKey(code)) {
			if (mContentItemCodeArgMap.get(code).ContentItemType != null)
				return mContentItemCodeArgMap.get(code).ContentItemType;
		}

		throw new IllegalArgumentException("Unknown URI " + uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// Validate the requested uri
		int match = sUriMatcher.match(uri);
		if (!mContentCodeArgMap.containsKey(match)) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(mContentCodeArgMap.get(match).TableName,
				mContentCodeArgMap.get(match).ColumnNullableName, values);

		if (rowId > 0) {
			Uri notifyUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(notifyUri, null);
			return notifyUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String orderBy = sortOrder;

		int code = sUriMatcher.match(uri);

		if (mContentCodeArgMap.containsKey(code)) {
			qb.setTables(mContentCodeArgMap.get(code).TableName);
			if (TextUtils.isEmpty(sortOrder))
				orderBy = mContentCodeArgMap.get(code).SortOrder;
		} else if (mContentItemCodeArgMap.containsKey(code)) {
			qb.setTables(mContentItemCodeArgMap.get(code).TableName);
			qb.appendWhere("_id" + "=" + uri.getPathSegments().get(1));
			if (TextUtils.isEmpty(sortOrder))
				orderBy = mContentItemCodeArgMap.get(code).SortOrder;
		} else {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);

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

		int code = sUriMatcher.match(uri);

		if (mContentCodeArgMap.containsKey(code)) {
			count = db.update(mContentCodeArgMap.get(code).TableName, values,
					where, whereArgs);
		} else if (mContentItemCodeArgMap.containsKey(code)) {
			String id = uri.getPathSegments().get(1);
			count = db.update(mContentItemCodeArgMap.get(code).TableName,
					values, "_id ="
							+ id
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
		} else {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
