package com.angelstone.android.callerid.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.R;
import com.angelstone.android.callerid.store.CallerIdManager;

public class YaCallerIdMainView extends Activity implements
		OnItemLongClickListener, OnItemClickListener {
	private ListView mListview;
	private Cursor mCursor;
	private Toast mToast = null;

	private Handler mHandler = new Handler();
	private ContentObserver mObserver = null;
	private CallerIdManager mCallerIdManager = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.caller_list_view);

		mCallerIdManager = new CallerIdManager(CallerIdConstants.AUTHORITY);

		mListview = (ListView) this.findViewById(R.id.caller_list);
		registerForContextMenu(mListview);
		mListview.setOnItemLongClickListener(this);
		mListview.setOnItemClickListener(this);

		mCursor = mCallerIdManager.getCallerIds(this);
		startManagingCursor(mCursor);

		CallerListViewAdapter adapter = new CallerListViewAdapter(this, mCursor);
		mListview.setAdapter(adapter);

		mObserver = new ContentObserver(mHandler) {

			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);

				mCursor.requery();
			}
		};

		getContentResolver().registerContentObserver(
				mCallerIdManager.getContentUri(), true, mObserver);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, 0, 0, R.string.clear_all).setIcon(
				getResources().getDrawable(
						android.R.drawable.ic_menu_delete));
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
		return true;
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

