package com.angelstone.android.callfirewall.ui;

import java.text.MessageFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.angelstone.android.callfirewall.CallFireWallConstants;
import com.angelstone.android.callfirewall.R;
import com.angelstone.android.phonetools.store.BlackListManager.BlockListAction;
import com.angelstone.android.phonetools.store.EventLog;
import com.angelstone.android.phonetools.store.PhoneToolsDBManager;
import com.angelstone.android.phonetools.ui.ClearWaitingDialog;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.SmsHelper;

public class RejectedCallLogView extends Activity implements
		OnItemLongClickListener, OnItemClickListener, OnClickListener {
	private ListView lv = null;

	private int mPosition = 0;
	private Cursor mCursor;

	public static final int CLEAR_CALL_LOG = 1;

	private final static int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final static int FC = ViewGroup.LayoutParams.FILL_PARENT;

	private Handler mHandler = new Handler();
	private EventLogObserver mObserver = null;

	private class EventLogObserver extends ContentObserver {

		public EventLogObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			if (mCursor != null)
				mCursor.requery();
			refreshList();
		}

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reject_log_view_layout);

		mCursor = PhoneToolsDBManager.getEventLogManager().getEventLogs(this);
		startManagingCursor(mCursor);

		lv = (ListView) this.findViewById(R.id.reject_log_list);

		registerForContextMenu(lv);
		lv.setOnItemLongClickListener(this);
		lv.setOnItemClickListener(this);
		refreshList();

		mObserver = new EventLogObserver(mHandler);
		getContentResolver().registerContentObserver(
				new EventLog().getContentUri(CallFireWallConstants.AUTHORITY), true,
				mObserver);
	}

	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(mObserver);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
			long id) {
		mPosition = position;
		return false;
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(0, 0, 0, R.string.not_spam);
		menu.add(0, 1, 1, R.string.delete);
		menu.setHeaderTitle(R.string.menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0: {
			setNotASpam();
			break;
		}
		case 1: {
			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.delete_confirm)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mCursor.moveToPosition(mPosition);
									PhoneToolsDBManager
											.getEventLogManager()
											.deleteLog(
													RejectedCallLogView.this,
													mCursor.getInt(mCursor
															.getColumnIndex(EventLog.COL_ID)));
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked Cancel so do some stuff */
								}
							}).create();
			ad.show();

			break;
		}
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	private void setNotASpam() {
		mCursor.moveToPosition(mPosition);
		final String number = mCursor.getString(mCursor
				.getColumnIndex(EventLog.COL_NUMBER));
		String text = MessageFormat.format(
				getString(R.string.add_number_to_allow_list), number);
		AlertDialog ad = new AlertDialog.Builder(this)
				.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(R.string.not_spam)
				.setMessage(text)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String realnumber = PhoneNumberHelpers
										.removeNonNumbericChar(number);
								if (PhoneToolsDBManager.getBlackListManager()
										.blacklistContainsNumber(
												RejectedCallLogView.this,
												realnumber) == BlockListAction.NO_NUMBER) {
									PhoneToolsDBManager.getBlackListManager()
											.blacklistAddNumber(
													RejectedCallLogView.this,
													realnumber, false);
								} else {
									PhoneToolsDBManager.getBlackListManager()
											.blacklistUpdateNumber(
													RejectedCallLogView.this,
													realnumber, false);
								}

								clearNotSpamLogs(number);

								mCursor.requery();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								/* User clicked Cancel so do some stuff */
							}
						}).create();
		ad.show();
	}

	private void clearNotSpamLogs(String number) {
		Cursor cur = PhoneToolsDBManager.getEventLogManager().getEventLogs(
				this, EventLog.COL_NUMBER + "=?", new String[] { number });
		try {
			int bodyIndex = cur.getColumnIndex(EventLog.COL_CONTENT);
			int timeIndex = cur.getColumnIndex(EventLog.COL_TIME);
			while (cur.moveToNext()) {
				String body = cur.getString(bodyIndex);
				long date = Long.parseLong(cur.getString(timeIndex));

				SmsHelper.sendToSmsInbox(this, number, body, date);
			}

			PhoneToolsDBManager.getEventLogManager().deleteLogs(this,
					EventLog.COL_NUMBER + "=?", new String[] { number });
		} finally {
			cur.close();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.clear();
		menu.add(0, 0, 0, R.string.ClearAllRecords).setIcon(
				this.getResources().getDrawable(
						android.R.drawable.ic_menu_delete));

		if (mCursor != null) {
			if (mCursor.getCount() == 0) {
				menu.getItem(0).setEnabled(false);
			}
		} else {
			menu.getItem(0).setEnabled(false);
		}

		// mMenu = menu;

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0: {
			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.note)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Intent intent = new Intent();
									intent.setClass(RejectedCallLogView.this,
											ClearWaitingDialog.class);
									intent.putExtra("clear_type",
											CLEAR_CALL_LOG);
									startActivityForResult(intent, 2);

								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									/* User clicked Cancel so do some stuff */
								}
							}).create();

			ad.show();

			return true;
		}
		default:
			return true;
		}
	}

	public void refreshList() {
		lv.setAdapter((new RejectedCallLogAdapter(this,
				R.layout.call_reject_record_item, mCursor)));
		if (mCursor != null) {
			if (mCursor.getCount() != 0) {
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						0, 0);
				TextView tv = (TextView) findViewById(R.id.empty_record_text);
				tv.setLayoutParams(param);

			} else {
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						FC, WC);
				param.weight = 1;
				TextView tv = (TextView) findViewById(R.id.empty_record_text);
				tv.setLayoutParams(param);
			}
		} else {

			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC,
					FC);
			TextView tv = (TextView) findViewById(R.id.empty_record_text);
			tv.setLayoutParams(param);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case 3: {
			break;
		}
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
