/* 
   Copyright (C) 2010 Symantec Corporate. All rights reserved.

   This document contains proprietary information and trade secrets of
   Symantec Corporate.

   This information is provided only under a written agreement containing
   restrictions on the use and disclosure of this information and
   is also protected by law.

   Any unauthorized review, use, disclosure, or distribution is prohibited.
*/

package com.angelstone.android.smsblocker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.content.res.Resources;
import android.util.Log;

public class ActivityLog {
	static public final int TYPE_INFO = 1;
	static public final int TYPE_WARNING = 2;
	static public final int TYPE_ERROR = 3;

	/*
	 * "module" must be full module name.
	*/
	static public void logInfo(Context context, String module, String description) {
		log(context, TYPE_INFO, module, description);
	}

	static public void logWarning(Context context, String module, String description) {
		log(context, TYPE_WARNING, module, description);
	}

	static public void logError(Context context, String module, String description) {
		log(context, TYPE_ERROR, module, description);
	}

	static public void logInfo(Context context, int module, int description) {
		log(context, TYPE_INFO, getString(context,module), getString(context,description));
	}

	static public void logWarning(Context context, int module, int description) {
		log(context, TYPE_WARNING, getString(context,module), getString(context,description));
	}

	static public void logError(Context context, int module, int description) {
		log(context, TYPE_ERROR, getString(context,module), getString(context,description));
	}

	
	static public SQLiteOpenHelper getDBHelper(Context context) {
		return new DatabaseHelper(context);
	}
	
	static private String getString(Context c,int id){
	 	Resources r = c.getResources();
	 	return r.getString(id);
	}

	static private void log(Context context, int type, String module, String description) {
		DatabaseHelper openHelper;

		openHelper = new DatabaseHelper(context);

		ContentValues values;
        values = new ContentValues();

        values.put(DbEventField.TIME, Long.valueOf(System.currentTimeMillis()));
        values.put(DbEventField.TYPE, type);
        values.put(DbEventField.UPLOADED, false);
        values.put(DbEventField.MODULE, module);
        values.put(DbEventField.DESCRIPTION, description);

        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.insert(EVENT_TABLE_NAME, null, values);

        limitLogSize(openHelper);

         //db.close();
         openHelper.close();
	}

	static private int		mCountItems = 0;
	static private final int MAX_ITEMS_COUNT = 5000;
	static private final int ITEMS_COUNT_TO_KEEP = MAX_ITEMS_COUNT * 90 / 100;

	static private synchronized void limitLogSize(DatabaseHelper openHelper) {
		if (mCountItems == 0)
			mCountItems = getLogItemsCount(openHelper);
		else
			mCountItems++;

        if (mCountItems >= MAX_ITEMS_COUNT)
        {
        	removeExtraLogItems(openHelper);
        	mCountItems = 0;
        }
	}

	static private int getLogItemsCount(DatabaseHelper openHelper) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(EVENT_TABLE_NAME);

        // Get the database and run the query
        SQLiteDatabase db = openHelper.getReadableDatabase();
    	String	projection[] = {"COUNT (*) as count_events" };
    	Cursor c = qb.query(db, projection,
    		null,
    		null, null, null, null);

        int count = 0;
        if (c != null) {
        	c.moveToFirst();
        	if (!c.isAfterLast())
        		count = c.getInt(0);
        	c.close();
        }

        return count;
	}
	
	static private void removeExtraLogItems(DatabaseHelper openHelper) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(EVENT_TABLE_NAME);
        
        String sqlDelete = "DELETE FROM " + EVENT_TABLE_NAME + " WHERE " + DbEventField._ID
        	+ " IN (SELECT " + DbEventField._ID + " FROM " + EVENT_TABLE_NAME + " ORDER BY " + DbEventField._ID
        	+ " ASC LIMIT " + Integer.toString(mCountItems - ITEMS_COUNT_TO_KEEP) + " )";

        SQLiteDatabase db = openHelper.getWritableDatabase();
        db.execSQL(sqlDelete);
        db.close();
	}
	
	public class DbEventField {
		public static final String		_ID = "_id";
		public static final String		TIME = "time";
		public static final String		TYPE = "type";
		public static final String		UPLOADED = "uploaded";
		public static final String		MODULE = "module";
		public static final String		DESCRIPTION = "description";
	}

    private static final String DATABASE_NAME = "activitylog.db";
    private static final int DATABASE_VERSION = 1;
    public static final String EVENT_TABLE_NAME = "events";

    private static final String DBG_LOG_TAG = "ActivityLog";
    
	/**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + EVENT_TABLE_NAME + " ("
                    + DbEventField._ID + " INTEGER PRIMARY KEY,"
                    + DbEventField.TIME + " INTEGER,"
                    + DbEventField.TYPE + " INTEGER,"
                    + DbEventField.UPLOADED + " INTEGER,"
                    + DbEventField.MODULE + " TEXT,"
                    + DbEventField.DESCRIPTION + " TEXT"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DBG_LOG_TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE_NAME);
            onCreate(db);
        }
    }

}
