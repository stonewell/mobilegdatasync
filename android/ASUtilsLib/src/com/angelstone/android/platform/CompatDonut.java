package com.angelstone.android.platform;

import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;

public class CompatDonut extends CompatCupcake {

	public CompatDonut(Context ctx) {
		super(ctx);
	}

	public boolean parseSmsMessages(Intent intent, Map<String, String> messages) {
		Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");

		if (pdus.length <= 0)
			return false;

		for (Object pdu : pdus) {
			SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu);
			String sender = msg.getOriginatingAddress();

			if (messages.containsKey(sender)) {
				messages.put(sender,
						messages.get(sender) + msg.getMessageBody());
			} else {
				messages.put(sender, msg.getMessageBody());
			}
		}
		return true;
	}

	public void setServiceForeground(Service service) {
		service.setForeground(true);
	}

}
