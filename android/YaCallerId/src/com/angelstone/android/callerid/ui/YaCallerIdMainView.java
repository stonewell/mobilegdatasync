package com.angelstone.android.callerid.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.R;
import com.angelstone.android.callerid.store.CallerIdManager;
import com.angelstone.android.callerid.utils.PhotoLoader;
import com.angelstone.android.ui.GenericActivity;

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

		mCallerIdManager = new CallerIdManager(CallerIdConstants.AUTHORITY);
		mPhotoLoader = new PhotoLoader(this, R.drawable.ic_contact_list_picture);

		mListview = (ListView) this.findViewById(R.id.caller_list);
		registerForContextMenu(mListview);
		mListview.setOnScrollListener(this);

		mCursor = mCallerIdManager.getCallerIds(this);
		startManagingCursor(mCursor);

		CallerListViewAdapter adapter = new CallerListViewAdapter(this, mCursor,
				mPhotoLoader);
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
								public void onClick(DialogInterface dialog, int whichButton) {
									mCallerIdManager.clearCallerIds(YaCallerIdMainView.this);
								}
							}).setNegativeButton(android.R.string.cancel, null).create();
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
		// TODO Auto-generated method stub
		super.exportTo(writer);
	}

	@Override
	protected String getImportExportFilePrefix() {
		return CallerIdConstants.AUTHORITY;
	}

	@Override
	protected int importFrom(BufferedReader br) throws IOException {
		// TODO Auto-generated method stub
		return super.importFrom(br);
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
								public void onClick(DialogInterface dialog, int whichButton) {
									mCallerIdManager.deleteCallerId(YaCallerIdMainView.this, id);			
								}
							}).setNegativeButton(android.R.string.cancel, null).create();
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

}