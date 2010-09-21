package com.angelstone.android.platform;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.Notification;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

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
		// COLUMN_PHONE_PHOTO_ID = ContactsContract.Contacts.PHOTO_ID;
		// COLUMN_PHONE_DATA = ContactsContract.Data.DATA15;
		// DATA_URI = ContactsContract.Data.CONTENT_URI;
	}

	@Override
	public boolean ContactObserverNeedToUpdate(int counter) {
		return true;
	}

	@Override
	public void ContactObserverReg() {
		// silenceRejectCall.contactObserver.setInitialContactInfo();
		//
		// mCtx.getContentResolver().registerContentObserver(
		// ContactsContract.Contacts.CONTENT_URI, false,
		// silenceRejectCall.contactObserver);
		//
		//
	}

	@Override
	public void removePhone(String phoneNum, String ContactID) {

		Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

		Cursor cursor = mCtx.getContentResolver()
				.query(
						phoneUri,
						new String[] { ContactsContract.CommonDataKinds.Phone._ID },
						ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + phoneNum
								+ "'", null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String person = Integer.toString(cursor.getInt(cursor
					.getColumnIndex(ContactsContract.Contacts._ID)));
//			int id = cursor.getInt(cursor
//					.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));

			// m_ctx.getContentResolver().unregisterContentObserver(contactObserver);

			if (ContactID != null && !person.equals(ContactID)) {
				cursor.moveToNext();
				continue;
			}

			// mCtx.getContentResolver().delete(ContactsContract.Contacts.DISPLAY_NAME
			// + " != '"
			// + silenceRejectCall.BL_CONTACT_NAME + "'"
			// Uri.withAppendedPath(phoneUri, Integer.toString(id)), null,
			// null);

			// contactObserver.setInitialContactInfo();
			// m_ctx.getContentResolver().registerContentObserver(
			// personUri, false,
			// contactObserver);
			cursor.moveToNext();
		}
		if (cursor != null)
			cursor.close();
	}

	@Override
	public Cursor ContactsQueryNonBL(int projection, Context ctx) {
		Cursor cur = null;
		ArrayList<String> colums = new ArrayList<String>();
		if ((projection & DataColumns.Contacts._ID) != 0)
			colums.add(ContactsContract.Contacts._ID);
		if ((projection & DataColumns.Contacts.NAME) != 0)
			colums.add(ContactsContract.Contacts.DISPLAY_NAME);
		if ((projection & DataColumns.Contacts.CUSTOM_RINGTONE) != 0)
			colums.add(ContactsContract.Contacts.CUSTOM_RINGTONE);

		String[] proj = new String[colums.size()];
		for (int i = 0; i < colums.size(); i++)
			proj[i] = colums.get(i);

		cur = ctx.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, // Contacts.People.CONTENT_URI,
				proj, null, null, null);

		return cur;
	}

	@Override
	public int ContactsUpdateRingtone(int id, String ringtone, Context ctx) {
		ContentValues value = new ContentValues();
		value.put(ContactsContract.Contacts.CUSTOM_RINGTONE, ringtone);

		return ctx.getContentResolver().update(
				ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id),
				value, null, null);
	}

	@Override
	public int ContactsGetInt(Cursor cur, int id) {
		switch (id) {
		case DataColumns.Contacts._ID:
			return cur.getInt(cur.getColumnIndex(ContactsContract.Contacts._ID));
		case DataColumns.Contacts.NAME:
			// error: return
			// Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
		case DataColumns.Contacts.CUSTOM_RINGTONE:
			// error: return
			// Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.CUSTOM_RINGTONE)));
		default:
			break;
		}
		return 0;
	}

	@Override
	public String ContactsGetString(Cursor cur, int ringtone) {
		switch (ringtone) {
		case DataColumns.Contacts._ID:
			// warning
			return Integer.toString(cur.getInt(cur
					.getColumnIndex(ContactsContract.Contacts._ID)));
		case DataColumns.Contacts.NAME:
			return cur.getString(cur
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		case DataColumns.Contacts.CUSTOM_RINGTONE:
			return cur.getString(cur
					.getColumnIndex(ContactsContract.Contacts.CUSTOM_RINGTONE));
		default:
			break;
		}
		return null;
	}

	@Override
	public String ContactGetName(int personId) {
		String name = null;
		Cursor cur = mCtx.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				new String[] { ContactsContract.Contacts.DISPLAY_NAME },
				ContactsContract.Contacts._ID + " = " + Integer.toString(personId),
				null, null);

		cur.moveToFirst();
		if (!cur.isNull(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))) {
			name = cur.getString(cur
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		}
		cur.close();

		return name;
	}

	@Override
	public void ContactDelete(int personId) {
		if (personId == 0) {
			Cursor cur = mCtx.getContentResolver().query(CONTACT_URI, null, null,
					null, null);
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				int id = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts._ID));
				mCtx.getContentResolver()
						.delete(Uri.withAppendedPath(CONTACT_URI, Integer.toString(id)),
								null, null);
				cur.moveToNext();
			}
			cur.close();

		} else {
			mCtx.getContentResolver().delete(
					Uri.withAppendedPath(CONTACT_URI, Integer.toString(personId)), null,
					null);
		}
	}

	public void setServiceForeground(Service service) {
		service.startForeground(999, new Notification());
	}

	public InputStream getContactPhoto(String phoneId) {
		String contactId = getContactId(phoneId);
		Uri uri = ContentUris.withAppendedId(CONTACT_URI, Long.valueOf(contactId));
		InputStream is = ContactsContract.Contacts.openContactPhotoInputStream(
				mCtx.getContentResolver(), uri);
		return is;
	}
}
