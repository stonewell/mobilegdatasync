package com.angelstone.android.profileswitcher.ui;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

class TimerProfileUpdateTask extends AsyncTask<Cursor, Integer, Integer> {
	private Object mWaiter = new Object();
	private boolean mStop;
	private boolean mPause;
	private Cursor mCursor;
	
	@Override
	protected Integer doInBackground(Cursor... params) {
		mCursor = params[0];
		mStop = false;
		mPause = false;

		while (!mStop) {
			if (!mPause) {
				synchronized (mWaiter) {
					try {
						mWaiter.wait(10 * 1000);
					} catch (InterruptedException e) {
					}
				}
			} else {
				synchronized (mWaiter) {
					try {
						mWaiter.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			publishProgress(1);
		}

		return 0;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		
		if (mCursor != null)
			mCursor.requery();
	}

	public void pause() {
		Log.e("pause","1!!!!!!pause");
		mPause = true;
		synchronized (mWaiter) {
			mWaiter.notify();
		}
	}

	public void resume() {
		Log.e("pause","1!!!!!!pause");
		mPause = false;
		synchronized (mWaiter) {
			mWaiter.notify();
		}
	}

	public void stop() {
		mPause = false;
		mStop = true;
		synchronized (mWaiter) {
			mWaiter.notify();
		}
	}
}
