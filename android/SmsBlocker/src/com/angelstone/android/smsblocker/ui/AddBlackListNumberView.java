package com.angelstone.android.smsblocker.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.ToastShowWaitHandler;

public class AddBlackListNumberView extends Activity implements OnClickListener,
		OnCheckedChangeListener {

	private ToastShowWaitHandler toastShowWaitHandler = new ToastShowWaitHandler();

	public static ArrayList<String> mSelectedNumbers;
	public static ArrayList<String> mSelectedNames;

	private boolean mIsRemoveFromContact = false;

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

			CheckBox bs = (CheckBox) findViewById(R.id.sms_block_check_box);
			bs.setChecked(true);

			Button btn = (Button) findViewById(R.id.add_number_view_done_btn);
			btn.setOnClickListener(this);

			// btn = (Button)findViewById(R.id.add_number_view_cancel_btn);
			// btn.setOnClickListener(this);

			mSelectedNumbers = new ArrayList<String>();
			mSelectedNames = new ArrayList<String>();

			if (savedInstanceState != null) {
				String[] numbers = savedInstanceState
						.getStringArray("selected_numbers");
				String[] names = savedInstanceState
						.getStringArray("selected_names");

				for (int i = 0; i < numbers.length; i++) {
					mSelectedNumbers.add(numbers[i]);
					mSelectedNames.add(names[i]);
				}
			}

			TextView tv = (TextView) findViewById(R.id.how_many_number_selected_view);
			String initText = mSelectedNumbers.size()
					+ " "
					+ this.getResources().getString(
							R.string.how_many_number_selected_string);
			tv.setText(initText);

			/*
			 * RadioGroup radioGroup = (RadioGroup)
			 * findViewById(R.id.add_bl_number_reply_option_radioGroup);
			 * radioGroup.setOnCheckedChangeListener(this);
			 * 
			 * 
			 * EditText smsEt =
			 * (EditText)findViewById(R.id.add_bl_number_reply_sms_editor);
			 * smsEt.setEnabled(false); smsEt.setInputType(InputType.TYPE_NULL);
			 */
		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		String[] numbers = (String[]) mSelectedNumbers
				.toArray(new String[mSelectedNumbers.size()]);
		String[] names = (String[]) mSelectedNames
				.toArray(new String[mSelectedNames.size()]);

		outState.putStringArray("selected_numbers", numbers);
		outState.putStringArray("selected_names", names);
	}

	@Override
	protected void onDestroy() {
		mSelectedNumbers.clear();

		mSelectedNumbers.clear();

		super.onDestroy();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_bl_number_from_contact_btn: {
			Intent intent = new Intent();
			intent.setClass(this, AddFromContactView.class);
			startActivityForResult(intent, 2);
			break;
		}
		case R.id.add_bl_number_from_call_record_btn: {
			Intent intent = new Intent();
			intent.setClass(this, AddFromCallRecordView.class);
			startActivityForResult(intent, 3);
			break;
		}
		case R.id.add_bl_number_from_sms_record_btn: {
			Intent intent = new Intent();
			intent.setClass(this, AddFromSmsRecordView.class);
			startActivityForResult(intent, 4);
			break;
		}
		case R.id.add_bl_number_from_input_btn: {
			Intent intent = new Intent();
			intent.setClass(this, InputBlNumberEditorView.class);
			startActivityForResult(intent, 5);

			break;
		}
		case R.id.add_number_view_done_btn: {
			CheckBox bs = (CheckBox) findViewById(R.id.sms_block_check_box);

			if (mSelectedNumbers.size() == 0) {
				if (toastShowWaitHandler.IsAllowShow()) {
					Toast.makeText(this, R.string.PleaseSelectOneNumberAtLeast,
							Toast.LENGTH_SHORT).show();
				}
				break;
			}

			if (hasContactNumber()) {
				new AlertDialog.Builder(this)
						.setIcon(R.drawable.alert_dialog_icon)
						.setTitle(R.string.alert_dialog_two_buttons_title_2)
						.setMessage(R.string.alert_dialog_two_buttons_msg)
						.setPositiveButton(R.string.btn_yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										CheckBox bs = (CheckBox) findViewById(R.id.sms_block_check_box);

										mIsRemoveFromContact = true;

										String[] numbers = (String[]) mSelectedNumbers
												.toArray(new String[mSelectedNumbers
														.size()]);
										String[] names = (String[]) mSelectedNames
												.toArray(new String[mSelectedNames
														.size()]);

										Intent intent = new Intent();
										intent.putExtra("added_numbers_result",
												numbers);
										intent.putExtra("added_names_result",
												names);
										// intent.putExtra("added_reply_sms_result",
										// replySms);

										if (bs.isChecked()) {
											intent.putExtra("sms_block", true);
										} else {
											intent.putExtra("sms_block", false);
										}

										intent.putExtra(
												"is_remove_from_contact",
												mIsRemoveFromContact);

										setResult(1, intent);

										// ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(smsEt.getWindowToken(),
										// InputMethodManager.HIDE_NOT_ALWAYS);

										finish();
									}
								})
						.setNegativeButton(R.string.btn_no,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										CheckBox bs = (CheckBox) findViewById(R.id.sms_block_check_box);
										mIsRemoveFromContact = false;

										String[] numbers = (String[]) mSelectedNumbers
												.toArray(new String[mSelectedNumbers
														.size()]);
										String[] names = (String[]) mSelectedNames
												.toArray(new String[mSelectedNames
														.size()]);

										Intent intent = new Intent();
										intent.putExtra("added_numbers_result",
												numbers);
										intent.putExtra("added_names_result",
												names);

										if (bs.isChecked()) {
											intent.putExtra("sms_block", true);
										} else {
											intent.putExtra("sms_block", false);
										}

										intent.putExtra(
												"is_remove_from_contact",
												mIsRemoveFromContact);

										setResult(1, intent);

										// ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(smsEt.getWindowToken(),
										// InputMethodManager.HIDE_NOT_ALWAYS);

										finish();
									}
								}).create().show();
			} else {

				String[] numbers = (String[]) mSelectedNumbers
						.toArray(new String[mSelectedNumbers.size()]);
				String[] names = (String[]) mSelectedNames
						.toArray(new String[mSelectedNames.size()]);

				Intent intent = new Intent();
				intent.putExtra("added_numbers_result", numbers);
				intent.putExtra("added_names_result", names);
				// intent.putExtra("added_reply_sms_result", replySms);
				intent.putExtra("is_remove_from_contact", false);

				if (bs.isChecked()) {
					intent.putExtra("sms_block", true);
				} else {
					intent.putExtra("sms_block", false);
				}

				intent.putExtra("is_remove_from_contact", false);

				setResult(1, intent);

				finish();
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
		try {
			switch (resultCode) {
			case 2: {
				String[] deleteNumbers = data.getExtras().getStringArray(
						"delete_numbers");
				String[] deleteNames = data.getExtras().getStringArray(
						"delete_names");

				for (int i = 0; i < deleteNumbers.length; i++) {
					mSelectedNumbers.remove(deleteNumbers[i]);
					mSelectedNames.remove(deleteNames[i]);
				}

				break;
			}

			case 3: {
				break;
			}
			case 4: {
				break;
			}
			case 5: {
				break;
			}
			case 6: {
				String number = PhoneNumberHelpers.removeNonNumbericChar(data
						.getExtras().getString("input_return_number"));
				String name = data.getExtras().getString("input_return_name");

				if (!isContainsNumber(number)) {
					mSelectedNumbers.add(number);
					mSelectedNames.add(name);
				}

			}
			case 0: {
				break;
			}
			default:
				break;
			}

			TextView tv = (TextView) findViewById(R.id.how_many_number_selected_view);
			String initText = mSelectedNumbers.size()
					+ " "
					+ this.getResources().getString(
							R.string.how_many_number_selected_string);
			tv.setText(initText);

		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}

	private boolean isContainsNumber(String number) {
		for (int i = 0; i < mSelectedNumbers.size(); i++) {
			if (PhoneNumberManager.getIntance(this).cmpNumber(
					mSelectedNumbers.get(i), number)) {
				return true;
			}
		}

		return false;
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
			Intent intent = new Intent();
			intent.setClass(this, SelectedNumberListView.class);

			intent.putExtra("number_count", mSelectedNumbers.size());

			String[] numbers = (String[]) mSelectedNumbers
					.toArray(new String[mSelectedNumbers.size()]);
			String[] names = (String[]) mSelectedNames
					.toArray(new String[mSelectedNames.size()]);

			intent.putExtra("selected_numbers", numbers);
			intent.putExtra("selected_names", names);

			startActivityForResult(intent, 6);
			break;
		}
		default:
			break;
		}
		return true;
	}

	private boolean hasContactNumber() {
		for (int i = 0; i < mSelectedNumbers.size(); i++) {
			if (PhoneNumberManager.getIntance(this).isContact(
					mSelectedNumbers.get(i))) {
				return true;
			}
		}
		return false;
	}

}
