package com.angelstone.android.smsblocker.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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

public class RejectedSmsLogView extends Activity implements
		OnItemLongClickListener, OnItemClickListener, OnClickListener {
	private int mPosition = 0;
	private ListView lv = null;

	private Cursor mLogCursor;

	public static final int CLEAR_CALL_LOG = 1;

	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;

	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.reject_log_view_layout);

			mLogCursor = PhoneNumberManager.getIntance(this).getCallRejectLogCursor(
					EventLog.SMS_LOG_BLOCK_TYPE_BL);

			lv = (ListView) this.findViewById(R.id.reject_log_list);

			registerForContextMenu(lv);
			lv.setOnItemLongClickListener(this);
			lv.setOnItemClickListener(this);
			refreshList();

		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}

	protected void onDestroy() {
		if (mLogCursor != null) {
			mLogCursor.close();
		}
		super.onDestroy();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
			long id) {
		mPosition = position;
		return false;
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(0, 5, 5, R.string.Delete);
		menu.setHeaderTitle(R.string.Menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 5: {
			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.alert_dialog_two_buttons_title)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									PhoneNumberManager.getIntance(RejectedSmsLogView.this)
											.deleteLog(mLogCursor, mPosition);

									if (mLogCursor != null) {
										mLogCursor.close();
									}

									mLogCursor = PhoneNumberManager.getIntance(
											RejectedSmsLogView.this).getCallRejectLogCursor(
											EventLog.SMS_LOG_BLOCK_TYPE_BL);
									refreshList();
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
		case 6:
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		menu.clear();
		menu.add(0, 0, 0, R.string.ClearAllRecords).setIcon(
				this.getResources().getDrawable(R.drawable.ic_menu_delete));

		if (mLogCursor != null) {
			if (mLogCursor.getCount() == 0) {
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
									intent.putExtra("log_type", EventLog.LOG_TYPE_SMS);
									intent.putExtra("block_type", EventLog.SMS_LOG_BLOCK_TYPE_BL);
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
				R.layout.call_reject_record_item, mLogCursor)));
		if (mLogCursor != null) {
			if (mLogCursor.getCount() != 0) {
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

	public void refreshList_2() {
		try {
			mLogCursor.requery();

		} catch (Exception e) {
			Log.d("scfw", e.getMessage() + ":" + e.getClass().toString());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			switch (resultCode) {
			case 3: {
				mLogCursor.requery();
			}
			default:
				break;
			}
		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}

	}

	@Override
	public void onClick(View v) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Intent intent = new Intent();
		intent.setClass(this, RejectedSmsBodyView.class);

		Bundle bundle = new Bundle();
		bundle.putInt("click_pos", position);
		intent.putExtras(bundle);

		startActivity(intent);

	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshList_2();
	}

}
