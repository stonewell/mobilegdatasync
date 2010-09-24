package com.angelstone.android.platform;

import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.provider.Contacts;
import android.telephony.gsm.SmsMessage;

@SuppressWarnings("deprecation")
public class CompatCupcake extends SysCompat {
	public CompatCupcake(Context ctx) {
		super(ctx);
		CONTACT_URI = Contacts.People.CONTENT_URI;
		PHONE_URI = Contacts.Phones.CONTENT_URI;
		COLUMN_PHONE_NAME = Contacts.Phones.DISPLAY_NAME;
		COLUMN_PHONE_NUMBER = Contacts.Phones.NUMBER;
		COLUMN_CONTACT_ID = Contacts.People._ID;
		COLUMN_CONTACT_NAME = Contacts.People.DISPLAY_NAME;
		COLUMN_PHONE_CONTACT_ID = Contacts.Phones.PERSON_ID;
		COLUMN_PHONE_ID = "phones." + Contacts.Phones._ID;
		COLUMN_PHONE_ID_v16 = Contacts.Phones._ID;
		PHONE_TYPE = Contacts.Phones.TYPE;
		COLUMN_PHONE_TYPE = String.valueOf(Contacts.Phones.TYPE_MOBILE);
		PHONE_LOOKUP_FILTER_URI = Contacts.Phones.CONTENT_FILTER_URL;
		PHONE_LOOKUP_NAME = Contacts.Phones.DISPLAY_NAME;
		PHONE_LOOKUP_NUMBER = Contacts.Phones.NUMBER_KEY;
	}

	public boolean parseSmsMessages(Intent intent, Map<String, String> messages) {
		Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");

		if (pdus.length <= 0)
			return false;

		for (Object pdu : pdus) {
			SmsMessage msg = SmsMessage.createFromPdu((byte[])pdu);
			String sender = msg.getOriginatingAddress();

			if (messages.containsKey(sender)) {
				messages.put(sender, messages.get(sender) + msg.getMessageBody());
			} else {
				messages.put(sender, msg.getMessageBody());
			}
		}
		return true;
	}
}
