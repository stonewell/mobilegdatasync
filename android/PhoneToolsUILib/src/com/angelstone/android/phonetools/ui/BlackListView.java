package com.angelstone.android.phonetools.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextThemeWrapper;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.angelstone.android.phonetools.store.BlackList;
import com.angelstone.android.phonetools.store.BlackListManager;
import com.angelstone.android.phonetools.store.PhoneToolsDBManager;
import com.angelstone.android.phonetools.store.PhoneToolsDatabaseValues;
import com.angelstone.android.ui.ActivityLogActivity;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class BlackListView extends Activity implements OnClickListener,
		OnItemLongClickListener, OnItemClickListener {
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final int FC = ViewGroup.LayoutParams.FILL_PARENT;

	private int mListLongClickPos = 0;

	private ListView mListview;
	private Button mAddbuttion;
	private Cursor mCursor;
	private Toast mToast = null;

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
	protected void onPause() {
		super.onPause();
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

		menu.add(0, 1, 1, R.string.Edit);
		menu.add(0, 2, 2, R.string.Delete);
		menu.setHeaderTitle(R.string.Menu);

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

			Intent intent = new Intent();
			intent.setClass(BlackListView.this, EditNumberView.class);

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
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
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
		case 2: // edit result
		{
			String editNumBefore = data.getExtras().getString("edited_number");
			String editNum = data.getExtras().getString("edited_number_return");
			boolean blockSms = data.getExtras().getBoolean("sms_block");

			editNum = PhoneNumberHelpers.removeNonNumbericChar(editNum);
			PhoneToolsDBManager.getBlackListManager().blacklistUpdateNumber(this,
					editNumBefore, editNum, blockSms);
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
		menu.add(0, 1, 2, R.string.Log).setIcon(R.drawable.ic_menu_activity_log);
		menu.add(0, 2, 3, R.string.About).setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 3, 0, R.string.import_export).setIcon(
				R.drawable.ic_menu_import_export);

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
								public void onClick(DialogInterface dialog, int whichButton) {
									Intent intent = new Intent();
									intent.setClass(BlackListView.this, ClearWaitingDialog.class);
									intent.putExtra("clear_type",
											UIConstants.CLEAR_BLACK_LIST_NUMBER);
									startActivityForResult(intent, 5);
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {

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
		case 3: {
			doImportExport();
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
		View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

		// When linking text, force to always use default color. This works
		// around a pressed color state bug.
		TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
		int defaultColor = textView.getTextColors().getDefaultColor();
		textView.setTextColor(defaultColor);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.icon);

		String version = "";
		try {
			PackageInfo info = getPackageManager()
					.getPackageInfo(getPackageName(), 0);
			version = info.versionName;
		} catch (Exception e) {

		}
		builder.setTitle(getString(R.string.app_name) + " " + version);
		builder.setView(messageView);
		builder.create();
		builder.show();
	}

	protected void doImportExport() {
		// Wrap our context to inflate list items using correct theme
		final Context dialogContext = new ContextThemeWrapper(this,
				android.R.style.Theme_Light);
		final LayoutInflater dialogInflater = (LayoutInflater) dialogContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Adapter that shows a list of string resources
		final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_list_item_1) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = dialogInflater.inflate(
							android.R.layout.simple_list_item_1, parent, false);
				}

				final int resId = this.getItem(position);
				((TextView) convertView).setText(resId);
				return convertView;
			}
		};

		adapter.add(R.string.import_from_sdcard);
		adapter.add(R.string.export_to_sdcard);

		final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				final int resId = adapter.getItem(which);
				switch (resId) {
				case R.string.import_from_sdcard: {
					doImport();
					break;
				}
				case R.string.export_to_sdcard: {
					doExport();
					break;
				}
				default:
					break;
				}
			}
		};

		new AlertDialog.Builder(this).setTitle(R.string.import_export)
				.setNegativeButton(android.R.string.cancel, null)
				.setSingleChoiceItems(adapter, -1, clickListener).show();

	}

	private void doExport() {
		File file = getBlackListFile();
		int errcode = 0;
		String msg = "";

		try {
			if (file.exists()) {
				if (!file.canWrite()) {
					errcode = 1;
					return;
				}
			} else {
				if (!file.createNewFile()) {
					errcode = 2;
					return;
				}
			}

			if (mCursor != null && mCursor.getCount() > 0) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));

				try {
					int pos = mCursor.getPosition();

					mCursor.moveToFirst();

					int nIdx = mCursor.getColumnIndex(BlackList.COL_NUMBER);
					int nIdx2 = mCursor.getColumnIndex(BlackList.COL_BLOCK);

					do {
						String number = mCursor.getString(nIdx);
						int block = mCursor.getInt(nIdx2);

						bw.write(number + "," + block);
						bw.newLine();
					} while (mCursor.moveToNext());

					mCursor.moveToPosition(pos);
				} finally {
					bw.flush();
					bw.close();
				}
			}
		} catch (Throwable t) {
			errcode = 3;
			msg = t.getLocalizedMessage();

			ActivityLog.logError(this, "Import/Export", t.getLocalizedMessage());

			Log.e("Import/Export", "Fail to export black list", t);
			return;
		} finally {
			int id = R.string.unkown_error_template;

			switch (errcode) {
			case 0:
				id = R.string.success_template;
				break;
			case 1:
				id = R.string.can_not_write_template;
				break;
			case 2:
				id = R.string.can_not_create_template;
				break;
			case 3:
			default:
				break;
			}

			String path = file.getName();

			try {
				path = file.getCanonicalPath();
			} catch (Throwable t) {

			}

			String text = MessageFormat.format(getString(id), new Object[] {
					getString(R.string.export_to_sdcard), path, msg, });

			showToast(text);
		}
	}

	private File getBlackListFile() {
		File sd = Environment.getExternalStorageDirectory();

		return new File(sd, PhoneToolsDBManager.getAuthority() + "_"
				+ getDateString() + ".txt");
	}

	private String getDateString() {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		return df.format(Calendar.getInstance().getTime());
	}

	private void doImport() {
		int errcode = 0;
		String msg = "";
		String name = "";

		try {
			File sd = Environment.getExternalStorageDirectory();

			File[] files = sd.listFiles(new FileFilter() {

				@Override
				public boolean accept(File f) {
					if (!f.isFile())
						return false;
					
					if (!f.canRead())
						return false;
					
					if (!f.getName().startsWith(PhoneToolsDBManager.getAuthority() + "_"))
						return false;
					
					if (!f.getName().endsWith(".txt"))
						return false;
					
					return true;
				}
			});

			if (files == null || files.length == 0) {
				errcode = 1;
				return;
			}

			SortedSet<String> names = new TreeSet<String>();
			for (File f : files) {

				try {
					names.add(f.getCanonicalPath());
				} catch (Throwable t) {

				}
			}

			if (names.size() == 0) {
				errcode = 1;
				return;
			}

			name = names.last();

			BufferedReader br = new BufferedReader(new FileReader(new File(name)));

			String line = null;

			HashMap<String, Integer> numbers = new HashMap<String, Integer>();

			while ((line = br.readLine()) != null) {
				line = line.trim();
				validateAndSave(line, numbers);
			}

			if (numbers.size() == 0) {
				errcode = 1;
				return;
			}

			for (String key : numbers.keySet()) {
				PhoneToolsDBManager.getBlackListManager().blacklistDeleteNumber(this,
						key);
				PhoneToolsDBManager.getBlackListManager().blacklistAddNumber(this, key,
						numbers.get(key) == 1);
			}

		} catch (Throwable t) {
			errcode = 2;
			msg = t.getLocalizedMessage();
			return;
		} finally {
			int id = R.string.unkown_error_template;

			switch (errcode) {
			case 0:
				id = R.string.success_template;
				break;
			case 1:
				id = R.string.no_record_template;
				break;
			case 2:
			default:
				break;
			}

			String text = MessageFormat.format(getString(id), new Object[] {
					getString(R.string.import_from_sdcard), name, msg, });

			showToast(text);
		}
	}

	private void validateAndSave(String line, HashMap<String, Integer> numbers) {
		String[] parts = line.split(",");

		if (parts.length != 2)
			return;

		// Validate number
		parts[0] = parts[0].trim();

		if (parts[0].length() == 0)
			return;

		if (parts[0].charAt(0) != '+'
				&& (parts[0].charAt(0) < '0' || parts[0].charAt(0) > '9')) {
			return;
		}

		if (parts[0].charAt(0) == '+' && parts[0].length() == 1)
			return;

		for (int i = 1; i < parts[0].length(); i++) {
			if (parts[0].charAt(i) < '0' || parts[0].charAt(i) > '9') {
				return;
			}
		}

		// Validate block
		parts[1] = parts[1].trim();

		if (parts[1].length() != 1)
			return;

		if (parts[1].charAt(0) != '0' && parts[1].charAt(0) != '1') {
			return;
		}

		numbers.put(parts[0], parts[1].charAt(0) == '0' ? 0 : 1);
	}

	protected void showToast(final String msg) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mToast != null)
					mToast.cancel();

				mToast = Toast.makeText(BlackListView.this, msg, Toast.LENGTH_SHORT);

				mToast.show();
			}
		});
	}
}
