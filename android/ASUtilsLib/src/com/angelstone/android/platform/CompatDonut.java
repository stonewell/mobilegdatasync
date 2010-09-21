package com.angelstone.android.platform;

import android.app.Service;
import android.content.Context;

public class CompatDonut extends CompatCupcake {

	public CompatDonut(Context ctx) {
		super(ctx);
	}

	@Override
	public void sendMsg(String phoneNum, String sms_str) {
		// final android.telephony.SmsManager sm =
		// android.telephony.SmsManager.getDefault();
		// sm.sendTextMessage(phoneNum, null, sms_str, null, null);
	}

	public void setServiceForeground(Service service) {
		service.setForeground(true);
	}
}
