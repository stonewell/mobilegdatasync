package com.angelstone.android.callerid.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.store.CallerId;
import com.angelstone.android.callerid.store.CallerIdManager;
import com.angelstone.android.callerid.ui.FullScreenCallerIdView;
import com.angelstone.android.phonetools.utils.PhoneNumberMatcher;
import com.angelstone.android.utils.ActivityLog;

public class CallerIdService extends Service {

	private final class CallerIdServiceHandler extends Handler {

		public CallerIdServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Context context = CallerIdService.this;

			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock lock = null;

			try {
				lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
						CallerIdConstants.TAG);
				lock.setReferenceCounted(false);
				lock.acquire();

				switch (msg.what) {
				case MSG_INTENT: {
					break;
				}
				case MSG_INIT: {
					if (!mInitialized) {
						initialize();
						initCallerIdMatcher();
						regContentObserver();
						hookPhoneStateChange(true);
						mInitialized = true;
					}
				}
					break;
				default:
					break;
				}
			} catch (Throwable t) {
				Log.e(CallerIdConstants.TAG, "CallerId service get exception",
						t);
			} finally {
				try {
					if (lock != null)
						lock.release();
				} catch (Throwable t) {

				}
			}
		}

	}

	private final class CallStateChangeListener extends PhoneStateListener {

		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			try {
				switch (state) {
				case TelephonyManager.CALL_STATE_RINGING: {

					mFullLock.acquire();

					Object result = mCallerIdMatcher.match(incomingNumber);

					long id = -1;

					if (result instanceof Long)
						id = (Long) result;

					Intent intent = new Intent(
							CallerIdService.this.getApplicationContext(),
							FullScreenCallerIdView.class);
					intent.putExtra(CallerIdConstants.DATA_ID, id);
					intent.putExtra(CallerIdConstants.DATA_INCOMING_NUMBER,
							incomingNumber);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_FROM_BACKGROUND
							| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

					startActivity(intent);
					mFullScreenCallerIdViewVisible = true;
				}
					break;
				default: {
					if (mFullScreenCallerIdViewVisible) {
						try {
							mFullLock.release();
						} catch (Throwable t) {

						}
						Intent intent = new Intent(
								CallerIdService.this.getApplicationContext(),
								FullScreenCallerIdView.class);
						intent.putExtra(CallerIdConstants.DATA_HIDE, true);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_FROM_BACKGROUND
								| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

						startActivity(intent);
						mFullScreenCallerIdViewVisible = false;
					}
				}
					break;
				}
			} catch (Throwable e) {
				ActivityLog.logError(CallerIdService.this, "CallerId",
						e.getLocalizedMessage());
				Log.e(CallerIdConstants.TAG,
						"Fail when do caller id operation", e);
			}
		}
	}

	private static final int MSG_INIT = 0;
	private static final int MSG_INTENT = 1;
	private static final int MSG_UPDATE_NUMBERS = 2;

	private PhoneNumberMatcher mCallerIdMatcher = null;

	private HandlerThread handler_thread_ = null;
	private Handler handler_ = null;

	private boolean mInitialized = false;

	private CallStateChangeListener mPhoneStateListener = new CallStateChangeListener();
	private TelephonyManager mTelephonyMgr = null;
	private CallerIdManager mCallerIdManager = null;
	private boolean mFullScreenCallerIdViewVisible = false;
	private PowerManager.WakeLock mFullLock = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();

		handler_thread_ = new HandlerThread("AS.CallFireWall_Service_Handler");
		handler_thread_.start();

		handler_ = new CallerIdServiceHandler(handler_thread_.getLooper());

		handler_.sendEmptyMessage(MSG_INIT);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		if (intent == null) {
			return;
		}

		Message msg = new Message();
		msg.arg1 = startId;
		msg.obj = intent;
		msg.what = MSG_INTENT;
		handler_.sendMessage(msg);
	}

	private void initCallerIdMatcher() {
		mCallerIdMatcher = new PhoneNumberMatcher();
		mCallerIdMatcher.getCountryCodes().add("+86");
		mCallerIdMatcher.getAreaCodes().add("010");
		mCallerIdMatcher.build();

		updateCallerIdNumbers();
	}

	private void hookPhoneStateChange(boolean register) {
		mTelephonyMgr.listen(mPhoneStateListener,
				register ? PhoneStateListener.LISTEN_CALL_STATE
						: PhoneStateListener.LISTEN_NONE);
	}

	private void initialize() {
		mCallerIdManager = new CallerIdManager(CallerIdConstants.AUTHORITY);
		mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		mFullLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE, "CallerIdServiceFullLocker");

	}

	private void regContentObserver() {
		getContentResolver().registerContentObserver(
				mCallerIdManager.getContentUri(), true,
				new ContentObserver(handler_) {

					@Override
					public void onChange(boolean selfChange) {
						super.onChange(selfChange);

						handler_.sendEmptyMessage(MSG_UPDATE_NUMBERS);
					}
				});
	}

	private void updateCallerIdNumbers() {
		HashMap<String, Long> newNumbers = new HashMap<String, Long>();

		Cursor c = mCallerIdManager.getCallerIds(this);

		try {
			int idxNumber = c.getColumnIndex(CallerId.COL_NUMBER);
			int idxId = c.getColumnIndex(CallerId.COL_ID);

			while (c.moveToNext()) {
				newNumbers.put(c.getString(idxNumber), c.getLong(idxId));
			}
		} finally {
			c.close();
		}

		updateNumberMatcher(mCallerIdMatcher, newNumbers);
	}

	private void updateNumberMatcher(PhoneNumberMatcher matcher,
			HashMap<String, Long> newNumbers) {
		Set<String> oldNumbers = new HashSet<String>(matcher.getNumbers()
				.keySet());

		for (String n : oldNumbers) {
			if (!newNumbers.containsKey(n))
				matcher.removeNumber(n);
			else
				newNumbers.remove(n);
		}

		for (String n : newNumbers.keySet()) {
			if (validNumber(n))
				matcher.addNumber(n, newNumbers.get(n));
		}
	}

	private boolean validNumber(String n) {
		if (TextUtils.isEmpty(n))
			return false;

		if (!(n.charAt(0) == '+')
				&& !(n.charAt(0) >= '0' && n.charAt(0) <= '9')) {
			return false;
		}

		for (int i = 1; i < n.length(); i++) {
			if (!(n.charAt(i) >= '0' && n.charAt(i) <= '9')) {
				return false;
			}
		}

		return true;
	}
}
