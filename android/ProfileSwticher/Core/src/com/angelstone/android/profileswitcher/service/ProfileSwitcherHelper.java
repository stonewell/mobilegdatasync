package com.angelstone.android.profileswitcher.service;

import java.util.Calendar;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.PowerManager;
import android.util.Log;

import com.angelstone.android.profileswitcher.ProfileSwitcherConstants;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.profileswitcher.store.Schedule;
import com.angelstone.android.profileswitcher.utils.Alarm;
import com.angelstone.android.profileswitcher.utils.Alarms;
import com.angelstone.android.profileswitcher.utils.ProfileSwitcherUtils;
import com.angelstone.android.utils.ActivityLog;

class ProfileSwitcherHelper {

	private static void processAlarm(Context context, Alarm alarm) {
		Cursor c = null;

		try {
			Uri uri = ContentUris
					.withAppendedId(Schedule.CONTENT_URI, alarm.id);

			c = context.getContentResolver().query(uri, null, null, null, null);

			if (!c.moveToNext())
				return;

			boolean enable = c.getInt(c.getColumnIndex(Schedule.COLUMN_ENABLE)) == 1;

			// if a repeat alarm is not enable, do not active profile
			// if a non-repeat alarm comes but database says it is not enable,
			// set the profile anyway, since the enable state changed by
			// Alarms.setNextAlarm()
			if (!enable && alarm.time == 0)
				return;

			ProfileSwitcherUtils.activateProfile(context,
					c.getLong(c.getColumnIndex(Schedule.COLUMN_PROFILE_ID)),
					Profile.ACTIVE_AUTO, 0,
					c.getLong(c.getColumnIndex(Schedule.COLUMN_ID)));
		} finally {
			if (c != null)
				c.close();
		}
	}

	private static void processTempProfileExpire(Context context, Alarm a) {
		ProfileSwitcherUtils.activateProfile(context, a.profileId,
				Profile.ACTIVE_NONE, 0);
	}

	private static void doActivateLastKnownProfile(Context context,
			boolean activeLatestSchedule) {
		Cursor c = context.getContentResolver().query(
				Profile.CONTENT_URI,
				null,
				Profile.COLUMN_ACTIVE + "!=?",
				new String[] { String.valueOf(Profile.ACTIVE_NONE) },
				Profile.COLUMN_ACTIVATE_TIME + " desc," + Profile.COLUMN_ACTIVE
						+ " asc");

		try {
			if (c == null || c.getCount() == 0) {
				// No current alarm active,
				// try to active the latest schedule
				if (activeLatestSchedule)
					doActivateLatestSchedule(context);

				return;
			}

			int idxActive = c.getColumnIndex(Profile.COLUMN_ACTIVE);
			int idxProfileId = c.getColumnIndex(Profile.COLUMN_ID);
			int idxExpireTime = c.getColumnIndex(Profile.COLUMN_EXPIRE_TIME);
			int idxActivateTime = c
					.getColumnIndex(Profile.COLUMN_ACTIVATE_TIME);
			long expireTime = 0;
			long activeTime = 0;
			long profileId = -1;
			int active = Profile.ACTIVE_NONE;

			while (c.moveToNext()) {
				int tmpActive = c.getInt(idxActive);

				long tmpExpireTime = c.getLong(idxExpireTime) * 1000;
				long tmpActiveTime = c.getLong(idxActivateTime);

				switch (tmpActive) {
				case Profile.ACTIVE_MANUAL_TIME: {
					if (tmpActiveTime <= System.currentTimeMillis()
							&& (tmpActiveTime + tmpExpireTime > System
									.currentTimeMillis())) {
						expireTime = tmpActiveTime + tmpExpireTime
								- System.currentTimeMillis();
						activeTime = tmpActiveTime;
						profileId = c.getLong(idxProfileId);
						active = tmpActive;
					} else {
						// Expired
						Uri uri = ContentUris.withAppendedId(
								Profile.CONTENT_URI, c.getLong(idxProfileId));
						ContentValues values = new ContentValues();
						values.put(Profile.COLUMN_ACTIVE, Profile.ACTIVE_NONE);

						context.getContentResolver().update(uri, values, null,
								null);
					}
				}
					break;
				case Profile.ACTIVE_MANUAL: {
					profileId = c.getLong(idxProfileId);
					active = tmpActive;
				}
					break;
				case Profile.ACTIVE_AUTO:
					if (activeTime < tmpActiveTime) {
						profileId = c.getLong(idxProfileId);
						active = tmpActive;
					}

				default:
					break;
				}

				if (active != Profile.ACTIVE_AUTO
						&& active != Profile.ACTIVE_NONE && profileId > 0) {
					break;
				}
			}

			if (profileId > 0) {
				// if last known profile is auto activate profile
				// we activate the latest auto activate profile
				if (active != Profile.ACTIVE_AUTO) {
					ProfileSwitcherUtils.activateProfile(context, profileId,
							active, expireTime);
				} else {
					if (!doActivateLatestSchedule(context)) {
						// if no profile activate, clear all auto active profile
						clearOtherAutoActiveProfile(context, -1);
					}
				}
			} else if (activeLatestSchedule) {
				doActivateLatestSchedule(context);
			}

		} finally {
			if (c != null)
				c.close();
		}

	}

