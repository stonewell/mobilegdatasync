package com.angelstone.android.utils;

import java.text.MessageFormat;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.angelstone.android.platform.SysCompat;

public class PhoneNumberHelpers {
	public static String removeNonNumbericChar(String source) {
		StringBuffer sb = new StringBuffer();
		sb.append(source);

		for (int i = 0; i < sb.length(); i++) {
			if (!Character.isDigit(sb.charAt(i))) {
				sb.setCharAt(i, '#');
			}
		}

		String temp = sb.toString();

		temp = temp.replace("#", "");

		return temp;
	}

	public static String delete86String(String number) {
		int pos = number.indexOf("+86");

		if (pos != -1) {
			number = number.substring(pos + 3, number.length());
		}
		return number;
	}

	public static Cursor queryContactByPhoneNumber(Context context,
			String phoneNum) {
		SysCompat sc = SysCompat.register(context);

		phoneNum = delete86String(phoneNum);
		phoneNum = removeNonNumbericChar(phoneNum);

		return context.getContentResolver().query(
				Uri.withAppendedPath(sc.PHONE_LOOKUP_FILTER_URI, phoneNum),
				new String[] { sc.PHONE_LOOKUP_NUMBER, sc.PHONE_LOOKUP_NAME },
				null, null, null);
	}

	public static boolean isContact(Context context, String phoneNum) {
		Cursor cur = null;

		try {
			cur = queryContactByPhoneNumber(context, phoneNum);

			return cur != null && cur.getCount() > 0;
		} catch (Exception e) {
			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return false;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public static String buildNumberMatchQuery(String number, String bl_number,
			boolean doPrefixMatch) {
		StringBuffer buf = new StringBuffer();

		buf.append("( ");
		buf.append("(length({0}) < 7 and {0}={1}) or").append(" ");
		buf.append("(").append(" ");
		buf.append("length({0}) >= 7 and length({1}) >= 7 and").append(" ");
		buf.append("(").append(" ");
		buf.append("(").append(" ");
		buf.append(
				"length({1}) >= length({0}) and {0} = substr({1},-1 * length({0}))")
				.append(" ");
		buf.append(") or").append(" ");
		buf.append("(").append(" ");
		buf.append(
				"length({0}) >= length({1}) and substr({0}, -1 * length({1})) = {1}")
				.append(" ");
		buf.append(")").append(" ");
		buf.append(")").append(" ");
		buf.append(")").append(" ");

		if (doPrefixMatch) {
			buf.append("or").append(" ");
			buf.append(
					"(length({1}) <= length({0}) and {1}= substr({0}, 1, length({1})))")
					.append(" ");
		}
		buf.append(") ");

		return MessageFormat.format(buf.toString(), new Object[] { number,
				bl_number });
	}

	public static String getContactName(Context context, String phoneNum) {
		Cursor cur = null;

		try {
			cur = queryContactByPhoneNumber(context, phoneNum);

			if (cur == null || cur.getCount() < 0)
				return null;

			cur.moveToFirst();

			SysCompat sc = SysCompat.register(context);
			return cur.getString(cur.getColumnIndex(sc.COLUMN_PHONE_NAME));
		} catch (Exception e) {
			Log.e("getConactName", e.getLocalizedMessage(), e);

			return null;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public static boolean isSameNumber(String num, String blnum) {
		if (num == null || blnum == null) {
			return false;
		}

		if (num.length() < 7 || blnum.length() < 7) {
			if (num.equals(blnum)) {
				return true;
			} else {
				return false;
			}
		}

		if (num.length() > blnum.length()) {
			// must be appendix matching
			// if (num.indexOf(blnum) != -1)
			if (num.substring(num.length() - blnum.length()).compareTo(blnum) == 0) {
				return true;
			} else {
				return false;
			}
		} else {
			// if (blnum.indexOf(num) != -1)
			if (blnum.substring(blnum.length() - num.length()).compareTo(num) == 0) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static void removeFromContact(Context context, String phone) {
	}

	public static String getContactNameById(Context context, int contactId) {
		SysCompat sc = SysCompat.register(context);

		Uri uri = ContentUris.withAppendedId(sc.CONTACT_URI, contactId);

		Cursor c = null;
		try {
			c = context.getContentResolver().query(uri,
					new String[] { sc.COLUMN_CONTACT_NAME }, null, null, null);

			c.moveToFirst();
			return c.getString(0);
		} finally {
			if (c != null)
				c.close();
		}
	}
	
	public static int indexOfSelectedNumber(List<String> selectedNumbers, String number) {
		for (int i = 0; i < selectedNumbers.size(); i++) {
			if (PhoneNumberHelpers
					.isSameNumber(selectedNumbers.get(i), number)) {
				return i;
			}
		}

		return -1;
	}

	public static boolean containsNumber(List<String> selectedNumbers, String number) {
		return indexOfSelectedNumber(selectedNumbers, number) >= 0;
	}}
