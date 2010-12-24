package com.angelstone.android.callerid.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.R;
import com.angelstone.android.phonetools.ui.AddFromCallLogView;
import com.angelstone.android.phonetools.ui.AddFromContactView;
import com.angelstone.android.phonetools.ui.AddFromSmsRecordView;
import com.angelstone.android.phonetools.ui.UIConstants;
import com.angelstone.android.ui.GenericActivity;
import com.angelstone.android.utils.PhoneToolsUtil;

public class CallerIdEditView extends GenericActivity implements
		OnClickListener {
	private static final int[][] OPTION_MENUS = new int[][] {
			new int[] { R.string.save, android.R.drawable.ic_menu_save },
			new int[] { R.string.delete, android.R.drawable.ic_menu_delete }, };

	private static final int CHOOSE_IMG = 1000;
	private static final int CHOOSE_NUMBER_FROM_LIST = 1001;

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

		ImageView imgView = (ImageView) findViewById(R.id.caller_img);
		imgView.setOnClickListener(this);

		Button btnAdd = (Button) findViewById(R.id.chooseNumber);
		btnAdd.setOnClickListener(this);
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

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.caller_img: {

			DisplayMetrics dm = getResources().getDisplayMetrics();

			int dstWidth = dm.heightPixels < dm.widthPixels ? dm.heightPixels
					: dm.widthPixels;
			int dstHeight = dm.heightPixels + dm.widthPixels - dstWidth;

			Intent intent = PhoneToolsUtil.getPhotoPickIntent(dstWidth,
					dstHeight);

			startActivityForResult(intent, CHOOSE_IMG);
			break;
		}
		case R.id.chooseNumber: {
			chooseNumber();
			break;
		}
		default:
			break;
		}
	}

	private void chooseNumber() {
		// Wrap our context to inflate list items using correct theme
		final Context dialogContext = new ContextThemeWrapper(this,
				android.R.style.Theme_Light);
		final LayoutInflater dialogInflater = (LayoutInflater) dialogContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Adapter that shows a list of string resources
		final ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_list_item_1) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
					convertView = dialogInflater.inflate(
							android.R.layout.simple_list_item_1, parent, false);
				}

				final int resId = this.getItem(position);
				((TextView) convertView).setText(resId);
				return convertView;
			}
		};

		adapter.add(R.string.add_bl_number_from_contact_string);
		adapter.add(R.string.add_bl_number_from_call_record_string);
		adapter.add(R.string.add_bl_number_from_sms_record_string);

		final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				final int resId = adapter.getItem(which);
				Intent intent = new Intent();
				switch (resId) {
				case R.string.add_bl_number_from_contact_string: {
					intent.setClass(CallerIdEditView.this,
							AddFromContactView.class);
					break;
				}
				case R.string.add_bl_number_from_call_record_string: {
					intent.setClass(CallerIdEditView.this,
							AddFromCallLogView.class);
					break;
				}
				case R.string.add_bl_number_from_sms_record_string: {
					intent.setClass(CallerIdEditView.this,
							AddFromSmsRecordView.class);
					break;
				}
				default:
					break;
				}
				
				intent.putExtra(UIConstants.EXTRA_SINGLE_SELECT, true);
				startActivityForResult(intent, CHOOSE_NUMBER_FROM_LIST);
			}
		};

		new AlertDialog.Builder(this).setTitle(R.string.choose_number_from)
				.setNegativeButton(android.R.string.cancel, null)
				.setSingleChoiceItems(adapter, -1, clickListener).show();

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

}