	private static boolean doActivateLatestSchedule(Context context) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		long scheduleBegin = c.getTimeInMillis();
		long scheduleEnd = System.currentTimeMillis();

		Cursor cursor = context.getContentResolver().query(
				Schedule.CONTENT_URI,
				null,
				Schedule.COLUMN_ENABLE + "=1 AND " + Schedule.COLUMN_START_TIME
						+ ">0", null, null);

		try {
			if (cursor == null || cursor.getCount() == 0) {
				return false;
			}

			long profileId = -1;
			long scheduleId = -1;
			long scheduleTime = 0;

			while (cursor.moveToNext()) {
				Alarm alarm = new Alarm(cursor);

				if (alarm.time == 0) {
					c.set(Calendar.HOUR_OF_DAY, alarm.hour);
					c.set(Calendar.MINUTE, alarm.minutes);
				} else {
					c.setTimeInMillis(alarm.time);
				}

				if (c.getTimeInMillis() >= scheduleBegin
						&& c.getTimeInMillis() <= scheduleEnd) {
					if (c.getTimeInMillis() > scheduleTime) {

						if (alarm.time == 0
								&& alarm.daysOfWeek.getNextAlarm(c) == 0) {
							profileId = alarm.profileId;
							scheduleTime = c.getTimeInMillis();
							scheduleId = alarm.id;
						} else if (alarm.time > 0) {
							profileId = alarm.profileId;
							scheduleTime = c.getTimeInMillis();
							scheduleId = alarm.id;
						}
					}
				}
			}

			if (profileId > 0) {
				ProfileSwitcherUtils.activateProfile(context, profileId,
						Profile.ACTIVE_AUTO, 0, scheduleId);
				return true;
			}

			return false;
		} finally {
			if (cursor != null)
				cursor.close();
		}

	}

	private static void doChangeProfile(Context context, Intent intent) {
		long profileId = intent.getLongExtra(Profile.COLUMN_ID, -1);

		if (profileId < 0) {
			ActivityLog.logError(context, ProfileSwitcherConstants.TAG,
					"activate profile id < 0");
			return;
		}

		int active = intent.getIntExtra(Profile.COLUMN_ACTIVE, -1);

		switch (active) {
		case Profile.ACTIVE_AUTO:
		case Profile.ACTIVE_MANUAL:
		case Profile.ACTIVE_MANUAL_TIME:
		case Profile.ACTIVE_NONE:
			break;
		default:
			ActivityLog.logError(context, ProfileSwitcherConstants.TAG,
					"activate profile id:" + profileId
							+ " with invalid active:" + active);
			return;
		}

		long activeTime = intent.getLongExtra(Profile.COLUMN_ACTIVATE_TIME,
				System.currentTimeMillis());
		long seconds = intent.getLongExtra(Profile.COLUMN_EXPIRE_TIME, 0);
		long scheduleId = intent.getLongExtra(
				ProfileSwitcherConstants.EXTRA_SCHEDULE_ID, -1);

		ContentValues values = new ContentValues();
		values.put(Profile.COLUMN_ACTIVE, active);
		values.put(Profile.COLUMN_ACTIVATE_TIME, activeTime);
		values.put(Profile.COLUMN_EXPIRE_TIME, seconds);

		Uri uri = ContentUris.withAppendedId(Profile.CONTENT_URI, profileId);

		if (context.getContentResolver().update(uri, values, null, null) == 0) {
			ActivityLog.logError(context, ProfileSwitcherConstants.TAG,
					"activate profile id:" + profileId + " not found");

			return;
		}

		switch (active) {
		case Profile.ACTIVE_AUTO:
			clearOtherAutoActiveProfile(context, profileId);

			if (isManualActiveProfileExists(context))
				return;
			break;
		case Profile.ACTIVE_MANUAL:
		case Profile.ACTIVE_MANUAL_TIME:
			clearOtherManualActiveProfile(context, profileId);
			break;
		case Profile.ACTIVE_NONE:
			doActivateLastKnownProfile(context, true);
			break;
		default:
			return;
		}

		if (active != Profile.ACTIVE_NONE)
			ProfileSwitcherUtils.enableProfile(context, profileId, scheduleId);
	}

	private static void clearOtherAutoActiveProfile(Context context,
			long profileId) {
		ContentValues values = new ContentValues();
		values.put(Profile.COLUMN_ACTIVE, Profile.ACTIVE_NONE);

		context.getContentResolver().update(
				Profile.CONTENT_URI,
				values,
				Profile.COLUMN_ACTIVE + "=? AND " + Profile.COLUMN_ID + "!=?",
				new String[] { String.valueOf(Profile.ACTIVE_AUTO),
						String.valueOf(profileId) });
	}

	private static boolean isManualActiveProfileExists(Context context) {
		Cursor c = context.getContentResolver()
				.query(Profile.CONTENT_URI,
						new String[] { Profile.COLUMN_ID },
						Profile.COLUMN_ACTIVE + "=? OR "
								+ Profile.COLUMN_ACTIVE + "=?",
						new String[] { String.valueOf(Profile.ACTIVE_MANUAL),
								String.valueOf(Profile.ACTIVE_MANUAL_TIME) },
						null);

		try {
			return c != null && c.getCount() > 0;
		} finally {
			if (c != null)
				c.close();
		}
	}

	private static void clearOtherManualActiveProfile(Context context,
			long profileId) {
		ContentValues values = new ContentValues();
		values.put(Profile.COLUMN_ACTIVE, Profile.ACTIVE_NONE);

		context.getContentResolver().update(
				Profile.CONTENT_URI,
				values,
				Profile.COLUMN_ACTIVE + "!=? AND " + Profile.COLUMN_ID + "!=?",
				new String[] { String.valueOf(Profile.ACTIVE_AUTO),
						String.valueOf(profileId) });
	}

	public static void handleIntent(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock lock = null;

		try {
			lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					ProfileSwitcherConstants.TAG);
			lock.setReferenceCounted(false);
			lock.acquire();

			String action = intent.getAction();

			if (ProfileSwitcherConstants.ACTION_ALARM_ALERT.equals(action)) {
				byte[] buf = intent.getExtras().getByteArray(
						ProfileSwitcherConstants.ALARM_RAW_DATA);

				Parcel out = Parcel.obtain();
				out.unmarshall(buf, 0, buf.length);
				out.setDataPosition(0);

				Alarm a = Alarm.CREATOR.createFromParcel(out);

				if (a.id >= 0) {
					processAlarm(context, a);
				} else {
					processTempProfileExpire(context, a);
				}
			} else if (ProfileSwitcherConstants.ACTION_SET_PROFILE
					.equals(action)) {
				doChangeProfile(context, intent);
			} else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
				Alarms.disableExpiredAlarms(context);
				doActivateLastKnownProfile(context, true);
			} else if (intent.getBooleanExtra(
					ProfileSwitcherConstants.DATA_NOTIFY, false)) {
				ProfileSwitcherUtils.cancelNotification(context);
			}

		} catch (Throwable t) {
			Log.e(ProfileSwitcherConstants.TAG, "Handler fail", t);
			ActivityLog.logError(context, ProfileSwitcherConstants.TAG,
					t.getLocalizedMessage());
		} finally {
			Alarms.setNextAlert(context);
			try {
				if (lock != null)
					lock.release();
			} catch (Throwable t) {

			}
		}
	}

}