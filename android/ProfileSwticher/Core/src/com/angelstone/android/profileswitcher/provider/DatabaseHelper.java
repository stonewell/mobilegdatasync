package com.angelstone.android.profileswitcher.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.angelstone.android.profileswitcher.store.DatabaseValues;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.profileswitcher.store.Schedule;

public class DatabaseHelper extends SQLiteOpenHelper implements DatabaseValues {

	DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + Schedule.TABLE_NAME + " ("
				+ Schedule.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Schedule.COLUMN_PROFILE_ID + " INTEGER, "
				+ Schedule.COLUMN_START_TIME + " LONG, "
				+ Schedule.COLUMN_LABEL + " VARCHAR, "
				+ Schedule.COLUMN_LOCATION + " VARCHAR, "
				+ Schedule.COLUMN_ENABLE + " INTEGER DEFAULT 1, "
				+ Schedule.COLUMN_REPEAT_WEEKDAYS + " INTEGER);");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + Profile.TABLE_NAME + " ("
				+ Profile.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Profile.COLUMN_EMAIL_VOLUME + " INTEGER,"
				+ Profile.COLUMN_PHONE_VOLUME + " INTEGER,"
				+ Profile.COLUMN_NOTIFY_VOLUME + " INTEGER,"
				+ Profile.COLUMN_ALARM_VOLUME + " INTEGER,"
				+ Profile.COLUMN_PHONE_RING_TONE + " VARCHAR,"
				+ Profile.COLUMN_NOTIFY_RING_TONE + " VARCHAR,"
				+ Profile.COLUMN_ALARM_RING_TONE + " VARCHAR,"
				+ Profile.COLUMN_EMAIL_RING_TONE + " VARCHAR,"
				+ Profile.COLUMN_ACTIVE + " INTEGER," + Profile.COLUMN_DEVICES
				+ " INTEGER," + Profile.COLUMN_NAME + " VARCHAR, "
				+ Profile.COLUMN_FLAGS + " INTEGER);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
			upgradeToVersion2(db);
			oldVersion = 2;
		}

		if (oldVersion < 3) {
			upgradeToVersion3(db);
			oldVersion = 3;
		}
		
		if (oldVersion < 4) {
			upgradeToVersion4(db);
			oldVersion = 4;
		}
		
		if (oldVersion < 5) {
			upgradeToVersion5(db);
			oldVersion = 5;
		}
	}

	private void upgradeToVersion5(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + Schedule.TABLE_NAME + " ADD "
				+ Schedule.COLUMN_ENABLE + " INTEGER DEFAULT 1");
	}

	private void upgradeToVersion4(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + Schedule.TABLE_NAME + " ADD "
				+ Schedule.COLUMN_LABEL + " VARCHAR DEFAULT NULL");
	}

	private void upgradeToVersion3(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + Schedule.TABLE_NAME + " ADD "
				+ Schedule.COLUMN_LOCATION + " VARCHAR DEFAULT NULL");
	}

	private void upgradeToVersion2(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE " + Profile.TABLE_NAME + " ADD "
				+ Profile.COLUMN_EXPIRE_TIME + " INTEGER DEFAULT 0");
		db.execSQL("ALTER TABLE " + Profile.TABLE_NAME + " ADD "
				+ Profile.COLUMN_ACTIVATE_TIME + " INTEGER DEFAULT 0");
	}
}
