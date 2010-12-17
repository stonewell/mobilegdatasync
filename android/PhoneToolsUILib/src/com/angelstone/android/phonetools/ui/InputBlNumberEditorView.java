package com.angelstone.android.phonetools.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.angelstone.android.phonetools.store.BlackListManager.BlockListAction;
import com.angelstone.android.phonetools.store.PhoneToolsDBManager;
import com.angelstone.android.utils.PhoneNumberHelpers;
import com.angelstone.android.utils.ToastShowWaitHandler;

public class InputBlNumberEditorView extends Activity implements
		OnClickListener {

	private ToastShowWaitHandler toastShowWaitHandler = new ToastShowWaitHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Be sure to call the super class.
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_LEFT_ICON);

		// See assets/res/any/layout/dialog_activity.xml for this
		// view layout definition, which is being set here as
		// the content of our screen.
		setContentView(R.layout.input_bl_number_dialog_activity);

		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				android.R.drawable.ic_menu_more);

		Button button1 = (Button) findViewById(R.id.input_number_view_ok_btn);
		button1.setOnClickListener(this);
		Button button2 = (Button) findViewById(R.id.input_number_view_cancel_btn);
		button2.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.input_number_view_ok_btn: {
			EditText numEt = (EditText) findViewById(R.id.input_number_edit);
			EditText nameEt = (EditText) findViewById(R.id.input_name_edit);

			String number = PhoneNumberHelpers.removeNonNumbericChar(
					String.valueOf(numEt.getText())).trim();
			String name = String.valueOf(nameEt.getText()).trim();

			if (number == null || number.equals("")) {
				if (toastShowWaitHandler.IsAllowShow()) {

					Toast.makeText(this, R.string.EmptyNumberIsNotAllowed,
							Toast.LENGTH_SHORT).show();
				}

				break;
			}

			if (PhoneToolsDBManager.getBlackListManager().blacklistContainsNumber(this,
					PhoneNumberHelpers.removeNonNumbericChar(number)) != BlockListAction.NO_NUMBER) {
				if (toastShowWaitHandler.IsAllowShow()) {
					Toast.makeText(this, R.string.TheNumberAlreadyExists,
							Toast.LENGTH_SHORT).show();

				}
				break;
			}

			Intent intent = new Intent();
			intent.putExtra("input_return_number", number);
			intent.putExtra("input_return_name", name);
			setResult(6, intent);

			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(numEt.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(nameEt.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);

			finish();
			break;
		}
		case R.id.input_number_view_cancel_btn: {
			EditText numEt = (EditText) findViewById(R.id.input_number_edit);
			EditText nameEt = (EditText) findViewById(R.id.input_name_edit);
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(numEt.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
			((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.hideSoftInputFromWindow(nameEt.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
			finish();
			break;
		}
		default:
			break;
		}

	}

}
