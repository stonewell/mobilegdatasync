package com.angelstone.android.smsblocker.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.ToastShowWaitHandler;

public class AddFromSmsRecordView extends Activity implements
		OnItemClickListener, OnClickListener {
	private Cursor mCursor = null;

	private static ToastShowWaitHandler mToastShowWaitHandler = new ToastShowWaitHandler();

	private ProgressDialog mDialog;

	private ArrayList<String> mTempNumberList = new ArrayList<String>();
	private ArrayList<String> mTempNameList = new ArrayList<String>();
	private int mCheckState[] = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_from_sms_record_view_layout);

			mTempNumberList.addAll(AddBlackListNumberView.mSelectedNumbers);
			mTempNameList.addAll(AddBlackListNumberView.mSelectedNames);

			String SORT_ORDER = "date DESC";
			
			ArrayList<String> where_args = new ArrayList<String>();
			StringBuffer where = new StringBuffer();
			
			buildWhere(where, where_args);
			
			mCursor = getContentResolver().query(Uri.parse("content://sms/inbox"),
					null, where.toString(), where_args.toArray(new String[0]), SORT_ORDER);

			startManagingCursor(mCursor);

			mCheckState = new int[mCursor.getCount()];
			
			for(int i=0;i < mCheckState.length;i++) {
				mCheckState[i] = PhoneNumberHelpers.CHECK_OFF;
			}
			
			CheckBox chkBox = (CheckBox) findViewById(R.id.sms_log_check_box);
			chkBox.setOnClickListener(this);

			final ListView listView = (ListView) findViewById(R.id.sms_record_list);

			listView.setAdapter(new SmsRecordViewAdapter(this, mCursor, mCheckState));

			listView.setOnItemClickListener(this);

			if (listView.getCount() == 0) {
				if (mToastShowWaitHandler.IsAllowShow()) {
					Toast.makeText(AddFromSmsRecordView.this, R.string.NoRecord,
							Toast.LENGTH_SHORT).show();
				}

				chkBox.setEnabled(false);
			}

			Button btn = (Button) findViewById(R.id.sms_log_ok_btn);
			btn.setOnClickListener(this);

			mDialog = new ProgressDialog(this);
			mDialog.setMessage(this.getResources().getText(R.string.PleaseWait));
			mDialog.setIndeterminate(true);
			mDialog.setCancelable(false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Map<String, Object> item = (Map<String, Object>) mList.get(position);
		String number = PhoneNumberHelpers.removeNonNumbericChar((String) item
				.get("number"));

		if (mCheckState[position] == PhoneNumberHelpers.CHECK_OFF) {
			item.put("checkImg", R.drawable.btn_check_on);
			mCheckState[position] = PhoneNumberHelpers.CHECK_ON;

			mCurrentCheckOnItemCount++;
		} else if (mCheckState[position] == PhoneNumberHelpers.CHECK_ON) {
			item.put("checkImg", R.drawable.btn_check_off);
			mCheckState[position] = PhoneNumberHelpers.CHECK_OFF;

			int pos;
			if ((pos = AddBlackListNumberView.indexOfSelectedNumber(number)) != -1) {
				AddBlackListNumberView.mSelectedNumbers.remove(pos);
				AddBlackListNumberView.mSelectedNames.remove(pos);
			}

			mCurrentCheckOnItemCount--;
		}

		ListView listView = (ListView) this.findViewById(R.id.sms_record_list);

		((SimpleAdapter) listView.getAdapter()).notifyDataSetInvalidated();

		CheckBox chkBox = (CheckBox) findViewById(R.id.sms_log_check_box);

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
		case R.id.sms_log_ok_btn: {
			for (int i = 0; i < mList.size(); i++) {
				Map<String, Object> item = mList.get(i);

				if (mCheckState[i] == PhoneNumberHelpers.CHECK_ON) {
					String number = PhoneNumberHelpers
							.removeNonNumbericChar((String) item.get("number"));
					String name = (String) item.get("name");

					if (name == null) {
						name = "";
					} else {
						if (name.length() > 2) {
							int len = name.length();
							name = name.substring(1, len - 1);
						} else {
							name = "";
						}
					}

					if (AddBlackListNumberView.indexOfSelectedNumber(number) == -1) {
						AddBlackListNumberView.mSelectedNumbers.add(number);
						AddBlackListNumberView.mSelectedNames.add(name);
					}

				}
			}

			Intent intent = new Intent();
			setResult(5, intent);

			finish();

			break;
		}
		case R.id.sms_log_check_box: {
			CheckBox chkBox = (CheckBox) findViewById(R.id.sms_log_check_box);

			if (chkBox.isChecked()) {
				Thread thread = new Thread() {
					public void run() {
						Message msg = new Message();
						Bundle msgdata = new Bundle();
						msgdata.putBoolean("IsRunning", true);
						msg.setData(msgdata);
						handler.sendMessage(msg);

						for (int i = 0; i < mCheckState.length; i++) {
							if (mCheckState[i] == -1) {
								continue;
							}

							Map<String, Object> item = (Map<String, Object>) mList.get(i);

							item.put("checkImg", R.drawable.btn_check_on);
							mCheckState[i] = PhoneNumberHelpers.CHECK_ON;
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

						for (int i = 0; i < mCheckState.length; i++) {
							if (mCheckState[i] == -1) {
								continue;
							}

							Map<String, Object> item = (Map<String, Object>) mList.get(i);
							String number = PhoneNumberHelpers
									.removeNonNumbericChar((String) item.get("number"));

							item.put("checkImg", R.drawable.btn_check_off);
							mCheckState[i] = PhoneNumberHelpers.CHECK_OFF;

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
				ListView listView = (ListView) findViewById(R.id.sms_record_list);
				((SimpleAdapter) listView.getAdapter()).notifyDataSetInvalidated();
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
