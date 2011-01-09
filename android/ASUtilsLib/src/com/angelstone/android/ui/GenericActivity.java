package com.angelstone.android.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.SortedSet;
import java.util.TreeSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Environment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.angelstone.android.R;
import com.angelstone.android.utils.ActivityLog;

public abstract class GenericActivity extends Activity {
	private Toast mToast = null;
	private int mOptionMenuStartPos = 0;

	private static final int[][] OPTION_MENUS = new int[][] {
			new int[] { R.string.view_logs,
					android.R.drawable.ic_menu_info_details },
			new int[] { R.string.preference,
					android.R.drawable.ic_menu_preferences },
			new int[] { R.string.about, android.R.drawable.ic_menu_help },
			new int[] { R.string.import_export,
					R.drawable.ic_menu_import_export }, };

	protected static final int IMPORT_SUCCESS = 0;
	protected static final int IMPORT_NO_RECORD = 1;
	protected static final int IMPORT_FAIL = 2;

	private boolean[] mMenuEnables = new boolean[OPTION_MENUS.length];

	private Class<?> mPreferenceClass = null;

	public GenericActivity(int optionMenuStartPos) {
		mOptionMenuStartPos = optionMenuStartPos;
		mMenuEnables[0] = true;
		mMenuEnables[1] = false;
		mMenuEnables[2] = true;
		mMenuEnables[3] = false;
	}

	public GenericActivity() {
		this(0);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mOptionMenuStartPos == 0)
			menu.clear();

		createMenus(menu, mOptionMenuStartPos, OPTION_MENUS, mMenuEnables);

		updateOptionMenuItemState(menu);
		
		return super.onPrepareOptionsMenu(menu);
	}

	protected void updateOptionMenuItemState(Menu menu) {
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		for (int i = 0; i < mMenuEnables.length; i++)
			if (mMenuEnables[i])
				return true;
		return false;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId() - mOptionMenuStartPos) {
		case 0: {
			Intent intent = new Intent();
			intent.setClass(this, ActivityLogActivity.class);
			startActivity(intent);
			break;
		}
		case 1: {
			Intent intent = new Intent();
			intent.setClass(this, mPreferenceClass);
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
			return false;
		}
		return true;
	}

	protected static void createMenus(Menu menu, int beginPos, int[][] items, 
			boolean[] menuEnables) {
		for (int i = 0; i < items.length; i++) {
			if (menuEnables == null || menuEnables[i]) {
				menu.add(0, beginPos + i, beginPos + i, items[i][0]).setIcon(
						items[i][1]);
			}
		}
	}

	protected static void createMenus(Menu menu, int beginPos, int[][] items) {
		createMenus(menu, beginPos, items, null);
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

	protected void showToast(final String msg) {
		showToast(msg, Toast.LENGTH_SHORT);
	}
	
	protected void showToast(final String msg, int length) {
		runOnUiThread(new Runnable() {

			public void run() {
				if (mToast != null)
					mToast.cancel();

				mToast = Toast.makeText(GenericActivity.this, msg,
						Toast.LENGTH_SHORT);

				mToast.show();
			}
		});
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
		File file = getExportFile();
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

			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			try {
				exportTo(bw);
			} finally {
				bw.flush();
				bw.close();
			}
		} catch (Throwable t) {
			errcode = 3;
			msg = t.getLocalizedMessage();

			ActivityLog
					.logError(this, "Import/Export", t.getLocalizedMessage());

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

	protected void exportTo(BufferedWriter writer) throws IOException {
		
	}

	protected File getExportFile() {
		File sd = Environment.getExternalStorageDirectory();

		return new File(sd, getImportExportFilePrefix() + "_" + getDateString()
				+ ".txt");
	}

	protected String getImportExportFilePrefix() {
		return getPackageName();
	}

	protected String getDateString() {
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		return df.format(Calendar.getInstance().getTime());
	}

	private void doImport() {
		int errcode = IMPORT_SUCCESS;
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

					if (!f.getName().startsWith(
							getImportExportFilePrefix() + "_"))
						return false;

					if (!f.getName().endsWith(".txt"))
						return false;

					return true;
				}
			});

			if (files == null || files.length == 0) {
				errcode = IMPORT_NO_RECORD;
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
				errcode = IMPORT_NO_RECORD;
				return;
			}

			name = names.last();

			BufferedReader br = new BufferedReader(new FileReader(
					new File(name)));

			try {
				errcode = importFrom(br);
			} finally {
				br.close();
			}

		} catch (Throwable t) {
			errcode = 2;
			msg = t.getLocalizedMessage();
			return;
		} finally {
			int id = R.string.unkown_error_template;

			switch (errcode) {
			case IMPORT_SUCCESS:
				id = R.string.success_template;
				break;
			case IMPORT_NO_RECORD:
				id = R.string.no_record_template;
				break;
			case IMPORT_FAIL:
			default:
				break;
			}

			String text = MessageFormat.format(getString(id), new Object[] {
					getString(R.string.import_from_sdcard), name, msg, });

			showToast(text);
		}
	}

	protected int importFrom(BufferedReader br) throws IOException {
		return IMPORT_SUCCESS;
	}

	public int getOptionMenuStartPos() {
		return mOptionMenuStartPos;
	}

	public void setOptionMenuStartPos(int optionMenuStartPos) {
		mOptionMenuStartPos = optionMenuStartPos;
	}

	public boolean isMenuEnableLogs() {
		return mMenuEnables[0];
	}

	public void setMenuEnableLogs(boolean menuEnableLogs) {
		mMenuEnables[0] = menuEnableLogs;
	}

	public boolean isMenuEnableAbout() {
		return mMenuEnables[2];
	}

	public void setMenuEnableAbout(boolean menuEnableAbout) {
		mMenuEnables[2] = menuEnableAbout;
	}

	public boolean isMenuEnablePreference() {
		return mMenuEnables[1];
	}

	public void setMenuEnablePreference(boolean menuEnablePreference) {
		mMenuEnables[1] = menuEnablePreference;
	}

	public Class<?> getPreferenceClass() {
		return mPreferenceClass;
	}

	public void setPreferenceClass(Class<?> preferenceClass) {
		mPreferenceClass = preferenceClass;
	}

	public boolean isMenuEnableImportExport() {
		return mMenuEnables[3];
	}

	public void setMenuEnableImportExport(boolean enable) {
		mMenuEnables[3] = enable;
	}
}
