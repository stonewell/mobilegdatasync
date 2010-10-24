package com.angelstone.android.smsblocker.store;

public interface DatabaseValues {
	public static final String DATABASE_NAME = "smsblocker.db";
	public static final int DATABASE_VERSION = 3;

	public static final String SETTING_TABLE = "setting";
	public static final String EVENT_LOG_TABLE = "event_logs";
	public static final String BLACK_LIST_TABLE = "black_list";

	public static final String OPTION_ALLOW_CONTACTS = "sms_allow_contacts";
}
