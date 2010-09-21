package com.angelstone.android.platform;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Contacts;

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
		// COLUMN_PHONE_PHOTO_ID = Contacts.Photos._ID;
		// COLUMN_PHONE_DATA = Contacts.Photos.DATA;
		// DATA_URI = Contacts.Photos.CONTENT_URI;
		// CONTACT = Contacts.People;
	}

	@Override
	public void sendMsg(String phoneNum, String smsStr) {

		final android.telephony.gsm.SmsManager sm = android.telephony.gsm.SmsManager
				.getDefault();
		sm.sendTextMessage(phoneNum, null, smsStr, null, null);
	}

	@Override
	public boolean ContactObserverNeedToUpdate(int counter) {
		return (counter == 3);
	}

	@Override
	public void ContactObserverReg() {
		// silenceRejectCall.contactObserver.setInitialContactInfo();
		//
		// Uri blContactUri = null;
		// String cid = silenceRejectCall.getBlContactID();
		// if (cid != null && !cid.equals(""))
		// {
		// int id = Integer.parseInt(silenceRejectCall.getBlContactID());
		// blContactUri = ContentUris.withAppendedId(
		// Contacts.People.CONTENT_URI, id);
		// } else {
		// blContactUri = Contacts.People.CONTENT_URI;
		// }
		// mCtx.getContentResolver().registerContentObserver(
		// blContactUri, false, silenceRejectCall.contactObserver);
	}

	@Override
	public void ContactObserverUnReg() {
		// mCtx.getContentResolver().unregisterContentObserver(silenceRejectCall.contactObserver);
	}

	@Override
	public void removePhone(String phoneNum, String ContactID) {

		Cursor cursor = mCtx.getContentResolver().query(
				Contacts.Phones.CONTENT_URI,
				new String[] { Contacts.Phones._ID, Contacts.Phones.PERSON_ID },
				Contacts.Phones.NUMBER + " = '" + phoneNum + "'", null, null);

		if (cursor.getCount() <= 0) {
			cursor.close();
			return;
		}

		cursor.moveToFirst();

		String person = Integer.toString(cursor.getInt(cursor
				.getColumnIndex(Contacts.Phones.PERSON_ID)));

		while (!cursor.isAfterLast()) {
			if (ContactID == null || ContactID.equals(person)) {
				Uri ContactUri = Uri.withAppendedPath(Contacts.People.CONTENT_URI,
						person);
				Uri phoneUri = Uri.withAppendedPath(ContactUri,
						Contacts.People.Phones.CONTENT_DIRECTORY);

				int id = cursor.getInt(cursor.getColumnIndex(Contacts.Phones._ID));

				// m_ctx.getContentResolver().unregisterContentObserver(contactObserver);

				mCtx.getContentResolver().delete(
						Uri.withAppendedPath(phoneUri, Integer.toString(id)), null, null);

				// contactObserver.setInitialContactInfo();
				// m_ctx.getContentResolver().registerContentObserver(
				// personUri, false,
				// contactObserver);
			}
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
			colums.add(Contacts.People._ID);
		if ((projection & DataColumns.Contacts.NAME) != 0)
			colums.add(Contacts.People.NAME);
		if ((projection & DataColumns.Contacts.CUSTOM_RINGTONE) != 0)
			colums.add(Contacts.People.CUSTOM_RINGTONE);

		String[] proj = new String[colums.size()];
		for (int i = 0; i < colums.size(); i++)
			proj[i] = colums.get(i);

		cur = ctx.getContentResolver().query(Contacts.People.CONTENT_URI, proj,
				null, null, null);

		return cur;
	}

	@Override
	public int ContactsUpdateRingtone(int id, String ringtone, Context ctx) {
		ContentValues value = new ContentValues();
		value.put(Contacts.People.CUSTOM_RINGTONE, ringtone);

		return ctx.getContentResolver().update(
				ContentUris.withAppendedId(Contacts.People.CONTENT_URI, id), value,
				null, null);
	}

	@Override
	public int ContactsGetInt(Cursor cur, int id) {
		switch (id) {
		case DataColumns.Contacts._ID:
			return cur.getInt(cur.getColumnIndex(Contacts.People._ID));
		case DataColumns.Contacts.NAME:
			// error: return
			// Integer.parseInt(cur.getString(cur.getColumnIndex(Contacts.People.DISPLAY_NAME)));
		case DataColumns.Contacts.CUSTOM_RINGTONE:
			// error: return
			// Integer.parseInt(cur.getString(cur.getColumnIndex(Contacts.People.CUSTOM_RINGTONE)));
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
					.getColumnIndex(Contacts.People._ID)));
		case DataColumns.Contacts.NAME:
			return cur.getString(cur.getColumnIndex(Contacts.People.DISPLAY_NAME));
		case DataColumns.Contacts.CUSTOM_RINGTONE:
			return cur.getString(cur.getColumnIndex(Contacts.People.CUSTOM_RINGTONE));
		default:
			break;
		}
		return null;
	}

	@Override
	public String ContactGetName(int personId) {

		String name = null;
		Cursor cur = mCtx.getContentResolver().query(CONTACT_URI,
				new String[] { Contacts.People.DISPLAY_NAME },
				Contacts.People._ID + " = " + Integer.toString(personId), null, null);

		cur.moveToFirst();
		if (!cur.isNull(cur.getColumnIndex(Contacts.People.DISPLAY_NAME))) {
			name = cur.getString(cur.getColumnIndex(Contacts.People.DISPLAY_NAME));
		}
		cur.close();

		return name;
	}

	@Override
	public void ContactDelete(int personId) {
		if (personId == 0) {
			mCtx.getContentResolver().delete(CONTACT_URI, null, null);
		} else {
			mCtx.getContentResolver().delete(CONTACT_URI,
					COLUMN_CONTACT_ID + " = " + Integer.toString(personId), null);
		}
	}

	public void setServiceForeground(Service service) {
		// service.setForeground(true);
	}

	public boolean SetRingerSilence(AudioManager audioManager,
			boolean beforeQueryAction) {
		if (beforeQueryAction) {
			audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

			return true;
		}

		return false;
	}

	public String getPhoneNumber(String phoneId) {
		if (getContactId(phoneId) == null) {
			return null;
		}

		String number = null;
		String[] projection = new String[] { COLUMN_PHONE_NUMBER };
		String selection = COLUMN_PHONE_ID + " = '" + phoneId + "'";
		Cursor cursor = mCtx.getContentResolver().query(PHONE_URI, projection,
				selection, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			number = cursor.getString(cursor.getColumnIndex(COLUMN_PHONE_NUMBER));
		}

		cursor.close();
		return number;
	}

	public String getDisplayName(String phoneId) {
		if (getContactId(phoneId) == null) {
			return null;
		}

		String displayName = null;
		String[] projection = new String[] { COLUMN_CONTACT_NAME };
		String selection = COLUMN_PHONE_ID + " = '" + phoneId + "'";
		Cursor cursor = mCtx.getContentResolver().query(PHONE_URI, projection,
				selection, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			displayName = cursor
					.getString(cursor.getColumnIndex(COLUMN_CONTACT_NAME));
		}

		cursor.close();
		return displayName;
	}

	/*
	 * public String[] getContactNumbers(String phoneId) { String[] projection =
	 * new String[] {COLUMN_CONTACT_ID}; String selection = COLUMN_PHONE_ID +
	 * " = '" + phoneId + "'"; Cursor cursor = mCtx.getContentResolver().query(
	 * PHONE_URI, projection, selection, null, null); String contactId = null; if
	 * (cursor != null && cursor.getCount() > 0) { cursor.moveToFirst(); contactId
	 * = cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_ID));
	 * cursor.close(); }
	 * 
	 * projection = new String[] {COLUMN_PHONE_NUMBER}; selection =
	 * COLUMN_CONTACT_ID + " = '" + contactId + "'" + " and " + PHONE_TYPE + "=" +
	 * COLUMN_PHONE_TYPE; cursor = mCtx.getContentResolver().query( CONTACT_URI,
	 * projection, selection, null, null);
	 * 
	 * String[] numbers = null; if (cursor != null && cursor.getCount() > 0) {
	 * cursor.moveToFirst(); //int phoneCount = cursor.getInt(cursor //
	 * .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)); //if
	 * (phoneCount != 0) { numbers = new String[cursor.getCount()]; for (int i =
	 * 0; i < cursor.getCount(); i++) { numbers[i] = cursor.getString(
	 * cursor.getColumnIndex(COLUMN_PHONE_NUMBER)); cursor.moveToNext(); } //} }
	 * 
	 * cursor.close(); return numbers; }
	 */

	public ArrayList<MobilePhoneRecord> getAllMobilePhoneNumbers() {
		String[] projection = new String[] { COLUMN_PHONE_ID_v16,
				COLUMN_PHONE_NAME, COLUMN_PHONE_NUMBER };
		// String selection = PHONE_TYPE + "=" + COLUMN_PHONE_TYPE;
		Cursor cursor = mCtx.getContentResolver().query(PHONE_URI, projection,
				null, null, COLUMN_PHONE_NAME);
		if (cursor != null && cursor.getCount() > 0) {
			ArrayList<MobilePhoneRecord> records = new ArrayList<MobilePhoneRecord>();
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				String id = cursor
						.getString(cursor.getColumnIndex(COLUMN_PHONE_ID_v16));
				if (getContactId(id) == null) {
					continue;
				}

				MobilePhoneRecord record = new MobilePhoneRecord();
				record.phoneId = id;
				record.displayName = cursor.getString(cursor
						.getColumnIndex(COLUMN_PHONE_NAME));
				record.phoneNumber = cursor.getString(cursor
						.getColumnIndex(COLUMN_PHONE_NUMBER));
				records.add(record);
				cursor.moveToNext();
			}

			cursor.close();
			return records;
		}

		cursor.close();
		return null;
	}

	public boolean phoneExisted(String phoneId) {
		// String[] projection = new String[] {Contacts.Phones.NUMBER};
		// String selection = Contacts.Phones._ID + " = '" + phoneId + "'";
		Cursor cursor = mCtx.getContentResolver().query(PHONE_URI, null, null,
				null, null);
		if (cursor == null || cursor.getCount() <= 0) {
			return false;
		}

		cursor.close();
		return true;
	}

	public InputStream getContactPhoto(String phoneId) {
		String contactId = getContactId(phoneId);
		Uri uri = ContentUris.withAppendedId(CONTACT_URI, Long.valueOf(contactId));
		InputStream is = Contacts.People.openContactPhotoInputStream(
				mCtx.getContentResolver(), uri);
		return is;
	}

	public String getContactId(String phoneId) {
		String[] projection = new String[] { COLUMN_PHONE_CONTACT_ID };
		String selection = COLUMN_PHONE_ID + " = '" + phoneId + "'";
		Cursor cursor = mCtx.getContentResolver().query(PHONE_URI, projection,
				selection, null, null);
		String contactId = null;
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			contactId = cursor.getString(cursor
					.getColumnIndex(COLUMN_PHONE_CONTACT_ID));
			cursor.close();
		}

		return contactId;
	}
}
