package com.angelstone.android.smsblocker;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;

import com.angelstone.android.platform.SysCompat;
import com.angelstone.android.smsblocker.store.EventLog;
import com.angelstone.android.smsblocker.store.PhoneNumberDisposition;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class SmsBlocker {
	private static SysCompat sys_ = null;

	public static synchronized SysCompat getSysCompat(Context ctx) {
		if (sys_ != null)
			return sys_;

		sys_ = SysCompat.register(ctx);

		return sys_;
	}

	public static boolean isSmsBlocked(Intent intent, Context context) {

		PhoneNumberManager db = PhoneNumberManager.getIntance(context);

		try {
			SmsMessage[] smses = getSMSMessagesFromIntent(intent);
			String messageBody = getMessageBody(smses);
			String sender = "";
			
			if (smses.length > 0) {
				sender = smses[0].getOriginatingAddress();
			}
			
			sender = PhoneNumberHelpers.delete86String(sender);
			sender = PhoneNumberHelpers.removeNonNumbericChar(sender);

			if (db.readSetting(PhoneNumberManager.OPTION_ALLOW_CONTACTS)) {
				if (PhoneNumberHelpers.isContact(context, sender))
					return false;
			}

			PhoneNumberDisposition disp = db.queryAction(sender);
			if (disp.m_SmsAction == PhoneNumberDisposition.SMS_REJECT) {
				WriteToLog(db, messageBody, sender, context);

				return true;
			}
		} finally {
			db.close();
		}
		return false;
	}

	private static void WriteToLog(PhoneNumberManager db, String smsBody, String smsNumber,
			Context context) {
		smsNumber = PhoneNumberHelpers.delete86String(smsNumber);
		EventLog evt = new EventLog(PhoneNumberUtils.formatNumber(smsNumber),
				EventLog.LOG_TYPE_SMS);
		evt.setSmsTxt(smsBody);
		String tag = db.getTagByNumber(
				smsNumber);
		evt.setTagOrName(tag);
		evt.setBlockType(EventLog.SMS_LOG_BLOCK_TYPE_BL);

		db.writeLog(evt);
	}

	private static SmsMessage[] getSMSMessagesFromIntent(Intent intent) {
		Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
		byte[][] pdus = new byte[messages.length][];
		SmsMessage[] smses = new SmsMessage[messages.length];
		for (int i = 0; i < messages.length; i++) {
			pdus[i] = (byte[]) messages[i];
			smses[i] = SmsMessage.createFromPdu(pdus[i]);
		}
		return smses;
	}

	private static String getMessageBody(SmsMessage[] smses) {
		String messageBody;
		if (smses.length == 1) {
			messageBody = smses[0].getMessageBody();
		} else {
			StringBuilder messageBuilder = new StringBuilder();
			for (int i = 0; i < smses.length; i++) {
				messageBuilder.append(smses[i].getMessageBody());
			}
			messageBody = messageBuilder.toString();
		}
		return messageBody;
	}

}
