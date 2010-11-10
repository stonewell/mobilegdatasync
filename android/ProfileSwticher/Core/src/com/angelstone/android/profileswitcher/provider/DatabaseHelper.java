package com.angelstone.android.profileswitcher.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.angelstone.android.profileswitcher.store.DatabaseValues;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.profileswitcher.store.Schedule;

public class DatabaseHelper extends SQLiteOpenHelper implements DatabaseValues {

	private Context mContext = null;

	DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + Schedule.TABLE_NAME + " ("
				+ Schedule.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Schedule.COLUMN_PROFILE_ID + " INTEGER, "
				+ Schedule.COLUMN_START_TIME + " LONG, "
				+ Schedule.COLUMN_REPEAT_WEEKDAYS + " INTEGER);");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + Profile.TABLE_NAME + " ("
				+ Profile.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Profile.COLUMN_NAME + " VARCHAR, " + Profile.COLUMN_FLAGS
				+ " INTEGER);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