//	protected void doImportExport() {
//		// Wrap our context to inflate list items using correct theme
//		final Context dialogContext = new ContextThemeWrapper(this,
//				android.R.style.Theme_Light);
//		final LayoutInflater dialogInflater = (LayoutInflater) dialogContext
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//		// Adapter that shows a list of string resources
//		final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
//				android.R.layout.simple_list_item_1) {
//			@Override
//			public View getView(int position, View convertView, ViewGroup parent) {
//				if (convertView == null) {
//					convertView = dialogInflater.inflate(
//							android.R.layout.simple_list_item_1, parent, false);
//				}
//
//				final int resId = this.getItem(position);
//				((TextView) convertView).setText(resId);
//				return convertView;
//			}
//		};
//
//		adapter.add(R.string.import_from_sdcard);
//		adapter.add(R.string.export_to_sdcard);
//
//		final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//
//				final int resId = adapter.getItem(which);
//				switch (resId) {
//				case R.string.import_from_sdcard: {
//					doImport();
//					break;
//				}
//				case R.string.export_to_sdcard: {
//					doExport();
//					break;
//				}
//				default:
//					break;
//				}
//			}
//		};
//
//		new AlertDialog.Builder(this).setTitle(R.string.import_export)
//				.setNegativeButton(android.R.string.cancel, null)
//				.setSingleChoiceItems(adapter, -1, clickListener).show();
//
//	}
//
//	private void doExport() {
//		File file = getBlackListFile();
//		int errcode = 0;
//		String msg = "";
//
//		try {
//			if (file.exists()) {
//				if (!file.canWrite()) {
//					errcode = 1;
//					return;
//				}
//			} else {
//				if (!file.createNewFile()) {
//					errcode = 2;
//					return;
//				}
//			}
//
//			if (mCursor != null && mCursor.getCount() > 0) {
//				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
//
//				try {
//					int pos = mCursor.getPosition();
//
//					mCursor.moveToFirst();
//
//					int nIdx = mCursor.getColumnIndex(BlackList.COL_NUMBER);
//					int nIdx2 = mCursor.getColumnIndex(BlackList.COL_BLOCK);
//
//					do {
//						String number = mCursor.getString(nIdx);
//						int block = mCursor.getInt(nIdx2);
//
//						bw.write(number + "," + block);
//						bw.newLine();
//					} while (mCursor.moveToNext());
//
//					mCursor.moveToPosition(pos);
//				} finally {
//					bw.flush();
//					bw.close();
//				}
//			}
//		} catch (Throwable t) {
//			errcode = 3;
//			msg = t.getLocalizedMessage();
//
//			ActivityLog.logError(this, "Import/Export", t.getLocalizedMessage());
//
//			Log.e("Import/Export", "Fail to export black list", t);
//			return;
//		} finally {
//			int id = R.string.unkown_error_template;
//
//			switch (errcode) {
//			case 0:
//				id = R.string.success_template;
//				break;
//			case 1:
//				id = R.string.can_not_write_template;
//				break;
//			case 2:
//				id = R.string.can_not_create_template;
//				break;
//			case 3:
//			default:
//				break;
//			}
//
//			String path = file.getName();
//
//			try {
//				path = file.getCanonicalPath();
//			} catch (Throwable t) {
//
//			}
//
//			String text = MessageFormat.format(getString(id), new Object[] {
//					getString(R.string.export_to_sdcard), path, msg, });
//
//			showToast(text);
//		}
//	}
//
//	private File getBlackListFile() {
//		File sd = Environment.getExternalStorageDirectory();
//
//		return new File(sd, PhoneToolsDBManager.getAuthority() + "_"
//				+ getDateString() + ".txt");
//	}
//
//	private String getDateString() {
//		DateFormat df = new SimpleDateFormat("yyyyMMdd");
//		return df.format(Calendar.getInstance().getTime());
//	}
//
//	private void doImport() {
//		int errcode = 0;
//		String msg = "";
//		String name = "";
//
//		try {
//			File sd = Environment.getExternalStorageDirectory();
//
//			File[] files = sd.listFiles(new FileFilter() {
//
//				@Override
//				public boolean accept(File f) {
//					if (!f.isFile())
//						return false;
//					
//					if (!f.canRead())
//						return false;
//					
//					if (!f.getName().startsWith(PhoneToolsDBManager.getAuthority() + "_"))
//						return false;
//					
//					if (!f.getName().endsWith(".txt"))
//						return false;
//					
//					return true;
//				}
//			});
//
//			if (files == null || files.length == 0) {
//				errcode = 1;
//				return;
//			}
//
//			SortedSet<String> names = new TreeSet<String>();
//			for (File f : files) {
//
//				try {
//					names.add(f.getCanonicalPath());
//				} catch (Throwable t) {
//
//				}
//			}
//
//			if (names.size() == 0) {
//				errcode = 1;
//				return;
//			}
//
//			name = names.last();
//
//			BufferedReader br = new BufferedReader(new FileReader(new File(name)));
//
//			String line = null;
//
//			HashMap<String, Integer> numbers = new HashMap<String, Integer>();
//
//			while ((line = br.readLine()) != null) {
//				line = line.trim();
//				validateAndSave(line, numbers);
//			}
//
//			if (numbers.size() == 0) {
//				errcode = 1;
//				return;
//			}
//
//			for (String key : numbers.keySet()) {
//				PhoneToolsDBManager.getBlackListManager().blacklistDeleteNumber(this,
//						key);
//				PhoneToolsDBManager.getBlackListManager().blacklistAddNumber(this, key,
//						numbers.get(key) == 1);
//			}
//
//		} catch (Throwable t) {
//			errcode = 2;
//			msg = t.getLocalizedMessage();
//			return;
//		} finally {
//			int id = R.string.unkown_error_template;
//
//			switch (errcode) {
//			case 0:
//				id = R.string.success_template;
//				break;
//			case 1:
//				id = R.string.no_record_template;
//				break;
//			case 2:
//			default:
//				break;
//			}
//
//			String text = MessageFormat.format(getString(id), new Object[] {
//					getString(R.string.import_from_sdcard), name, msg, });
//
//			showToast(text);
//		}
//	}
//
//	private void validateAndSave(String line, HashMap<String, Integer> numbers) {
//		String[] parts = line.split(",");
//
//		if (parts.length != 2)
//			return;
//
//		// Validate number
//		parts[0] = parts[0].trim();
//
//		if (parts[0].length() == 0)
//			return;
//
//		if (parts[0].charAt(0) != '+'
//				&& (parts[0].charAt(0) < '0' || parts[0].charAt(0) > '9')) {
//			return;
//		}
//
//		if (parts[0].charAt(0) == '+' && parts[0].length() == 1)
//			return;
//
//		for (int i = 1; i < parts[0].length(); i++) {
//			if (parts[0].charAt(i) < '0' || parts[0].charAt(i) > '9') {
//				return;
//			}
//		}
//
//		// Validate block
//		parts[1] = parts[1].trim();
//
//		if (parts[1].length() != 1)
//			return;
//
//		if (parts[1].charAt(0) != '0' && parts[1].charAt(0) != '1') {
//			return;
//		}
//
//		numbers.put(parts[0], parts[1].charAt(0) == '0' ? 0 : 1);
//	}

	protected void showToast(final String msg) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mToast != null)
					mToast.cancel();

				mToast = Toast.makeText(YaCallerIdMainView.this, msg, Toast.LENGTH_SHORT);

				mToast.show();
			}
		});
	}
}