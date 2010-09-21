package com.angelstone.android.smsblocker.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.utils.ToastShowWaitHandler;

public class EditBlackListNumberView extends Activity implements OnClickListener,
		OnCheckedChangeListener {
	private int mPosition;

	private ToastShowWaitHandler toastShowWaitHandler = new ToastShowWaitHandler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.edit_bl_number_view);

			Bundle bundle = this.getIntent().getExtras();
			mPosition = bundle.getInt("POSITION");
			String number = bundle.getString("EDIT_NUMBER");
			String name = bundle.getString("EDIT_NAME");

			EditText numberEt = (EditText) findViewById(R.id.edit_bl_number_editor);
			EditText nameEt = (EditText) findViewById(R.id.edit_bl_name_editor);

			numberEt.setText(number);
			nameEt.setText(name);

			CheckBox bs = (CheckBox) findViewById(R.id.edit_bl_number_sms_block_check_box);
			bs.setChecked(bundle.getBoolean("block_sms"));

			Button btn = (Button) findViewById(R.id.edit_bl_number_view_done_btn);
			btn.setOnClickListener(this);

		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.edit_bl_number_view_done_btn: {
			EditText numberEt = (EditText) findViewById(R.id.edit_bl_number_editor);
			EditText nameEt = (EditText) findViewById(R.id.edit_bl_name_editor);
			CheckBox bs = (CheckBox) findViewById(R.id.edit_bl_number_sms_block_check_box);

			if (bs.isChecked() == false) {
				if (toastShowWaitHandler.IsAllowShow()) {
					Toast.makeText(this,
							R.string.PleaseChooseBlockCallsOrMessages,
							Toast.LENGTH_SHORT).show();
				}
				break;
			}
			Intent intent = new Intent();
			intent.putExtra("edited_number_return",
					String.valueOf(numberEt.getText()));
			intent.putExtra("edited_name_return",
					String.valueOf(nameEt.getText()));

			if (bs.isChecked())
				intent.putExtra("sms_block", true);
			else
				intent.putExtra("sms_block", false);

			intent.putExtra("position", mPosition);
			setResult(2, intent);

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			switch (resultCode) {
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
			}
			case 0: {
				break;
			}
			default:
				break;
			}
		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
	}
}
