package com.angelstone.android.callerid.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.R;
import com.angelstone.android.callerid.service.CallerIdService;
import com.angelstone.android.callerid.store.CallerId;
import com.angelstone.android.callerid.store.CallerIdManager;
import com.angelstone.android.callerid.utils.PhotoLoader;
import com.angelstone.android.ui.GenericActivity;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class YaCallerIdMainView extends GenericActivity implements
		OnScrollListener {
	private static final int[][] OPTION_MENUS = {
			new int[] { R.string.add, android.R.drawable.ic_menu_add },
			new int[] { R.string.clear_all, android.R.drawable.ic_menu_delete }, };

	private ListView mListview;
	private Cursor mCursor;

	private Handler mHandler = new Handler();
	private ContentObserver mObserver = null;
	private CallerIdManager mCallerIdManager = null;

	private PhotoLoader mPhotoLoader = null;

	public YaCallerIdMainView() {
		super(OPTION_MENUS.length);

		setMenuEnableImportExport(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.caller_list_view);

		startService(new Intent(getApplicationContext(), CallerIdService.class));

		mCallerIdManager = new CallerIdManager(CallerIdConstants.AUTHORITY);
		mPhotoLoader = new PhotoLoader(this, R.drawable.ic_contact_list_picture);

		mListview = (ListView) this.findViewById(R.id.caller_list);
		registerForContextMenu(mListview);
		mListview.setOnScrollListener(this);

		mCursor = mCallerIdManager.getCallerIds(this);
		startManagingCursor(mCursor);

		CallerListViewAdapter adapter = new CallerListViewAdapter(this,
				mCursor, mPhotoLoader);
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		createMenus(menu, 0, OPTION_MENUS);
		super.onPrepareOptionsMenu(menu);

		if (mCursor == null || mCursor.getCount() == 0) {
			menu.getItem(1).setEnabled(false);
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0: {
			startActivity(new Intent(this, CallerIdEditView.class));
			break;
		}
		case 1: {

			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.note)
					.setMessage(R.string.clear_all_confirm)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mCallerIdManager
											.clearCallerIds(YaCallerIdMainView.this);
								}
							}).setNegativeButton(android.R.string.cancel, null)
					.create();
			ad.show();

			break;
		}
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void exportTo(BufferedWriter writer) throws IOException {
		super.exportTo(writer);

		Cursor c = mCallerIdManager.getCallerIds(this);
		try {
			int idxNumber = c.getColumnIndex(CallerId.COL_NUMBER);
			int idxData = c.getColumnIndex(CallerId.COL_DATA);

			while (c.moveToNext()) {
				byte[] data = c.getBlob(idxData);
				String number = c.getString(idxNumber);

				writer.write(number);
				writer.write(",");
				writer.write(toHexString(data));
				writer.newLine();
			}
		} finally {
			c.close();
		}
	}

	@Override
	protected String getImportExportFilePrefix() {
		return CallerIdConstants.AUTHORITY;
	}

	@Override
	protected int importFrom(BufferedReader br) throws IOException {
		int ret = super.importFrom(br);

		if (ret != IMPORT_SUCCESS)
			return ret;

		String line = null;

		HashMap<String, byte[]> values = new HashMap<String, byte[]>();

		while ((line = br.readLine()) != null) {
			String[] parts = line.split(",");

			if (parts == null || parts.length != 2) {
				continue;
			}

			if (!PhoneNumberHelpers.isValidNumber(parts[0]))
				continue;

			byte[] buf = fromHexString(parts[1]);

			if (buf == null || buf.length == 0)
				continue;

			if (!isValidBitmap(buf))
				continue;

			values.put(parts[0].trim(), buf);
		}

		if (values.size() == 0)
			return IMPORT_NO_RECORD;

		for (String number : values.keySet()) {
			if (null == mCallerIdManager.addCallerId(this, number,
					values.get(number))) {
				mCallerIdManager.updateCallerId(this, number,
						values.get(number));
			}
		}

		return IMPORT_SUCCESS;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mPhotoLoader.stop();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mPhotoLoader.resume();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
			mPhotoLoader.pause();
		} else {
			mPhotoLoader.resume();
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenuInfo menuInfo = item.getMenuInfo();
		long childId = 0;

		if (menuInfo instanceof AdapterContextMenuInfo) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			childId = info.id;
		} else {
			return false;
		}

		final long id = childId;
		switch (item.getItemId()) {
		case 1: {
			Intent intent = new Intent(this, CallerIdEditView.class);
			intent.putExtra(CallerIdConstants.DATA_ID, id);
			startActivity(intent);
			break;
		}
		case 2: {
			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.delete_confirm)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									mCallerIdManager.deleteCallerId(
											YaCallerIdMainView.this, id);
								}
							}).setNegativeButton(android.R.string.cancel, null)
					.create();
			ad.show();
			break;

		}
		default:
			return super.onContextItemSelected(item);
		}

		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		menu.add(0, 1, 1, R.string.edit);
		menu.add(0, 2, 2, R.string.delete);

		super.onCreateContextMenu(menu, v, menuInfo);

	}

	private String toHexString(byte[] data) {
		StringBuilder sb = new StringBuilder(data.length * 2);

		for (int i = 0; i < data.length; i++) {
			String tmp = Integer.toHexString(data[i]);

			if (tmp.length() < 2)
				sb.append("0");
			sb.append(tmp);
		}

		return sb.toString();
	}

	private boolean isValidBitmap(byte[] buf) {
		Bitmap bmp = null;

		try {
			bmp = BitmapFactory.decodeByteArray(buf, 0, buf.length);

			return true;
		} catch (Throwable t) {
			return false;
		} finally {
			try {
				if (bmp != null && !bmp.isRecycled()) {
					bmp.recycle();
				}
			} catch (Throwable t) {

			}
		}
	}

	private byte[] fromHexString(String string) {
		byte[] buf = new byte[string.length() / 2];

		if (buf.length == 0)
			return null;

		for (int i = 0; i < buf.length; i++) {
			buf[i] = (byte)Integer.parseInt(string.substring(i * 2, i * 2 + 2), 16);
		}
		return buf;
	}

}