package com.angelstone.android.phonetools.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.angelstone.android.phonetools.store.BlackList;
import com.angelstone.android.phonetools.store.BlackListManager;
import com.angelstone.android.phonetools.store.PhoneToolsDBManager;
import com.angelstone.android.phonetools.store.PhoneToolsDatabaseValues;
import com.angelstone.android.ui.GenericActivity;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class BlackListView extends GenericActivity implements OnClickListener,
		OnItemLongClickListener, OnItemClickListener {
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FC = ViewGroup.LayoutParams.FILL_PARENT;

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

	public BlackListView() {
		super(1);
		setMenuEnableImportExport(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bl_mgr_root_view);

		CheckBox chkBox = (CheckBox) findViewById(R.id.sms_allow_contacts_check_box);
		chkBox.setOnClickListener(this);
		chkBox.setChecked(PhoneToolsDBManager.getSettingsManager().readSetting(
				this, PhoneToolsDatabaseValues.OPTION_ALLOW_CONTACTS));

		mAddbuttion = (Button) findViewById(R.id.add_number_btn);
		mAddbuttion.setOnClickListener(this);

		mListview = (ListView) this.findViewById(R.id.exist_mgr_bl_list);
		registerForContextMenu(mListview);
		mListview.setOnItemLongClickListener(this);
		mListview.setOnItemClickListener(this);

		mCursor = PhoneToolsDBManager.getBlackListManager().getBlacklistNumbers(
				this);
		startManagingCursor(mCursor);

		BlackListViewAdapter adapter = new BlackListViewAdapter(this, mCursor);
		mListview.setAdapter(adapter);

		refreshViewList();

		mObserver = new BlackListObserver(mHandler);
		getContentResolver().registerContentObserver(
				PhoneToolsDBManager.getBlackListManager().getContentUri(), true,
				mObserver);
	}

	private void refreshViewList() {
		if (mCursor.getCount() == 0) {
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(FC, FC);
			ScrollView sv = (ScrollView) findViewById(R.id.bl_root_mgr_user_guide_text);
			sv.setLayoutParams(param);

			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(0, 0);
			TextView tv = (TextView) findViewById(R.id.add_bl_number_edit_guide_text);
			tv.setLayoutParams(param1);

		} else {
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(0, 0);
			ScrollView sv = (ScrollView) findViewById(R.id.bl_root_mgr_user_guide_text);
			sv.setLayoutParams(param);

			LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(WC, WC);
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
	protected void onResume() {
		super.onResume();

		mCursor.requery();

		CheckBox chkBox = (CheckBox) findViewById(R.id.sms_allow_contacts_check_box);
		chkBox.setChecked(PhoneToolsDBManager.getSettingsManager().readSetting(
				this, PhoneToolsDatabaseValues.OPTION_ALLOW_CONTACTS));
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

			PhoneToolsDBManager.getSettingsManager().writeSetting(this,
					PhoneToolsDatabaseValues.OPTION_ALLOW_CONTACTS, chkBox.isChecked());
			break;
		}
		default:
			break;
		}
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		menu.add(0, 1, 1, R.string.edit);
		menu.add(0, 2, 2, R.string.delete);
		menu.setHeaderTitle(R.string.menu);

		super.onCreateContextMenu(menu, v, menuInfo);

	}

	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1: {
			Cursor c = (Cursor) mListview.getItemAtPosition(mListLongClickPos);

			int numberColumnId = c.getColumnIndex(BlackList.COL_NUMBER);
			int blockSmsColumnId = c.getColumnIndex(BlackList.COL_BLOCK);

			String number = c.getString(numberColumnId);

			boolean blockSms = c.getInt(blockSmsColumnId) == 1;

			editBlackListNumber(number, blockSms);
			break;
		}
		case 2: {
			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.delete_confirm)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									Cursor c = (Cursor) mListview
											.getItemAtPosition(mListLongClickPos);

									int id = c.getColumnIndex(BlackList.COL_ID);

									Uri uri = ContentUris.withAppendedId(PhoneToolsDBManager
											.getBlackListManager().getContentUri(), c.getLong(id));

									BlackListView.this.getContentResolver().delete(uri, null,
											null);
								}
							})
					.setNegativeButton(android.R.string.cancel,
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

	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		mListLongClickPos = position;
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		
		switch (requestCode) {
		case 1: // add result
		{
			String[] addedNumbers = data.getExtras().getStringArray(
					"added_numbers_result");
			boolean blockSms = data.getExtras().getBoolean("sms_block");
			boolean isRemoveFromContact = data.getExtras().getBoolean(
					"is_remove_from_contact");

			// //phone_id is useless, always set it to 0 is OK
			for (int i = 0; i < addedNumbers.length; i++) {
				if (PhoneToolsDBManager.getBlackListManager().blacklistAddNumber(this,
						addedNumbers[i], blockSms) == BlackListManager.INSERT_ERROR_AREADY_EXIST) {
					continue;
				}

				if (isRemoveFromContact
						&& PhoneNumberHelpers.isContact(this, addedNumbers[i])) {
					PhoneNumberHelpers.removeFromContact(this, addedNumbers[i]);
				}
			}
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

		menu.add(0, 0, 0, R.string.clear_all).setIcon(
				android.R.drawable.ic_menu_delete);

		if (mCursor == null || mCursor.getCount() == 0) {
			menu.getItem(0).setEnabled(false);
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
					.setTitle(R.string.clear_all_confirm)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									Intent intent = new Intent();
									intent.setClass(BlackListView.this, ClearWaitingDialog.class);
									intent.putExtra("clear_type",
											UIConstants.CLEAR_BLACK_LIST_NUMBER);
									startActivityForResult(intent, 5);
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {

								}
							}).create();
			ad.show();

			break;
		}
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	}

	@Override
	protected String getImportExportFilePrefix() {
		return PhoneToolsDBManager.getAuthority();
	}

	@Override
	protected void exportTo(BufferedWriter writer) throws IOException {
		if (mCursor != null && mCursor.getCount() > 0) {
			try {
				int pos = mCursor.getPosition();

				mCursor.moveToFirst();

				int nIdx = mCursor.getColumnIndex(BlackList.COL_NUMBER);
				int nIdx2 = mCursor.getColumnIndex(BlackList.COL_BLOCK);

				do {
					String number = mCursor.getString(nIdx);
					int block = mCursor.getInt(nIdx2);

					writer.write(number + "," + block);
					writer.newLine();
				} while (mCursor.moveToNext());

				mCursor.moveToPosition(pos);
			} finally {
				writer.flush();
			}
		}
	}

	@Override
	protected int importFrom(BufferedReader br) throws IOException {
		String line = null;

		HashMap<String, Integer> numbers = new HashMap<String, Integer>();

		while ((line = br.readLine()) != null) {
			line = line.trim();
			validateAndSave(line, numbers);
		}

		if (numbers.size() == 0) {
			return 1;
		}

		for (String key : numbers.keySet()) {
			PhoneToolsDBManager.getBlackListManager()
					.blacklistDeleteNumber(this, key);
			PhoneToolsDBManager.getBlackListManager().blacklistAddNumber(this, key,
					numbers.get(key) == 1);
		}

		return 0;
	}

	private void validateAndSave(String line, HashMap<String, Integer> numbers) {
		String[] parts = line.split(",");

		if (parts.length != 2)
			return;

		if (!PhoneNumberHelpers.isValidNumber(parts[0]))
			return;

		// Validate block
		parts[1] = parts[1].trim();

		if (parts[1].length() != 1)
			return;

		if (parts[1].charAt(0) != '0' && parts[1].charAt(0) != '1') {
			return;
		}

		numbers.put(parts[0], parts[1].charAt(0) == '0' ? 0 : 1);
	}

	private void editBlackListNumber(final String number, final boolean blockSms) {
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.edit_bl_number_view,
				null);

		final EditText etNumber = (EditText) textEntryView
				.findViewById(R.id.edit_bl_number_editor);
		final CheckBox chkBlock = (CheckBox) textEntryView
				.findViewById(R.id.block_sms_check_box);

		etNumber.setText(number);
		chkBlock.setChecked(blockSms);

		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String editNum = etNumber.getText().toString();

				boolean editblockSms = chkBlock.isChecked();

				if (editNum.equals(number) && editblockSms == blockSms)
					return;

				editNum = PhoneNumberHelpers.removeNonNumbericChar(editNum);
				PhoneToolsDBManager.getBlackListManager().blacklistUpdateNumber(
						BlackListView.this, number, editNum, editblockSms);
			}

		};
		new AlertDialog.Builder(this)
				.setTitle(R.string.edit)
				.setNegativeButton(android.R.string.cancel, null)
				.setView(textEntryView)
				.setPositiveButton(android.R.string.ok, listener).show();
	}

}
