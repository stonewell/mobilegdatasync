package com.angelstone.android.callerid.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.angelstone.android.callerid.receiver.CallerIdReceiver;

public class CallerIdScreenMonitorService extends Service {
	private volatile Looper mLooper = null;
	private volatile Handler mHandler = null;
	private volatile CallerIdReceiver mReceiver = new CallerIdReceiver();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		HandlerThread thread = new HandlerThread("CallerIdScreenMonitor");
		thread.start();

		mLooper = thread.getLooper();

		mHandler = new Handler(mLooper);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);

		registerReceiver(mReceiver, intentFilter, null, mHandler);
		
		System.out.println("CallerId register screen receiver");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mLooper.quit();

		unregisterReceiver(mReceiver);
		System.out.println("CallerId screen receiver quit");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		if (intent.getBooleanExtra("QUIT", false)) {
			stopSelf(startId);
			stopSelf();
		}
	}

}
