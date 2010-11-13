package com.angelstone.android.profileswitcher.store;

import android.net.Uri;

public class Schedule implements DatabaseValues {
	public static final String TABLE_NAME = "schedules";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_PROFILE_ID = "profile_id";
	public static final String COLUMN_START_TIME = "start_time";
	public static final String COLUMN_REPEAT_WEEKDAYS = "repeat_week_days";

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.angelstone.android.profileswitch.schedule";
  public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.angelstone.android.profileswitch.schedule";

  public static final String DEFAULT_SORT_ORDER = COLUMN_START_TIME + " asc";

	public static final String URI_STRING = "schedule";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + URI_STRING);
}
