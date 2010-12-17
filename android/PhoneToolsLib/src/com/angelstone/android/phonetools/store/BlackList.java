package com.angelstone.android.phonetools.store;

import java.util.Map;

import com.angelstone.android.database.Entity;

public class BlackList extends Entity{

	public static final String TABLE_NAME = "black_list";

	public static final String COL_NUMBER = "number";
	public static final String COL_BLOCK = "block";
	public static final String DEFAULT_SORT_ORDER = COL_BLOCK + " asc,"
			+ COL_NUMBER + " asc";

	@Override
	public String getTableName() {
		return TABLE_NAME;
	}

	@Override
	public String getDefaultSortOrder() {
		return DEFAULT_SORT_ORDER;
	}

	@Override
	public Map<String, String> getColumnTypeMap() {
		Map<String, String> mapping = super.getColumnTypeMap();
		
		mapping.put(COL_NUMBER, "VARCHAR");
		mapping.put(COL_BLOCK, "INTEGER");
		
		return mapping;
	}

	@Override
	public String getNullableColumnName() {
		return COL_NUMBER;
	}
}
