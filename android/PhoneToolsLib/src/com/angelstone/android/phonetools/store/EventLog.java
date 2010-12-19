package com.angelstone.android.phonetools.store;

import java.util.Date;
import java.util.Map;

import com.angelstone.android.database.Entity;

public class EventLog extends Entity implements Comparable<EventLog> {

	public static final String COL_TIME = "time";
	public static final String COL_NUMBER = "number";
	public static final String COL_CONTENT = "content";
	public static final String DEFAULT_SORT_ORDER = COL_TIME + " desc";

	public static final String TABLE_NAME = "event_logs";

	private Date mTime;
	private String mNumber;
	private String mContent;

	public int compareTo(EventLog cmp_obj) {
		EventLog cmp = cmp_obj;
		return mTime.compareTo(cmp.mTime);
	}

	public EventLog() {
		mTime = new Date();
		mNumber = "";
	}

	public EventLog(String Number) {
		mTime = new Date();
		mNumber = Number;
	}

	public EventLog(String Number, long Time) {
		mTime = new Date();
		mTime.setTime(Time);
		mNumber = Number;
		mContent = null;
	}

	public Date getTime() {
		return mTime;
	}

	public String getNumber() {
		return mNumber;
	}

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
		return COL_CONTENT;
	}

	@Override
	public Map<String, String> getColumnTypeMap() {
		Map<String, String> mapping = super.getColumnTypeMap();
		
		mapping.put(COL_TIME,"LONG");
		mapping.put(COL_NUMBER,"VARCHAR");
		mapping.put(COL_CONTENT,"VARCHAR");
		
		return mapping;
	}

	public String getContent() {
		return mContent;
	}

	public void setContent(String content) {
		mContent = content;
	}	
}
