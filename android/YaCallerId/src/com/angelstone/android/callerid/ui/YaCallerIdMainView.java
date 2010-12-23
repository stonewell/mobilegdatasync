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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.R;
import com.angelstone.android.callerid.store.CallerIdManager;
import com.angelstone.android.ui.GenericActivity;

public class YaCallerIdMainView extends GenericActivity implements
		OnItemLongClickListener, OnItemClickListener {
	private static final int[][] OPTION_MENUS = {
			new int[] { R.string.add, android.R.drawable.ic_menu_add },
			new int[] { R.string.clear_all, android.R.drawable.ic_menu_delete }, };
	
	private ListView mListview;
	private Cursor mCursor;

	private Handler mHandler = new Handler();
	private ContentObserver mObserver = null;
	private CallerIdManager mCallerIdManager = null;

	public YaCallerIdMainView() {
		super(OPTION_MENUS.length);

		setMenuEnableImportExport(true);
	}

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
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

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

}