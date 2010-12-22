package com.angelstone.android.callerid.ui;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.angelstone.android.callerid.R;

public class YaCallerIdMainView extends Activity implements
		OnItemLongClickListener, OnItemClickListener {
	private ListView mListview;
	private Cursor mCursor;
	private Toast mToast = null;

	private Handler mHandler = new Handler();
	private ContentObserver mObserver = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.caller_list_view);

		mListview = (ListView) this.findViewById(R.id.caller_list);
		registerForContextMenu(mListview);
		mListview.setOnItemLongClickListener(this);
		mListview.setOnItemClickListener(this);

		mCursor = null;
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
				null,
				true, mObserver);
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
}