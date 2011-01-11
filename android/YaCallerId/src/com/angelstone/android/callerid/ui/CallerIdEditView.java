package com.angelstone.android.callerid.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.angelstone.android.callerid.CallerIdConstants;
import com.angelstone.android.callerid.R;
import com.angelstone.android.callerid.store.CallerId;
import com.angelstone.android.callerid.store.CallerIdManager;
import com.angelstone.android.phonetools.ui.AddFromCallLogView;
import com.angelstone.android.phonetools.ui.AddFromContactView;
import com.angelstone.android.phonetools.ui.AddFromSmsRecordView;
import com.angelstone.android.phonetools.ui.UIConstants;
import com.angelstone.android.ui.GenericActivity;
import com.angelstone.android.utils.ActivityLog;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.PhoneToolsUtil;

public class CallerIdEditView extends GenericActivity implements
		OnClickListener, OnLongClickListener {
	private static final int[][] OPTION_MENUS = new int[][] {
			new int[] { R.string.save, android.R.drawable.ic_menu_save },
			new int[] { R.string.delete, android.R.drawable.ic_menu_delete }, };

	private static final int CHOOSE_IMG = 1000;
	private static final int CHOOSE_NUMBER_FROM_LIST = 1001;

	private static final String EXTRA_IMG_DATA = "img_data";
	private static final String EXTRA_NUMBER = "number";

	private long mId = -1;
	private CallerIdManager mCallerIdManager = null;
	private Bitmap mPhoto = null;
	private byte[] mPhotoBuf = null;

	public CallerIdEditView() {
		super(OPTION_MENUS.length);

		setMenuEnableLogs(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.caller_id_view);

		setTitle(R.string.new_caller_id);

		mCallerIdManager = new CallerIdManager(CallerIdConstants.AUTHORITY);

		Intent intent = getIntent();

		ImageView imgView = (ImageView) findViewById(R.id.caller_img);
		imgView.setLongClickable(true);
		imgView.setOnLongClickListener(this);

		Button btnAdd = (Button) findViewById(R.id.chooseNumber);
		btnAdd.setOnClickListener(this);

		if (intent != null)
			mId = intent.getLongExtra(CallerIdConstants.DATA_ID, -1);

		if (savedInstanceState != null) {

			if (savedInstanceState.containsKey(EXTRA_IMG_DATA)) {
				mPhotoBuf = savedInstanceState.getByteArray(EXTRA_IMG_DATA);
				updateImage();
			}

			if (savedInstanceState.containsKey(EXTRA_NUMBER)) {
				EditText et = (EditText) findViewById(R.id.number);
				et.setText(savedInstanceState.getString(EXTRA_NUMBER));
			}
		} else if (mId >= 0) {
			updateView();
		}
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
								public void onClick(DialogInterface dialog, int whichButton) {
									deleteCallerId();
								}
							}).setNegativeButton(android.R.string.cancel, null).create();
			ad.show();

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
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
					intent.setClass(CallerIdEditView.this, AddFromContactView.class);
					break;
				}
				case R.string.add_bl_number_from_call_record_string: {
					intent.setClass(CallerIdEditView.this, AddFromCallLogView.class);
					break;
				}
				case R.string.add_bl_number_from_sms_record_string: {
					intent.setClass(CallerIdEditView.this, AddFromSmsRecordView.class);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;

		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case CHOOSE_NUMBER_FROM_LIST: {
			ArrayList<String> addedNumbers = data
					.getStringArrayListExtra(UIConstants.EXTRA_ADDED_NUMBERS);
			ArrayList<String> removedNumbers = data
					.getStringArrayListExtra(UIConstants.EXTRA_REMOVED_NUMBERS);

			ArrayList<String> selectedNumbers = new ArrayList<String>();

			for (String n : addedNumbers) {
				if (!PhoneNumberHelpers.containsNumber(selectedNumbers, n)) {
					selectedNumbers.add(n);
				}
			}

			for (String n : removedNumbers) {
				if (PhoneNumberHelpers.containsNumber(selectedNumbers, n)) {
					selectedNumbers.remove(n);
				}
			}

			if (selectedNumbers.size() == 0)
				break;

			EditText et = (EditText) findViewById(R.id.number);
			et.setText(selectedNumbers.get(0));

			break;
		}
		case CHOOSE_IMG:
			if (resultCode == RESULT_OK) {
				try {
					mPhotoBuf = loadPhotoBuffer();

					updateImage();
				} catch (IOException ex) {
					ActivityLog.logError(this, "LoadImage", ex.getLocalizedMessage());
					Log.e(getString(R.string.app_name), "LoadImage fail", ex);

					showToast(MessageFormat.format(
							getString(R.string.unale_choose_img_template),
							new Object[] { ex.getLocalizedMessage() }));
				}
			}
			break;
		default:
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private byte[] loadPhotoBuffer() throws IOException {
		FileInputStream fis = new FileInputStream(getCachedFile());

		try {
			byte[] buf = new byte[fis.available()];

			int count = 1000;
			int offset = 0;

			while (offset < buf.length) {
				count = buf.length - offset > 1000 ? 1000 : buf.length - offset;

				int read = fis.read(buf, offset, count);

				if (read < 0)
					break;

				offset += read;
			}

			return buf;
		} finally {
			fis.close();
		}
	}

	private void updateImage() {
		if (mPhoto != null) {
			mPhoto.recycle();
		}

		if (mPhotoBuf == null)
			return;

		mPhoto = BitmapFactory.decodeByteArray(mPhotoBuf, 0, mPhotoBuf.length);
		ImageView iv = (ImageView) findViewById(R.id.caller_img);
		iv.setImageBitmap(mPhoto);
	}

	protected void deleteCallerId() {
		if (mId < 0)
			return;

		mCallerIdManager.deleteCallerId(this, mId);

		finish();
	}

	private void saveCallerId() {
		EditText et = (EditText) findViewById(R.id.number);

		final String number = et.getText().toString();

		if (TextUtils.isEmpty(number)) {
			showToast(getString(R.string.EmptyNumberIsNotAllowed));
			return;
		}

		if (mPhotoBuf == null || mPhotoBuf.length == 0 || mPhoto == null) {
			showToast(getString(R.string.empty_image_is_not_allowed));
			return;
		}

		final long id = mCallerIdManager.findOtherCaller(this, number, mId);

		if (id < 0) {
			if (mId < 0) {
				Uri uri = mCallerIdManager.addCallerId(this, number, mPhotoBuf);

				mId = ContentUris.parseId(uri);
			} else {
				mCallerIdManager.updateCallerId(this, mId, number, mPhotoBuf);
			}
		} else {
			String msg = MessageFormat.format(
					getString(R.string.replace_caller_id_confirm_template),
					new Object[] { number });
			AlertDialog ad = new AlertDialog.Builder(this)
					.setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.note)
					.setMessage(msg)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									mCallerIdManager.updateCallerId(CallerIdEditView.this, id,
											number, mPhotoBuf);
									mId = id;
								}
							}).setNegativeButton(android.R.string.cancel, null).create();
			ad.show();
		}

		showToast(getString(R.string.caller_id_saved));
	}

	private void updateView() {
		EditText et = (EditText) findViewById(R.id.number);
		et.setText("");

		mPhoto = null;
		mPhotoBuf = null;

		if (mId < 0) {
			return;
		}

		Cursor c = mCallerIdManager.getCallerId(this, mId);

		try {
			if (c.getCount() == 0)
				return;

			c.moveToFirst();

			int idxNumber = c.getColumnIndex(CallerId.COL_NUMBER);
			int idxData = c.getColumnIndex(CallerId.COL_DATA);

			et.setText(c.getString(idxNumber));

			mPhotoBuf = c.getBlob(idxData);

			updateImage();
		} finally {
			if (c != null)
				c.close();
		}

		setTitle(R.string.edit_caller_id);
	}

	@Override
	public boolean onLongClick(View v) {
		try {
			switch (v.getId()) {
			case R.id.caller_img: {

				DisplayMetrics dm = getResources().getDisplayMetrics();

				int dstWidth = dm.heightPixels < dm.widthPixels ? dm.heightPixels
						: dm.widthPixels;
				int dstHeight = dm.heightPixels + dm.widthPixels - dstWidth;

				File f = getCachedFile();

				Uri uri = Uri.fromFile(f);

				Intent intent = PhoneToolsUtil.getPhotoPickIntent(uri, dstWidth,
						dstHeight);

				startActivityForResult(intent, CHOOSE_IMG);

				return true;
			}
			default:
				break;
			}
		} catch (IOException ex) {
			ActivityLog.logError(this, "EditCallerId", ex.getLocalizedMessage());
			Log.e(getString(R.string.app_name), "CropImage fail", ex);

			showToast(MessageFormat.format(
					getString(R.string.unale_choose_img_template),
					new Object[] { ex.getLocalizedMessage() }));
		}
		return false;
	}

	private File getCachedFile() throws IOException {
		File f = new File(getCacheDir(), ("tmp.jpg"));

		f.getParentFile().mkdirs();
		f.deleteOnExit();

		if (!f.exists()) {
			f.createNewFile();
		}

		Runtime.getRuntime().exec("chmod 777 " + f.getCanonicalPath());

		return f;
	}

	@Override
	protected void onDestroy() {
		if (mPhoto != null)
			mPhoto.recycle();

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		if (mPhoto != null)
			mPhoto.recycle();

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateImage();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mPhotoBuf != null)
			outState.putByteArray(EXTRA_IMG_DATA, mPhotoBuf);

		EditText et = (EditText) findViewById(R.id.number);

		outState.putString(EXTRA_NUMBER, et.getText().toString());
	}
}
