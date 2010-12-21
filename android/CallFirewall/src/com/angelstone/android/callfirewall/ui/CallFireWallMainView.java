package com.angelstone.android.callfirewall.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;

import com.angelstone.android.callfirewall.CallFireWallService;
import com.angelstone.android.callfirewall.CallFireWallConstants;
import com.angelstone.android.callfirewall.R;
import com.angelstone.android.phonetools.store.PhoneToolsDBManager;
import com.angelstone.android.phonetools.ui.BlackListView;
import com.angelstone.android.utils.ActivityLog;

public class CallFireWallMainView extends TabActivity implements
		OnTabChangeListener {
	static {
		PhoneToolsDBManager.initialize(CallFireWallConstants.AUTHORITY);
	}

	private TabHost mTabHost;

	private Drawable mDefaultDrawable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startService(new Intent(getApplicationContext(),
				CallFireWallService.class));

		mTabHost = getTabHost();

		try {
			mTabHost.addTab(mTabHost
					.newTabSpec("tab1")
					.setIndicator(
							this.getResources().getText(R.string.black_list),
							this.getResources().getDrawable(R.drawable.bl_mgr))
					.setContent(new Intent(this, BlackListView.class)));

			mTabHost.addTab(mTabHost
					.newTabSpec("tab3")
					.setIndicator(
							this.getResources()
									.getText(R.string.block_call_log),
							this.getResources().getDrawable(R.drawable.sys_log))
					.setContent(new Intent(this, RejectedCallLogView.class)));

			LinearLayout ll = (LinearLayout) mTabHost.getChildAt(0);
			TabWidget tw = (TabWidget) ll.getChildAt(0);

			RelativeLayout rl = (RelativeLayout) tw.getChildAt(0);
			mDefaultDrawable = rl.getBackground();

			for (int i = 1; i < tw.getChildCount(); i++) {
				ImageView iv = (ImageView) mTabHost.getTabWidget()
						.getChildAt(i).findViewById(android.R.id.icon);
				iv.setMaxHeight(16);
				iv.setMaxWidth(16);

				RelativeLayout rl_1 = (RelativeLayout) tw.getChildAt(i);
				rl_1.setBackgroundResource(R.drawable.background);

			}
			mTabHost.setOnTabChangedListener(this);

			Intent intent = getIntent();

			if (intent != null
					&& intent.getBooleanExtra(
							CallFireWallConstants.DATA_NOTIFY, false)) {
				mTabHost.setCurrentTab(1);
				
				intent.setClass(getApplicationContext(), CallFireWallService.class);
				
				startService(intent);
			}
		} catch (Exception e) {
			ActivityLog.logError(this, "CallFireWall", e.getLocalizedMessage());
			Log.e("CallFireWall", "CallFirewallMainActivity Create Fail", e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	public void onTabChanged(String arg0) {
		int tabId = mTabHost.getCurrentTab();
		changeTab(tabId);
	}

	private void changeTab(int tabId) {
		LinearLayout ll = (LinearLayout) mTabHost.getChildAt(0);
		TabWidget tw = (TabWidget) ll.getChildAt(0);

		int tabCount = tw.getChildCount();

		for (int i = 0; i < tabCount; i++) {
			RelativeLayout rl = (RelativeLayout) tw.getChildAt(i);

			if (i == tabId) {
				rl.setBackgroundDrawable(mDefaultDrawable);
			} else {
				rl.setBackgroundResource(R.drawable.background);
			}

		}
	}

}
