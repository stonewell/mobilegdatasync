package com.angelstone.android.platform;

import java.util.Map;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts;
import android.telephony.gsm.SmsMessage;

import com.angelstone.android.R;

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
		PHONE_LOOKUP_TYPE = Contacts.Phones.TYPE;
		PHONE_LOOKUP_PHOTO_ID = Contacts.Phones.PERSON_ID;
	}

	public boolean parseSmsMessages(Intent intent, Map<String, String> messages) {
		Object[] pdus = (Object[]) intent.getSerializableExtra("pdus");

		if (pdus.length <= 0)
			return false;

		for (Object pdu : pdus) {
			SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu);
			String sender = msg.getOriginatingAddress();

			if (messages.containsKey(sender)) {
				messages.put(sender, messages.get(sender) + msg.getMessageBody());
			} else {
				messages.put(sender, msg.getMessageBody());
			}
		}
		return true;
	}

	public void setServiceForeground(Service service) {
	}

	public CharSequence getPhoneTypeLabel(int type) {
		return Contacts.Phones.getDisplayLabel(mCtx, type,
				mCtx.getString(R.string.unknown));
	}

	public Bitmap getPhoto(int photoId, int defaultResource) {
		Uri uri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, photoId);
		return Contacts.People.loadContactPhoto(mCtx, uri, defaultResource, null);
	}
}
