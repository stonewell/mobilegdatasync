package com.angelstone.android.smsblocker.ui;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.angelstone.android.smsblocker.R;
import com.angelstone.android.smsblocker.store.EventLog;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;

public class RejectedSmsBodyView extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		try {

			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_LEFT_ICON);

			Bundle bundle = this.getIntent().getExtras();
			int pos = bundle.getInt("click_pos");

			EventLog[] logs = null;

			logs = PhoneNumberManager.getIntance(this).getLogs(
					EventLog.LOG_TYPE_SMS, EventLog.LOG_SCOPE_INTERCEPTED, 0);
			this.setContentView(R.layout.reject_sms_body_show_layout);

			getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
					android.R.drawable.ic_menu_more);

			String number = logs[pos].getNumber();
			String body = logs[pos].getSmsTxt();

			String timeStr = DateUtils.formatDateTime(this, logs[pos].getTime()
					.getTime(), DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_ALL);

			String tag_or_name = logs[pos].getTagOrName();

			// String dateStr = date.toLocaleString();

			TextView tv1 = (TextView) findViewById(R.id.sms_reject_record_number_text);

			if (tag_or_name != null && !tag_or_name.equals("")) {
				tv1.setText(number + "<" + tag_or_name + ">");
			} else {
				tv1.setText(number);
			}

			TextView tv2 = (TextView) findViewById(R.id.sms_reject_record_body_text);

			tv2.setText(body.replace("\r", ""));

			TextView tv3 = (TextView) findViewById(R.id.sms_reject_record_date_text);
			tv3.setText(timeStr);
		} catch (Exception e) {
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
	}
}
