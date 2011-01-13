package com.angelstone.android.profileswitcher;

public interface ProfileSwitcherConstants {
	public static final String TAG = "ProfileSwitcher";
	public static final String EXTRA_ID = "data_id";
	
	public static final String ACTION_ALARM_ALERT = "com.angelstone.android.alarm.SCHEDULE";
	public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";
	public static final String ACTION_SET_PROFILE = "com.angelstone.android.alarm.PROFILE_SET";
	public static final int MSG_WHAT_LOAD_LOCATION_ONLY_SCHEDULES = 1000;
	public static final int MSG_WHAT_LOCATION_CHANGED = 1001;
	public static final int MSG_WHAT_LOAD_LAST_KNOWN_PROFILE = 1002;
	public static final int PROFILE_ENABLE_NOTIFICATION = 1;
	public static final String DATA_NOTIFY = "exta_data_notify";
}
