package com.angelstone.android.callerid.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.R;
import com.angelstone.android.ui.GenericActivity;
import com.angelstone.android.utils.PhoneToolsUtil;

public class CallerIdEditView extends GenericActivity implements OnClickListener {
	private static final int[][] OPTION_MENUS = new int[][] {
			new int[] { R.string.save, android.R.drawable.ic_menu_save },
			new int[] { R.string.delete, android.R.drawable.ic_menu_delete }, };

	private static final int CHOOSE_IMG = 1000;
	
	private int mId = -1;

	public CallerIdEditView() {
		super(OPTION_MENUS.length);

		setMenuEnableLogs(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.caller_id_view);

		setTitle(R.string.new_caller_id);

		Intent intent = getIntent();

		if (intent != null) {
			mId = intent.getIntExtra(CallerIdConstants.DATA_ID, -1);
		}

		ImageView imgView = (ImageView)findViewById(R.id.caller_img);
		imgView.setOnClickListener(this);
		updateView();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		createMenus(menu, 0, OPTION_MENUS);

		if (mId < 0)
			menu.getItem(1).setEnabled(false);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			saveCallerId();
			break;
		case 1:
			AlertDialog ad = new AlertDialog.Builder(this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.delete_confirm)
			.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {
					deleteCallerId();
				}
			})
			.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,
						int whichButton) {
					/* User clicked Cancel so do some stuff */
				}
			}).create();
			ad.show();

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void deleteCallerId() {
		// TODO Auto-generated method stub
		
	}

	private void saveCallerId() {
		// TODO Auto-generated method stub
		
	}

	private void updateView() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View view) {
		if (view.getId() != R.id.caller_img)
			return;
		
		DisplayMetrics dm = getResources().getDisplayMetrics();

		int dstWidth = dm.heightPixels < dm.widthPixels ? dm.heightPixels : dm.widthPixels;
		int dstHeight = dm.heightPixels + dm.widthPixels - dstWidth;
		
        Intent intent = PhoneToolsUtil.getPhotoPickIntent(dstWidth, dstHeight);
		
		startActivityForResult(intent, CHOOSE_IMG);
	}

}
