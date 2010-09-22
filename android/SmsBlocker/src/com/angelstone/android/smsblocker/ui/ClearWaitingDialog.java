package com.angelstone.android.smsblocker.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ProgressBar;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;

public class ClearWaitingDialog extends Activity {
	public static ProgressBar mProgressBar = null;
	public int mClearType;
	private String[] mClearDatas;
	private int mLogType;
	private int mBlockType;

	private PhoneNumberManager mPhoneNumberManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.waiting_progress_bar);

		mPhoneNumberManager = PhoneNumberManager.getIntance(this);

		Bundle bundle = this.getIntent().getExtras();

		mClearType = bundle.getInt("clear_type");

		if (mClearType == BlackListManageRootView.CLEAR_BLACK_LIST_NUMBER) {
			mClearDatas = bundle.getStringArray("array");
		} else if (mClearType == RejectedSmsLogView.CLEAR_CALL_LOG) {
			mLogType = bundle.getInt("log_type");
			mBlockType = bundle.getInt("block_type");
		}

		mProgressBar = (ProgressBar) findViewById(R.id.waiting_progress_bar_view);
		// mProgressBar.setMax(BlMgrRootView.numlist.size());

		Thread thread = new Thread() {
			public void run() {
				if (mClearType == BlackListManageRootView.CLEAR_BLACK_LIST_NUMBER) {
					for (int i = 0; i < mClearDatas.length; i++) {
						mPhoneNumberManager.blacklistDeleteNumber(mClearDatas[i]);

					}

					Message msg = new Message();
					Bundle msgdata = new Bundle();
					msgdata.putInt("clearType",
							BlackListManageRootView.CLEAR_BLACK_LIST_NUMBER);
					msg.setData(msgdata);
					handler.sendMessage(msg);
				} else if (mClearType == RejectedSmsLogView.CLEAR_CALL_LOG) {
					mPhoneNumberManager.deleteLogs(mLogType, mBlockType);

					Message msg = new Message();
					Bundle msgdata = new Bundle();
					msgdata.putInt("clearType", RejectedSmsLogView.CLEAR_CALL_LOG);
					msg.setData(msgdata);
					handler.sendMessage(msg);
				}

			}
		};

		thread.start();

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int type = msg.getData().getInt("clearType");

			switch (type) {
			case BlackListManageRootView.CLEAR_BLACK_LIST_NUMBER: {
				Intent intent = new Intent();
				setResult(5, intent);
				finish();
				break;
			}
			default: {
				Intent intent = new Intent();
				setResult(3, intent);
				finish();
				break;
			}
			}
		}

	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	@Override
	protected void onDestroy() {
		mPhoneNumberManager.close();

		super.onDestroy();
	}

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

}
