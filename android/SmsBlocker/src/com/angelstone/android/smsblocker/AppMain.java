package com.angelstone.android.smsblocker;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
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

public class AppMain extends TabActivity implements OnTabChangeListener {
	// public static PhoneNumberManager db = null;
	// public static CallerLocation cl = null;

	public static AudioManager audioManager = null;

	public static boolean supportCallForward = true;
	/** Called when the activity is first created. */

	private TabHost mTabHost;

	private Drawable mDefaultDrawable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.main);

		// new PhoneNumberManager( (Context)this ); //use a Activity derived
		// class instance as parameter
		// cl = new CallerLocation( (Context)this );

		// db = PhoneNumberManager.getIntance(this);
		// db = new PhoneNumberManager(this);

		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		mTabHost = getTabHost();

		try {
			mTabHost.addTab(mTabHost
					.newTabSpec("tab1")
					.setIndicator(
							this.getResources().getText(R.string.Blacklist),
							this.getResources().getDrawable(R.drawable.bl_mgr))
					.setContent(new Intent(this, BlMgrRootView.class)));

			/*
			 * tabHost.addTab(tabHost.newTabSpec("tab2")
			 * .setIndicator(this.getResources().getText(R.string.Auto_reply),
			 * this.getResources().getDrawable(R.drawable.call_management))
			 * .setContent(new Intent(this, AdvanceMgrRootView.class)));
			 */

			// mTabHost.addTab(mTabHost.newTabSpec("tab2")
			// .setIndicator(this.getResources().getText(R.string.block_calls_log),
			// this.getResources().getDrawable(R.drawable.sys_log))
			// .setContent(new Intent(this, CallRejectLogView.class)));

			mTabHost.addTab(mTabHost
					.newTabSpec("tab3")
					.setIndicator(
							this.getResources().getText(R.string.block_sms_log),
							this.getResources().getDrawable(R.drawable.sys_log))
					.setContent(new Intent(this, SmsRejectLogView.class)));

			LinearLayout ll = (LinearLayout) mTabHost.getChildAt(0);
			TabWidget tw = (TabWidget) ll.getChildAt(0);

			RelativeLayout rl = (RelativeLayout) tw.getChildAt(0);
			mDefaultDrawable = rl.getBackground();

			for (int i = 1; i < tw.getChildCount(); i++) {
				// TextView ev =
				// (TextView)mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
				// ev.setTextSize(15);

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
			intent.setClass(this, ProductAboutView.class);
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
		// TODO Auto-generated method stub
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