package com.angelstone.android.profileswitcher.utils;

import java.text.MessageFormat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

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
		return 10;
	}

	public static float getLocationUpdateDistancePreference() {
		// TODO Auto-generated method stub
		return 500;
	}

	public static void enableProfile(Context context, long profileId,
			long scheduleId) {
		// TODO Auto-generated method stub
		Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, profileId);

		Cursor c = context.getContentResolver().query(uri, null, null, null,
				null);
		String profileName = null;
		int active = Profile.ACTIVE_AUTO;

		try {
			if (c == null || c.getCount() == 0)
				return;

			c.moveToNext();

			int idxName = c.getColumnIndex(Profile.COLUMN_NAME);
			int idxActive = c.getColumnIndex(Profile.COLUMN_ACTIVE);

			active = c.getInt(idxActive);

			if (active == Profile.ACTIVE_NONE)
				return;

			profileName = c.getString(idxName);

			notifyProfileEnabled(context, profileName, true,
					active == Profile.ACTIVE_AUTO, scheduleId);
		} catch (Throwable t) {
			Log.e(ProfileSwitcherConstants.TAG, "enable profile fail", t);
			ActivityLog.logError(context, ProfileSwitcherConstants.TAG,
					t.getLocalizedMessage());
			notifyProfileEnabled(context,
					profileName == null ? String.valueOf(profileId)
							: profileName, false,
					active == Profile.ACTIVE_AUTO, scheduleId);
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
}
