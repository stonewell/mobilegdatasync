package com.angelstone.android.smsblocker;


import android.app.Activity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TextView;

import com.angelstone.android.smsblocker.store.EventLog;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;

public class AdvanceCallLogDetailView extends Activity
{
	public void onCreate(Bundle savedInstanceState) 
    {
		try
		{
			
	        super.onCreate(savedInstanceState);
	        this.setContentView(R.layout.advance_call_log_detail_view_layout);
	        
	        Bundle bundle = this.getIntent().getExtras();
	        int pos = bundle.getInt("click_pos");
	        //int viewType = bundle.getInt("which_view");
	        
	        
	        //EventLog[] logs = AppMain.db.getLogs(EventLog.LOG_TYPE_CALL, EventLog.LOG_SCOPE_INTERCEPTED, 1);
	        EventLog[] logs = PhoneNumberManager.getIntance(this).getLogs(EventLog.LOG_TYPE_CALL, EventLog.LOG_SCOPE_INTERCEPTED, 1);
	        
	        String number = logs[pos].getNumber();
	        String body = logs[pos].getSmsTxt();
     	
        	String timeStr = DateUtils.formatDateTime(this, logs[pos].getTime().getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME
                                                                                               |DateUtils.FORMAT_ABBREV_ALL);    
	        String tag_or_name = logs[pos].getTagOrName();
	        String sceneName = logs[pos].getSceneOrKeyword();
	        String replySmsTxt = logs[pos].getReplySmsTxt();
	        //AdvScene scene_info = new AdvScene();
	        
	        //AppMain.db.advGetSceneInfo(sceneName, scene_info );
	        
        	//String dateStr = date.toLocaleString();
        	
        	TextView tv1 = (TextView)findViewById(R.id.adv_call_log_number_text);
        	
        	if (tag_or_name != null)
        	{
        		tv1.setText(number + "<" + tag_or_name + ">");
        	}
        	else
        	{
        		tv1.setText(number);
        	}
        	
        	TextView tv2 = (TextView)findViewById(R.id.adv_call_log_scene_name_text);
        	tv2.setText(sceneName);
        	
        	TextView tv3 = (TextView)findViewById(R.id.adv_call_log_reply_sms_text);
        	tv3.setText(replySmsTxt);
        	
        	TextView tv4 = (TextView)findViewById(R.id.adv_call_log_date_text);
        	tv4.setText(timeStr);
        	
        	
		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
		}
    }
}
