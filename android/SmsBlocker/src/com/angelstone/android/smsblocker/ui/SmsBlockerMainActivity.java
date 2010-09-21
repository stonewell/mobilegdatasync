package com.angelstone.android.smsblocker.ui;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;

import com.angelstone.android.smsblocker.R;

public class SmsBlockerMainActivity extends TabActivity implements
		OnTabChangeListener {
	private TabHost mTabHost;

	private Drawable mDefaultDrawable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mTabHost = getTabHost();

		try {
			mTabHost.addTab(mTabHost
					.newTabSpec("tab1")
					.setIndicator(
							this.getResources().getText(R.string.Blacklist),
							this.getResources().getDrawable(R.drawable.bl_mgr))
					.setContent(new Intent(this, BlackListManageRootView.class)));

			mTabHost.addTab(mTabHost
					.newTabSpec("tab3")
					.setIndicator(
							this.getResources().getText(R.string.block_sms_log),
							this.getResources().getDrawable(R.drawable.sys_log))
					.setContent(new Intent(this, RejectedSmsLogView.class)));

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

		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.About).setIcon(
				this.getResources().getDrawable(R.drawable.ic_menu_about));
		menu.add(0, 1, 0, R.string.Exit).setIcon(
				this.getResources().getDrawable(R.drawable.ic_menu_exit));

		return false;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0: {
			Intent intent = new Intent();
			intent.setClass(this, SmsBlockerAboutView.class);
			this.startActivity(intent);
			return true;
		}
		case 1: {
			finish();
		}
		default:
			return true;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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