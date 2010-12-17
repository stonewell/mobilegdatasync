package com.angelstone.android.callfirewall;

import java.util.Date;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ViewGroup;

import com.angelstone.android.phonetools.store.EventLog;
import com.angelstone.android.phonetools.store.PhoneNumberDisposition;
import com.angelstone.android.platform.SysCompat;

public class CallFwService extends Service {

	private ITelephony tpCallModule = null;

	private AudioManager audioManager = null;

	private SysCompat mSysCompat = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
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

		tpCallModule = ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
	}

	class TeleListener extends PhoneStateListener {

		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);

			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING: {

				try {
					// audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					mSysCompat.SetRingerSilence(audioManager, true));

					disp = CallFwService.db.queryAction(incomingNumber);

					if (disp.m_CallAction == PhoneNumberDisposition.CALL_REJECT) {

						mSysCompat.SetRingerSilence(audioManager, false));

						boolean isEndCall = tpCallModule.endCall();

						// write call reject log
						WriteToCallRejectLog(incomingNumber, disp.m_ReplySms);

						break;
					}

					audioManager
							.setRingerMode(GlobalObjects.mCurrentRingtoneMode);
					break;
				} catch (Exception e) {
				}
			}
			default:
				break;
			}
		}

		private void WriteToCallRejectLog(String incomeNumber, String replySms) {
			EventLog evt = new EventLog(
					PhoneNumberUtils.formatNumber(incomeNumber),
					EventLog.LOG_TYPE_CALL);

			String tag = CallFwService.db.getTagByNumber(incomeNumber);
			evt.setTagOrName(tag);
			evt.setReplySmsTxt(replySms);
			evt.setBlockType(EventLog.CALL_LOG_BLOCK_TYPE_BL);

			CallFwService.db.writeLog(evt);
		}

	}

}
