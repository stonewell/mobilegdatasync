package com.angelstone.android.profileswitcher.store;

import android.database.Cursor;
import android.net.Uri;

public class Profile implements DatabaseValues {
	public static final int ACTIVE_NONE = 0;
	public static final int ACTIVE_AUTO = 0x00000001;
	public static final int ACTIVE_MANUAL = 0x00000001 << 1;
	public static final int ACTIVE_MANUAL_TIME = 0x00000001 << 2;

	public static final String TABLE_NAME = "profiles";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_FLAGS = "flags";
	public static final String COLUMN_ACTIVE = "active";

	public static final String COLUMN_DEVICES = "devices";

	public static final String COLUMN_EMAIL_VOLUME = "media_volume";
	public static final String COLUMN_PHONE_VOLUME = "phone_volume";
	public static final String COLUMN_NOTIFY_VOLUME = "notify_volume";
	public static final String COLUMN_ALARM_VOLUME = "alarm_volume";

	public static final String COLUMN_PHONE_RING_TONE = "phone_ring_tone";
	public static final String COLUMN_NOTIFY_RING_TONE = "notify_ring_tone";
	public static final String COLUMN_ALARM_RING_TONE = "alarm_ring_tone";
	public static final String COLUMN_EMAIL_RING_TONE = "email_ring_tone";

	public static final String COLUMN_EXPIRE_TIME = "expire_time";
	public static final String COLUMN_ACTIVATE_TIME = "activate_time";

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

	public static final int FLAG_VIBRATE = 0x00000001 << 15;
	public static final int FLAG_MUTE = 0x00000001 << 16;
	public static final int FLAG_PHONE = 0x00000001 << 17;
	public static final int FLAG_PHONE_DATA_CONN = 0x00000001 << 18;

	public static final int DEVICE_WIFI_ENABLE = 0x00000001;
	public static final int DEVICE_GPS_ENABLE = 0x00000001 << 1;
	public static final int DEVICE_BLUETOOTH_ENABLE = 0x00000001 << 2;
	public static final int DEVICE_MUTE_ENABLE = 0x00000001 << 3;
	public static final int DEVICE_VIBRATE_ENABLE = 0x00000001 << 4;
	public static final int DEVICE_PHONE = 0x00000001 << 5;
	public static final int DEVICE_PHONE_DATA_CONN_ENABLE = 0x00000001 << 6;

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
	private int mActive = Profile.ACTIVE_NONE;
	private long mActiveTime = 0;
	private long mExpireTime = 0;

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

	public boolean isPhoneRingtoneAndVolumeConfigured() {
		return isPhoneRingtoneConfigured() || isPhoneVibrateConfigured()
				|| isPhoneVolumeConfigured();
	}

	public boolean isEmailRingtoneAndVolumeConfigured() {
		return isEmailRingtoneConfigured() || isEmailVibrateConfigured()
				|| isEmailVolumeConfigured();
	}

	public boolean isNotificationRingtoneAndVolumeConfigured() {
		return isNotificationRingtoneConfigured()
				|| isNotificationVibrateConfigured()
				|| isNotificationVolumeConfigured();
	}

	public boolean isAlarmRingtoneAndVolumeConfigured() {
		return isAlarmRingtoneConfigured() || isAlarmVibrateConfigured()
				|| isAlarmVolumeConfigured();
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public boolean isMuteConfigured() {
		return (mFlags & FLAG_MUTE) == FLAG_MUTE;
	}

	public void setMuteConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_MUTE;
		else
			mFlags &= ~FLAG_MUTE;
	}

	public boolean isMuteEnable() {
		return isMuteConfigured()
				&& (mDevices & DEVICE_MUTE_ENABLE) == DEVICE_MUTE_ENABLE;
	}

	public void setMuteEnable(boolean enable) {
		setMuteConfigured(true);

		if (enable)
			mDevices |= DEVICE_MUTE_ENABLE;
		else
			mDevices &= ~DEVICE_MUTE_ENABLE;
	}

	public boolean isVibrateConfigured() {
		return (mFlags & FLAG_VIBRATE) == FLAG_VIBRATE;
	}

