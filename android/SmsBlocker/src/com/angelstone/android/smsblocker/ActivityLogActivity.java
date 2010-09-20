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

import android.app.ListActivity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.angelstone.android.smsblocker.ActivityLog.DbEventField;

public class ActivityLogActivity extends ListActivity {
	
	SQLiteOpenHelper mDbHelper;
	SQLiteDatabase mDb;
	Cursor mLogCursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activitylog);	
	
		//Removed by Jun
		//ActivityLog.logInfo(this, "LiveUpdate", "Heeesss");

		mDbHelper = ActivityLog.getDBHelper(this);
		mDb = mDbHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(ActivityLog.EVENT_TABLE_NAME);
		// Get the database and run the query
		mLogCursor = qb.query(mDb, null,
				null,
				null, null, null, DbEventField.TIME + " DESC");

		setListAdapter(new EventLogAdapter(this, R.layout.activity_log_item, mLogCursor));
		
		/*
	     * add by xiongpeng 2010.04.21
	     */ 
	    getListView().setCacheColorHint(0);
	}

	@Override
	protected void onDestroy () {
		super.onDestroy();
		
		mLogCursor.close();
		mDb.close();
		mDbHelper.close();
	}

    public static String formatTimeStampString(Context context, long when, boolean fullFormat) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                           DateUtils.FORMAT_ABBREV_ALL |
                           DateUtils.FORMAT_CAP_AMPM;

        if (then.year != now.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            format_flags |= DateUtils.FORMAT_SHOW_DATE;
        } else {
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
        }

        if (fullFormat) {
            format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        }

        return DateUtils.formatDateTime(context, when, format_flags);
    }

	
	/**
	 * This class is used to represent the data for the activity log list box. The only 
	 * real work done by this class is to construct a custom view for the line
	 * items.
	 */
	public class EventLogAdapter extends ResourceCursorAdapter {

	    private int mTimeColumnId;
	    private int mTypeColumnId;
	    private int mModuleColumnId;
	    private int mDescriptionColumnId;

	    public EventLogAdapter(Context context, int layout, Cursor c) {
	        super(context, layout, c);
	        mTimeColumnId = c.getColumnIndexOrThrow(DbEventField.TIME);
	        mTypeColumnId = c.getColumnIndexOrThrow(DbEventField.TYPE);
	        mModuleColumnId = c.getColumnIndexOrThrow(DbEventField.MODULE);
	        mDescriptionColumnId = c.getColumnIndexOrThrow(DbEventField.DESCRIPTION);
	    }

	    @Override
	    public void bindView(View view, Context context, Cursor cursor) {
	        Resources r = context.getResources();

	        // Retrieve the icon for type
	        int type = cursor.getInt(mTypeColumnId);
	        int typeIconId;
	        ImageView iv = (ImageView) view.findViewById(R.id.event_type_icon);
	        if (type == ActivityLog.TYPE_INFO)
	        	typeIconId = R.drawable.info_icon;
	        else if (type == ActivityLog.TYPE_WARNING)
	        	typeIconId = R.drawable.warning_icon;
	        else
	        	typeIconId = R.drawable.error_icon;
            iv.setImageResource(typeIconId);

            long time = cursor.getLong(mTimeColumnId);
            TextView tv = (TextView) view.findViewById(R.id.event_time);
			tv.setText(formatTimeStampString(ActivityLogActivity.this,time,false));

	        tv = (TextView) view.findViewById(R.id.event_module);
	        String module = cursor.getString(mModuleColumnId);
	        tv.setText(module);

	        tv = (TextView) view.findViewById(R.id.event_description);
	        tv.setText(cursor.getString(mDescriptionColumnId));
	    }
	}

}
