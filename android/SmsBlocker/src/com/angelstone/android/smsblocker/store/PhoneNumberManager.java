package com.angelstone.android.smsblocker.store;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class PhoneNumberManager {
	public static final int INSERT_ERROR_NONE = 0;
	public static final int INSERT_ERROR_AREADY_EXIST = 2;
	public static final int INSERT_ERROR_EXCEPTION_OCCUR = -1;

	public enum LogSelection {
		call_log, sms_log, both_log
	};

	public static enum BlockListAction {
		NO_NUMBER, BLOCK_SMS, NOT_BLOCK_SMS,
	};

	public static PhoneNumberDisposition queryAction(Context context, String number) {
		PhoneNumberDisposition disp = new PhoneNumberDisposition();
		queryBlackList(context, number, disp);

		return disp;
	}

	public static Cursor getBlacklistNumbers(Context context) {
		return context.getContentResolver().query(BlackList.CONTENT_BLACK_LIST_URI,
				new String[] { BlackList._ID, BlackList.NUMBER, BlackList.BLOCK_SMS }, null, null,
				null);
	}

	public static int blacklistAddNumber(Context context, String number,
			boolean blockSms) {
		int err = INSERT_ERROR_NONE;
		Cursor cur = null;

		try {
			String where_str = "number=?";

			cur = context.getContentResolver().query(
					BlackList.CONTENT_BLACK_LIST_URI, new String[] { BlackList._ID },
					where_str, new String[] { number }, null);

			if (cur != null && cur.getCount() != 0) {
				return INSERT_ERROR_AREADY_EXIST;
			} // number already existed

			ContentValues args = new ContentValues();
			args.put(BlackList.NUMBER, number);
			args.put(BlackList.BLOCK_SMS, blockSms ? 1 : 0);
			context.getContentResolver().insert(BlackList.CONTENT_BLACK_LIST_URI,
					args);

		} catch (Exception e) {
			Log.e(context.getString(R.string.app_name), "blackListAddNumber", e);
			err = INSERT_ERROR_EXCEPTION_OCCUR;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

		return err;
	}

	public static int blacklistDeleteNumber(Context context) {
		int err = 0;

		context.getContentResolver().delete(BlackList.CONTENT_BLACK_LIST_URI,
				null, null);

		return err;
	}

	public static int blacklistDeleteNumber(Context context, String number) {
		int err = 0;

		String where_str = "number=?";

		context.getContentResolver().delete(BlackList.CONTENT_BLACK_LIST_URI,
				where_str, new String[] { number });

		return err;
	}

	public static int blacklistUpdateNumber(Context context, String number,
			boolean blockSms) {
		return blacklistUpdateNumber(context, number, number, blockSms);
	}

	public static int blacklistUpdateNumber(Context context, String oldNumber,
			String number, boolean blockSms) {
		int err = 0;
		ContentValues args = new ContentValues();
		args.put(BlackList.BLOCK_SMS, blockSms ? 1 : 0);
		if (!oldNumber.equals(number)) {
			args.put(BlackList.NUMBER, number);
		}
		context.getContentResolver().update(BlackList.CONTENT_BLACK_LIST_URI, args,
				"number=?", new String[] { oldNumber });
		return err;
	}

	public static boolean writeSettingString(Context context, String option_name,
			String option_value) {
		Cursor cur = null;

		try {
			String where_str = "option=?";
			cur = context.getContentResolver()
					.query(Setting.CONTENT_SETTINGS_URI, new String[] { "_id" },
							where_str, new String[] { option_name }, null);

			if (cur == null) {
				return false;
			}

			ContentValues args = new ContentValues();

			if (cur.getCount() == 0) {
				args.put("option", option_name);
				args.put("value", option_value);
				context.getContentResolver().insert(Setting.CONTENT_SETTINGS_URI, args);
			} else {
				args.put("value", option_value);
				context.getContentResolver().update(Setting.CONTENT_SETTINGS_URI, args,
						where_str, new String[] { option_name });

			}
		} catch (Exception e) {
			return false;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

		return true;
	}

	public static boolean writeSetting(Context context, String option_name,
			boolean option_value) {
		return writeSettingString(context, option_name, option_value ? "1" : "0");
	}

	public static String readSettingString(Context context, String option_name) {
		Cursor cur = null;

		try {
			String where_str = "option=?";
			cur = context.getContentResolver()
					.query(Setting.CONTENT_SETTINGS_URI, new String[] { "_id" },
							where_str, new String[] { option_name }, null);

			if (cur == null) {
				return null;
			}

			if (!cur.moveToFirst())
				return null;

			int idx = cur.getColumnIndex(Setting.VALUE);

			String value = cur.getString(idx);

			return value;
		} catch (Exception e) {
			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return null;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public static boolean readSetting(Context context, String option_name) {
		return "1".equals(readSettingString(context, option_name));
	}

	public static boolean writeLog(Context context, EventLog log) {
		ContentValues args = new ContentValues();
		args.put(EventLog.TIME, Long.valueOf(System.currentTimeMillis()));
		args.put(EventLog.NUMBER, log.getNumber());
		args.put(EventLog.SMS_TEXT, log.getSmsTxt());
		context.getContentResolver().insert(EventLog.CONTENT_EVENT_LOG_URI, args);
		return true;
	}

	public static Cursor getEventLogs(Context context) {
		return getEventLogs(context, null, null);
	}
	
	public static Cursor getEventLogs(Context context, String where, String[] where_args) {
		return context.getContentResolver().query(EventLog.CONTENT_EVENT_LOG_URI,
				new String[] { EventLog._ID, EventLog.TIME, EventLog.NUMBER, EventLog.SMS_TEXT },
				where, where_args, null);
	}

	public static Cursor getEventLog(Context context, int id) {
		Uri queryUri = ContentUris.withAppendedId(EventLog.CONTENT_EVENT_LOG_URI, id);
		
		return context.getContentResolver().query(queryUri,
				new String[] { EventLog._ID, EventLog.TIME, EventLog.NUMBER, EventLog.SMS_TEXT },
				null, null, null);
	}

	public static boolean deleteLog(Context context, int _id) {
		Uri deleteUri = ContentUris.withAppendedId(EventLog.CONTENT_EVENT_LOG_URI,
				_id);

		context.getContentResolver().delete(deleteUri, null, null);
		return true;
	}

	public static void deleteLogs(Context context) {
		deleteLogs(context, null, null);
	}

	public static void deleteLogs(Context context, String where,
			String[] where_args) {
		context.getContentResolver().delete(EventLog.CONTENT_EVENT_LOG_URI, where,
				where_args);
	}

	public static BlockListAction blacklistContainsNumber(Context context,
			String number) {
		Cursor cur = null;

		try {
			cur = context.getContentResolver().query(
					BlackList.CONTENT_BLACK_LIST_URI,
					new String[] { BlackList.BLOCK_SMS }, "number = ?",
					new String[] { number }, null);

			if (cur == null) {
				return BlockListAction.NO_NUMBER;
			}

			if (!cur.moveToFirst())
				return BlockListAction.NO_NUMBER;

			return 1 == cur.getInt(cur.getColumnIndex(BlackList.BLOCK_SMS)) ? BlockListAction.BLOCK_SMS
					: BlockListAction.NOT_BLOCK_SMS;
		} catch (Exception e) {
			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return BlockListAction.NO_NUMBER;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

	}

	public static void queryBlackList(Context context, String number,
			PhoneNumberDisposition disp) {
		Cursor cur = null;

		try {
			String whereString = PhoneNumberHelpers.buildNumberMatchQuery("?1",
					BlackList.NUMBER, true);

			cur = context.getContentResolver().query(
					BlackList.CONTENT_BLACK_LIST_URI,
					new String[] { BlackList.NUMBER, BlackList.BLOCK_SMS }, whereString,
					new String[] { number }, null);

			if (cur == null || cur.getCount() <= 0) {

				disp.m_SmsAction = PhoneNumberDisposition.SMS_ACCEPT;

				return;
			}

			cur.moveToFirst();

			int block_sms = cur.getInt(cur.getColumnIndex("block_sms"));

			if (block_sms == 1) {
				disp.m_SmsAction = PhoneNumberDisposition.SMS_REJECT;
			} else {
				disp.m_SmsAction = PhoneNumberDisposition.SMS_ACCEPT;
			}

		} catch (Exception e) {
			Log.e(context.getString(R.string.app_name), "queryBlackList fail", e);
			ActivityLog.logError(context, context.getString(R.string.app_name),
					e.getLocalizedMessage());
			disp.m_SmsAction = PhoneNumberDisposition.SMS_ACCEPT;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}// try
	}
}
