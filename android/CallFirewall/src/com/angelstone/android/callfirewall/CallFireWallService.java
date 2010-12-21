package com.angelstone.android.callfirewall;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.ServiceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.angelstone.android.callfirewall.store.DatabaseValues;
import com.angelstone.android.phonetools.store.EventLog;
import com.angelstone.android.phonetools.store.PhoneToolsDBManager;
import com.angelstone.android.phonetools.utils.PhoneNumberMatcher;
import com.angelstone.android.platform.SysCompat;
import com.angelstone.android.utils.ActivityLog;

public class CallFireWallService extends Service {
	static {
		PhoneToolsDBManager.initialize(DatabaseValues.AUTHORITY);
	}

	private ITelephony tpCallModule = null;

	private AudioManager audioManager = null;

	private SysCompat mSysCompat = null;

	private PhoneNumberMatcher mWhiteListMatcher = null;
	private PhoneNumberMatcher mBlackListMatcher = null;

	private static final Integer ONE = new Integer(1);

	private int mRingerMode = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();

		mSysCompat = SysCompat.register(this);

		mSysCompat.setServiceForeground(this);

		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyMgr.listen(new TeleListener(),
				PhoneStateListener.LISTEN_CALL_STATE);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		mRingerMode = audioManager.getRingerMode();

		tpCallModule = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
		
		initWhiteList();
		initBlackList();
	}

	class TeleListener extends PhoneStateListener {

		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING: {

				try {
					mSysCompat.SetRingerSilence(audioManager, true);

					Object result = mWhiteListMatcher.match(incomingNumber);

					if (!ONE.equals(result)
							&& ONE.equals(mBlackListMatcher
									.match(incomingNumber))) {

						tpCallModule.endCall();

						// write call reject log
						WriteToCallRejectLog(incomingNumber);
					}

					break;
				} catch (Throwable e) {
 					ActivityLog.logError(CallFireWallService.this,
							"CallFirewall", e.getLocalizedMessage());
					Log.e("CallFirewall", "Fail when do firewall operation", e);
					mSysCompat.SetRingerSilence(audioManager, false);
				} finally {
					audioManager.setRingerMode(mRingerMode);
				}
			}
				break;
			default:
				mRingerMode = audioManager.getRingerMode();
				break;
			}
		}
	}

	private void WriteToCallRejectLog(String incomeNumber) {
		EventLog evt = new EventLog(PhoneNumberUtils.formatNumber(incomeNumber));

		PhoneToolsDBManager.getEventLogManager().writeLog(this, evt);
	}

	private void initBlackList() {
		mBlackListMatcher = new PhoneNumberMatcher();
		mBlackListMatcher.getCountryCodes().add("+86");
		mBlackListMatcher.getAreaCodes().add("010");
		mBlackListMatcher.addNumber("12345", 1);
		mBlackListMatcher.addNumber("62725169", 1);
		mBlackListMatcher.build();
	}

	private void initWhiteList() {
		mWhiteListMatcher = new PhoneNumberMatcher();
		mWhiteListMatcher.getCountryCodes().add("+86");
		mWhiteListMatcher.getAreaCodes().add("010");
		mWhiteListMatcher.build();
	}

}
