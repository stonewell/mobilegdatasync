package com.angelstone.android.platform;

import java.util.Map;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Contacts;
import android.telephony.gsm.SmsMessage;
import android.util.Log;

import com.angelstone.android.R;
import com.angelstone.android.utils.ReflectionHelper;

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
				messages.put(sender,
						messages.get(sender) + msg.getMessageBody());
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
		Uri uri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI,
				photoId);
		return Contacts.People.loadContactPhoto(mCtx, uri, defaultResource,
				null);
	}

	@Override
	public boolean enableBluetooth(boolean enable) {
		String serviceName = null;
		try {
			serviceName = (String) Context.class.getField("BLUETOOTH_SERVICE")
					.get(null);
			BluetoothDevice manager = (BluetoothDevice) mCtx
					.getApplicationContext().getSystemService(serviceName);

			if (manager == null)
				return false;

			return enable ? (Boolean) BluetoothDevice.class.getMethod("enable")
					.invoke(manager) : (Boolean) BluetoothDevice.class
					.getMethod("disable").invoke(manager);
		} catch (Throwable t) {
			Log.e(mCtx.getString(R.string.app_name), "enable Bluetooth fail:"
					+ serviceName, t);
			return false;
		}
	}

	@Override
	public boolean enableMobileNetwork(boolean enable) {
		return true;
	}

	@Override
	public Uri getReceiptUri() {
		return Uri.parse("content://mms-sms/canonical-address/");
	}

	private static final java.lang.String DESCRIPTOR = "android.os.IPowerManager";
	private static final int TRANSACTION_getScreenOnTime = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);

	@Override
	public boolean isScreenOn() {
		android.os.Parcel _data = android.os.Parcel.obtain();
		android.os.Parcel _reply = android.os.Parcel.obtain();
		long _result = -1;
		try {
			Class<?> smCls = Class.forName("android.os.ServiceManager");

			IBinder mRemote = (IBinder) ReflectionHelper.callMethod(smCls,
					"getService", null, new Class<?>[] { String.class },
					new Object[] { Context.POWER_SERVICE });
			_data.writeInterfaceToken(DESCRIPTOR);
			mRemote.transact(TRANSACTION_getScreenOnTime, _data, _reply, 0);
			_reply.readException();
			_result = _reply.readLong();
		} catch (Throwable t) {
			Log.e("Platform", "isScreenOn 1.6 fail", t);
		} finally {
			_reply.recycle();
			_data.recycle();
		}
		return _result != 0;
	}
}
