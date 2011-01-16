package com.angelstone.android.profileswitcher.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.angelstone.android.profileswitcher.ProfileSwitcherConstants;
import com.angelstone.android.profileswitcher.store.Profile;
import com.angelstone.android.profileswitcher.store.Schedule;
import com.angelstone.android.profileswitcher.utils.Alarm;
import com.angelstone.android.profileswitcher.utils.Alarms;
import com.angelstone.android.profileswitcher.utils.ProfileSwitcherUtils;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.LocationUtils;
import com.angelstone.android.utils.PhoneToolsUtil;

class ProfileSwitcherHandler extends Handler {

	private final Context mContext;
	private Location mCurrentLoc;
	private ConcurrentHashMap<Location, Alarm> mLocationProfileMap = new ConcurrentHashMap<Location, Alarm>();
	private ConcurrentHashMap<Location, Alarm> mLocationProfileTempMap = new ConcurrentHashMap<Location, Alarm>();

	public ProfileSwitcherHandler(Context context, Looper looper) {
		super(looper);
		mContext = context;

		LocationManager lm = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		mCurrentLoc = lm
				.getLastKnownLocation(PhoneToolsUtil.getLocProvider(lm));
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);

		PowerManager pm = (PowerManager) mContext
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock lock = null;

		try {
			lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
					ProfileSwitcherConstants.TAG);
			lock.setReferenceCounted(false);
			lock.acquire();

			Intent intent = null;

			if (msg.obj instanceof Intent) {
				intent = (Intent) msg.obj;
			}

			if (intent == null) {
				if (!processOtherMessages(msg)) {
					ActivityLog.logWarning(mContext,
							ProfileSwitcherConstants.TAG,
							"Handler receive null intent and the msg has unknown what:"
									+ msg.what);
				}
				return;
			}

			String action = intent.getAction();

