package com.angelstone.android.callerid.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;
import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.R;
import com.angelstone.android.callerid.store.CallerId;
import com.angelstone.android.callerid.store.CallerIdManager;
import com.angelstone.android.callerid.utils.PhotoLoader;
import com.angelstone.android.phonetools.utils.PhoneNumberMatcher;
import com.angelstone.android.phonetools.utils.PhoneUtils;
import com.angelstone.android.platform.SysCompat;
import com.angelstone.android.proxy.ServiceManagerProxy;
import com.angelstone.android.utils.ActivityLog;

public class FullScreenCallerIdView extends Activity implements OnClickListener {

	private PhotoLoader mPhotoLoader = null;
	private boolean mHide = false;
	private ITelephony mTelephony = null;
	private SysCompat mSysCompat = null;
	private Bitmap mContactPhoto = null;
	private long mId = 0;
	private String mNumber = null;
	private PowerManager.WakeLock mLock = null;
	private HandlerThread mHandlerThread = null;
	private Handler mHandler = null;
	private TelephonyManager mTelephonyManager = null;
	private CallerIdManager mCallerIdManager;
	private PhoneNumberMatcher mCallerIdMatcher;
	private KeyguardLock mKeyguardLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mSysCompat = SysCompat.register(this);

		Intent intent = getIntent();
		boolean hide = intent.getBooleanExtra(CallerIdConstants.DATA_HIDE,
				false);

		if (hide) {
			finish();
			return;
		}

		int flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;

		flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

		getWindow().addFlags(flags);

