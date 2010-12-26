package com.angelstone.android.platform;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.PhoneLookup;

import com.angelstone.android.R;

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
		PHONE_LOOKUP_TYPE = PhoneLookup.TYPE;
		PHONE_LOOKUP_PHOTO_ID = PhoneLookup.PHOTO_ID;
	}

	public void setServiceForeground(Service service) {
		service.startForeground(999, new Notification());
	}

	public CharSequence getPhoneTypeLabel(int type) {
		return ContactsContract.CommonDataKinds.Phone.getTypeLabel(
				mCtx.getResources(), type, mCtx.getString(R.string.unknown));
	}

	public Bitmap getPhoto(int photoId, int defaultResource) {
		Cursor c = mCtx.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI,
				new String[] { Photo._ID, Photo.PHOTO }, Photo._ID + "=" + photoId,
				null, null);

		try {
			if (c.moveToNext()) {
				byte[] buf = c.getBlob(1);

				return BitmapFactory.decodeByteArray(buf, 0, buf.length);
			}

			return BitmapFactory.decodeResource(mCtx.getResources(), defaultResource);
		} finally {
			c.close();
		}
	}
}