			if (ProfileSwitcherConstants.ACTION_ALARM_ALERT.equals(action)) {
				byte[] buf = intent.getExtras().getByteArray(
						ProfileSwitcherConstants.ALARM_RAW_DATA);

				Parcel out = Parcel.obtain();
				out.unmarshall(buf, 0, buf.length);
				out.setDataPosition(0);

				Alarm a = Alarm.CREATOR.createFromParcel(out);

				if (a.id >= 0) {
					processAlarm(a);
				} else {
					processTempProfileExpire(a);
				}
			} else if (ProfileSwitcherConstants.ACTION_SET_PROFILE
					.equals(action)) {
				doChangeProfile(intent);
			} else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
				Alarms.disableExpiredAlarms(mContext);
			} else if (intent.getBooleanExtra(
					ProfileSwitcherConstants.DATA_NOTIFY, false)) {
				ProfileSwitcherUtils.cancelNotification(mContext);
			} else {
				Log.w(ProfileSwitcherConstants.TAG,
						"Handler receive unknown action:" + action);
			}

		} catch (Throwable t) {
			Log.e(ProfileSwitcherConstants.TAG, "Handler fail", t);
			ActivityLog.logError(mContext, ProfileSwitcherConstants.TAG,
					t.getLocalizedMessage());
		} finally {
			Alarms.setNextAlert(mContext);
			try {
				if (lock != null)
					lock.release();
			} catch (Throwable t) {

			}
		}
	}

	private boolean processOtherMessages(Message msg) {
		switch (msg.what) {
		case ProfileSwitcherConstants.MSG_WHAT_LOAD_LOCATION_ONLY_SCHEDULES:
			loadLocationOnlySchedules();
			break;
		case ProfileSwitcherConstants.MSG_WHAT_LOCATION_CHANGED:
			doLocationChanged((Location) msg.obj);
			break;
		case ProfileSwitcherConstants.MSG_WHAT_LOAD_LAST_KNOWN_PROFILE:
			doActivateLastKnownProfile(true);
		default:
			return false;
		}

		return true;
	}

	private void processAlarm(Alarm alarm) {
		Cursor c = null;

		try {
			clearTempLocationQueue();

			Uri uri = ContentUris
					.withAppendedId(Schedule.CONTENT_URI, alarm.id);

			c = mContext.getContentResolver()
					.query(uri, null, null, null, null);

			if (!c.moveToNext())
				return;

			boolean enable = c.getInt(c.getColumnIndex(Schedule.COLUMN_ENABLE)) == 1;

			// if a repeat alarm is not enable, do not active profile
			// if a non-repeat alarm comes but database says it is not enable,
			// set the profile anyway, since the enable state changed by
			// Alarms.setNextAlarm()
			if (!enable && alarm.time == 0)
				return;

			String locStr = c.getString(c
					.getColumnIndex(Schedule.COLUMN_LOCATION));

			boolean activeProfile = true;

			if (!TextUtils.isEmpty(locStr)) {
				// Has Location, query
				Location loc = LocationUtils.locationFromString(locStr);
				activeProfile = mCurrentLoc != null
						&& loc.distanceTo(mCurrentLoc) < ProfileSwitcherUtils
								.getLocationDistancePreference();

				if (!activeProfile)
					queueForTempLocation(loc, c.getLong(c
							.getColumnIndex(Schedule.COLUMN_PROFILE_ID)),
							c.getLong(c.getColumnIndex(Schedule.COLUMN_ID)));
			}

			if (activeProfile) {
				ProfileSwitcherUtils
						.activateProfile(mContext, c.getLong(c
								.getColumnIndex(Schedule.COLUMN_PROFILE_ID)),
								Profile.ACTIVE_AUTO, 0, c.getLong(c
										.getColumnIndex(Schedule.COLUMN_ID)));
			}
		} finally {
			if (c != null)
				c.close();
		}
	}

	private void processTempProfileExpire(Alarm a) {
		ProfileSwitcherUtils.activateProfile(mContext, a.profileId,
				Profile.ACTIVE_NONE, 0);
	}

	private void loadLocationOnlySchedules() {
		Cursor c = mContext.getContentResolver().query(
				Schedule.CONTENT_URI,
				null,
				Schedule.COLUMN_ENABLE + "=1 AND " + Schedule.COLUMN_START_TIME
						+ "=0", null, null);

		try {
			mLocationProfileMap.clear();

			if (c == null || c.getCount() == 0)
				return;

			int idxLoc = c.getColumnIndex(Schedule.COLUMN_LOCATION);

			while (c.moveToNext()) {
				String locStr = c.getString(idxLoc);

				Location loc = LocationUtils.locationFromString(locStr);

				mLocationProfileMap.put(loc, new Alarm(c));
			}

		} finally {
			if (c != null)
				c.close();
		}

	}

	private void queueForTempLocation(Location loc, long profileId,
			long scheduleId) {
		Alarm alarm = new Alarm();
		alarm.profileId = profileId;
		alarm.id = (int) scheduleId;

		mLocationProfileTempMap.put(loc, alarm);
	}

	private void clearTempLocationQueue() {
		mLocationProfileTempMap.clear();
	}

	private void doLocationChanged(Location location) {
		if (!PhoneToolsUtil.isBetterLocation(location, mCurrentLoc)) {
			return;
		}

		mCurrentLoc = location;

		ArrayList<Location> locations = new ArrayList<Location>(
				mLocationProfileMap.keySet());
		locations.addAll(mLocationProfileTempMap.keySet());

		Iterator<Location> it = locations.iterator();

		while (it.hasNext()) {
			Location loc = it.next();

			boolean activeProfile = mCurrentLoc != null
					&& loc.distanceTo(mCurrentLoc) < ProfileSwitcherUtils
							.getLocationDistancePreference();

			if (activeProfile) {
				if (mLocationProfileMap.containsKey(loc)) {
					Alarm alarm = mLocationProfileMap.get(loc);

					activeProfile = !alarm.daysOfWeek.isRepeatSet()
							|| (alarm.daysOfWeek.isRepeatSet() && alarm.daysOfWeek
									.getNextAlarm(Calendar.getInstance()) == 0);

					if (activeProfile) {
						ProfileSwitcherUtils.activateProfile(mContext,
								alarm.profileId, Profile.ACTIVE_AUTO, 0,
								alarm.id);
						clearTempLocationQueue();
					}
				} else if (mLocationProfileTempMap.containsKey(loc)) {
					Alarm alarm = mLocationProfileTempMap.get(loc);

					ProfileSwitcherUtils.activateProfile(mContext,
							alarm.profileId, Profile.ACTIVE_AUTO, 0, alarm.id);
					clearTempLocationQueue();
				}
			}
		}
	}

	private void doActivateLastKnownProfile(boolean activeLatestSchedule) {
		Cursor c = mContext.getContentResolver().query(
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
					doActivateLatestSchedule();

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

						mContext.getContentResolver().update(uri, values, null,
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
				ProfileSwitcherUtils.activateProfile(mContext, profileId,
						active, expireTime);
			} else if (activeLatestSchedule) {
				doActivateLatestSchedule();
			}

		} finally {
			if (c != null)
				c.close();
		}

	}

	private void doActivateLatestSchedule() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		long scheduleBegin = c.getTimeInMillis();
		long scheduleEnd = System.currentTimeMillis();

		Cursor cursor = mContext.getContentResolver().query(
				Schedule.CONTENT_URI,
				null,
				Schedule.COLUMN_ENABLE + "=1 AND " + Schedule.COLUMN_START_TIME
						+ ">0", null, null);

		try {
			if (cursor == null || cursor.getCount() == 0) {
				return;
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
				ProfileSwitcherUtils.activateProfile(mContext, profileId,
						Profile.ACTIVE_AUTO, 0, scheduleId);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}

	}

	private void doChangeProfile(Intent intent) {
		long profileId = intent.getLongExtra(Profile.COLUMN_ID, -1);

		if (profileId < 0) {
			ActivityLog.logError(mContext, ProfileSwitcherConstants.TAG,
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
			ActivityLog.logError(mContext, ProfileSwitcherConstants.TAG,
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

		if (mContext.getContentResolver().update(uri, values, null, null) == 0) {
			ActivityLog.logError(mContext, ProfileSwitcherConstants.TAG,
					"activate profile id:" + profileId + " not found");

			return;
		}

		switch (active) {
		case Profile.ACTIVE_AUTO:
			clearOtherAutoActiveProfile(profileId);
			
			if (isManualActiveProfileExists())
				return;
			break;
		case Profile.ACTIVE_MANUAL:
			clearOtherManualActiveProfile(profileId);
			break;
		case Profile.ACTIVE_MANUAL_TIME:
			clearOtherManualActiveProfile(profileId);
			break;
		case Profile.ACTIVE_NONE:
			doActivateLastKnownProfile(false);
			break;
		default:
			return;
		}

		ProfileSwitcherUtils.enableProfile(mContext, profileId, scheduleId);
	}

	private void clearOtherAutoActiveProfile(long profileId) {
		ContentValues values = new ContentValues();
		values.put(Profile.COLUMN_ACTIVE, Profile.ACTIVE_NONE);

		mContext.getContentResolver().update(
				Profile.CONTENT_URI,
				values,
				Profile.COLUMN_ACTIVE + "=? AND " + Profile.COLUMN_ID + "!=?",
				new String[] { String.valueOf(Profile.ACTIVE_AUTO),
						String.valueOf(profileId) });
	}

	private boolean isManualActiveProfileExists() {
		Cursor c = mContext.getContentResolver()
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

	private void clearOtherManualActiveProfile(long profileId) {
		ContentValues values = new ContentValues();
		values.put(Profile.COLUMN_ACTIVE, Profile.ACTIVE_NONE);

		mContext.getContentResolver().update(
				Profile.CONTENT_URI,
				values,
				Profile.COLUMN_ACTIVE + "!=? AND " + Profile.COLUMN_ID + "!=?",
				new String[] { String.valueOf(Profile.ACTIVE_AUTO),
						String.valueOf(profileId) });
	}

}