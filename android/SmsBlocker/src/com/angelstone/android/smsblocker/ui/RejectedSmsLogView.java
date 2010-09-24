package com.angelstone.android.smsblocker.ui;

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

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.EventLog;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.smsblocker.store.PhoneNumberManager.BlockListAction;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.SmsHelper;

public class RejectedSmsLogView extends Activity implements
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

		mCursor = PhoneNumberManager.getEventLogs(this);
		startManagingCursor(mCursor);

		lv = (ListView) this.findViewById(R.id.reject_log_list);

		registerForContextMenu(lv);
		lv.setOnItemLongClickListener(this);
		lv.setOnItemClickListener(this);
		refreshList();

		mObserver = new EventLogObserver(mHandler);
		getContentResolver().registerContentObserver(
				EventLog.CONTENT_EVENT_LOG_URI, true, mObserver);
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
		menu.add(0, 0, 0, R.string.allow_sms_from_number);
		menu.add(0, 1, 1, R.string.Delete);
		menu.setHeaderTitle(R.string.Menu);
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
					.setTitle(R.string.alert_dialog_two_buttons_title)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									mCursor.moveToPosition(mPosition);
									PhoneNumberManager.deleteLog(RejectedSmsLogView.this,
											mCursor.getInt(mCursor.getColumnIndex(EventLog._ID)));
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
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
				.getColumnIndex(EventLog.NUMBER));
		String text = MessageFormat.format(
				getString(R.string.add_number_to_allow_list), number);
		AlertDialog ad = new AlertDialog.Builder(this)
				.setIcon(R.drawable.alert_dialog_icon)
				.setTitle(R.string.allow_sms_from_number)
				.setMessage(text)
				.setPositiveButton(R.string.alert_dialog_ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								String realnumber = PhoneNumberHelpers
										.removeNonNumbericChar(number);
								if (PhoneNumberManager.blacklistContainsNumber(
										RejectedSmsLogView.this, realnumber) == BlockListAction.NO_NUMBER) {
									PhoneNumberManager.blacklistAddNumber(
											RejectedSmsLogView.this, realnumber, false);
								} else {
									PhoneNumberManager.blacklistUpdateNumber(
											RejectedSmsLogView.this, realnumber, false);
								}

								clearNotSpamLogs(number);

								mCursor.requery();
							}
						})
				.setNegativeButton(R.string.alert_dialog_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								/* User clicked Cancel so do some stuff */
							}
						}).create();
		ad.show();
	}

	private void clearNotSpamLogs(String number) {
		Cursor cur = PhoneNumberManager.getEventLogs(this, "number=?",
				new String[] { number });
		try {
			int bodyIndex = cur.getColumnIndex(EventLog.SMS_TEXT);
			int timeIndex = cur.getColumnIndex(EventLog.TIME);
			while (cur.moveToNext()) {
				String body = cur.getString(bodyIndex);
				long date = Long.parseLong(cur.getString(timeIndex));

				SmsHelper.sendToSmsInbox(this, number, body, date);
			}

			PhoneNumberManager.deleteLogs(this, "number=?", new String[] { number });
		} finally {
			cur.close();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.clear();
		menu.add(0, 0, 0, R.string.ClearAllRecords).setIcon(
				this.getResources().getDrawable(android.R.drawable.ic_menu_delete));

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
					.setTitle(R.string.alert_dialog_two_buttons_title_3)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									Intent intent = new Intent();
									intent.setClass(RejectedSmsLogView.this,
											ClearWaitingDialog.class);
									intent.putExtra("clear_type", CLEAR_CALL_LOG);
									startActivityForResult(intent, 2);

								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {

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
		lv.setAdapter((new RejectedSmsLogAdapter(this,
				R.layout.call_reject_record_item, mCursor)));
		if (mCursor != null) {
			if (mCursor.getCount() != 0) {
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, 0);
				TextView tv = (TextView) findViewById(R.id.empty_record_text);
				tv.setLayoutParams(param);

			} else {
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC, WC);
				param.weight = 1;
				TextView tv = (TextView) findViewById(R.id.empty_record_text);
				tv.setLayoutParams(param);
			}
		} else {

			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC, FC);
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
		Intent intent = new Intent();
		intent.setClass(this, RejectedSmsBodyView.class);

		mCursor.moveToPosition(position);
		Bundle bundle = new Bundle();
		bundle.putInt("click_id", mCursor.getInt(mCursor.getColumnIndex(EventLog._ID)));
		intent.putExtras(bundle);

		startActivity(intent);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
