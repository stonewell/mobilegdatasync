package com.angelstone.android.callfirewall.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.angelstone.android.callfirewall.CallFireWallConstants;
import com.angelstone.android.callfirewall.CallFireWallService;

public class CallFireWallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			intent.setClass(context, CallFireWallService.class);
			context.startService(intent);
		} catch (Throwable t) {
			if (Log.isLoggable(CallFireWallConstants.TAG, Log.ERROR))
				Log.e(CallFireWallConstants.TAG, "onReceive Fail", t);
		}
	}

}
