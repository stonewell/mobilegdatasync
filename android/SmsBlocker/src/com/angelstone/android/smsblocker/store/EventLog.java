package com.angelstone.android.smsblocker.store;

import java.util.Date;

import android.net.Uri;

public class EventLog implements SmsBlockerContent {
	public static final Uri CONTENT_EVENT_LOG_URI = Uri.parse("content://"
			+ AUTHORITY + "/eventlog");
	public static final String _ID = "_id";
	public static final String TIME = "time";
	public static final String NUMBER = "number";
	public static final String SMS_TEXT = "sms_text";
	public static final String DEFAULT_SORT_ORDER = TIME + " desc";

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.angelstone.android.smsblocker.eventlog";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.angelstone.android.smsblocker.eventlog";

	public int compareTo(Object cmp_obj) {
		EventLog cmp = (EventLog) cmp_obj;
		return m_Time.compareTo(cmp.m_Time);
	}

	public EventLog(String Number) {
		m_Time = new Date();
		m_Number = Number;
	}

	public EventLog(String Number, long Time) {
		m_Time = new Date();
		m_Time.setTime(Time);
		m_Number = Number;
		m_SmsTxt = null;
	}

	public Date getTime() {
		return m_Time;
	}

	public String getNumber() {
		return m_Number;
	}

	public String getSmsTxt() {
		return m_SmsTxt;
	}

	public void setSmsTxt(String txt) {
		m_SmsTxt = txt;
	}

	private Date m_Time;
	private String m_Number;
	private String m_SmsTxt;
}
