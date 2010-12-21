package com.angelstone.android.callfirewall.store;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.angelstone.android.callfirewall.CallFireWallConstants;
import com.angelstone.android.phonetools.store.Setting;

public class CallFireWallSetting extends Setting {

	@Override
	public void initData(SQLiteDatabase db) {
		super.initData(db);

		try {
			db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COL_OPTION + ","
					+ COL_VALUE + ")" + " VALUES(?,?);", new String[] {
					CallFireWallConstants.OPTION_ENABLE, "1" });
		} catch (SQLException e) {

		}
	}

}
