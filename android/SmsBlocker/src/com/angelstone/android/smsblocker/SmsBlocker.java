package com.angelstone.android.smsblocker;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.angelstone.android.phonetools.store.EventLog;
import com.angelstone.android.phonetools.store.PhoneNumberDisposition;
import com.angelstone.android.phonetools.store.PhoneToolsDBManager;
import com.angelstone.android.phonetools.store.PhoneToolsDatabaseValues;
import com.angelstone.android.platform.SysCompat;
import com.angelstone.android.smsblocker.store.DatabaseValues;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.ReflectionHelper;

public class SmsBlocker {
	static {
		PhoneToolsDBManager.initialize(DatabaseValues.AUTHORITY);
	}

	public static boolean isSmsBlocked(Intent intent, Context context) {

		SysCompat sc = SysCompat.register(context);

		Map<String, String> messages = new HashMap<String, String>();
		if (!sc.parseSmsMessages(intent, messages))
			return false;

		if (messages.size() > 1) {
			StringBuffer sb = new StringBuffer();

			for (String sender : messages.keySet()) {
				sb.append(sender).append(",");
			}

			ActivityLog.logWarning(context, "SmsParser",
					"Multipal sender in 1 intent:" + sb.toString());
		}

		for (String sender : messages.keySet()) {
			String messageBody = messages.get(sender);

			sender = PhoneNumberHelpers.delete86String(sender);
			sender = PhoneNumberHelpers.removeNonNumbericChar(sender);

			if (PhoneToolsDBManager.getSettingsManager().readSetting(context,
					PhoneToolsDatabaseValues.OPTION_ALLOW_CONTACTS)) {
				if (PhoneNumberHelpers.isContact(context, sender))
					return false;
			}

			PhoneNumberDisposition disp = PhoneToolsDBManager
					.getBlackListManager().queryAction(context, sender);
			if (disp.mAction == PhoneNumberDisposition.REJECT) {
				WriteToLog(messageBody, sender, context);

				return true;
			}
		}
		return false;
	}

	private static void WriteToLog(String smsBody, String smsNumber,
			Context context) {
		smsNumber = PhoneNumberHelpers.delete86String(smsNumber);
		EventLog evt = new EventLog(PhoneNumberUtils.formatNumber(smsNumber));
		evt.setContent(smsBody);

		PhoneToolsDBManager.getEventLogManager().writeLog(context, evt);
	}

	public static boolean isMmsBlocked(Intent intent, Context context) {
		try {
			byte[] pushData = intent.getByteArrayExtra("data");

			Class<?> pduParserCls = Class
					.forName("com.google.android.mms.pdu.PduParser");

			Object parser = ReflectionHelper.newInstance(pduParserCls,
					new Class<?>[] { byte[].class }, pushData);

			Object pdu = ReflectionHelper.callMethod(pduParserCls, "parse",
					parser);

			String sender = getFrom(pdu);

			if (sender != null) {
				sender = PhoneNumberHelpers.delete86String(sender);
				sender = PhoneNumberHelpers.removeNonNumbericChar(sender);

				ActivityLog
						.logInfo(context, "MMS Received", "Sender:" + sender);

				if (PhoneToolsDBManager.getSettingsManager()
						.readSetting(context,
								PhoneToolsDatabaseValues.OPTION_ALLOW_CONTACTS)) {
					if (PhoneNumberHelpers.isContact(context, sender))
						return false;
				}

				PhoneNumberDisposition disp = PhoneToolsDBManager
						.getBlackListManager().queryAction(context, sender);
				if (disp.mAction == PhoneNumberDisposition.REJECT) {
					WriteToLog("MMS", sender, context);

					return true;
				}
			}
		} catch (Throwable t) {
			Log.e(context.getString(R.string.app_name), "isMmsBlocked fail", t);
			ActivityLog.logError(context, "MMS Blocking",
					t.getLocalizedMessage());
		}
		return false;
	}

	private static String getFrom(Object pdu) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException,
			ClassNotFoundException, NoSuchFieldException {
		Object headers = ReflectionHelper.callMethod(pdu, "getPduHeaders");

		int from = (Integer) ReflectionHelper.getStaticField(headers, "FROM");

		// call headers.getEncodedStringValue(PduHeaders.FROM)
		return (String) ReflectionHelper.callMethod(headers,
				"getEncodedStringValue", new Class<?>[] { int.class }, from);
	}
}