	public void setVibrateConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_VIBRATE;
		else
			mFlags &= ~FLAG_VIBRATE;
	}

	public boolean isVibrateEnable() {
		return isVibrateConfigured()
				&& (mDevices & DEVICE_VIBRATE_ENABLE) == DEVICE_VIBRATE_ENABLE;
	}

	public void setVibrateEnable(boolean enable) {
		setVibrateConfigured(true);

		if (enable)
			mDevices |= DEVICE_VIBRATE_ENABLE;
		else
			mDevices &= ~DEVICE_VIBRATE_ENABLE;
	}

	public boolean isPhoneConfigured() {
		return (mFlags & FLAG_PHONE) == FLAG_PHONE;
	}

	public void setPhoneConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_PHONE;
		else
			mFlags &= ~FLAG_PHONE;
	}

	public boolean isPhoneEnable() {
		return isPhoneConfigured()
				&& (mDevices & DEVICE_PHONE) == DEVICE_PHONE;
	}

	public void setPhoneEnable(boolean enable) {
		setPhoneConfigured(true);

		if (enable)
			mDevices |= DEVICE_PHONE;
		else
			mDevices &= ~DEVICE_PHONE;
	}

	public boolean isPhoneDataConnConfigured() {
		return (mFlags & FLAG_PHONE_DATA_CONN) == FLAG_PHONE_DATA_CONN;
	}

	public void setPhoneDataConnConfigured(boolean v) {
		if (v)
			mFlags |= FLAG_PHONE_DATA_CONN;
		else
			mFlags &= ~FLAG_PHONE_DATA_CONN;
	}

	public boolean isPhoneDataConnEnable() {
		return isPhoneDataConnConfigured()
				&& (mDevices & DEVICE_PHONE_DATA_CONN_ENABLE) == DEVICE_PHONE_DATA_CONN_ENABLE;
	}

	public void setPhoneDataConnEnable(boolean enable) {
		setPhoneDataConnConfigured(true);

		if (enable)
			mDevices |= DEVICE_PHONE_DATA_CONN_ENABLE;
		else
			mDevices &= ~DEVICE_PHONE_DATA_CONN_ENABLE;
	}
	
	public Profile() {
		
	}
	
	public Profile(Cursor c) {
		int idxName = c.getColumnIndex(Profile.COLUMN_NAME);
		int idxFlags = c.getColumnIndex(Profile.COLUMN_FLAGS);

		int idxDevices = c.getColumnIndex(Profile.COLUMN_DEVICES);

		int idxEmailVolume = c
				.getColumnIndex(Profile.COLUMN_EMAIL_VOLUME);
		int idxPhoneVolume = c
				.getColumnIndex(Profile.COLUMN_PHONE_VOLUME);
		int idxNotifyVolume = c
				.getColumnIndex(Profile.COLUMN_NOTIFY_VOLUME);
		int idxAlarmVolume = c
				.getColumnIndex(Profile.COLUMN_ALARM_VOLUME);

		int idxPhoneRingtone = c
				.getColumnIndex(Profile.COLUMN_PHONE_RING_TONE);
		int idxNotifyRingtone = c
				.getColumnIndex(Profile.COLUMN_NOTIFY_RING_TONE);
		int idxAlarmRingtone = c
				.getColumnIndex(Profile.COLUMN_ALARM_RING_TONE);
		int idxEmailRingtone = c
				.getColumnIndex(Profile.COLUMN_EMAIL_RING_TONE);

		setName(c.getString(idxName));
		setEmailRingtone(c.getString(idxEmailRingtone));
		setEmailVolume(c.getInt(idxEmailVolume));
		setPhoneRingtone(c.getString(idxPhoneRingtone));
		setPhoneVolume(c.getInt(idxPhoneVolume));
		setNotificationRingtone(c.getString(idxNotifyRingtone));
		setNotificationVolume(c.getInt(idxNotifyVolume));
		setAlarmRingtone(c.getString(idxAlarmRingtone));
		setAlarmVolume(c.getInt(idxAlarmVolume));

		// Set flags later since set volumn ringtone will change flags
		setFlags(c.getInt(idxFlags));
		setDevices(c.getInt(idxDevices));
		
		setActive(c.getInt(c.getColumnIndex(Profile.COLUMN_ACTIVE)));
		setActiveTime(c.getLong(c.getColumnIndex(Profile.COLUMN_ACTIVATE_TIME)));
		setExpireTime(c.getLong(c.getColumnIndex(Profile.COLUMN_EXPIRE_TIME)));
	}

	public int getActive() {
		return mActive;
	}

	public void setActive(int active) {
		mActive = active;
	}

	public long getActiveTime() {
		return mActiveTime;
	}

	public void setActiveTime(long activeTime) {
		mActiveTime = activeTime;
	}

	public long getExpireTime() {
		return mExpireTime;
	}

	public void setExpireTime(long expireTime) {
		mExpireTime = expireTime;
	}
}
