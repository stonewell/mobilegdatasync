package com.angelstone.android.easydialer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EasyDialerAppWidgetEventsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		intent.setClass(context, EasyDialerService.class);
		context.startService(intent);
	}

}
