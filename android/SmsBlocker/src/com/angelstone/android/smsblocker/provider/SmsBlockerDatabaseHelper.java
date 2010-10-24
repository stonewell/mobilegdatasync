package com.angelstone.android.smsblocker.provider;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.DatabaseValues;
import com.angelstone.android.smsblocker.store.EventLog;
import com.angelstone.android.smsblocker.store.Setting;
import com.angelstone.android.utils.ActivityLog;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsBlockerDatabaseHelper extends SQLiteOpenHelper implements
		DatabaseValues {

	private Context mContext = null;

	SmsBlockerDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ SETTING_TABLE
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, option VARCHAR, value VARCHAR);");

		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ EVENT_LOG_TABLE
				+ " (_id INTEGER PRIMARY KEY, time LONG, number VARCHAR, sms_text VARCHAR);");

		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ BLACK_LIST_TABLE
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, number VARCHAR, block_sms INTEGER);");

		try {
			db.execSQL("INSERT INTO " + SETTING_TABLE + "(" + Setting.OPTION + ","
					+ Setting.VALUE + ")" + " VALUES(?,?);", new String[] {
					OPTION_ALLOW_CONTACTS, "1" });
		} catch (SQLException e) {

		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		ActivityLog.logInfo(mContext, mContext.getString(R.string.app_name),
				"Upgrade Databae from Version " + oldVersion + " to Version "
						+ newVersion);
		if (oldVersion < DATABASE_VERSION) {
			upgrade_black_list(db);
			upgrade_settings(db);
			upgrade_eventlog(db);
		}
	}

	private static void upgrade_black_list(SQLiteDatabase db) {
		try {
			Cursor c = db.query(BLACK_LIST_TABLE, new String[] { "number" }, "1=0",
					null, null, null, null);
			c.close();

			// upgrade
			try {
				c = db.query(BLACK_LIST_TABLE, new String[] { "_id" }, "1=0", null,
						null, null, null);
				c.close();
			} catch (SQLException e) {
				// Do Upgrade
				db.beginTransaction();

				try {
					try {
						db.execSQL("DROP TABLE " + BLACK_LIST_TABLE + "_tmp" + ";");
					} catch (Throwable t) {

					}

					db.execSQL("CREATE TABLE "
							+ BLACK_LIST_TABLE
							+ "_tmp"
							+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, number VARCHAR, block_sms INTEGER);");

					db.execSQL("INSERT INTO " + BLACK_LIST_TABLE + "_tmp"
							+ "(number, block_sms) " + " SELECT number, block_sms FROM "
							+ BLACK_LIST_TABLE + ";");

					db.execSQL("DROP TABLE " + BLACK_LIST_TABLE + ";");

					db.execSQL("CREATE TABLE "
							+ BLACK_LIST_TABLE
							+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, number VARCHAR, block_sms INTEGER);");

					db.execSQL("INSERT INTO " + BLACK_LIST_TABLE + "(number, block_sms) "
							+ " SELECT number, block_sms FROM " + BLACK_LIST_TABLE + "_tmp"
							+ ";");

					db.execSQL("DROP TABLE " + BLACK_LIST_TABLE + "_tmp" + ";");

					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}
		} catch (SQLException e) {
			db.execSQL("CREATE TABLE IF NOT EXISTS "
					+ BLACK_LIST_TABLE
					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, number VARCHAR, block_sms INTEGER);");
		}
	}

	private static void upgrade_settings(SQLiteDatabase db) {
		try {
			Cursor c = db.query(SETTING_TABLE, new String[] { Setting.OPTION },
					"1=0", null, null, null, null);
			c.close();

			// upgrade
			try {
				c = db.query(SETTING_TABLE, new String[] { Setting._ID }, "1=0", null,
						null, null, null);
				c.close();

				try {
					db.execSQL("INSERT INTO " + SETTING_TABLE + "(" + Setting.OPTION
							+ "," + Setting.VALUE + ")" + " VALUES(?,?);", new String[] {
							OPTION_ALLOW_CONTACTS, "1" });
				} catch (SQLException e) {

				}
			} catch (SQLException e) {
				// Do Upgrade
				db.beginTransaction();

				try {
					try {
						db.execSQL("DROP TABLE " + SETTING_TABLE + "_tmp" + ";");
					} catch (Throwable t) {

					}

					db.execSQL("CREATE TABLE "
							+ SETTING_TABLE
							+ "_tmp"
							+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,option VARCHAR, value VARCHAR);");

					db.execSQL("INSERT INTO " + SETTING_TABLE + "_tmp"
							+ "(option, value) " + " SELECT option,value FROM "
							+ SETTING_TABLE + ";");

					db.execSQL("DROP TABLE " + SETTING_TABLE + ";");

					db.execSQL("CREATE TABLE "
							+ SETTING_TABLE
							+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,option VARCHAR, value VARCHAR);");

					db.execSQL("INSERT INTO " + SETTING_TABLE + "(option, value) "
							+ " SELECT option,value FROM " + SETTING_TABLE + "_tmp" + ";");

					db.execSQL("DROP TABLE " + SETTING_TABLE + "_tmp" + ";");

					try {
						db.execSQL("INSERT INTO " + SETTING_TABLE + "(" + Setting.OPTION
								+ "," + Setting.VALUE + ")" + " VALUES(?,?);", new String[] {
								OPTION_ALLOW_CONTACTS, "1" });
					} catch (SQLException ex) {

					}
					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}
		} catch (SQLException e) {
			db.execSQL("CREATE TABLE IF NOT EXISTS "
					+ SETTING_TABLE
					+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT,option VARCHAR, value VARCHAR);");
			try {
				db.execSQL("INSERT INTO " + SETTING_TABLE + "(" + Setting.OPTION + ","
						+ Setting.VALUE + ")" + " VALUES(?,?);", new String[] {
						OPTION_ALLOW_CONTACTS, "1" });
			} catch (SQLException ex) {

			}
		}
	}

	private static void upgrade_eventlog(SQLiteDatabase db) {
		try {
			Cursor c = db.query(EVENT_LOG_TABLE, new String[] { EventLog.NUMBER },
					"1=0", null, null, null, null);
			c.close();

			// upgrade
			try {
				c = db.query(SETTING_TABLE, new String[] { Setting._ID }, "1=0", null,
						null, null, null);
				c.close();
			} catch (SQLException e) {
				// Do Upgrade
				db.beginTransaction();

				try {
					try {
						db.execSQL("DROP TABLE " + SETTING_TABLE + "_tmp" + ";");
					} catch (Throwable t) {

					}

					db.execSQL("CREATE TABLE "
							+ EVENT_LOG_TABLE
							+ "_tmp"
							+ " (_id INTEGER PRIMARY KEY, time LONG, number VARCHAR, sms_text VARCHAR);");

					db.execSQL("INSERT INTO " + EVENT_LOG_TABLE + "_tmp"
							+ "(time, number, sms_text) "
							+ " SELECT time, number, sms_text FROM " + EVENT_LOG_TABLE + ";");

					db.execSQL("DROP TABLE " + EVENT_LOG_TABLE + ";");

					db.execSQL("CREATE TABLE "
							+ EVENT_LOG_TABLE
							+ " (_id INTEGER PRIMARY KEY, time LONG, number VARCHAR, sms_text VARCHAR);");

					db.execSQL("INSERT INTO " + EVENT_LOG_TABLE
							+ "(time, number, sms_text) "
							+ " SELECT time, number, sms_text FROM " + EVENT_LOG_TABLE
							+ "_tmp" + ";");

					db.execSQL("DROP TABLE " + EVENT_LOG_TABLE + "_tmp" + ";");

					db.setTransactionSuccessful();
				} finally {
					db.endTransaction();
				}
			}
		} catch (SQLException e) {
			db.execSQL("CREATE TABLE IF NOT EXISTS "
					+ EVENT_LOG_TABLE
					+ " (_id INTEGER PRIMARY KEY, time LONG, number VARCHAR, sms_text VARCHAR);");
		}
	}
}
