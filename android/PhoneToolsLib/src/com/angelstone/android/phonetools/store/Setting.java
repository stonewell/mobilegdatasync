package com.angelstone.android.phonetools.store;

import java.util.Map;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.angelstone.android.database.Entity;

public class Setting extends Entity {

	public static final String COL_OPTION = "option";
	public static final String COL_VALUE = "value";
	public static final String DEFAULT_SORT_ORDER = COL_OPTION + " asc";

	public static final String TABLE_NAME = "setting";

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public String getDefaultSortOrder() {
		return DEFAULT_SORT_ORDER;
	}

	@Override
	public String getNullableColumnName() {
		return COL_VALUE;
	}

	@Override
	public Map<String, String> getColumnTypeMap() {
		Map<String, String> mapping = super.getColumnTypeMap();

		mapping.put(COL_OPTION, "VARCHAR");
		mapping.put(COL_VALUE, "VARCHAR");

		return mapping;
	}

	@Override
	public void initData(SQLiteDatabase db) {
		super.initData(db);

		try {
			db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COL_OPTION + ","
					+ COL_VALUE + ")" + " VALUES(?,?);", new String[] {
					PhoneToolsDatabaseValues.OPTION_ALLOW_CONTACTS, "1" });
		} catch (SQLException e) {

		}
	}
}
