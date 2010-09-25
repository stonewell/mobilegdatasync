package com.angelstone.android.smsblocker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.angelstone.android.smsblocker.SmsBlocker;

public class SmsReceivedReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		try {
			if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
				if (SmsBlocker.isSmsBlocked(intent, context)) {
					this.setOrderedHint(true);
					this.abortBroadcast();
				}
			} else if (action.equals("android.provider.Telephony.WAP_PUSH_RECEIVED")) {
				if (SmsBlocker.isMmsBlocked(intent, context)) {
					this.setOrderedHint(true);
					this.abortBroadcast();
				}
			}
		} catch (Exception e) {
			Log.e("SmsBlocker", e.getLocalizedMessage(), e);
		}
	}

}
