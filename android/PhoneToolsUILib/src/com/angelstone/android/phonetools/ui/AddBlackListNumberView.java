package com.angelstone.android.phonetools.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.angelstone.android.utils.PhoneNumberHelpers;

public class AddBlackListNumberView extends Activity implements
		OnClickListener, OnCheckedChangeListener {

	private final class RemoveContactsAlertDialogClickListener implements
			DialogInterface.OnClickListener {
		private boolean mRemove = false;

		public RemoveContactsAlertDialogClickListener(boolean removeFromContact) {
			mRemove = removeFromContact;
		}

		public void onClick(DialogInterface dialog, int which) {
			doneAddBlackNumber(mRemove);
		}
	}

	private static final int CALL_ADD_FROM_LIST = 1000;

	private ArrayList<String> mSelectedNumbers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.add_bl_number_view);

			Button button1 = (Button) findViewById(R.id.add_bl_number_from_contact_btn);
			Button button2 = (Button) findViewById(R.id.add_bl_number_from_call_record_btn);
			Button button3 = (Button) findViewById(R.id.add_bl_number_from_sms_record_btn);
			Button button4 = (Button) findViewById(R.id.add_bl_number_from_input_btn);

			button1.setOnClickListener(this);
			button2.setOnClickListener(this);
			button3.setOnClickListener(this);
			button4.setOnClickListener(this);

			Button btn = (Button) findViewById(R.id.add_number_view_done_btn);
			btn.setOnClickListener(this);

			mSelectedNumbers = new ArrayList<String>();

			if (savedInstanceState != null) {
				String[] numbers = savedInstanceState
						.getStringArray("selected_numbers");

				for (int i = 0; i < numbers.length; i++) {
					mSelectedNumbers.add(numbers[i]);
				}
			}

			updateView();

		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		String[] numbers = (String[]) mSelectedNumbers
				.toArray(new String[mSelectedNumbers.size()]);

		outState.putStringArray("selected_numbers", numbers);
	}

	@Override
	protected void onDestroy() {
		mSelectedNumbers.clear();

		super.onDestroy();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_bl_number_from_contact_btn: {
			Intent intent = new Intent();
			intent.setClass(this, AddFromContactView.class);
			intent.putExtra(
					UIConstants.EXTRA_SELECTED_NUMBERS,
					mSelectedNumbers.toArray(new String[mSelectedNumbers.size()]));
			startActivityForResult(intent, CALL_ADD_FROM_LIST);
			break;
		}
		case R.id.add_bl_number_from_call_record_btn: {
			Intent intent = new Intent();
			intent.setClass(this, AddFromCallLogView.class);
			intent.putExtra(
					UIConstants.EXTRA_SELECTED_NUMBERS,
					mSelectedNumbers.toArray(new String[mSelectedNumbers.size()]));
			startActivityForResult(intent, CALL_ADD_FROM_LIST);
			break;
		}
		case R.id.add_bl_number_from_sms_record_btn: {
			Intent intent = new Intent();
			intent.setClass(this, AddFromSmsRecordView.class);
			intent.putExtra(
					UIConstants.EXTRA_SELECTED_NUMBERS,
					mSelectedNumbers.toArray(new String[mSelectedNumbers.size()]));
			startActivityForResult(intent, CALL_ADD_FROM_LIST);
			break;
		}
		case R.id.add_bl_number_from_input_btn: {
			doInputNumber();
			break;
		}
		case R.id.add_number_view_done_btn: {
			if (hasContactNumber()) {
				new AlertDialog.Builder(this)
						.setIcon(R.drawable.alert_dialog_icon)
						.setTitle(R.string.note)
						.setMessage(R.string.remove_from_contacts_confirm)
						.setPositiveButton(
								android.R.string.yes,
								new RemoveContactsAlertDialogClickListener(true))
						.setNegativeButton(
								android.R.string.no,
								new RemoveContactsAlertDialogClickListener(
										false)).create().show();
			} else {
				doneAddBlackNumber(false);
			}

			break;

		}

		default:
			break;
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case CALL_ADD_FROM_LIST: {
			ArrayList<String> addedNumbers = data
					.getStringArrayListExtra(UIConstants.EXTRA_ADDED_NUMBERS);
			ArrayList<String> removedNumbers = data
					.getStringArrayListExtra(UIConstants.EXTRA_REMOVED_NUMBERS);

			for (String n : addedNumbers) {
				if (!PhoneNumberHelpers.containsNumber(mSelectedNumbers, n)) {
					mSelectedNumbers.add(n);
				}
			}

			for (String n : removedNumbers) {
				if (PhoneNumberHelpers.containsNumber(mSelectedNumbers, n)) {
					mSelectedNumbers.remove(n);
				}
			}
			break;
		}
		default:
			break;
		}

		updateView();
	}

	private void updateView() {
		TextView tv = (TextView) findViewById(R.id.how_many_number_selected_view);
		String initText = mSelectedNumbers.size()
				+ " "
				+ this.getResources().getString(
						R.string.how_many_number_selected_string);
		tv.setText(initText);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, R.string.showSelectedNumbers).setIcon(
				this.getResources()
						.getDrawable(android.R.drawable.ic_menu_more));
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0: {
			doModifySelectedNumbers();
			break;
		}
		default:
			break;
		}
		return true;
	}

	private boolean hasContactNumber() {
		for (int i = 0; i < mSelectedNumbers.size(); i++) {
			if (PhoneNumberHelpers.isContact(this, mSelectedNumbers.get(i))) {
				return true;
			}
		}
		return false;
	}

	private void doneAddBlackNumber(boolean removeContacts) {
		String[] numbers = (String[]) mSelectedNumbers
				.toArray(new String[mSelectedNumbers.size()]);

		Intent intent = new Intent();
		intent.putExtra("added_numbers_result", numbers);

		intent.putExtra("sms_block", true);

		intent.putExtra("is_remove_from_contact", removeContacts);

		setResult(RESULT_OK, intent);

		finish();
	}

	private void doModifySelectedNumbers() {
		final String[] numbers = (String[]) mSelectedNumbers
				.toArray(new String[mSelectedNumbers.size()]);

		final boolean[] checked = new boolean[numbers.length];
		for (int i = 0; i < checked.length; i++)
			checked[i] = false;

		new AlertDialog.Builder(this)
				.setTitle(R.string.selectedNumbersTitle)
				.setNegativeButton(android.R.string.cancel, null)
				.setMultiChoiceItems(numbers, checked,
						new DialogInterface.OnMultiChoiceClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton, boolean isChecked) {
								checked[whichButton] = isChecked;
							}
						})
				.setPositiveButton(R.string.delete,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								for (int i = 0; i < checked.length; i++) {
									if (checked[i])
										mSelectedNumbers.remove(numbers[i]);
								}

								updateView();
							}
						}).show();
	}

	private void doInputNumber() {
		// This example shows how to add a custom layout to an AlertDialog
		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.input_number_view,
				null);

		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText et = (EditText) textEntryView
						.findViewById(R.id.edt_number);
				String number = PhoneNumberHelpers.removeNonNumbericChar(et
						.getText().toString());

				if (TextUtils.isEmpty(number))
					return;

				if (!PhoneNumberHelpers
						.containsNumber(mSelectedNumbers, number)) {
					mSelectedNumbers.add(number);

					updateView();
				}
			}

		};
		new AlertDialog.Builder(this)
				.setTitle(R.string.input_number_dlg_title_string)
				.setNegativeButton(android.R.string.cancel, null)
				.setView(textEntryView)
				.setPositiveButton(android.R.string.ok, listener).show();
	}
}
