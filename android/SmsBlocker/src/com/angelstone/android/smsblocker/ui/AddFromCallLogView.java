package com.angelstone.android.smsblocker.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.smsblocker.ui.CallLogAdapter.CallLogItem;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.ToastShowWaitHandler;

public class AddFromCallLogView extends Activity implements
		OnItemClickListener, OnClickListener {
	private Cursor mCursor = null;

	private static ToastShowWaitHandler mToastShowWaitHandler = new ToastShowWaitHandler();
	private ArrayList<CallLogItem> mCheckState = new ArrayList<CallLogItem>();

	private ProgressDialog mDialog;

	private int mAllCheckAllowedItemCount = 0;
	private int mCurrentCheckOnItemCount = 0;

	private ArrayList<String> mTempNumberList = new ArrayList<String>();
	private ArrayList<String> mTempNameList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_from_call_record_view_layout);

		PhoneNumberManager pnm = PhoneNumberManager.getIntance(this);

		try {
			mTempNumberList.addAll(AddBlackListNumberView.mSelectedNumbers);
			mTempNameList.addAll(AddBlackListNumberView.mSelectedNames);

			mCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
					null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
			startManagingCursor(mCursor);

			mCursor.moveToFirst();
			ListView listView = (ListView) this.findViewById(R.id.call_record_list);

			CheckBox chkBox = (CheckBox) findViewById(R.id.call_record_check_box);
			chkBox.setOnClickListener(this);

			if (mCursor.getCount() == 0) {
				if (mToastShowWaitHandler.IsAllowShow()) {
					Toast.makeText(AddFromCallLogView.this, R.string.NoRecord,
							Toast.LENGTH_SHORT).show();
				}
				listView.setAdapter(null);
				chkBox.setEnabled(false);
			} else {
				mCursor.moveToFirst();
				int mNumberColIndex = mCursor
						.getColumnIndexOrThrow(CallLog.Calls.NUMBER);

				while (!mCursor.isAfterLast()) {
					String number = PhoneNumberHelpers.removeNonNumbericChar(mCursor
							.getString(mNumberColIndex));

					if (pnm.isInBlacklist(number)) {
					} else if (AddBlackListNumberView.indexOfSelectedNumber(number) >= 0) {
						mCheckState.add(new CallLogItem(mCursor.getPosition(),
								PhoneNumberHelpers.CHECK_ON));

						mCurrentCheckOnItemCount++;
						mAllCheckAllowedItemCount++;
					} else {
						mCheckState.add(new CallLogItem(mCursor.getPosition(),
								PhoneNumberHelpers.CHECK_OFF));

						mAllCheckAllowedItemCount++;

					}

					mCursor.moveToNext();
				}

				chkBox = (CheckBox) findViewById(R.id.call_record_check_box);

				if (mAllCheckAllowedItemCount == 0) {
					chkBox.setEnabled(false);
				} else if (mCurrentCheckOnItemCount == mAllCheckAllowedItemCount) {
					chkBox.setChecked(true);
				}

				listView.setAdapter((new CallLogAdapter(this, mCursor, mCheckState)));
			}

			listView.setOnItemClickListener(this);

			Button btn = (Button) findViewById(R.id.call_record_ok_btn);
			btn.setOnClickListener(this);

			mDialog = new ProgressDialog(this);
			mDialog.setMessage(this.getResources().getText(R.string.PleaseWait));
			mDialog.setIndeterminate(true);
			mDialog.setCancelable(false);

		} catch (Exception e) {
			if (mCursor != null) {
				mCursor.close();
			}
			Log.d("scfw", "AddFromCallRecordView:" + e.getClass().toString());
		} finally {
			pnm.close();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mCursor.close();
		Log.d("scfw", this.toString() + ":close cursor");
	}

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		ListView listView = (ListView) this.findViewById(R.id.call_record_list);
		View view = listView.getChildAt(position
				- listView.getFirstVisiblePosition());
		ImageView iv = (ImageView) view
				.findViewById(R.id.call_record_list_item_check_img);

		TextView tv = (TextView) view.findViewById(R.id.call_record_phonenumber);
		String number = PhoneNumberHelpers.removeNonNumbericChar(String.valueOf(tv
				.getText()));

		CallLogItem item = mCheckState.get(position);

		if (item.CheckState == PhoneNumberHelpers.CHECK_OFF) {
			iv.setImageResource(R.drawable.btn_check_on);
			item.CheckState = PhoneNumberHelpers.CHECK_ON;

			mCurrentCheckOnItemCount++;
		} else if (item.CheckState == PhoneNumberHelpers.CHECK_ON) {
			iv.setImageResource(R.drawable.btn_check_off);
			item.CheckState = PhoneNumberHelpers.CHECK_OFF;

			mCurrentCheckOnItemCount--;

			int pos;

			if ((pos = AddBlackListNumberView.indexOfSelectedNumber(number)) != -1) {
				AddBlackListNumberView.mSelectedNumbers.remove(pos);
				AddBlackListNumberView.mSelectedNames.remove(pos);
			}
		}

		listView.invalidate();

		CheckBox chkBox = (CheckBox) findViewById(R.id.call_record_check_box);

		if (!chkBox.isEnabled()) {
			return;
		}

		if (mCurrentCheckOnItemCount == mAllCheckAllowedItemCount) {
			chkBox.setChecked(true);
		} else if (mCurrentCheckOnItemCount >= 0
				&& mCurrentCheckOnItemCount < mAllCheckAllowedItemCount) {
			chkBox.setChecked(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.call_record_ok_btn: {

			mCursor.moveToFirst();

			for (int i = 0; i < mCheckState.size(); i++) {
				CallLogItem item = mCheckState.get(i);
				mCursor.moveToPosition(item.Position);

				if (item.CheckState == PhoneNumberHelpers.CHECK_ON) {
					String number = mCursor.getString(mCursor
							.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
					number = PhoneNumberHelpers.removeNonNumbericChar(number);
					String name = mCursor.getString(mCursor
							.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME));

					if (name == null) {
						name = "";
					}

					if (AddBlackListNumberView.indexOfSelectedNumber(number) == -1) {
						AddBlackListNumberView.mSelectedNumbers.add(number);
						AddBlackListNumberView.mSelectedNames.add(name);
					}

				}
			}

			Intent intent = new Intent();
			setResult(4, intent);
			finish();

			break;
		}

		case R.id.call_record_check_box: {
			CheckBox chkBox = (CheckBox) findViewById(R.id.call_record_check_box);
			if (chkBox.isChecked()) {
				Thread thread = new Thread() {
					public void run() {
						Message msg = new Message();
						Bundle msgdata = new Bundle();
						msgdata.putBoolean("IsRunning", true);
						msg.setData(msgdata);
						handler.sendMessage(msg);

						for (int i = 0; i < mCheckState.size(); i++) {
							ListView listView = (ListView) findViewById(R.id.call_record_list);

							View view = listView.getAdapter().getView(i, null, null);
							ImageView iv = (ImageView) view
									.findViewById(R.id.call_record_list_item_check_img);
							iv.setImageResource(R.drawable.btn_check_on);
							mCheckState.get(i).CheckState = PhoneNumberHelpers.CHECK_ON;
						}

						mCurrentCheckOnItemCount = mAllCheckAllowedItemCount;

						Message msg_1 = new Message();
						msgdata = new Bundle();
						msgdata.putBoolean("isRunning", false);
						msg_1.setData(msgdata);
						handler.sendMessage(msg_1);
					}
				};

				thread.start();

			} else {
				Thread thread = new Thread() {
					public void run() {
						Message msg = new Message();
						Bundle msgdata = new Bundle();
						msgdata.putBoolean("IsRunning", true);
						msg.setData(msgdata);
						handler.sendMessage(msg);

						for (int i = 0; i < mCheckState.size(); i++) {
							ListView listView = (ListView) findViewById(R.id.call_record_list);

							View view = listView.getAdapter().getView(i, null, null);
							ImageView iv = (ImageView) view
									.findViewById(R.id.call_record_list_item_check_img);
							iv.setImageResource(R.drawable.btn_check_off);
							mCheckState.get(i).CheckState = PhoneNumberHelpers.CHECK_OFF;

							TextView tv = (TextView) view
									.findViewById(R.id.call_record_phonenumber);
							String number = PhoneNumberHelpers.removeNonNumbericChar(String
									.valueOf(tv.getText()));

							int pos;

							if ((pos = AddBlackListNumberView.indexOfSelectedNumber(number)) != -1) {
								AddBlackListNumberView.mSelectedNumbers.remove(pos);
								AddBlackListNumberView.mSelectedNames.remove(pos);
							}

						}

						mCurrentCheckOnItemCount = 0;

						Message msg_1 = new Message();
						msgdata = new Bundle();
						msgdata.putBoolean("isRunning", false);
						msg_1.setData(msgdata);
						handler.sendMessage(msg_1);
					}

				};

				thread.start();

			}

			break;
		}

		default:
			break;
		}

	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (msg.getData().getBoolean("IsRunning")) {
				mDialog.show();
			} else {
				mDialog.dismiss();
				ListView listView = (ListView) findViewById(R.id.call_record_list);
				((ResourceCursorAdapter) listView.getAdapter()).notifyDataSetChanged();
			}
		}

	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.app.Activity#onConfigurationChanged(android.content.res.Configuration
	 * ) This method is due to disable the activity destroyed when the screen
	 * orientation is changed
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		try {
			super.onConfigurationChanged(newConfig);

			if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// do nothing
			} else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				// do nothing
			}
		} catch (Exception ex) {
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			AddBlackListNumberView.mSelectedNumbers.clear();
			AddBlackListNumberView.mSelectedNumbers.addAll(mTempNumberList);
			AddBlackListNumberView.mSelectedNames.clear();
			AddBlackListNumberView.mSelectedNames.addAll(mTempNameList);
		}

		return super.onKeyDown(keyCode, event);
	}

}
