package com.angelstone.android.profileswitcher.utils;

import java.lang.reflect.Method;
import java.text.MessageFormat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;

import com.angelstone.android.platform.SysCompat;
import com.angelstone.android.profileswitcher.ProfileSwitcherConstants;
import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.profileswitcher.store.Schedule;
import com.angelstone.android.profileswitcher.ui.ProfileSwitcherMainActivity;
import com.angelstone.android.utils.ActivityLog;

public class ProfileSwitcherUtils {
	public static void activateProfile(Context context, long profileId,
			int active, long seconds) {
		activateProfile(context, profileId, active, seconds, -1);
	}

	public static void activateProfile(Context context, long profileId,
			int active, long seconds, long scheduleId) {
		Intent intent = new Intent(ProfileSwitcherConstants.ACTION_SET_PROFILE);
		intent.putExtra(Profile.COLUMN_ID, profileId);
		intent.putExtra(Profile.COLUMN_ACTIVE, active);
		intent.putExtra(Profile.COLUMN_ACTIVATE_TIME,
				System.currentTimeMillis());
		intent.putExtra(Profile.COLUMN_EXPIRE_TIME, seconds);
		intent.putExtra(ProfileSwitcherConstants.EXTRA_SCHEDULE_ID, scheduleId);

		context.sendBroadcast(intent);
	}

	public static float getLocationDistancePreference() {
		// TODO Load location distance from preference
		return 50;
	}

	public static int getLocationUpdateTimePreference() {
		// TODO Auto-generated method stub
		return 1;
	}

	public static float getLocationUpdateDistancePreference() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static void enableProfile(Context context, long profileId,
			long scheduleId) {
		Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, profileId);

		Cursor c = context.getContentResolver().query(uri, null, null, null,
				null);

		Profile p = new Profile();

		try {
			if (c == null || c.getCount() == 0)
				return;

			if (!c.moveToNext())
				return;

			p = new Profile(c);

			if (p.getActive() == Profile.ACTIVE_NONE)
				return;

			realUpdateDeviceUsingProfile(context, p);

			notifyProfileEnabled(context, p.getName(), true,
					p.getActive() == Profile.ACTIVE_AUTO, scheduleId);

		} catch (Throwable t) {
			Log.e(ProfileSwitcherConstants.TAG, "enable profile fail", t);
			ActivityLog.logError(context, ProfileSwitcherConstants.TAG,
					t.getLocalizedMessage());
			notifyProfileEnabled(context,
					TextUtils.isEmpty(p.getName()) ? String.valueOf(profileId)
							: p.getName(), false,
					p.getActive() == Profile.ACTIVE_AUTO, scheduleId);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	public static void notifyProfileEnabled(Context context,
			String profileName, boolean success, boolean autoActive,
			long scheduleId) {
		String expandedText = MessageFormat.format(context
				.getString(success ? R.string.notification_enable_profile
						: R.string.notification_enable_profile_fail),
				profileName, getScheduleMessage(context, scheduleId),
				getActiveMessage(context, autoActive));

		// create the target call log intent
		final PendingIntent intent = getNotificationIntent(context);

		Notification notification = new Notification(
				success ? android.R.drawable.stat_notify_more
						: android.R.drawable.stat_notify_error, // icon
				expandedText, // tickerText
				System.currentTimeMillis());

		notification.setLatestEventInfo(context,
				context.getString(R.string.notification_enable_profile_title),
				expandedText, intent);

		// make the notification
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(ProfileSwitcherConstants.PROFILE_ENABLE_NOTIFICATION,
				notification);
	}

	private static String getScheduleMessage(Context context, long scheduleId) {
		StringBuffer sb = new StringBuffer(100);

		if (scheduleId > 0) {
			sb.append(context.getString(R.string.schedule));
			sb.append(getScheduleName(context, scheduleId));
		}

		return sb.toString();
	}

	private static String getActiveMessage(Context context, boolean autoActive) {
		StringBuffer sb = new StringBuffer(100);

		if (autoActive) {
			sb.append(context.getString(R.string.automatically));
		} else {
			sb.append(context.getString(R.string.manually));
		}
		return sb.toString();
	}

	private static String getScheduleName(Context context, long scheduleId) {
		Uri uri = ContentUris.withAppendedId(Schedule.CONTENT_URI, scheduleId);

		Cursor c = context.getContentResolver().query(uri,
				new String[] { Schedule.COLUMN_LABEL }, null, null, null);

		try {
			if (c != null && c.moveToNext()) {
				return c.getString(0);
			}
			return "";
		} finally {
			if (c != null)
				c.close();
		}

	}

	private static PendingIntent getNotificationIntent(Context context) {
		Intent intent = new Intent(context, ProfileSwitcherMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(ProfileSwitcherConstants.DATA_NOTIFY, true);

		return PendingIntent.getActivity(context.getApplicationContext(), 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public static void cancelNotification(Context context) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(ProfileSwitcherConstants.PROFILE_ENABLE_NOTIFICATION);
	}

	private static boolean realUpdateDeviceUsingProfile(Context context,
			Profile p) throws SettingNotFoundException {
		boolean result = true;

		if (p == null)
			return true;

		if (p.isPhoneConfigured())
			result &= enablePhone(context, p.isPhoneEnable());

		if (p.isGpsConfigured())
			result &= enableGps(context, p.isGpsEnable());

		if (p.isWifiConfigured())
			result &= enableWifi(context, p.isWifiEnable());

		if (p.isBlueToothConfigured())
			result &= enableBlueTooth(context, p.isBlueToothEnable());

		boolean muteActive = false;
		boolean mute = false;

		if (p.isMuteConfigured()) {
			muteActive = true;
			mute = p.isMuteEnable();
		}

		boolean vibeActive = false;
		boolean vibe = false;

		if (p.isVibrateConfigured()) {
			vibeActive = true;
			vibe = p.isVibrateEnable();
		}

		if (muteActive || vibeActive)
			result &= enableVibrateMute(context, vibeActive, vibe, muteActive,
					mute);

		if (p.isPhoneDataConnConfigured())
			result &= enablePhoneDataConn(context, p.isPhoneDataConnEnable());

		if (p.isPhoneRingtoneAndVolumeConfigured())
			result &= setPhoneRingtoneAndVolume(p);

		if (p.isNotificationRingtoneAndVolumeConfigured())
			result &= setNotificationRingtoneAndVolume(p);

		if (p.isEmailRingtoneAndVolumeConfigured()) {
			result &= setEmailRingtoneAndVolume(p);
		}

		if (p.isAlarmRingtoneAndVolumeConfigured()) {
			result &= setAlarmRingtoneAndVolume(p);
		}

		return result;
	}

	private static boolean enablePhone(Context context, boolean phoneEnable)
			throws SettingNotFoundException {
		if (Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON) == (phoneEnable ? 0 : 1))
			return true;

		Settings.System.putString(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_RADIOS,
				Settings.System.RADIO_CELL);

		Settings.System.putInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, phoneEnable ? 0 : 1);

		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", phoneEnable);
		context.sendBroadcast(intent);

		return Settings.System.getInt(context.getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON) == (phoneEnable ? 0 : 1);
	}

	private static boolean enableGps(Context context, boolean gpsEnable) {
		try {
			Class<?> c = Class.forName("android.provider.Settings$Secure");

			Method m = c.getMethod("", ContentResolver.class, String.class,
					boolean.class);
			m.invoke(null, context.getContentResolver(),
					LocationManager.GPS_PROVIDER, gpsEnable);
		} catch (Throwable t) {
			Log.e(ProfileSwitcherConstants.TAG, "reflection for gps fail:"
					+ Settings.Secure.class.getName(), t);

			String locationString = Settings.Secure.getString(
					context.getContentResolver(),
					Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

			if (gpsEnable) {
				if (locationString.indexOf(LocationManager.GPS_PROVIDER) >= 0)
					return true;

				if (locationString.length() > 0)
					locationString += ",";
				locationString += LocationManager.GPS_PROVIDER;
			} else {
				if (locationString.indexOf(LocationManager.GPS_PROVIDER) < 0)
					return true;

				String[] parts = locationString.split(",");

				StringBuilder sb = new StringBuilder(100);

				for (int i = 0; i < parts.length; i++) {
					if (!LocationManager.GPS_PROVIDER.equals(parts[i])) {
						sb.append(parts[i]).append(",");
					}
				}

				if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') {
					sb.deleteCharAt(sb.length() - 1);
				}

				locationString = sb.toString();
			}

			Settings.Secure.putString(context.getContentResolver(),
					Settings.Secure.LOCATION_PROVIDERS_ALLOWED, locationString);
		}
		return Settings.Secure.getString(context.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED).indexOf(
				LocationManager.GPS_PROVIDER) >= 0;
	}

	private static boolean enableWifi(Context context, boolean wifiEnable) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		if (wifiEnable == wifiManager.isWifiEnabled())
			return true;

		wifiManager.setWifiEnabled(wifiEnable);

		return wifiEnable == wifiManager.isWifiEnabled();
	}

