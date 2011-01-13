package com.angelstone.android.profileswitcher.utils;

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
import com.angelstone.android.profileswitcher.ui.ProfileSwitcherMainActivity;
import com.angelstone.android.utils.ActivityLog;

public class ProfileSwitcherUtils {
	public static void activateProfile(Context context, long id, int active,
			long seconds) {
		Intent intent = new Intent(ProfileSwitcherConstants.ACTION_SET_PROFILE);
		intent.putExtra(Profile.COLUMN_ID, id);
		intent.putExtra(Profile.COLUMN_ACTIVE, active);
		intent.putExtra(Profile.COLUMN_ACTIVATE_TIME,
				System.currentTimeMillis());
		intent.putExtra(Profile.COLUMN_EXPIRE_TIME, seconds);

		context.startService(intent);
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

	public static void enableProfile(Context context, long profileId) {
		// TODO Auto-generated method stub
		Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, profileId);

		Cursor c = context.getContentResolver().query(uri, null, null, null,
				null);
		String profileName = null;

		try {
			if (c == null || c.getCount() == 0)
				return;
			
			c.moveToNext();
			int idxName = c.getColumnIndex(Profile.COLUMN_NAME);
			profileName = c.getString(idxName);
			
			notifyProfileEnabled(context, profileName, true);
		} catch (Throwable t) {
			Log.e(ProfileSwitcherConstants.TAG, "enable profile fail", t);
			ActivityLog.logError(context, ProfileSwitcherConstants.TAG,
					t.getLocalizedMessage());
			notifyProfileEnabled(context,
					profileName == null ? String.valueOf(profileId)
							: profileName, false);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	private static void notifyProfileEnabled(Context context, String profileName, boolean success) {
		String expandedText = context.getString(
				success ? R.string.notification_enable_profile : 
					R.string.notification_enable_profile_fail, profileName);

		// create the target call log intent
		final PendingIntent intent = getNotificationIntent(context);

		Notification notification = new Notification(
				success ? android.R.drawable.stat_notify_more :
					android.R.drawable.stat_notify_error, // icon
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

	private static PendingIntent getNotificationIntent(Context context) {
		Intent intent = new Intent(context, ProfileSwitcherMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);

		return PendingIntent.getActivity(context.getApplicationContext(), 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
