package com.angelstone.android.profileswitcher.store;

public class Profile {
	public static final String TABLE_NAME = "profiles";
	
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_FLAGS = "flags";
	
	public static final String COLUMN_MEDIA_VOLUME = "media_volume";
	public static final String COLUMN_PHONE_VOLUME = "phone_volume";
	public static final String COLUMN_NOTIFY_VOLUME = "notify_volume";
	public static final String COLUMN_ALARM_VOLUME = "alarm_volume";
	
	public static final String COLUMN_PHONE_RING_TONE = "phone_ring_tone";
	public static final String COLUMN_NOTIFY_RING_TONE = "notify_ring_tone";
	public static final String COLUMN_ALARM_RING_TONE = "alarm_ring_tone";
	public static final String COLUMN_EMAIL_RING_TONE = "email_ring_tone";

	public static final int FLAG_MEDIA_VOLUME = 0x00000001;
	public static final int FLAG_PHONE_VOLUME = 0x00000001 << 1;
	public static final int FLAG_NOTIFY_VOLUME = 0x00000001 << 2;
	public static final int FLAG_ALARM_VOLUME = 0x00000001 << 3;

	public static final int FLAG_WIFI = 0x00000001 << 4;
	public static final int FLAG_GPS = 0x00000001 << 5;
	public static final int FLAG_BLUE_TOOTH = 0x00000001 << 6;

	public static final int FLAG_PHONE_VIRBRATE = 0x00000001 << 7;
	public static final int FLAG_NOTIFY_VIRBRATE = 0x00000001 << 8;
	public static final int FLAG_EMAIL_VIRBRATE = 0x00000001 << 9;

	public static final int FLAG_MEDIA_RING_TONE = 0x00000001 << 10;
	public static final int FLAG_PHONE_RING_TONE = 0x00000001 << 11;
	public static final int FLAG_NOTIFY_RING_TONE = 0x00000001 << 12;
	public static final int FLAG_ALARM_RING_TONE = 0x00000001 << 13;
}
