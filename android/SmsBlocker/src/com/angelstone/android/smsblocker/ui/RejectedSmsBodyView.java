package com.angelstone.android.smsblocker.ui;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Window;
import android.widget.TextView;

import com.angelstone.android.phonetools.store.EventLog;
import com.angelstone.android.phonetools.store.PhoneToolsDBManager;
import com.angelstone.android.smsblocker.R;

public class RejectedSmsBodyView extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_LEFT_ICON);
		setContentView(R.layout.reject_sms_body_show_layout);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				android.R.drawable.ic_menu_more);

		Bundle bundle = this.getIntent().getExtras();
		int id = bundle.getInt("click_id");

		Cursor cur = PhoneToolsDBManager.getEventLogManager().getEventLog(this, id);

		try {
			cur.moveToNext();

			String number = cur.getString(cur.getColumnIndex(EventLog.COL_NUMBER));
			String body = cur.getString(cur.getColumnIndex(EventLog.COL_CONTENT));
			long time = cur.getLong(cur.getColumnIndex(EventLog.COL_TIME));

			String timeStr = DateUtils.formatDateTime(this, time,
					DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME
							| DateUtils.FORMAT_ABBREV_ALL);

			TextView tv1 = (TextView) findViewById(R.id.sms_reject_record_number_text);

			tv1.setText(number);

			TextView tv2 = (TextView) findViewById(R.id.sms_reject_record_body_text);

			tv2.setText(body.replace("\r", ""));

			TextView tv3 = (TextView) findViewById(R.id.sms_reject_record_date_text);
			tv3.setText(timeStr);
		} finally {
			cur.close();
		}
	}
}
