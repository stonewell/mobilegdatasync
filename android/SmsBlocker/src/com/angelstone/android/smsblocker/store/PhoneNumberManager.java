package com.angelstone.android.smsblocker.store;

import java.util.Iterator;
import java.util.TreeMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.SmsBlocker;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class PhoneNumberManager {

	private static final String DATABASE_NAME = "smsblocker.db";
	private static final String SETTING_TABLE = "setting";
	private static final String LOG_TABLE = "event_logs";
	private static final String BLACK_LIST_TABLE = "black_list";

	public static final int INSERT_ERROR_NONE = 0;
	public static final int INSERT_ERROR_AREADY_EXIST = 2;
	public static final int INSERT_ERROR_EXCEPTION_OCCUR = -1;

	public static final String OPTION_ALLOW_CONTACTS = "sms_allow_contacts";

	private SQLiteDatabase m_db = null;
	public Context m_ctx = null;

	private static PhoneNumberManager _intance = null;

	private static int refcount = 0;

	public enum LogSelection {
		call_log, sms_log, both_log
	};

	public void close() {
		if (refcount == 0) {
			if (m_db != null) {
				m_db.close();
			}

			_intance = null;
		} else {
			synchronized (_intance) {
				refcount--;
			}
		}
	}

	public static PhoneNumberManager getIntance(Context ctx) {
		if (_intance == null) {
			String lockStr = "lock";
			synchronized (lockStr) {
				if (_intance == null) {
					_intance = new PhoneNumberManager(ctx);
				}
			}
		}

		synchronized (_intance) {
			refcount++;
		}

		return _intance;
	}

	private PhoneNumberManager(Context ctx) {
		m_ctx = ctx;
		initDatabase();
	}

	public PhoneNumberDisposition queryAction(String number) {
		PhoneNumberDisposition disp = new PhoneNumberDisposition();
		queryBlackList(number, disp);

		return disp;
	}

	public boolean isInBlacklist(String number) {
		Cursor cur = null;

		try {
			String whereString = PhoneNumberHelpers.buildNumberMatchQuery("?1",
					"number", false);
			cur = m_db.query(BLACK_LIST_TABLE, new String[] { "number" },
					whereString, new String[] { number }, null, null, null);

			Log.d(m_ctx.getString(R.string.app_name), "isInBlackList query where:"
					+ whereString);
			if (cur != null && cur.getCount() >= 1) {
				return true;
			}
			return false;
		} catch (Exception e) {
			Log.e(m_ctx.getString(R.string.app_name), "isInBlackList query fail", e);
			return false;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public String[][] getBlacklistNumbers() {
		Cursor cur = null;

		try {
			cur = m_db.query(BLACK_LIST_TABLE, new String[] { "number", "block_call",
					"block_sms", "tag" }, null, null, null, null, null);
			if (cur == null || cur.getCount() <= 0) {
				if (cur != null) {
					cur.close();
				}

				return null;
			}

			cur.moveToFirst();
			int i = 0;
			String[][] ret = new String[cur.getCount()][5];
			while (!cur.isAfterLast()) {
				ret[i][0] = new String(cur.getString(cur.getColumnIndex("number")));
				ret[i][1] = new String(Integer.toString(cur.getInt(cur
						.getColumnIndex("block_call"))));
				ret[i][2] = new String(Integer.toString(cur.getInt(cur
						.getColumnIndex("block_sms"))));
				ret[i][3] = new String(cur.getString(cur.getColumnIndex("tag")));
				i++;
				cur.moveToNext();
			}
			if (cur != null)
				cur.close();
			return ret;
		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}
			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return null;
		}
	}

	public int blacklistAddNumber(String number, boolean blockCall,
			boolean blockSms, String tag) {
		int err = INSERT_ERROR_NONE;
		Cursor cur = null;

		try {
			String where_str = new String("number='" + number + "'");
			cur = m_db.query(BLACK_LIST_TABLE, new String[] { "number" }, where_str,
					null, null, null, null);
			// if( cur == null ) { return -2; } //unexpected
			// cur.moveToFirst();
			if (cur != null && cur.getCount() != 0) {
				cur.close();
				return INSERT_ERROR_AREADY_EXIST;
			} // number already existed

			ContentValues args = new ContentValues();
			args.put("number", number);
			args.put("block_call", blockCall ? 1 : 0);
			args.put("block_sms", blockSms ? 1 : 0);
			args.put("tag", tag);
			// args.put( "reply_sms", replySms);
			// args.put( "phone_id", 0);
			m_db.insert(BLACK_LIST_TABLE, null, args);

			if (cur != null) {
				cur.close();
			}
		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}
			err = INSERT_ERROR_EXCEPTION_OCCUR;
		}

		return err;
	}

	public int blacklistDeleteNumber(String number) {
		int err = 0;
		Cursor cur = null;

		try {
			String where_str = new String("number='" + number + "'");
			cur = m_db.query(BLACK_LIST_TABLE, new String[] { "number" }, where_str,
					null, null, null, null);

			if (cur == null) {
				return -1;
			} // unexpected

			cur.moveToFirst();

			if (cur.getCount() != 1) {
				cur.close();
				return 2;
			} // number not existed

			m_db.delete(BLACK_LIST_TABLE, where_str, null);

			cur.close();
		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}
			err = -1;
		}
		return err;
	}

	public int blacklistUpdateNumber(String number, boolean blockCall,
			boolean blockSms, String tag, int id) {
		int err = 0;
		try {
			/*
			 * String where_str = new String( "number='" + number + "'" ); Cursor cur
			 * = m_db.query( BLACK_LIST_TABLE, new String[]{"number"}, where_str,
			 * null, null, null, null ); //if( cur == null ) { return -2; }
			 * //unexpected //cur.moveToFirst(); if( cur != null && cur.getCount() !=
			 * 0 ) { return 2; } //number already existed
			 */
			ContentValues args = new ContentValues();
			args.put("block_call", blockCall ? 1 : 0);
			args.put("block_sms", blockSms ? 1 : 0);
			args.put("tag", tag);
			// args.put( "phone_id", id);
			m_db.update(BLACK_LIST_TABLE, args, "number='" + number + "'", null);
		} catch (Exception e) {
			err = -1;
		}
		return err;
	}

	public int blacklistUpdateNumber(String number, boolean blockCall,
			boolean blockSms, String tag) {
		int err = 0;
		try {
			/*
			 * String where_str = new String( "number='" + number + "'" ); Cursor cur
			 * = m_db.query( BLACK_LIST_TABLE, new String[]{"number"}, where_str,
			 * null, null, null, null ); //if( cur == null ) { return -2; }
			 * //unexpected //cur.moveToFirst(); if( cur != null && cur.getCount() !=
			 * 0 ) { return 2; } //number already existed
			 */
			ContentValues args = new ContentValues();
			args.put("block_call", blockCall ? 1 : 0);
			args.put("block_sms", blockSms ? 1 : 0);
			args.put("tag", tag);
			m_db.update(BLACK_LIST_TABLE, args, "number='" + number + "'", null);
		} catch (Exception e) {
			err = -1;
		}
		return err;
	}

	public int blacklistUpdateNumber(int id, String number, boolean blockCall,
			boolean blockSms, String tag) {
		int err = 0;
		try {
			/*
			 * String where_str = new String( "number='" + number + "'" ); Cursor cur
			 * = m_db.query( BLACK_LIST_TABLE, new String[]{"number"}, where_str,
			 * null, null, null, null ); //if( cur == null ) { return -2; }
			 * //unexpected //cur.moveToFirst(); if( cur != null && cur.getCount() !=
			 * 0 ) { return 2; } //number already existed
			 */
			ContentValues args = new ContentValues();
			args.put("number", number);
			args.put("block_call", blockCall ? 1 : 0);
			args.put("block_sms", blockSms ? 1 : 0);
			args.put("tag", tag);
			m_db.update(BLACK_LIST_TABLE, args, null, null);
		} catch (Exception e) {
			err = -1;
		}
		return err;
	}

	public boolean writeSettingString(String option_name, String option_value) {

		Cursor cur = null;

		try {
			String where_str = "option='" + option_name + "'";
			cur = m_db.query(SETTING_TABLE, new String[] { "value" }, where_str,
					null, null, null, null);

			if (cur == null) {
				return false;
			}

			cur.moveToFirst();
			ContentValues args = new ContentValues();

			if (cur.getCount() == 0) {
				args.put("option", option_name);
				args.put("value", option_value);
				m_db.insert(SETTING_TABLE, null, args);
			} else {
				args.put("value", option_value);
				m_db.update(SETTING_TABLE, args, where_str, null);

			}

			cur.close();
		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}
			return false;
		}

		return true;
	}

	public boolean writeSetting(String option_name, boolean option_value) {
		Cursor cur = null;
		try {
			String where_str = "option='" + option_name + "'";
			cur = m_db.query(SETTING_TABLE, new String[] { "value" }, where_str,
					null, null, null, null);

			if (cur == null) {
				return false;
			}

			cur.moveToFirst();
			ContentValues args = new ContentValues();

			if (cur.getCount() == 0) {
				args.put("option", option_name);
				args.put("value", option_value ? "1" : "0");
				m_db.insert(SETTING_TABLE, null, args);
			} else {
				args.put("value", option_value ? "1" : "0");
				m_db.update(SETTING_TABLE, args, where_str, null);
			}

			cur.close();
		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}
			return false;
		}
		return true;
	}

	public String readSettingString(String option_name) {
		Cursor cur = null;

		try {
			String where_str = "option='" + option_name + "'";
			cur = m_db.query(SETTING_TABLE, new String[] { "value" }, where_str,
					null, null, null, null);

			if (cur == null) {
				return null;
			}

			cur.moveToFirst();

			if (cur.getCount() == 0) {
				cur.close();
				return null;
			}

			int idx = cur.getColumnIndex("value");

			String value = cur.getString(idx);
			cur.close();

			return value;
		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}

			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return null;
		}
	}

	public boolean readSetting(String option_name) {
		Cursor cur = null;

		try {
			String where_str = "option='" + option_name + "'";
			cur = m_db.query(SETTING_TABLE, new String[] { "value" }, where_str,
					null, null, null, null);

			if (cur == null) {
				return false;
			}

			cur.moveToFirst();

			if (cur.getCount() == 0) {
				cur.close();
				return false;
			}

			int idx = cur.getColumnIndex("value");
			String opt_str = cur.getString(idx);

			cur.close();

			return opt_str.equals("1");
		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}

			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return false;
		}
	}

	public boolean writeLog(EventLog log) {
		Cursor cur = null;
		try {
			ContentValues args = new ContentValues();
			// long tm = log.getTime().getTime();
			args.put("time", Long.valueOf(System.currentTimeMillis()));
			args.put("number", log.getNumber());
			args.put("type", log.getType());
			args.put("block_type", log.getBlockType());
			args.put("action", 0);
			args.put("sms_text",
					log.getType() == EventLog.LOG_TYPE_SMS ? log.getSmsTxt() : null);
			args.put("contact_name", log.getTagOrName());
			args.put("scene_name", log.getSceneOrKeyword());
			args.put("reply_sms_text", log.getReplySmsTxt());
			m_db.insert(LOG_TABLE, null, args);

			String where_str = "block_type='" + log.getBlockType() + "'";
			int cnt = 0;

			cur = m_db
					.query(LOG_TABLE, null, where_str, null, null, null, "time ASC");

			if (cur != null) {
				cur.moveToFirst();
				cnt = cur.getCount();
				int id = cur.getInt(cur.getColumnIndexOrThrow("_id"));

				if (cnt > 300) {
					m_db.delete(LOG_TABLE, "_id = '" + id + "'", null);
				}
				cur.close();
			}

		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}
			return false;
		}
		return true;
	}

	private String getTypeWhereStr(int Type) {
		if (Type == EventLog.LOG_TYPE_CALL || Type == EventLog.LOG_TYPE_SMS) {
			return String.format("type='%d'", Type);
		} else {
			return null;
		}
	}

	// Block_Type
	// 0 for blacklist,
	// 1 for scene or keyword
	public EventLog[] getLogs(int Type, int Scope, int Block_Type) {
		boolean get_intercepted = (Scope & EventLog.LOG_SCOPE_INTERCEPTED) != 0;
		boolean get_received = (Scope & EventLog.LOG_SCOPE_RECEIVED) != 0;

		Cursor cur = null;

		try {
			int log_count = 0;
			String where_str = null;
			final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
			final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI,
					"inbox");
			String SORT_ORDER = "date DESC";
			TreeMap<Long, EventLog> log_sorter = new TreeMap<Long, EventLog>();

			if (get_intercepted) {
				where_str = getTypeWhereStr(Type);
				cur = m_db.query(LOG_TABLE, new String[] { "time", "number", "type",
						"sms_text" }, where_str, null, null, null, null);

				if (cur == null) {
					return null;
				}

				cur.moveToFirst();
				log_count += cur.getCount();

				cur.close();
			}

			if (get_received) {
				if ((Type & EventLog.LOG_TYPE_CALL) != 0) {
					cur = m_ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI,
							null, null, null, SORT_ORDER);// CallLog.Calls.DEFAULT_SORT_ORDER);

					if (cur == null) {
						return null;
					}

					cur.moveToFirst();

				} else if ((Type & EventLog.LOG_TYPE_SMS) != 0) { // "_id",
					// "thread_id",
					// "address",
					// "person",
					// "date",
					// "body"
					cur = m_ctx.getContentResolver().query(SMS_INBOX_CONTENT_URI, null,
							null, null, SORT_ORDER);
					cur.moveToFirst();
				}
				log_count += cur.getCount();

				cur.close();
			}

			if (log_count == 0) {
				return null;
			}

			EventLog logs[] = new EventLog[log_count];
			int i = 0, k = 0;
			int idx_date = 0, idx_number = 0, idx_smstext = 0, idx_tag_or_name = 0, idx_scene_or_keyword = 0, idx_reply_sms_txt = 0;

			if (get_intercepted) {
				// String[][] blist = getBlacklistNumbers();

				where_str = getTypeWhereStr(Type);
				cur = m_db.query(LOG_TABLE, new String[] { "time", "number", "type",
						"sms_text", "contact_name", "scene_name", "reply_sms_text" },
						where_str, null, null, null, null);

				if (cur == null) {
					return null;
				}

				cur.moveToFirst();
				idx_date = cur.getColumnIndex("time");
				idx_number = cur.getColumnIndex("number");
				idx_smstext = cur.getColumnIndex("sms_text");
				idx_tag_or_name = cur.getColumnIndex("contact_name");
				idx_scene_or_keyword = cur.getColumnIndex("scene_name");
				idx_reply_sms_txt = cur.getColumnIndex("reply_sms_text");
				k = cur.getCount();
				boolean selected = false;

				for (i = 0; i < cur.getCount(); i++) {
					String number = cur.getString(idx_number);
					String contact_name = cur.getString(idx_tag_or_name);
					String scene_name = cur.getString(idx_scene_or_keyword);
					String reply_sms_txt = cur.getString(idx_reply_sms_txt);

					number = PhoneNumberHelpers.delete86String(number);

					long time = Long.parseLong(cur.getString(idx_date));
					logs[i] = new EventLog(number, Type, -1, time);
					selected = false;

					// check for tag if number is in blacklist
					/*
					 * for( j = 0; j < blist.length; j++ ) { //if( number.equals(
					 * blist[j][0] ) ) if (isEqualFromLast(number, blist[j][0])) { //the
					 * number is in blacklist logs[i].setTagOrName(blist[j][3]); break; }
					 * }
					 */
					if (Block_Type == 0) { // only get the records blocked by
						// blacklist
						if (scene_name == null) {
							selected = true;
						}
					} else if (Block_Type == 1) { // only get the records
						// blocked by scene or
						// keyword
						if (scene_name != null) {
							selected = true;
						}
					}
					/*
					 * if( logs[i].getTagOrName() == null) { //blocked by scene
					 * logs[i].setTagOrName( contact_name );
					 * logs[i].setSceneOrKeyword(scene_name); }else { //blocked by black
					 * list logs[i].setSceneOrKeyword(null); }
					 */
					if (Type == EventLog.LOG_TYPE_SMS) {
						logs[i].setSmsTxt(cur.getString(idx_smstext));
					}
					if (selected) {
						logs[i].setTagOrName(contact_name);
						logs[i].setSceneOrKeyword(scene_name);
						logs[i].setReplySmsTxt(reply_sms_txt);
						log_sorter.put(Long.valueOf(time), logs[i]);
					}
					cur.moveToNext();
				}

				cur.close();
			}

			if (get_received) {
				idx_smstext = -1;
				int idx_type = -1;

				if (Type == EventLog.LOG_TYPE_CALL) {
					cur = m_ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI,
							null, null, null, SORT_ORDER);// CallLog.Calls.DEFAULT_SORT_ORDER);

					if (cur == null) {
						return null;
					}

					cur.moveToFirst();
					idx_date = cur.getColumnIndex(CallLog.Calls.DATE);
					idx_number = cur.getColumnIndex(CallLog.Calls.NUMBER);
					idx_type = cur.getColumnIndex(CallLog.Calls.TYPE);
					idx_smstext = -1; // mark this index is not applicable
				} else if (Type == EventLog.LOG_TYPE_SMS) { // "_id",
					// "thread_id",
					// "address",
					// "person", "date",
					// "body"
					cur = m_ctx.getContentResolver().query(SMS_INBOX_CONTENT_URI,
							new String[] { "address", "date", "body", "person" }, null, null,
							SORT_ORDER);

					if (cur == null) {
						return null;
					}

					cur.moveToFirst();
					idx_date = cur.getColumnIndex("date");
					idx_number = cur.getColumnIndex("address");
					idx_smstext = cur.getColumnIndex("body");
					idx_type = -1;
				}

				for (i = 0; i < cur.getCount(); i++) {
					String number = cur.getString(idx_number);
					long time = Long.parseLong(cur.getString(idx_date));
					int CallType = -1;

					if (idx_type != -1) {
						CallType = Integer.parseInt(cur.getString(idx_type));
					}
					logs[i + k] = new EventLog(number, Type, CallType, time);

					if (idx_smstext != -1) {
						logs[i + k].setSmsTxt(cur.getString(idx_smstext));
					}

					if (Type == EventLog.LOG_TYPE_CALL) {
						logs[i + k].setTagOrName(cur.getString(cur
								.getColumnIndex(CallLog.Calls.CACHED_NAME)));
					} else if (Type == EventLog.LOG_TYPE_SMS) {
						if (cur.getString(cur.getColumnIndex("person")) != null) {
							int name_id = Integer.parseInt(cur.getString(cur
									.getColumnIndex("person")));
							logs[i + k].setTagOrName(getNameById(name_id));
						}
					}
					logs[i + k].setSceneOrKeyword(null);
					log_sorter.put(Long.valueOf(time), logs[i + k]);
					cur.moveToNext();
				}

				cur.close();
			}

			EventLog ret_logs[] = new EventLog[log_sorter.size()];

			Iterator<EventLog> iter = log_sorter.values().iterator();
			i = 0;
			k = log_sorter.size();

			while (iter.hasNext()) {
				ret_logs[k - 1 - i] = (EventLog) iter.next();
				i++;
			}
			return ret_logs;
		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}

			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return null;
		}

	}

	public Cursor getCallRejectLogCursor(int Block_Type) {
		String where_str = "block_type='" + Block_Type + "'";

		Cursor cur = m_db.query(LOG_TABLE, null, where_str, null, null, null,
				"time DESC");

		return cur;
	}

	public boolean deleteLog(Cursor cur, int position) {
		if (cur == null) {
			return false;
		}

		boolean bIsExist = cur.moveToPosition(position);

		if (!bIsExist) {
			return false;
		}

		int id = cur.getInt(cur.getColumnIndex("_id"));

		m_db.delete(LOG_TABLE, "_id = '" + id + "'", null);

		return true;
	}

	public void deleteLogs(int Type, int Block_Type) {
		String where_str = getTypeWhereStr(Type);

		where_str += " and block_type = '" + Block_Type + "'";

		m_db.delete(LOG_TABLE, where_str, null);
	}

	public void initDatabase() {
		m_db = m_ctx
				.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);

		m_db.execSQL("CREATE TABLE IF NOT EXISTS " + SETTING_TABLE
				+ " (option VARCHAR PRIMARY KEY , value VARCHAR);");
		m_db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ LOG_TABLE
				+ " (_id INTEGER PRIMARY KEY, time LONG, number VARCHAR, type INTEGER, block_type INTEGER, action INTEGER, sms_text VARCHAR, contact_name VARCHAR, scene_name VARCHAR, reply_sms_text VARCHAR );");

		m_db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ BLACK_LIST_TABLE
				+ " (number VARCHAR PRIMARY KEY, block_call INTEGER, block_sms INTEGER, tag VARCHAR, reply_sms VARCHAR);");

		writeSetting(PhoneNumberManager.OPTION_ALLOW_CONTACTS, true);
	}

	public String getNameById(int Contact_id) {
		Cursor cursor = m_ctx.getContentResolver().query(
				SmsBlocker.getSysCompat(m_ctx).CONTACT_URI,
				new String[] { SmsBlocker.getSysCompat(m_ctx).COLUMN_CONTACT_NAME },
				SmsBlocker.getSysCompat(m_ctx).COLUMN_CONTACT_ID + " = '" + Contact_id
						+ "'", null, null);

		if (cursor != null && cursor.getCount() != 0) {
			cursor.moveToFirst();
			String name = cursor.getString(cursor.getColumnIndex(SmsBlocker
					.getSysCompat(m_ctx).COLUMN_CONTACT_NAME));
			cursor.close();
			return name;
		}

		if (cursor != null) {
			cursor.close();
		}

		return null;
	}

	public boolean isBlBlockCall(String number) {
		Cursor cur = null;

		try {
			cur = m_db.query(BLACK_LIST_TABLE, new String[] { "block_call" },
					"number = '" + number + "'", null, null, null, null);

			if (cur == null) {
				return false;
			}

			cur.moveToFirst();

			int blockCallFlag = -1;

			blockCallFlag = cur.getInt(cur.getColumnIndex("block_call"));

			if (blockCallFlag == 1) {
				cur.close();
				return true;
			} else {
				cur.close();
				return false;
			}
		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}

			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return false;
		}

	}

	public boolean isBlBlockSms(String number) {
		Cursor cur = null;

		try {
			cur = m_db.query(BLACK_LIST_TABLE, new String[] { "block_sms" },
					"number = '" + number + "'", null, null, null, null);

			if (cur == null) {
				return false;
			}

			cur.moveToFirst();

			int blockSmsFlag = -1;

			blockSmsFlag = cur.getInt(cur.getColumnIndex("block_sms"));

			if (blockSmsFlag == 1) {
				cur.close();
				return true;
			} else {
				cur.close();
				return false;
			}
		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}
			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return false;
		}

	}

	public void removeFromContact(String numberToDelete) {
	}

	public String getTagByNumber(String phoneNumber) {
		Cursor cur = null;

		try {
			phoneNumber = PhoneNumberHelpers.delete86String(phoneNumber);
			phoneNumber = PhoneNumberHelpers.removeNonNumbericChar(phoneNumber);

			String whereString = PhoneNumberHelpers.buildNumberMatchQuery("?1",
					"number", false);

			cur = m_db.query(BLACK_LIST_TABLE, new String[] { "number", "tag" },
					whereString, new String[] { phoneNumber }, null, null, null);

			if (cur == null || cur.getCount() == 0) {
				return null;
			}

			cur.moveToFirst();

			return cur.getString(cur.getColumnIndex("tag"));
		} catch (Exception e) {
			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return null;
		} finally {
			if (cur != null)
				cur.close();
		}
	}

	public void queryBlackList(String number, PhoneNumberDisposition disp) {
		Cursor cur = null;

		try {
			String whereString = PhoneNumberHelpers.buildNumberMatchQuery("?1",
					"number", true);

			cur = m_db.query(BLACK_LIST_TABLE, new String[] { "number", "block_call",
					"block_sms", "tag" }, whereString, new String[] { number }, null,
					null, "block_sms asc, number desc");

			if (cur == null || cur.getCount() <= 0) {

				disp.m_CallAction = PhoneNumberDisposition.CALL_ACCEPT;
				disp.m_SmsAction = PhoneNumberDisposition.SMS_ACCEPT;

				return;
			}

			cur.moveToFirst();

			int block_call = cur.getInt(cur.getColumnIndex("block_call"));
			int block_sms = cur.getInt(cur.getColumnIndex("block_sms"));

			if (block_call == 1) {
				disp.m_CallAction = PhoneNumberDisposition.CALL_REJECT;
			} else {
				disp.m_CallAction = PhoneNumberDisposition.CALL_ACCEPT;
			}

			if (block_sms == 1) {
				disp.m_SmsAction = PhoneNumberDisposition.SMS_REJECT;
			} else {
				disp.m_SmsAction = PhoneNumberDisposition.SMS_ACCEPT;
			}

			disp.m_ReplySms = null;
		} catch (Exception e) {
			Log.e(m_ctx.getString(R.string.app_name), "queryBlackList fail", e);
			ActivityLog.logError(m_ctx, m_ctx.getString(R.string.app_name),
					e.getLocalizedMessage());
			disp.m_CallAction = PhoneNumberDisposition.CALL_ACCEPT;
			disp.m_SmsAction = PhoneNumberDisposition.SMS_ACCEPT;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}// try
	}
}
