package com.angelstone.android.phonetools.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.angelstone.android.phonetools.store.BlackListManager.BlockListAction;
import com.angelstone.android.phonetools.store.PhoneToolsDBManager;
import com.angelstone.android.utils.PhoneNumberHelpers;

public class EditBlackListNumberView extends Activity implements
		OnClickListener, DialogInterface.OnClickListener {

	private int mPosition;
	private String mNumber = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_bl_number_view);

		Bundle bundle = this.getIntent().getExtras();
		mPosition = bundle.getInt("POSITION");
		String number = bundle.getString("EDIT_NUMBER");
		String name = bundle.getString("EDIT_NAME");
		boolean block_sms = bundle.getBoolean("block_sms");

		number = PhoneNumberHelpers.removeNonNumbericChar(number);
		mNumber = number;
		EditText numberEt = (EditText) findViewById(R.id.edit_bl_number_editor);
		EditText nameEt = (EditText) findViewById(R.id.edit_bl_name_editor);

		numberEt.setText(number);
		nameEt.setText(name);

		Button btn = (Button) findViewById(R.id.edit_bl_number_view_done_btn);
		btn.setOnClickListener(this);

		CheckBox chk = (CheckBox) findViewById(R.id.block_sms_check_box);
		chk.setChecked(block_sms);

		SharedPreferences p = getPreferences(MODE_PRIVATE);
		Editor ed = p.edit();
		ed.clear();
		ed.commit();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.edit_bl_number_view_done_btn: {
			EditText numberEt = (EditText) findViewById(R.id.edit_bl_number_editor);

			String number = numberEt.getText().toString();

			if (mNumber.equals(number)) {
				finishEdit();
			}

			if (!cancelUpdateNumber(number))
				finishEdit();
			break;

		}
		default:
			break;
		}

	}

	private void finishEdit() {
		EditText numberEt = (EditText) findViewById(R.id.edit_bl_number_editor);
		EditText nameEt = (EditText) findViewById(R.id.edit_bl_name_editor);
		CheckBox chk = (CheckBox) findViewById(R.id.block_sms_check_box);

		String number = numberEt.getText().toString();

		Intent intent = new Intent();
		intent.putExtra("edited_number_return", number);
		intent.putExtra("edited_number", mNumber);
		intent.putExtra("edited_name_return", String.valueOf(nameEt.getText()));
		intent.putExtra("sms_block", chk.isChecked());
		intent.putExtra("position", mPosition);
		setResult(2, intent);

		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(nameEt.getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

		finish();
	}

	private boolean cancelUpdateNumber(String number) {
		number = PhoneNumberHelpers.removeNonNumbericChar(number);
		if (PhoneToolsDBManager.getBlackListManager().blacklistContainsNumber(this,
				PhoneNumberHelpers.removeNonNumbericChar(number)) != BlockListAction.NO_NUMBER) {
			Builder b = new AlertDialog.Builder(this);
			b.setIcon(R.drawable.alert_dialog_icon);
			b.setTitle(R.string.alert_dialog_two_buttons_title_1);
			b.setMessage(R.string.number_in_list_continue_update);
			b.setPositiveButton(R.string.btn_ok, this);
			// b.setNegativeButton(R.string.btn_no, this);
			AlertDialog dlg = b.create();
			dlg.show();
			return true;
		}

		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		EditText numberEt = (EditText) findViewById(R.id.edit_bl_number_editor);
		EditText nameEt = (EditText) findViewById(R.id.edit_bl_name_editor);
		CheckBox chk = (CheckBox) findViewById(R.id.block_sms_check_box);

		SharedPreferences p = getPreferences(MODE_PRIVATE);
		Editor ed = p.edit();
		ed.putString("number", numberEt.getText().toString());
		ed.putString("name", nameEt.getText().toString());
		ed.putString("old_number", mNumber);
		ed.putBoolean("block_sms", chk.isChecked());
		ed.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		EditText numberEt = (EditText) findViewById(R.id.edit_bl_number_editor);
		EditText nameEt = (EditText) findViewById(R.id.edit_bl_name_editor);
		CheckBox chk = (CheckBox) findViewById(R.id.block_sms_check_box);

		SharedPreferences p = getPreferences(MODE_PRIVATE);
		if (p.contains("number") && p.contains("name") && p.contains("block_sms")
				&& p.contains("old_number")) {
			numberEt.setText(p.getString("number", ""));
			nameEt.setText(p.getString("name", ""));
			chk.setChecked(p.getBoolean("block_sms", true));
			mNumber = p.getString("old_number", "");
		}
		Editor ed = p.edit();
		ed.clear();
		ed.commit();
	}

	@Override
	protected void onDestroy() {
		SharedPreferences p = getPreferences(MODE_PRIVATE);
		Editor ed = p.edit();
		ed.clear();
		ed.commit();
		super.onDestroy();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			break;
		default:
			break;
		}
	}
}
