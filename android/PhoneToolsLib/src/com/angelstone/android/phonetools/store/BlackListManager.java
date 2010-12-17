package com.angelstone.android.phonetools.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.angelstone.android.phonetools.R;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class BlackListManager {
	public static final int INSERT_ERROR_NONE = 0;
	public static final int INSERT_ERROR_AREADY_EXIST = 2;
	public static final int INSERT_ERROR_EXCEPTION_OCCUR = -1;

	public static enum BlockListAction {
		NO_NUMBER, BLOCK, NOT_BLOCK,
	};

	private String mAuthority = null;
	private Uri mContentUri = null;

	public BlackListManager(String authority) {
		mAuthority = authority;
		mContentUri = new BlackList().getContentUri(mAuthority);
	}

	public PhoneNumberDisposition queryAction(Context context, String number) {
		PhoneNumberDisposition disp = new PhoneNumberDisposition();
		queryBlackList(context, number, disp);

		return disp;
	}

	public Cursor getBlacklistNumbers(Context context) {
		return context.getContentResolver().query(
				mContentUri,
				new String[] { BlackList.COL_ID, BlackList.COL_NUMBER,
						BlackList.COL_BLOCK }, null, null, null);
	}

	public int blacklistAddNumber(Context context, String number, boolean block) {
		int err = INSERT_ERROR_NONE;
		Cursor cur = null;

		try {
			String where_str = BlackList.COL_NUMBER + "=?";

			cur = context.getContentResolver().query(mContentUri,
					new String[] { BlackList.COL_ID }, where_str,
					new String[] { number }, null);

			if (cur != null && cur.getCount() != 0) {
				return INSERT_ERROR_AREADY_EXIST;
			} // number already existed

			ContentValues args = new ContentValues();
			args.put(BlackList.COL_NUMBER, number);
			args.put(BlackList.COL_BLOCK, block ? 1 : 0);
			context.getContentResolver().insert(mContentUri, args);

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

	public int blacklistDeleteNumber(Context context) {
		int err = 0;

		context.getContentResolver().delete(mContentUri, null, null);

		return err;
	}

	public int blacklistDeleteNumber(Context context, String number) {
		int err = 0;

		String where_str = BlackList.COL_NUMBER + "=?";

		context.getContentResolver().delete(mContentUri, where_str,
				new String[] { number });

		return err;
	}

	public int blacklistUpdateNumber(Context context, String number,
			boolean blockSms) {
		return blacklistUpdateNumber(context, number, number, blockSms);
	}

	public int blacklistUpdateNumber(Context context, String oldNumber,
			String number, boolean blockSms) {
		int err = 0;
		ContentValues args = new ContentValues();
		args.put(BlackList.COL_BLOCK, blockSms ? 1 : 0);
		if (!oldNumber.equals(number)) {
			args.put(BlackList.COL_NUMBER, number);
		}
		context.getContentResolver().update(mContentUri, args,
				BlackList.COL_NUMBER + "=?", new String[] { oldNumber });
		return err;
	}

	public BlockListAction blacklistContainsNumber(Context context,
			String number) {
		Cursor cur = null;

		try {
			cur = context.getContentResolver()
					.query(mContentUri, new String[] { BlackList.COL_BLOCK },
							BlackList.COL_NUMBER + "= ?",
							new String[] { number }, null);

			if (cur == null) {
				return BlockListAction.NO_NUMBER;
			}

			if (!cur.moveToFirst())
				return BlockListAction.NO_NUMBER;

			return 1 == cur.getInt(cur.getColumnIndex(BlackList.COL_BLOCK)) ? BlockListAction.BLOCK
					: BlockListAction.NOT_BLOCK;
		} catch (Exception e) {
			Log.e("PhoneNumberManager", e.getLocalizedMessage(), e);

			return BlockListAction.NO_NUMBER;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

	}

	public void queryBlackList(Context context, String number,
			PhoneNumberDisposition disp) {
		Cursor cur = null;

		try {
			String whereString = PhoneNumberHelpers.buildNumberMatchQuery("?1",
					BlackList.COL_NUMBER, true);

			cur = context.getContentResolver().query(mContentUri,
					new String[] { BlackList.COL_NUMBER, BlackList.COL_BLOCK },
					whereString, new String[] { number }, null);

			if (cur == null || cur.getCount() <= 0) {

				disp.mAction = PhoneNumberDisposition.ACCEPT;

				return;
			}

			cur.moveToFirst();

			int block_sms = cur.getInt(cur.getColumnIndex(BlackList.COL_BLOCK));

			if (block_sms == 1) {
				disp.mAction = PhoneNumberDisposition.REJECT;
			} else {
				disp.mAction = PhoneNumberDisposition.ACCEPT;
			}

		} catch (Exception e) {
			Log.e(context.getString(R.string.app_name), "queryBlackList fail",
					e);
			ActivityLog.logError(context, context.getString(R.string.app_name),
					e.getLocalizedMessage());
			disp.mAction = PhoneNumberDisposition.ACCEPT;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}// try
	}

	public Uri getContentUri() {
		return mContentUri;
	}

	public void setContentUri(Uri contentUri) {
		mContentUri = contentUri;
	}
}
