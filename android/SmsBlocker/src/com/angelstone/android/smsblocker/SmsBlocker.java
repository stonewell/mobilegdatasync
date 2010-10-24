package com.angelstone.android.smsblocker;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.angelstone.android.platform.SysCompat;
import com.angelstone.android.smsblocker.store.DatabaseValues;
import com.angelstone.android.smsblocker.store.EventLog;
import com.angelstone.android.smsblocker.store.PhoneNumberDisposition;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.PduParser;

public class SmsBlocker {
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

			if (PhoneNumberManager.readSetting(context,
					DatabaseValues.OPTION_ALLOW_CONTACTS)) {
				if (PhoneNumberHelpers.isContact(context, sender))
					return false;
			}

			PhoneNumberDisposition disp = PhoneNumberManager.queryAction(
					context, sender);
			if (disp.m_SmsAction == PhoneNumberDisposition.SMS_REJECT) {
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
		evt.setSmsTxt(smsBody);

		PhoneNumberManager.writeLog(context, evt);
	}

	public static boolean isMmsBlocked(Intent intent, Context context) {
		try {
			byte[] pushData = intent.getByteArrayExtra("data");
			PduParser parser = new PduParser(pushData);

			GenericPdu pdu = parser.parse();
			if (pdu != null && pdu.getFrom() != null) {
				String sender = pdu.getFrom().toString();
				sender = PhoneNumberHelpers.delete86String(sender);
				sender = PhoneNumberHelpers.removeNonNumbericChar(sender);

				ActivityLog
						.logInfo(context, "MMS Received", "Sender:" + sender);

				if (PhoneNumberManager.readSetting(context,
						DatabaseValues.OPTION_ALLOW_CONTACTS)) {
					if (PhoneNumberHelpers.isContact(context, sender))
						return false;
				}

				PhoneNumberDisposition disp = PhoneNumberManager.queryAction(
						context, sender);
				if (disp.m_SmsAction == PhoneNumberDisposition.SMS_REJECT) {
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
}
