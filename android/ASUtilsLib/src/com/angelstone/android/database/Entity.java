package com.angelstone.android.database;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public abstract class Entity {
	public static final String COL_ID = "_id";

	public abstract String getTableName();

	public Uri getContentUri(String authority) {
		return Uri.parse(new StringBuilder(512).append("content://")
				.append(authority).append("/").append(getTableName())
				.toString());
	}

	public String getContentType(String authority) {
		return new StringBuilder(512).append("vnd.android.cursor.dir/vnd.")
				.append(authority).append(".").append(getTableName())
				.toString();

	}

	public String getContentItemType(String authority) {
		return new StringBuilder(512).append("vnd.android.cursor.item/vnd.")
				.append(authority).append(".").append(getTableName())
				.toString();
	}
	
	public abstract String getDefaultSortOrder();
	
	public abstract String getNullableColumnName();
	
	public Map<String, String> getColumnTypeMap() {
		Map<String, String> mapping = new HashMap<String, String>();
		
		mapping.put(COL_ID, "INTEGER PRIMARY KEY AUTOINCREMENT");
		
		return mapping;
	}
	
	public String getCreateTableSQL() {
		StringBuilder sb = new StringBuilder(1024);
		
		sb.append("CREATE TABLE IF NOT EXISTS ")
				.append(getTableName())
				.append(" (");
		
		Map<String, String> mapping = getColumnTypeMap();
		Iterator<Entry<String,String>> it = mapping.entrySet().iterator();
		
		while(it.hasNext()) {
			Entry<String, String> entry = it.next();
			
			sb.append(entry.getKey()).append(" ").append(entry.getValue()).append(",");
		}
		
		if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		
		sb.append(");");

		return sb.toString();
	}

	public void initData(SQLiteDatabase db) {
	}
}
