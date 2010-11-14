package com.angelstone.android.profileswitcher.store;

import android.net.Uri;

public class Profile implements DatabaseValues {
	public static final String TABLE_NAME = "profiles";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_FLAGS = "flags";
	public static final String COLUMN_ACTIVE = "active";

	public static final String COLUMN_DEVICES = "devices";

	public static final String COLUMN_MEDIA_VOLUME = "media_volume";
	public static final String COLUMN_PHONE_VOLUME = "phone_volume";
	public static final String COLUMN_NOTIFY_VOLUME = "notify_volume";
	public static final String COLUMN_ALARM_VOLUME = "alarm_volume";

	public static final String COLUMN_PHONE_RING_TONE = "phone_ring_tone";
	public static final String COLUMN_NOTIFY_RING_TONE = "notify_ring_tone";
	public static final String COLUMN_ALARM_RING_TONE = "alarm_ring_tone";
	public static final String COLUMN_EMAIL_RING_TONE = "email_ring_tone";

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.angelstone.android.profileswitch.profile";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.angelstone.android.profileswitch.profile";

	public static final String DEFAULT_SORT_ORDER = COLUMN_ACTIVE + " desc,"
			+ COLUMN_NAME + " asc";

	public static final String URI_STRING = "profile";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + URI_STRING);

	public static final int FLAG_EMAIL_VOLUME = 0x00000001;
	public static final int FLAG_PHONE_VOLUME = 0x00000001 << 1;
	public static final int FLAG_NOTIFY_VOLUME = 0x00000001 << 2;
	public static final int FLAG_ALARM_VOLUME = 0x00000001 << 3;

	public static final int FLAG_WIFI = 0x00000001 << 4;
	public static final int FLAG_GPS = 0x00000001 << 5;
	public static final int FLAG_BLUE_TOOTH = 0x00000001 << 6;

	public static final int FLAG_PHONE_VIBRATE = 0x00000001 << 7;
	public static final int FLAG_NOTIFY_VIBRATE = 0x00000001 << 8;
	public static final int FLAG_EMAIL_VIBRATE = 0x00000001 << 9;

	public static final int FLAG_EMAIL_RINGTONE = 0x00000001 << 10;
	public static final int FLAG_PHONE_RINGTONE = 0x00000001 << 11;
	public static final int FLAG_NOTIFY_RINGTONE = 0x00000001 << 12;
	public static final int FLAG_ALARM_RINGTONE = 0x00000001 << 13;

	public static final int FLAG_ALARM_VIBRATE = 0x00000001 << 14;

	public static final int DEVICE_WIFI_ENABLE = 0x00000001;
	public static final int DEVICE_GPS_ENABLE = 0x00000001 << 1;
	public static final int DEVICE_BLUETOOTH_ENABLE = 0x00000001 << 2;

	private int mPhoneVolume = 0;
	private String mPhoneRingtone = null;
	private boolean mPhoneVibrate = false;
	private int mNotificationVolume = 0;
	private String mNotificationRingtone = null;
	private boolean mNotificationVibrate = false;
	private int mEmailVolume = 0;
	private String mEmailRingtone = null;
	private boolean mEmailVibrate = false;
	private int mAlarmVolume = 0;
	private String mAlarmRingtone = null;
	private boolean mAlarmVibrate = false;
	private int mFlags = 0;
	private int mDevices = 0;
	private String mName = "";

	public int getPhoneVolume() {
		return mPhoneVolume;
	}

	public void setPhoneVolume(int phoneVolume) {
		setPhoneVolumeConfigured(true);
		mPhoneVolume = phoneVolume;
	}

	public String getPhoneRingtone() {
		return mPhoneRingtone;
	}

	public void setPhoneRingtone(String phoneRingtone) {
		setPhoneRingtoneConfigured(true);
		mPhoneRingtone = phoneRingtone;
	}

	public boolean isPhoneVibrate() {
		return mPhoneVibrate;
	}

	public void setPhoneVibrate(boolean phoneVibrate) {
		setPhoneVibrateConfigured(true);
		mPhoneVibrate = phoneVibrate;
	}

	public int getNotificationVolume() {
		return mNotificationVolume;
	}

	public void setNotificationVolume(int notificationVolume) {
		mNotificationVolume = notificationVolume;
		setNotificationVolumeConfigured(true);
	}

	public String getNotificationRingtone() {
		return mNotificationRingtone;
	}

	public void setNotificationRingtone(String notificationRingtone) {
		setNotificationRingtoneConfigured(true);
		mNotificationRingtone = notificationRingtone;
	}

	public boolean isNotificationVibrate() {
		return mNotificationVibrate;
	}

	public void setNotificationVibrate(boolean notificationVibrate) {
		setNotificationVibrateConfigured(true);
		mNotificationVibrate = notificationVibrate;
	}

	public int getEmailVolume() {
		return mEmailVolume;
	}

	public void setEmailVolume(int emailVolume) {
		setEmailVolumeConfigured(true);
		mEmailVolume = emailVolume;
	}

	public String getEmailRingtone() {
		return mEmailRingtone;
	}

	public void setEmailRingtone(String emailRingtone) {
		setEmailRingtoneConfigured(true);
		mEmailRingtone = emailRingtone;
	}

	public boolean isEmailVibrate() {
		return mEmailVibrate;
	}

	public void setEmailVibrate(boolean emailVibrate) {
		setEmailVibrateConfigured(true);
		mEmailVibrate = emailVibrate;
	}

	public int getAlarmVolume() {
		return mAlarmVolume;
	}

	public void setAlarmVolume(int alarmVolume) {
		setAlarmVolumeConfigured(true);
		mAlarmVolume = alarmVolume;
	}

	public String getAlarmRingtone() {
		return mAlarmRingtone;
	}

	public void setAlarmRingtone(String alarmRingtone) {
		setAlarmRingtoneConfigured(true);
		mAlarmRingtone = alarmRingtone;
	}

	public boolean isAlarmVibrate() {
		return mAlarmVibrate;
	}

	public void setAlarmVibrate(boolean alarmVibrate) {
		setAlarmVibrateConfigured(true);
		mAlarmVibrate = alarmVibrate;
	}

	public int getFlags() {
		return mFlags;
	}

	public void setFlags(int flags) {
		mFlags = flags;
	}

	public int getDevices() {
		return mDevices;
	}

	public void setDevices(int devices) {
		mDevices = devices;
	}

	public boolean isGpsConfigured() {
		return (mFlags & FLAG_GPS) == FLAG_GPS;
	}

	public void setGpsConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_GPS;
		else
			mFlags &= ~FLAG_GPS;
	}

	public boolean isGpsEnable() {
		return isGpsConfigured()
				&& (mDevices & DEVICE_GPS_ENABLE) == DEVICE_GPS_ENABLE;
	}

	public void setGpsEnable(boolean enable) {
		mFlags |= FLAG_GPS;

		if (enable)
			mDevices |= DEVICE_GPS_ENABLE;
		else
			mDevices &= ~DEVICE_GPS_ENABLE;
	}

	public boolean isWifiConfigured() {
		return (mFlags & FLAG_WIFI) == FLAG_WIFI;
	}

	public void setWifiConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_WIFI;
		else
			mFlags &= ~FLAG_WIFI;
	}

	public boolean isWifiEnable() {
		return isWifiConfigured()
				&& (mDevices & DEVICE_WIFI_ENABLE) == DEVICE_WIFI_ENABLE;
	}

	public void setWifiEnable(boolean enable) {
		mFlags |= FLAG_WIFI;

		if (enable)
			mDevices |= DEVICE_WIFI_ENABLE;
		else
			mDevices &= ~DEVICE_WIFI_ENABLE;
	}

	public boolean isBlueToothConfigured() {
		return (mFlags & FLAG_BLUE_TOOTH) == FLAG_BLUE_TOOTH;
	}

	public void setBlueToothConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_BLUE_TOOTH;
		else
			mFlags &= ~FLAG_BLUE_TOOTH;
	}

	public boolean isBlueToothEnable() {
		return isBlueToothConfigured()
				&& (mDevices & DEVICE_BLUETOOTH_ENABLE) == DEVICE_BLUETOOTH_ENABLE;
	}

	public void setBlueToothEnable(boolean enable) {
		mFlags |= FLAG_BLUE_TOOTH;

		if (enable)
			mDevices |= DEVICE_BLUETOOTH_ENABLE;
		else
			mDevices &= ~DEVICE_BLUETOOTH_ENABLE;
	}

	public boolean isPhoneVolumeConfigured() {
		return (mFlags & FLAG_PHONE_VOLUME) == FLAG_PHONE_VOLUME;
	}

	public void setPhoneVolumeConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_PHONE_VOLUME;
		else
			mFlags &= ~FLAG_PHONE_VOLUME;
	}

	public boolean isPhoneRingtoneConfigured() {
		return (mFlags & FLAG_PHONE_RINGTONE) == FLAG_PHONE_RINGTONE;
	}

	public void setPhoneRingtoneConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_PHONE_RINGTONE;
		else
			mFlags &= ~FLAG_PHONE_RINGTONE;
	}

	public boolean isPhoneVibrateConfigured() {
		return (mFlags & FLAG_PHONE_VIBRATE) == FLAG_PHONE_VIBRATE;
	}

	public void setPhoneVibrateConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_PHONE_VIBRATE;
		else
			mFlags &= ~FLAG_PHONE_VIBRATE;
	}

	public boolean isNotificationVolumeConfigured() {
		return (mFlags & FLAG_NOTIFY_VOLUME) == FLAG_NOTIFY_VOLUME;
	}

	public void setNotificationVolumeConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_NOTIFY_VOLUME;
		else
			mFlags &= ~FLAG_NOTIFY_VOLUME;
	}

	public boolean isNotificationRingtoneConfigured() {
		return (mFlags & FLAG_NOTIFY_RINGTONE) == FLAG_NOTIFY_RINGTONE;
	}

	public void setNotificationRingtoneConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_NOTIFY_RINGTONE;
		else
			mFlags &= ~FLAG_NOTIFY_RINGTONE;
	}

	public boolean isNotificationVibrateConfigured() {
		return (mFlags & FLAG_NOTIFY_VIBRATE) == FLAG_NOTIFY_VIBRATE;
	}

	public void setNotificationVibrateConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_NOTIFY_VIBRATE;
		else
			mFlags &= ~FLAG_NOTIFY_VIBRATE;
	}

	public boolean isEmailVolumeConfigured() {
		return (mFlags & FLAG_EMAIL_VOLUME) == FLAG_EMAIL_VOLUME;
	}

	public void setEmailVolumeConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_EMAIL_VOLUME;
		else
			mFlags &= ~FLAG_EMAIL_VOLUME;
	}

	public boolean isEmailRingtoneConfigured() {
		return (mFlags & FLAG_EMAIL_RINGTONE) == FLAG_EMAIL_RINGTONE;
	}

	public void setEmailRingtoneConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_EMAIL_RINGTONE;
		else
			mFlags &= ~FLAG_EMAIL_RINGTONE;
	}

	public boolean isEmailVibrateConfigured() {
		return (mFlags & FLAG_EMAIL_VIBRATE) == FLAG_EMAIL_VIBRATE;
	}

	public void setEmailVibrateConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_EMAIL_VIBRATE;
		else
			mFlags &= ~FLAG_EMAIL_VIBRATE;
	}

	public boolean isAlarmVolumeConfigured() {
		return (mFlags & FLAG_ALARM_VOLUME) == FLAG_ALARM_VOLUME;
	}

	public void setAlarmVolumeConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_ALARM_VOLUME;
		else
			mFlags &= ~FLAG_ALARM_VOLUME;
	}

	public boolean isAlarmRingtoneConfigured() {
		return (mFlags & FLAG_ALARM_RINGTONE) == FLAG_ALARM_RINGTONE;
	}

	public void setAlarmRingtoneConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_ALARM_RINGTONE;
		else
			mFlags &= ~FLAG_ALARM_RINGTONE;
	}

	public boolean isAlarmVibrateConfigured() {
		return (mFlags & FLAG_ALARM_VIBRATE) == FLAG_ALARM_VIBRATE;
	}

	public void setAlarmVibrateConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_ALARM_VIBRATE;
		else
			mFlags &= ~FLAG_ALARM_VIBRATE;
	}

	public boolean isPhoneConfigured() {
		return isPhoneRingtoneConfigured() || isPhoneVibrateConfigured()
				|| isPhoneVolumeConfigured();
	}
	public boolean isEmailConfigured() {
		return isEmailRingtoneConfigured() || isEmailVibrateConfigured()
				|| isEmailVolumeConfigured();
	}
	public boolean isNotificationConfigured() {
		return isNotificationRingtoneConfigured() || isNotificationVibrateConfigured()
				|| isNotificationVolumeConfigured();
	}
	public boolean isAlarmConfigured() {
		return isAlarmRingtoneConfigured() || isAlarmVibrateConfigured()
				|| isAlarmVolumeConfigured();
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}
}
