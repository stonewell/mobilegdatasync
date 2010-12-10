package com.angelstone.android.smsblocker.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.BlackList;
import com.angelstone.android.smsblocker.store.DatabaseValues;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.ui.ActivityLogActivity;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class BlackListManageRootView extends Activity implements
		OnClickListener, OnItemLongClickListener, OnItemClickListener {
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	public static final int CLEAR_BLACK_LIST_NUMBER = 0;

	private int mListLongClickPos = 0;

	private ListView mListview;
	private Button mAddbuttion;
	private Cursor mCursor;

	private Handler mHandler = new Handler();
	private BlackListObserver mObserver = null;

	private class BlackListObserver extends ContentObserver {

		public BlackListObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			if (mCursor != null)
				mCursor.requery();
			refreshViewList();
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bl_mgr_root_view);

		CheckBox chkBox = (CheckBox) findViewById(R.id.sms_allow_contacts_check_box);
		chkBox.setOnClickListener(this);
		chkBox.setChecked(PhoneNumberManager.readSetting(this,
				DatabaseValues.OPTION_ALLOW_CONTACTS));

		mAddbuttion = (Button) findViewById(R.id.add_number_btn);
		mAddbuttion.setOnClickListener(this);

		mListview = (ListView) this.findViewById(R.id.exist_mgr_bl_list);
		registerForContextMenu(mListview);
		mListview.setOnItemLongClickListener(this);
		mListview.setOnItemClickListener(this);

		mCursor = PhoneNumberManager.getBlacklistNumbers(this);
		startManagingCursor(mCursor);

		BlackListViewAdapter adapter = new BlackListViewAdapter(this, mCursor);
		mListview.setAdapter(adapter);

		refreshViewList();

		mObserver = new BlackListObserver(mHandler);
		getContentResolver().registerContentObserver(
				BlackList.CONTENT_BLACK_LIST_URI, true, mObserver);
	}

	private void refreshViewList() {
		if (mCursor.getCount() == 0) {
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC,
					FC);
			ScrollView sv = (ScrollView) findViewById(R.id.bl_root_mgr_user_guide_text);
			sv.setLayoutParams(param);

			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(0,
					0);
			TextView tv = (TextView) findViewById(R.id.add_bl_number_edit_guide_text);
			tv.setLayoutParams(param1);

		} else {
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0,
					0);
			ScrollView sv = (ScrollView) findViewById(R.id.bl_root_mgr_user_guide_text);
			sv.setLayoutParams(param);

			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
					WC, WC);
			param1.gravity = Gravity.CENTER_HORIZONTAL;
			TextView tv = (TextView) findViewById(R.id.add_bl_number_edit_guide_text);
			tv.setLayoutParams(param1);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(mObserver);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mCursor.requery();

		CheckBox chkBox = (CheckBox) findViewById(R.id.sms_allow_contacts_check_box);
		chkBox.setChecked(PhoneNumberManager.readSetting(this,
				DatabaseValues.OPTION_ALLOW_CONTACTS));
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_number_btn: {
			Intent intent = new Intent();
			intent.setClass(this, AddBlackListNumberView.class);
			startActivityForResult(intent, 1);
			break;
		}

		case R.id.sms_allow_contacts_check_box: {
			CheckBox chkBox = (CheckBox) findViewById(R.id.sms_allow_contacts_check_box);

			PhoneNumberManager.writeSetting(this,
					DatabaseValues.OPTION_ALLOW_CONTACTS, chkBox.isChecked());
			break;
		}
		default:
			break;
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		menu.add(0, 1, 1, R.string.Edit);
		menu.add(0, 2, 2, R.string.Delete);
		menu.setHeaderTitle(R.string.Menu);

		super.onCreateContextMenu(menu, v, menuInfo);

	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1: {
			Cursor c = (Cursor) mListview.getItemAtPosition(mListLongClickPos);

			int numberColumnId = c.getColumnIndex(BlackList.NUMBER);
			int blockSmsColumnId = c.getColumnIndex(BlackList.BLOCK_SMS);

			String number = c.getString(numberColumnId);

			boolean blockSms = c.getInt(blockSmsColumnId) == 1;

			Intent intent = new Intent();
			intent.setClass(BlackListManageRootView.this,
					EditBlackListNumberView.class);

			Bundle bundle = new Bundle();
			bundle.putInt("POSITION", mListLongClickPos);
			bundle.putString("EDIT_NUMBER", number);
			bundle.putBoolean("block_sms", blockSms);
			intent.putExtras(bundle);
			startActivityForResult(intent, 2);
			break;
		}
		case 2: {
			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.alert_dialog_two_buttons_title)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Cursor c = (Cursor) mListview
											.getItemAtPosition(mListLongClickPos);

									int id = c.getColumnIndex(BlackList._ID);

									Uri uri = ContentUris.withAppendedId(
											BlackList.CONTENT_BLACK_LIST_URI,
											c.getLong(id));

									BlackListManageRootView.this
											.getContentResolver().delete(uri,
													null, null);
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									/* User clicked Cancel so do some stuff */
								}
							}).create();
			ad.show();

			break;

		}
		case 4:
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		mListLongClickPos = position;
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case 1: // add result
		{
			String[] addedNumbers = data.getExtras().getStringArray(
					"added_numbers_result");
			boolean blockSms = data.getExtras().getBoolean("sms_block");
			boolean isRemoveFromContact = data.getExtras().getBoolean(
					"is_remove_from_contact");

			// //phone_id is useless, always set it to 0 is OK
			for (int i = 0; i < addedNumbers.length; i++) {
				if (PhoneNumberManager.blacklistAddNumber(this,
						addedNumbers[i], blockSms) == PhoneNumberManager.INSERT_ERROR_AREADY_EXIST) {
					continue;
				}

				if (isRemoveFromContact
						&& PhoneNumberHelpers.isContact(this, addedNumbers[i])) {
					PhoneNumberHelpers.removeFromContact(this, addedNumbers[i]);
				}
			}
			break;
		}
		case 2: // edit result
		{
			String editNumBefore = data.getExtras().getString("edited_number");
			String editNum = data.getExtras().getString("edited_number_return");
			boolean blockSms = data.getExtras().getBoolean("sms_block");

			editNum = PhoneNumberHelpers.removeNonNumbericChar(editNum);
			PhoneNumberManager.blacklistUpdateNumber(this, editNumBefore,
					editNum, blockSms);
			break;
		}
		case 5: {
			ActivityLog.logInfo(this, getString(R.string.app_name),
					getString(R.string.BlacklistCleared));
		}

		default:
			break;
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		menu.add(0, 0, 1, R.string.ClearAll).setIcon(
				android.R.drawable.ic_menu_delete);
		menu.add(0, 1, 2, R.string.Log)
				.setIcon(R.drawable.ic_menu_activity_log);
		menu.add(0, 2, 3, R.string.About).setIcon(
				android.R.drawable.ic_menu_help);
		menu.add(0, 3, 0, R.string.backup).setIcon(
				android.R.drawable.ic_menu_share);

		if (mCursor == null || mCursor.getCount() == 0) {
			menu.getItem(1).setEnabled(false);
		}

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
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Intent intent = new Intent();
									intent.setClass(
											BlackListManageRootView.this,
											ClearWaitingDialog.class);
									intent.putExtra("clear_type",
											CLEAR_BLACK_LIST_NUMBER);
									startActivityForResult(intent, 5);
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).create();
			ad.show();

			break;
		}
		case 1: {
			Intent intent = new Intent();
			intent.setClass(this, ActivityLogActivity.class);
			startActivity(intent);
			break;
		}
		case 2: {
			showAbout();
			break;
		}
		default:
			break;
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	}

	protected void showAbout() {
		// Inflate the about message contents
		View messageView = getLayoutInflater().inflate(R.layout.about, null,
				false);

		// When linking text, force to always use default color. This works
		// around a pressed color state bug.
		TextView textView = (TextView) messageView
				.findViewById(R.id.about_credits);
		int defaultColor = textView.getTextColors().getDefaultColor();
		textView.setTextColor(defaultColor);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.icon);

		String version = "";
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = info.versionName;
		} catch (Exception e) {

		}
		builder.setTitle(getString(R.string.app_name) + " " + version);
		builder.setView(messageView);
		builder.create();
		builder.show();
	}
}