	private static boolean enableBlueTooth(Context context,
			boolean blueToothEnable) {
		SysCompat sc = SysCompat.register(context);

		return sc.enableBluetooth(blueToothEnable);
	}

	private static boolean enableVibrateMute(Context context,
			boolean vibeActive, boolean vibe, boolean muteActive, boolean mute) {
		int callsVibrateSetting = vibe ? AudioManager.VIBRATE_SETTING_ON
				: AudioManager.VIBRATE_SETTING_OFF;

		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);

		int ringMode = am.getRingerMode();

		if (vibeActive && muteActive) {
			if (vibe) {
				ringMode = AudioManager.RINGER_MODE_VIBRATE;
			} else {
				if (mute)
					ringMode = AudioManager.RINGER_MODE_SILENT;
				else
					ringMode = AudioManager.RINGER_MODE_NORMAL;
			}

		} else if (vibeActive) {
			if (vibe) {
				ringMode = AudioManager.RINGER_MODE_VIBRATE;
			} else if (ringMode == AudioManager.RINGER_MODE_VIBRATE)
				ringMode = AudioManager.RINGER_MODE_NORMAL;
		} else if (muteActive) {
			if (mute) {
				ringMode = AudioManager.RINGER_MODE_SILENT;
			} else if (ringMode == AudioManager.RINGER_MODE_SILENT) {
				ringMode = AudioManager.RINGER_MODE_NORMAL;
			}
		}

		am.setRingerMode(ringMode);

		if (vibeActive) {
			// 2.3 need to update the setting
			try {
				String name = (String) Settings.System.class.getField(
						"VIBRATE_IN_SILENT").get(null);
				Settings.System.putInt(context.getContentResolver(), name,
						vibe ? 1 : 0);
			} catch (Throwable t) {

			}
			am.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
					callsVibrateSetting);
		}

		return true;
	}

	private static boolean enablePhoneDataConn(Context context,
			boolean phoneDataConnEnable) {
		SysCompat sc = SysCompat.register(context);

		return sc.enableMobileNetwork(phoneDataConnEnable);
	}

	private static boolean setPhoneRingtoneAndVolume(Profile p) {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean setNotificationRingtoneAndVolume(Profile p) {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean setEmailRingtoneAndVolume(Profile p) {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean setAlarmRingtoneAndVolume(Profile p) {
		// TODO Auto-generated method stub
		return false;
	}
}