		setContentView(R.layout.fullscreen_caller_id);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		mLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "FullScreenCallerId");
		mLock.setReferenceCounted(false);
		mLock.acquire();

		mCallerIdManager = new CallerIdManager(CallerIdConstants.AUTHORITY);

		initCallerIdMatcher();

		mTelephony = ITelephony.Stub.asInterface(ServiceManagerProxy
				.getService(Context.TELEPHONY_SERVICE));
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		mKeyguardLock = km.newKeyguardLock("FullScreenCallerId");

		mHide = false;

		mPhotoLoader = new PhotoLoader(this, R.drawable.ic_contact_list_picture);
		mHandlerThread = new HandlerThread("YACallerId_ContactInfo_Loader",
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper());

		Button btn = (Button) findViewById(R.id.answerButton);
		btn.setOnClickListener(this);

		btn = (Button) findViewById(R.id.endButton);
		btn.setOnClickListener(this);

		if (mId < 0)
			mId = 0;

		mId = intent.getLongExtra(CallerIdConstants.DATA_ID, -1);
		mNumber = intent.getStringExtra(CallerIdConstants.DATA_INCOMING_NUMBER);

		Object result = mCallerIdMatcher.match(mNumber);

		if (result instanceof Long) {
			mId = (Long) result;
		}

		if (!isRing()) {
			finish();
		} else {
			mKeyguardLock.disableKeyguard();
			updateView(intent, mId);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mPhotoLoader != null) {
			mPhotoLoader.stop();
		}

		if (mContactPhoto != null)
			mContactPhoto.recycle();

		if (mTelephony != null && isAnswered()) {
			try {
				mTelephony.showCallScreen();
			} catch (RemoteException e) {
				ActivityLog.logError(this, getString(R.string.app_name),
						e.getLocalizedMessage());
				Log.e(CallerIdConstants.TAG, "Fail when show call screen", e);
			}
		} else if (mKeyguardLock != null) {
			mKeyguardLock.reenableKeyguard();
		}

		if (mHandler != null) {
			try {
				mHandler.getLooper().quit();
			} catch (Throwable t) {
				ActivityLog.logError(this, getString(R.string.app_name),
						t.getLocalizedMessage());
				Log.e(CallerIdConstants.TAG, "Fail when quit handler thread", t);
			}
		}

		try {
			if (mLock != null && mLock.isHeld())
				mLock.release();
		} catch (Throwable t) {
			ActivityLog.logError(this, getString(R.string.app_name),
					t.getLocalizedMessage());
			Log.e(CallerIdConstants.TAG, "Fail when release lock", t);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mPhotoLoader != null)
			mPhotoLoader.resume();

		try {
			if (mLock != null && !mLock.isHeld())
				mLock.acquire();
		} catch (Throwable t) {
			ActivityLog.logError(this, getString(R.string.app_name),
					t.getLocalizedMessage());
			Log.e(CallerIdConstants.TAG, "Fail when acquire lock", t);
		}

		if (mHide || !isRing())
			finish();
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			if (mLock != null && mLock.isHeld())
				mLock.release();

			if (mPhotoLoader != null)
				mPhotoLoader.pause();
		} catch (Throwable t) {
			ActivityLog.logError(this, getString(R.string.app_name),
					t.getLocalizedMessage());
			Log.e(CallerIdConstants.TAG, "Fail when release lock", t);
		}

		if (!mHide && isRing()) {
			Intent intent = new Intent(getApplicationContext(),
					FullScreenCallerIdView.class);

			intent.putExtra(CallerIdConstants.DATA_PAUSE_Call, true);
			intent.putExtra(CallerIdConstants.DATA_ID, mId);
			intent.putExtra(CallerIdConstants.DATA_INCOMING_NUMBER, mNumber);

			startActivity(intent);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (mHide) {
			finish();

			return;
		}

		boolean hide = intent.getBooleanExtra(CallerIdConstants.DATA_HIDE,
				false);

		if (hide || !isRing()) {
			mHide = true;
			finish();
		} else {
			if (intent
					.getBooleanExtra(CallerIdConstants.DATA_PAUSE_Call, false)) {
				return;
			}

			long id = intent.getLongExtra(CallerIdConstants.DATA_ID, -1);

			if (id < 0)
				finish();
			else {
				updateView(intent, id);
			}
		}
	}

	private void updateView(Intent intent, final long id) {
		ImageView iv = (ImageView) findViewById(R.id.caller_img);

		if (id > 0) {
			mPhotoLoader.loadPhoto(iv, id);
		}

		TextView tv = (TextView) findViewById(R.id.incoming_number);

		String number = intent
				.getStringExtra(CallerIdConstants.DATA_INCOMING_NUMBER);

		if (TextUtils.isEmpty(number))
			number = getString(R.string.unknown);

		tv.setText(number);

		final String fNumber = number;

		mHandler.post(new Runnable() {

			@Override
			public void run() {
				// Load Contacts Infomation
				Cursor cur = getContentResolver().query(
						Uri.withAppendedPath(
								mSysCompat.PHONE_LOOKUP_FILTER_URI, fNumber),
						new String[] { mSysCompat.PHONE_LOOKUP_NUMBER,
								mSysCompat.PHONE_LOOKUP_NAME,
								mSysCompat.PHONE_LOOKUP_TYPE,
								mSysCompat.PHONE_LOOKUP_PHOTO_ID }, null, null,
						null);

				try {
					String name = "";
					String type = "";

					if (cur.moveToNext()) {
						int idxName = cur
								.getColumnIndex(mSysCompat.PHONE_LOOKUP_NAME);
						int idxType = cur
								.getColumnIndex(mSysCompat.PHONE_LOOKUP_TYPE);
						int idxPhoto = cur
								.getColumnIndex(mSysCompat.PHONE_LOOKUP_PHOTO_ID);

						name = cur.getString(idxName);
						type = mSysCompat
								.getPhoneTypeLabel(cur.getInt(idxType))
								.toString();

						if (id <= 0) {
							if (mContactPhoto != null)
								mContactPhoto.recycle();

							mContactPhoto = mSysCompat.getPhoto(
									cur.getInt(idxPhoto),
									R.drawable.ic_contact_list_picture);

							final ImageView contactImage = (ImageView) findViewById(R.id.contact_img);
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									contactImage.setImageBitmap(mContactPhoto);
								}
							});
						}
					}

					final TextView ci = (TextView) findViewById(R.id.contact_info);

					final String txt = name + " " + type;
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							ci.setText(txt);
						}
					});
				} finally {
					cur.close();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
			case R.id.answerButton:
				try {
					mTelephony.answerRingingCall();
				} catch (Throwable t) {
					// fall back to bluetooth button
					// http://code.google.com/p/auto-answer/source/browse/trunk/src/com/everysoft/autoanswer/AutoAnswerIntentService.java
					PhoneUtils.answerPhoneHeadsethook(this);
				}
				break;
			case R.id.endButton:
				mTelephony.endCall();
				break;
			}

			if (mHide || !isRing())
				finish();

		} catch (Throwable e) {
			ActivityLog.logError(this, getString(R.string.app_name),
					e.getLocalizedMessage());
			Log.e(CallerIdConstants.TAG, "Fail when do handling ringing call",
					e);
		}
	}

	private boolean isRing() {
		return mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_RINGING;
	}

	private boolean isAnswered() {
		return mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK;
	}

	private void initCallerIdMatcher() {
		mCallerIdMatcher = new PhoneNumberMatcher();
		mCallerIdMatcher.getCountryCodes().add("+86");
		mCallerIdMatcher.getAreaCodes().add("010");
		mCallerIdMatcher.build();

		updateCallerIdNumbers();
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
