package com.angelstone.android.callerid.store;

import java.util.Map;

import com.angelstone.android.database.Entity;

public class CallerId extends Entity {
	public static final String TABLE_NAME = "callers";

	public static final String COL_NUMBER = "number";
	public static final String COL_DATA = "_data";
	public static final String DEFAULT_SORT_ORDER = COL_NUMBER + " asc";

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
		return COL_DATA;
	}

	@Override
	public Map<String, String> getColumnTypeMap() {
		Map<String, String> mapping = super.getColumnTypeMap();

		mapping.put(COL_NUMBER, "VARCHAR");
		mapping.put(COL_DATA, "BLOB");

		return mapping;
	}
}
