package com.angelstone.android.smsblocker;

import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.angelstone.android.smsblocker.store.EventLog;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;

public class BlockedCallLogDetailView extends Activity
{
	public void onCreate(Bundle savedInstanceState) 
    {
		try
		{
			
	        super.onCreate(savedInstanceState);
	        
	        requestWindowFeature(Window.FEATURE_LEFT_ICON);
	        this.setContentView(R.layout.blocked_call_log_detail_view_layout);
	        
	        
	        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, 
	                android.R.drawable.ic_menu_more);
	        
	        Bundle bundle = this.getIntent().getExtras();
	        int pos = bundle.getInt("click_pos");

	        EventLog[] logs = PhoneNumberManager.getIntance(this).getLogs(EventLog.LOG_TYPE_CALL, EventLog.LOG_SCOPE_INTERCEPTED, 0);      
	        String number = logs[pos].getNumber();
        	String timeStr = DateUtils.formatDateTime(this, logs[pos].getTime().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME
                                                                                               |DateUtils.FORMAT_ABBREV_ALL);    
	        String tag_or_name = logs[pos].getTagOrName();
	        String replySmsTxt = logs[pos].getReplySmsTxt();
        	
        	TextView tv1 = (TextView)findViewById(R.id.blocked_call_log_number_text);
        	
        	if (tag_or_name != null)
        	{
        		if (!tag_or_name.equals(""))
        		{
        			tv1.setText(number + "<" + tag_or_name + ">");
        		}
        		else
        		{
        			tv1.setText(number);
        		}
        	}
        	else
        	{
        		tv1.setText(number);
        	}
        	
        	TextView tv3 = (TextView)findViewById(R.id.blocked_call_log_reply_sms_text);
        	tv3.setText(replySmsTxt);
        	
        	TextView tv4 = (TextView)findViewById(R.id.blocked_call_log_date_text);
        	tv4.setText(timeStr);
        	
        	
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
    }
}
