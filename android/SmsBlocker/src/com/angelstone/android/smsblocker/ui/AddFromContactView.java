package com.angelstone.android.smsblocker.ui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
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
import com.angelstone.android.smsblocker.SmsBlocker;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.ToastShowWaitHandler;

public class AddFromContactView extends Activity implements
		OnItemClickListener, OnClickListener {
	private int mNumberColIndex = 0;
	private int mDisplayNameColIndex = 0;
	// private int mNameColIndex = 0;

	private List<Map<String, Object>> mList;

	private static ToastShowWaitHandler mToastShowWaitHandler = new ToastShowWaitHandler();

	private int[] mCheckState;

	private ProgressDialog mDialog;

	private int mAllCheckAllowedItemCount = 0;
	private int mCurrentCheckOnItemCount = 0;

	private ArrayList<String> mTempNumberList = new ArrayList<String>();
	private ArrayList<String> mTempNameList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_from_contact_view_layout);

		mTempNumberList.addAll(AddBlackListNumberView.mSelectedNumbers);
		mTempNameList.addAll(AddBlackListNumberView.mSelectedNames);

		Cursor cur = getContentResolver().query(
				SmsBlocker.getSysCompat(this).PHONE_URI,
				null,
				null,
				null,
				"UPPER(" + SmsBlocker.getSysCompat(this).COLUMN_PHONE_NAME
						+ ") ASC");

		try {
			Log.d("scfw", "test_" + String.valueOf(cur.getCount()));

			CheckBox chkBox = (CheckBox) findViewById(R.id.contact_check_box);
			chkBox.setOnClickListener(this);

			if (cur == null || cur.getCount() <= 0) {
				if (mToastShowWaitHandler.IsAllowShow()) {
					Toast.makeText(AddFromContactView.this, R.string.NoRecord,
							Toast.LENGTH_SHORT).show();
				}

				cur.close();
				chkBox.setEnabled(false);
			}

			mNumberColIndex = cur
					.getColumnIndex(SmsBlocker.getSysCompat(this).COLUMN_PHONE_NUMBER);
			mDisplayNameColIndex = cur.getColumnIndex(SmsBlocker
					.getSysCompat(this).COLUMN_PHONE_NAME);
			// mNameColIndex =
			// cur.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME);
			mList = new ArrayList<Map<String, Object>>();

			cur.moveToFirst();

			mCheckState = new int[cur.getCount()];

			for (int i = 0; i < cur.getCount(); i++) {
				Map<String, Object> map = new HashMap<String, Object>();

				map.put("number", PhoneNumberUtils.formatNumber(cur
						.getString(mNumberColIndex)));

				if (cur.getString(mDisplayNameColIndex) != null) {
					map.put("display_name", cur.getString(mDisplayNameColIndex));
				} else {
					map.put("display_name", "");
				}

				if (isInBlacklist(PhoneNumberHelpers.removeNonNumbericChar(cur
						.getString(mNumberColIndex)))) {
					cur.moveToNext();
					continue;
				} else if (AddBlackListNumberView.indexOfSelectedNumber(PhoneNumberHelpers
						.removeNonNumbericChar(cur.getString(mNumberColIndex))) != -1) {
					map.put("checkImg", R.drawable.btn_check_on);
					mCheckState[cur.getPosition()] = PhoneNumberHelpers.CHECK_ON;

					mCurrentCheckOnItemCount++;
					mAllCheckAllowedItemCount++;
				} else {
					map.put("checkImg", R.drawable.btn_check_off);
					mCheckState[i] = PhoneNumberHelpers.CHECK_OFF;

					mAllCheckAllowedItemCount++;
				}

				mList.add(map);

				cur.moveToNext();
			}
			cur.close();

			final Collator c = Collator.getInstance(Locale.getDefault());
			c.setStrength(Collator.PRIMARY);

			Collections.sort(mList, new Comparator<Map<String, Object>>() {

				@Override
				public int compare(Map<String, Object> object1,
						Map<String, Object> object2) {
					return c.compare(object1.get("display_name"),
							object2.get("display_name"));
				}

			});

			if (mAllCheckAllowedItemCount == 0) {
				chkBox.setEnabled(false);
			} else if (mCurrentCheckOnItemCount == mAllCheckAllowedItemCount) {
				chkBox.setChecked(true);

			}

			final ListView listView = (ListView) this
					.findViewById(R.id.contact_list);

			SimpleAdapter adapter = new SimpleAdapter(this, mList,
					R.layout.contacts_row, new String[] { "display_name",
							"number", "checkImg" }, new int[] { R.id.name,
							R.id.phonenumber, R.id.contact_row_check_img });

			listView.setAdapter(adapter);

			listView.setOnItemClickListener(this);

			Button btn = (Button) findViewById(R.id.contact_ok_btn);
			btn.setOnClickListener(this);

			mDialog = new ProgressDialog(this);
			mDialog.setMessage(this.getResources().getText(R.string.PleaseWait));
			mDialog.setIndeterminate(true);
			mDialog.setCancelable(false);

		} catch (Exception e) {
			if (cur != null) {
				cur.close();
			}
			Log.d("scfw",
					"AddFromContactView:" + e.getClass().toString()
							+ e.getLocalizedMessage());
		}
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

		ListView listView = (ListView) this.findViewById(R.id.contact_list);

		((SimpleAdapter) listView.getAdapter()).notifyDataSetInvalidated();

		CheckBox chkBox = (CheckBox) findViewById(R.id.contact_check_box);

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

	private boolean isInBlacklist(String number) {
		return PhoneNumberManager.getIntance(this).isInBlacklist(number);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.contact_ok_btn: {
			for (int i = 0; i < mList.size(); i++) {
				Map<String, Object> item = mList.get(i);

				if (mCheckState[i] == PhoneNumberHelpers.CHECK_ON) {
					String number = PhoneNumberHelpers
							.removeNonNumbericChar((String) item.get("number"));
					String name = (String) item.get("display_name");

					if (name == null || number.equals(name)) {
						name = "";
					}

					if (AddBlackListNumberView.indexOfSelectedNumber(number) == -1) {
						AddBlackListNumberView.mSelectedNumbers.add(number);
						AddBlackListNumberView.mSelectedNames.add(name);
					}
				}
			}

			Intent intent = new Intent();
			setResult(3, intent);
			finish();

			break;
		}
		case R.id.contact_check_box: {
			CheckBox chkBox = (CheckBox) findViewById(R.id.contact_check_box);

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

							Map<String, Object> item = (Map<String, Object>) mList
									.get(i);

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

							Map<String, Object> item = (Map<String, Object>) mList
									.get(i);
							String number = PhoneNumberHelpers
									.removeNonNumbericChar((String) item
											.get("number"));

							item.put("checkImg", R.drawable.btn_check_off);
							mCheckState[i] = PhoneNumberHelpers.CHECK_OFF;

							int pos;
							if ((pos = AddBlackListNumberView.indexOfSelectedNumber(number)) != -1) {
								AddBlackListNumberView.mSelectedNumbers
										.remove(pos);
								AddBlackListNumberView.mSelectedNames
										.remove(pos);
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
				ListView listView = (ListView) findViewById(R.id.contact_list);
				((SimpleAdapter) listView.getAdapter())
						.notifyDataSetInvalidated();
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
