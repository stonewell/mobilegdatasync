package com.angelstone.android.platform;

import java.io.InputStream;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;

public abstract class SysCompat {

	protected Context mCtx = null;
	
	public static class DataColumns
	{
		public static class Contacts
		{
			final static int _ID = 0x00000001;
			final static int NAME = 0x00000002;
			final static int CUSTOM_RINGTONE = 0x00000004;
		}
	}
	
	public Uri CONTACT_URI = null;
	public Uri PHONE_URI = null;
	

	public String COLUMN_CONTACT_NAME = null;
	public String COLUMN_CONTACT_ID = null;
	
	public String COLUMN_PHONE_NAME = null;
	public String COLUMN_PHONE_NUMBER = null;	
	
	public String COLUMN_PHONE_CONTACT_ID = null;
	public String COLUMN_PHONE_ID = null;
	public String PHONE_TYPE = null;
	public String COLUMN_PHONE_TYPE = null;
	public String COLUMN_PHONE_ID_v16 = null;
	
	//public static String COLUMN_PHONE_PHOTO_ID = null;
	//public static String COLUMN_PHONE_DATA = null;
	//public static Uri DATA_URI = null;
	//public static String CONTACT = null;
	
	public SysCompat(Context ctx)
	{
		mCtx = ctx;
	}
	
	public static SysCompat register(Context ctx)
	{
		String release = android.os.Build.VERSION.RELEASE;
		if (release.equals("1.5"))
			return new CompatCupcake(ctx);
		else if (release.equals("1.6"))
			return new CompatDonut(ctx);
		else if (release.equals("2.2"))
			return new CompatCookie(ctx);
		else	// 2.0/2.0.1/2.1
			return new CompatEclair(ctx);
	}
	
	public abstract void sendMsg(String phoneNum, String sms_str);	
	
	public abstract String ContactGetName(int person_id);
	public abstract void ContactDelete(int person_id);
	
	public abstract void ContactObserverReg();
	public abstract void ContactObserverUnReg();
	public abstract boolean ContactObserverNeedToUpdate(int counter);
	
	public abstract void removePhone(String phoneNum, String blContactID);
	
	public abstract Cursor ContactsQueryNonBL(int projection_flags, Context ctx);
	public abstract int ContactsUpdateRingtone(int id, String ringtone, Context ctx);
	
	public abstract int ContactsGetInt(Cursor cur, int id);
	public abstract String ContactsGetString(Cursor cur, int ringtone);
	
	public abstract void setServiceForeground(Service service);
	
	public abstract boolean SetRingerSilence(AudioManager audioManager, boolean beforeQueryAction);

	public abstract String getPhoneNumber(String phoneId);
	public abstract String getDisplayName(String phoneId);
	//public abstract String[] getContactNumbers(String phoneId);
	public abstract ArrayList<MobilePhoneRecord> getAllMobilePhoneNumbers();
	public abstract boolean phoneExisted(String phoneId);
	public abstract InputStream getContactPhoto(String phoneId);
	
	public abstract String getContactId(String phoneId);
}
