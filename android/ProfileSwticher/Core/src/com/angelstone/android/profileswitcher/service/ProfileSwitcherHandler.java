package com.angelstone.android.profileswitcher.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentUris;
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
import android.os.Parcelable;
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
	private ConcurrentHashMap<Location, Long> mLocationProfileMap = new ConcurrentHashMap<Location, Long>();
	private ConcurrentHashMap<Location, Long> mLocationProfileTempMap = new ConcurrentHashMap<Location, Long>();

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
				Log.w(ProfileSwitcherConstants.TAG,
						"Handler receive null intent");

				processOtherMessages(msg);
				return;
			}

			String action = intent.getAction();

			if (ProfileSwitcherConstants.ACTION_ALARM_ALERT.equals(action)) {
				Parcelable p = intent.getExtras().getParcelable(
						ProfileSwitcherConstants.ALARM_RAW_DATA);

				Parcel out = Parcel.obtain();
				p.writeToParcel(out, 0);
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
			} else {
				Log.w(ProfileSwitcherConstants.TAG,
						"Handler receive unknown action:" + action);
			}

		} catch (Throwable t) {
			Log.e(ProfileSwitcherConstants.TAG, "Handler fail", t);
			ActivityLog.logError(mContext, ProfileSwitcherConstants.TAG,
					t.getLocalizedMessage());
		} finally {
			try {
				if (lock != null)
					lock.release();
			} catch (Throwable t) {

			}
		}
	}

	private void processOtherMessages(Message msg) {
		switch (msg.what) {
		case ProfileSwitcherConstants.MSG_WHAT_LOAD_LOCATION_ONLY_SCHEDULES:
			loadLocationOnlySchedules();
			break;
		case ProfileSwitcherConstants.MSG_WHAT_LOCATION_CHANGED:
			doLocationChanged((Location) msg.obj);
			break;
		case ProfileSwitcherConstants.MSG_WHAT_LOAD_LAST_KNOWN_PROFILE:
			doActivateLastKnownProfile();
		default:
			break;
		}

	}

	public void processAlarm(Alarm alarm) {
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

			if (!enable)
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
							.getColumnIndex(Schedule.COLUMN_PROFILE_ID)));
			}

			if (activeProfile) {
				ProfileSwitcherUtils
						.activateProfile(mContext, c.getLong(c
								.getColumnIndex(Schedule.COLUMN_PROFILE_ID)),
								Profile.ACTIVE_AUTO, 0);
			}
		} finally {
			if (c != null)
				c.close();

			Alarms.setNextAlert(mContext);
		}
	}

	void processTempProfileExpire(Alarm a) {
		Cursor c = null;

		try {
			ProfileSwitcherUtils.activateProfile(mContext, a.profileId,
					Profile.ACTIVE_NONE, 0);

			c = mContext.getContentResolver().query(Profile.CONTENT_URI,
					new String[] { Profile.COLUMN_ID },
					Profile.COLUMN_ACTIVE + "=?",
					new String[] { String.valueOf(Profile.ACTIVE_AUTO) }, null);

			if (c != null && c.moveToNext()) {
				ProfileSwitcherUtils.activateProfile(mContext, c.getLong(0),
						Profile.ACTIVE_AUTO, 0);
			}
		} finally {
			Alarms.setNextAlert(mContext);

			if (c != null)
				c.close();
		}
	}

	private void loadLocationOnlySchedules() {
		Cursor c = mContext.getContentResolver().query(
				Schedule.CONTENT_URI,
				new String[] { Schedule.COLUMN_LOCATION,
						Schedule.COLUMN_PROFILE_ID },
				Schedule.COLUMN_ENABLE + "=1 AND " + Schedule.COLUMN_START_TIME
						+ "=0", null, null);

		try {
			mLocationProfileMap.clear();

			if (c == null)
				return;

			while (c.moveToNext()) {
				String locStr = c.getString(0);
				long profileId = c.getLong(1);

				Location loc = LocationUtils.locationFromString(locStr);

				mLocationProfileMap.put(loc, profileId);
			}

		} finally {
			if (c != null)
				c.close();
		}

	}

	private void queueForTempLocation(Location loc, long profileId) {
		mLocationProfileTempMap.put(loc, profileId);
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
					ProfileSwitcherUtils.activateProfile(mContext,
							mLocationProfileMap.get(loc), Profile.ACTIVE_AUTO,
							0);
					clearTempLocationQueue();
				} else if (mLocationProfileTempMap.containsKey(loc)) {
					ProfileSwitcherUtils.activateProfile(mContext,
							mLocationProfileTempMap.get(loc),
							Profile.ACTIVE_AUTO, 0);
					clearTempLocationQueue();
				}
			}
		}
	}

	private void doChangeProfile(Intent intent) {
		// TODO Auto-generated method stub

	}

	private void doActivateLastKnownProfile() {
		// TODO Auto-generated method stub
		
	}
}