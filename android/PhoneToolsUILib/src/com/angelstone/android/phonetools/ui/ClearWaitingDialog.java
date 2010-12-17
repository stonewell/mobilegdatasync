package com.angelstone.android.phonetools.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ProgressBar;

import com.angelstone.android.phonetools.store.PhoneToolsDBManager;

public class ClearWaitingDialog extends Activity {
	public static ProgressBar mProgressBar = null;
	public int mClearType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.waiting_progress_bar);

		Bundle bundle = this.getIntent().getExtras();

		mClearType = bundle.getInt("clear_type");

		mProgressBar = (ProgressBar) findViewById(R.id.waiting_progress_bar_view);
		// mProgressBar.setMax(BlMgrRootView.numlist.size());

		Thread thread = new Thread() {
			public void run() {
				if (mClearType == UIConstants.CLEAR_BLACK_LIST_NUMBER) {
					PhoneToolsDBManager.getBlackListManager()
							.blacklistDeleteNumber(ClearWaitingDialog.this);

					Message msg = new Message();
					Bundle msgdata = new Bundle();
					msgdata.putInt("clearType",
							UIConstants.CLEAR_BLACK_LIST_NUMBER);
					msg.setData(msgdata);
					handler.sendMessage(msg);
				} else if (mClearType == UIConstants.CLEAR_CALL_LOG) {
					PhoneToolsDBManager.getEventLogManager().deleteLogs(
							ClearWaitingDialog.this);

					Message msg = new Message();
					Bundle msgdata = new Bundle();
					msgdata.putInt("clearType", UIConstants.CLEAR_CALL_LOG);
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
			case UIConstants.CLEAR_BLACK_LIST_NUMBER: {
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
}
