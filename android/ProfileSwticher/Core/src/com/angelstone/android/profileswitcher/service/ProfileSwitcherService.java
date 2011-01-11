package com.angelstone.android.profileswitcher.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

import com.angelstone.android.profileswitcher.ProfileSwitcherConstants;
import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.profileswitcher.store.Schedule;
import com.angelstone.android.profileswitcher.utils.ProfileSwitcherUtils;
import com.angelstone.android.utils.HandlerThreadQuiter;
import com.angelstone.android.utils.PhoneToolsUtil;

public class ProfileSwitcherService extends Service {
	private final LocationListener mLocationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {
			Message msg = Message.obtain(mHandler,
					ProfileSwitcherConstants.MSG_WHAT_LOCATION_CHANGED,
					location);

			mHandler.sendMessage(msg);
		}
	};

	private ProfileSwitcherHandler mHandler;
	private HandlerThread mHandlerThread;
	private LocationManager mLocationManager;
	private ContentObserver mScheduleObserver = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mHandlerThread = new HandlerThread(getString(R.string.app_name),
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();

		mHandler = new ProfileSwitcherHandler(this, mHandlerThread.getLooper());

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		mLocationManager
				.requestLocationUpdates(PhoneToolsUtil
						.getLocProvider(mLocationManager), ProfileSwitcherUtils
						.getLocationUpdateTimePreference() * 60 * 1000,
						ProfileSwitcherUtils
								.getLocationUpdateDistancePreference(),
						mLocationListener);

		mScheduleObserver = new ContentObserver(mHandler) {

			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);

				requestLoadLocationOnlySchedules();
			}

		};

		getContentResolver().registerContentObserver(Schedule.CONTENT_URI,
				true, mScheduleObserver);

		requestLoadLocationOnlySchedules();
		requestActivateLastKnownProfile();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		Message msg = Message.obtain(mHandler, startId, intent);

		mHandler.sendMessage(msg);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		try {
			HandlerThreadQuiter.quit(mHandlerThread);
		} catch (VerifyError e) {
			mHandler.getLooper().quit();
		}

		mLocationManager.removeUpdates(mLocationListener);
		getContentResolver().unregisterContentObserver(mScheduleObserver);
	}

	private void requestLoadLocationOnlySchedules() {
		mHandler.sendEmptyMessage(ProfileSwitcherConstants.MSG_WHAT_LOAD_LOCATION_ONLY_SCHEDULES);
	}

	private void requestActivateLastKnownProfile() {
		mHandler.sendEmptyMessage(ProfileSwitcherConstants.MSG_WHAT_LOAD_LAST_KNOWN_PROFILE);
	}

}
