package com.angelstone.android.callfirewall;

import java.util.HashSet;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.angelstone.android.callfirewall.ui.CallFireWallMainView;
import com.angelstone.android.phonetools.store.BlackList;
import com.angelstone.android.phonetools.store.EventLog;
import com.angelstone.android.phonetools.store.PhoneToolsDBManager;
import com.angelstone.android.phonetools.store.PhoneToolsDatabaseValues;
import com.angelstone.android.phonetools.utils.PhoneNumberMatcher;
import com.angelstone.android.platform.SysCompat;
import com.angelstone.android.utils.ActivityLog;

public class CallFireWallService extends Service {
	static {
		PhoneToolsDBManager.initialize(CallFireWallConstants.AUTHORITY);
	}

	private final class CallFireWallServiceHandler extends Handler {

		public CallFireWallServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Context context = CallFireWallService.this;

			if (!isCallFireWallEnabled()) {
				if (mInitialized)
					hookPhoneStateChange(false);

				try {
					stopSelf(msg.arg1);
				} catch (Throwable t) {

				}

				stopSelf();
				return;
			}

			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock lock = null;

			try {
				lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
						CallFireWallConstants.TAG);
				lock.setReferenceCounted(false);
				lock.acquire();

				switch (msg.what) {
				case MSG_INTENT: {
					Intent intent = (Intent) msg.obj;

					if (intent != null
							&& intent.getBooleanExtra(CallFireWallConstants.DATA_NOTIFY,
									false)) {
						cancelNotification();
					}
				}
				case MSG_INIT: {
					if (!mInitialized) {
						initialize();
						initWhiteList();
						initBlackList();
						regContentObserver();
						hookPhoneStateChange(true);
						mInitialized = true;
					}
				}
					break;
				case MSG_CALL_BLOCKED: {
					String incomingNumber = msg.getData().getString(BLOCK_NUMBER);
					long date = msg.getData().getLong(BLOCK_DATE);
					// write call reject log
					WriteToCallRejectLog(incomingNumber, date);

					notifyIncomingCallBlocked(incomingNumber, date);
				}
					break;
				case MSG_UPDATE_WHITE_LIST: {
					updateWhitelistNumbers();
				}
					break;
				case MSG_UPDATE_BLACK_LIST: {
					updateBlacklistNumbers();
				}
					break;
				default:
					break;
				}
			} catch (Throwable t) {
				Log.e(CallFireWallConstants.TAG, "CallFirewall get exception", t);
			} finally {
				try {
					if (lock != null)
						lock.release();
				} catch (Throwable t) {

				}
			}
		}

	}

	private final class CallFireWallCallStateChangeListener extends
			PhoneStateListener {

		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING: {

				try {
					mSysCompat.SetRingerSilence(audioManager, true);

					Object result = mWhiteListMatcher.match(incomingNumber);

					if (!ONE.equals(result)
							&& ONE.equals(mBlackListMatcher.match(incomingNumber))) {

						tpCallModule.endCall();

						Message msg = new Message();
						msg.what = MSG_CALL_BLOCKED;
						Bundle data = new Bundle();
						data.putLong(BLOCK_DATE, System.currentTimeMillis());
						data.putString(BLOCK_NUMBER, incomingNumber);
						msg.setData(data);
						handler_.sendMessage(msg);
					}

					break;
				} catch (Throwable e) {
					ActivityLog.logError(CallFireWallService.this, "CallFirewall",
							e.getLocalizedMessage());
					Log.e("CallFirewall", "Fail when do firewall operation", e);
					mSysCompat.SetRingerSilence(audioManager, false);
				} finally {
					audioManager.setRingerMode(mRingerMode);
					audioManager.setStreamVolume(AudioManager.STREAM_RING, mRingerVolume,
							AudioManager.FLAG_ALLOW_RINGER_MODES
									| AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_VIBRATE);
				}
			}
				break;
			default:
				mRingerMode = audioManager.getRingerMode();
				mRingerVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
				break;
			}
		}
	}

	private static final int MSG_INIT = 0;
	private static final int MSG_INTENT = 1;
	private static final int MSG_CALL_BLOCKED = 2;
	private static final int MSG_UPDATE_WHITE_LIST = 3;
	private static final int MSG_UPDATE_BLACK_LIST = 4;

	private static final Integer ONE = new Integer(1);

	private static final String BLOCK_DATE = "BLOCK_DATE";
	private static final String BLOCK_NUMBER = "BLOCK_NUMBER";
	private static final int BLOCKED_CALL_NOTIFICATION = 1;

	private ITelephony tpCallModule = null;

	private AudioManager audioManager = null;

	private SysCompat mSysCompat = null;

	private PhoneNumberMatcher mWhiteListMatcher = null;
	private PhoneNumberMatcher mBlackListMatcher = null;

	private int mRingerMode = 0;
	private int mRingerVolume = 0;
	private int mNumberMissedCalls = 0;

	private HandlerThread handler_thread_ = null;
	private Handler handler_ = null;

	private TelephonyManager mTelephonyMgr = null;
	private NotificationManager mNotificationMgr = null;

	private boolean mInitialized = false;

	private CallFireWallCallStateChangeListener mPhoneStateListener = new CallFireWallCallStateChangeListener();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();

		handler_thread_ = new HandlerThread("AS.CallFireWall_Service_Handler");
		handler_thread_.start();

		handler_ = new CallFireWallServiceHandler(handler_thread_.getLooper());

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

	private void WriteToCallRejectLog(String incomeNumber, long date) {
		EventLog evt = new EventLog(PhoneNumberUtils.formatNumber(incomeNumber),
				date);

		PhoneToolsDBManager.getEventLogManager().writeLog(this, evt);
	}

	private void initBlackList() {
		mBlackListMatcher = new PhoneNumberMatcher();
		mBlackListMatcher.getCountryCodes().add("+86");
		mBlackListMatcher.getAreaCodes().add("010");
		mBlackListMatcher.build();

		updateBlacklistNumbers();
	}

	private void initWhiteList() {
		mWhiteListMatcher = new PhoneNumberMatcher();
		mWhiteListMatcher.getCountryCodes().add("+86");
		mWhiteListMatcher.getAreaCodes().add("010");

		updateWhitelistNumbers();
	}

	private void hookPhoneStateChange(boolean register) {
		mTelephonyMgr.listen(mPhoneStateListener,
				register ? PhoneStateListener.LISTEN_CALL_STATE
						: PhoneStateListener.LISTEN_NONE);
	}

	private void initialize() {
		mSysCompat = SysCompat.register(this);

		mSysCompat.setServiceForeground(this);

		mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		mRingerMode = audioManager.getRingerMode();

		tpCallModule = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));

		mNotificationMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private boolean isCallFireWallEnabled() {
		return PhoneToolsDBManager.getSettingsManager().readSetting(this,
				CallFireWallConstants.OPTION_ENABLE);
	}

	private void notifyIncomingCallBlocked(String number, long date) {
		// title resource id
		int titleResId;
		// the text in the notification's line 1 and 2.
		String expandedText;

		// increment number of missed calls.
		mNumberMissedCalls++;

		// display the first line of the notification:
		// 1 missed call: call name
		// more than 1 missed call: <number of calls> + "missed calls"
		if (mNumberMissedCalls == 1) {
			titleResId = R.string.notification_blockedCallTitle;
			expandedText = number;
		} else {
			titleResId = R.string.notification_blockedCallsTitle;
			expandedText = getString(R.string.notification_blockedCallsMsg,
					mNumberMissedCalls);
		}

		// create the target call log intent
		final PendingIntent intent = getNotificationIntent();

		Notification notification = new Notification(
				android.R.drawable.stat_notify_missed_call, // icon
				getString(R.string.notification_blockedCallTicker, number), // tickerText
				date);

		notification.setLatestEventInfo(this, getText(titleResId), expandedText,
				intent);

		// make the notification
		mNotificationMgr.notify(BLOCKED_CALL_NOTIFICATION, notification);
	}

	private void regContentObserver() {
		getContentResolver().registerContentObserver(
				PhoneToolsDBManager.getSettingsManager().getContentUri(), true,
				new ContentObserver(handler_) {

					@Override
					public void onChange(boolean selfChange) {
						super.onChange(selfChange);

						handler_.sendEmptyMessage(MSG_UPDATE_WHITE_LIST);
					}
				});

		getContentResolver().registerContentObserver(
				PhoneToolsDBManager.getBlackListManager().getContentUri(), true,
				new ContentObserver(handler_) {

					@Override
					public void onChange(boolean selfChange) {
						super.onChange(selfChange);
						handler_.sendEmptyMessage(MSG_UPDATE_BLACK_LIST);
					}
				});

		getContentResolver().registerContentObserver(mSysCompat.PHONE_URI, true,
				new ContentObserver(handler_) {

					@Override
					public void onChange(boolean selfChange) {
						super.onChange(selfChange);

						handler_.sendEmptyMessage(MSG_UPDATE_WHITE_LIST);
					}
				});
	}

	private void updateBlacklistNumbers() {
		Set<String> newNumbers = new HashSet<String>();

		Cursor c = PhoneToolsDBManager.getBlackListManager().getBlacklistNumbers(
				this);

		try {
			int idxNumber = c.getColumnIndex(BlackList.COL_NUMBER);
			int idxBlock = c.getColumnIndex(BlackList.COL_BLOCK);

			while (c.moveToNext()) {
				int block = c.getInt(idxBlock);

				if (block == 1) {
					newNumbers.add(c.getString(idxNumber));
				}
			}
		} finally {
			c.close();
		}

		updateNumberMatcher(mBlackListMatcher, newNumbers);
	}

	private void updateWhitelistNumbers() {
		Set<String> newNumbers = new HashSet<String>();

		if (PhoneToolsDBManager.getSettingsManager().readSetting(this,
				PhoneToolsDatabaseValues.OPTION_ALLOW_CONTACTS)) {
			Cursor cur = getContentResolver().query(mSysCompat.PHONE_URI, null,
					mSysCompat.COLUMN_PHONE_NUMBER + " is not null", null,
					"UPPER(" + mSysCompat.COLUMN_PHONE_NAME + ") ASC");
			try {
				int idxNumber = cur.getColumnIndex(mSysCompat.COLUMN_PHONE_NUMBER);

				while (cur.moveToNext()) {
					newNumbers.add(cur.getString(idxNumber));
				}
			} finally {
				cur.close();
			}
		}

		Cursor c = PhoneToolsDBManager.getBlackListManager().getBlacklistNumbers(
				this);

		try {
			int idxNumber = c.getColumnIndex(BlackList.COL_NUMBER);
			int idxBlock = c.getColumnIndex(BlackList.COL_BLOCK);

			while (c.moveToNext()) {
				int block = c.getInt(idxBlock);

				if (block == 0) {
					newNumbers.add(c.getString(idxNumber));
				}
			}
		} finally {
			c.close();
		}

		updateNumberMatcher(mWhiteListMatcher, newNumbers);
	}

	private void updateNumberMatcher(PhoneNumberMatcher matcher,
			Set<String> newNumbers) {
		Set<String> oldNumbers = new HashSet<String>(matcher.getNumbers().keySet());

		for (String n : oldNumbers) {
			if (!newNumbers.contains(n))
				matcher.removeNumber(n);
			else
				newNumbers.remove(n);
		}

		for (String n : newNumbers) {
			if (validNumber(n))
				matcher.addNumber(n, ONE);
		}
	}

	private PendingIntent getNotificationIntent() {
		Intent intent = new Intent(this, CallFireWallMainView.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(CallFireWallConstants.DATA_NOTIFY, true);

		return PendingIntent.getActivity(getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public void cancelNotification() {
		mNumberMissedCalls = 0;

		mNotificationMgr.cancel(BLOCKED_CALL_NOTIFICATION);
	}

	private boolean validNumber(String n) {
		if (TextUtils.isEmpty(n))
			return false;

		if (!(n.charAt(0) == '+') && !(n.charAt(0) >= '0' && n.charAt(0) <= '9')) {
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
