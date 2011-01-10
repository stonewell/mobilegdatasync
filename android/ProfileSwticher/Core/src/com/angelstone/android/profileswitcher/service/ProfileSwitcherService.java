package com.angelstone.android.profileswitcher.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.angelstone.android.profileswitcher.R;
import com.angelstone.android.utils.HandlerThreadQuiter;
import com.angelstone.android.utils.PhoneToolsUtil;

public class ProfileSwitcherService extends Service {
	private class ProfileSwitcherHandler extends Handler {

		public ProfileSwitcherHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			try{
				
			}catch(Throwable t) {
				
			}finally {
				
			}
		}

	}

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
			if (PhoneToolsUtil.isBetterLocation(location, mCurrentLoc)) {
				//TODO: find if there is a profile to active
			}
		}
	};

	private ProfileSwitcherHandler mHandler;
	private HandlerThread mHandlerThread;
	private Location mCurrentLoc;
	private LocationManager mLocationManager;

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

		mHandler = new ProfileSwitcherHandler(mHandlerThread.getLooper());

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		mCurrentLoc = mLocationManager.getLastKnownLocation(PhoneToolsUtil
				.getLocProvider(mLocationManager));
		//TODO: Move time/distance to preference
		mLocationManager.requestLocationUpdates(
				PhoneToolsUtil.getLocProvider(mLocationManager), 600 * 1000, 500,
				mLocationListener); 
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
	}

}
