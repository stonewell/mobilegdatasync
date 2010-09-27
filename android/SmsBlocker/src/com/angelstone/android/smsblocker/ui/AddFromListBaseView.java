package com.angelstone.android.smsblocker.ui;

import java.util.HashMap;
import java.util.Map;

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

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.ToastShowWaitHandler;

public abstract class AddFromListBaseView extends Activity implements
		OnItemClickListener, OnClickListener {
	private Cursor mCursor = null;

	private static ToastShowWaitHandler mToastShowWaitHandler = new ToastShowWaitHandler();

	private Map<String, Integer> mCheckState = new HashMap<String, Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_from_list_view_layout);

		for (String selNumber : AddBlackListNumberView.mSelectedNumbers) {
			mCheckState.put(selNumber, UIConstants.CHECK_ON);
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

		if (mCheckState.containsKey(number)) {
			if (mCheckState.get(number) == UIConstants.CHECK_OFF) {
				mCheckState.put(number, UIConstants.CHECK_ON);
			} else if (mCheckState.get(number) == UIConstants.CHECK_ON) {
				mCheckState.put(number, UIConstants.CHECK_OFF);
			}
		} else {
			mCheckState.put(number, UIConstants.CHECK_ON);
		}

		((CursorAdapter) listView.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.content_ok_btn: {
			for (String number : mCheckState.keySet()) {
				if (mCheckState.get(number) == UIConstants.CHECK_ON) {
					if (AddBlackListNumberView.indexOfSelectedNumber(number) == -1) {
						AddBlackListNumberView.mSelectedNumbers.add(number);
						AddBlackListNumberView.mSelectedNames.add("");
					}
				} else if (mCheckState.get(number) == UIConstants.CHECK_OFF) {
					int pos = AddBlackListNumberView
							.indexOfSelectedNumber(number);

					if (pos >= 0) {
						AddBlackListNumberView.mSelectedNumbers.remove(pos);
						AddBlackListNumberView.mSelectedNames.remove(pos);
					}
				}
			}

			Intent intent = new Intent();
			setResult(getResultCode(), intent);

			finish();

			break;
		}
		default:
			break;
		}

	}
	
	protected abstract int getResultCode();
}
