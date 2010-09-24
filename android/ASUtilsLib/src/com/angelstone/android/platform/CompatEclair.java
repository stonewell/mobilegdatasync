package com.angelstone.android.platform;

import android.content.Context;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

public class CompatEclair extends CompatDonut {

	public CompatEclair(Context ctx) {
		super(ctx);
		CONTACT_URI = ContactsContract.Contacts.CONTENT_URI;
		PHONE_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		COLUMN_PHONE_NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
		COLUMN_PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
		COLUMN_CONTACT_ID = ContactsContract.Contacts._ID;
		COLUMN_CONTACT_NAME = ContactsContract.Contacts.DISPLAY_NAME;
		COLUMN_PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
		COLUMN_PHONE_ID = ContactsContract.CommonDataKinds.Phone._ID;
		PHONE_TYPE = ContactsContract.CommonDataKinds.Phone.TYPE;
		COLUMN_PHONE_TYPE = String
				.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
		COLUMN_PHONE_ID_v16 = ContactsContract.CommonDataKinds.Phone._ID;
		PHONE_LOOKUP_FILTER_URI = PhoneLookup.CONTENT_FILTER_URI;
		PHONE_LOOKUP_NAME = PhoneLookup.DISPLAY_NAME;
		PHONE_LOOKUP_NUMBER = PhoneLookup.NUMBER;
	}
}
