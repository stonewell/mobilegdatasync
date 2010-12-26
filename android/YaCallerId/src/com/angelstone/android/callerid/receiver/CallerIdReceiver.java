package com.angelstone.android.callerid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.service.CallerIdService;
import com.angelstone.android.utils.ActivityLog;

public class CallerIdReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			intent.setClass(context, CallerIdService.class);
			context.startService(intent);
		} catch (Throwable t) {
			ActivityLog.logError(context, CallerIdConstants.TAG,
					t.getLocalizedMessage());

			if (Log.isLoggable(CallerIdConstants.TAG, Log.ERROR))
				Log.e(CallerIdConstants.TAG, "onReceive Fail", t);
		}
	}

}
