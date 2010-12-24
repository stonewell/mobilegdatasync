package com.angelstone.android.phonetools.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.ToastShowWaitHandler;

public abstract class AddFromListBaseView extends Activity implements
		OnItemClickListener, OnClickListener {
	private Cursor mCursor = null;

	private static ToastShowWaitHandler mToastShowWaitHandler = new ToastShowWaitHandler();

	private Map<String, Integer> mCheckState = new HashMap<String, Integer>();

	private List<String> mSelectedNumbers = new Vector<String>();

	private boolean mSingleSelect = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_from_list_view_layout);

		if (getIntent() != null) {
			String[] selectedNumbers = getIntent().getStringArrayExtra(
					UIConstants.EXTRA_SELECTED_NUMBERS);

			if (selectedNumbers != null && selectedNumbers.length > 0) {
				for (String selNumber : selectedNumbers) {
					mCheckState.put(selNumber, UIConstants.CHECK_ON);
					mSelectedNumbers.add(selNumber);
				}
			}

			mSingleSelect = getIntent().getBooleanExtra(
					UIConstants.EXTRA_SINGLE_SELECT, false);
		}

		mCursor = getListCursor();

		startManagingCursor(mCursor);

		final ListView listView = (ListView) findViewById(R.id.content_list);

		listView.setAdapter(createAdapter(this, mCursor, mCheckState));

		listView.setOnItemClickListener(this);

		if (listView.getCount() == 0) {
			if (mToastShowWaitHandler.IsAllowShow()) {
				Toast.makeText(AddFromListBaseView.this, R.string.NoRecord,
						Toast.LENGTH_SHORT).show();
			}
		}

		Button btn = (Button) findViewById(R.id.content_ok_btn);
		btn.setOnClickListener(this);
	}

	protected abstract ListAdapter createAdapter(Context context, Cursor c,
			Map<String, Integer> checkStates);

	protected abstract Cursor getListCursor();

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		ListView listView = (ListView) findViewById(R.id.content_list);
		ContentListViewAdapter adapter = (ContentListViewAdapter) listView
				.getAdapter();
		String number = PhoneNumberHelpers.removeNonNumbericChar(adapter
				.getNumber(this, position));

		if (mSingleSelect) {
			if (!mCheckState.containsKey(number)) {
				mCheckState.clear();
				mCheckState.put(number, UIConstants.CHECK_ON);
			} else {
				mCheckState.clear();
			}
		} else {
			if (mCheckState.containsKey(number)) {
				if (mCheckState.get(number) == UIConstants.CHECK_OFF) {
					mCheckState.put(number, UIConstants.CHECK_ON);
				} else if (mCheckState.get(number) == UIConstants.CHECK_ON) {
					mCheckState.put(number, UIConstants.CHECK_OFF);
				}
			} else {
				mCheckState.put(number, UIConstants.CHECK_ON);
			}
		}

		((CursorAdapter) listView.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.content_ok_btn: {
			ArrayList<String> addedNumber = new ArrayList<String>();
			ArrayList<String> removedNumber = new ArrayList<String>();

			for (String number : mCheckState.keySet()) {
				if (mCheckState.get(number) == UIConstants.CHECK_ON) {
					if (PhoneNumberHelpers.indexOfSelectedNumber(
							mSelectedNumbers, number) == -1) {
						addedNumber.add(number);
					}
				} else if (mCheckState.get(number) == UIConstants.CHECK_OFF) {
					int pos = PhoneNumberHelpers.indexOfSelectedNumber(
							mSelectedNumbers, number);

					if (pos >= 0) {
						removedNumber.add(number);
					}
				}
			}

			Intent intent = new Intent();
			intent.putStringArrayListExtra(UIConstants.EXTRA_ADDED_NUMBERS,
					addedNumber);
			intent.putStringArrayListExtra(UIConstants.EXTRA_REMOVED_NUMBERS,
					removedNumber);
			setResult(RESULT_OK, intent);

			finish();

			break;
		}
		default:
			break;
		}

	}

	protected abstract int getResultCode();
}
