package com.angelstone.android.smsblocker;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.angelstone.android.smsblocker.store.EventLog;
import com.angelstone.android.smsblocker.store.PhoneNumberDisposition;
import com.angelstone.android.smsblocker.store.PhoneNumberManager;

public class SMSObserver extends ContentObserver {
	private Context ctx = null;
	private static boolean isSmsReject = false;
	
	public SMSObserver(Handler handler, Context _ctx) { 
		super(handler); 
		ctx = _ctx;
	}

	@Override
	public boolean deliverSelfNotifications() { 
		return false; 
	}

	@Override
	public void onChange(boolean arg0) { 
		super.onChange(arg0);

		if (!PhoneNumberManager.getIntance(ctx).readSetting("Firewall_switch")
			|| !PhoneNumberManager.getIntance(ctx).readSetting("sms_reject_switch")
			|| isSmsReject)
		{
			/* 
			if (SmsRejectLogView.getIntance() != null)
			{
				SmsRejectLogView.getIntance().refreshList_2();
			}
			*/
			
			isSmsReject = false;
			
			return;
		}
		
		Uri uriSMSURI = Uri.parse("content://sms/");
		Cursor cur = ctx.getContentResolver().query(uriSMSURI, null, null,
             null, null);
		if (cur == null || cur.isAfterLast())
			return;
		cur.moveToNext();
		String protocol = cur.getString(cur.getColumnIndex("protocol"));
		if (protocol != null)	//receiving
		{
			Log.d("SMS", "SMS RECIEVE");  
			int threadId = cur.getInt(cur.getColumnIndex("thread_id"));
			String number = cur.getString(cur.getColumnIndex("address"));
			String msgTxt = cur.getString(cur.getColumnIndex("body"));
			
			PhoneNumberDisposition disp = PhoneNumberManager.getIntance(ctx).queryAction(number);
			
			boolean matched = false;
			String firstMatchedFilter = new String();
			if (disp.m_SmsAction != PhoneNumberDisposition.SMS_REJECT)
			{	
				String[] filter = GetSmsFilter();
				for (int j = 0; j < filter.length; j++)
				{
					if (msgTxt.indexOf(filter[j]) != -1) 
					{
						firstMatchedFilter = filter[j];
						matched = true;
						break;
					}
				}
				if (!matched)
				{
					/*
					if (SmsRejectLogView.getIntance() != null)
					{
						SmsRejectLogView.getIntance().refreshList_2();
					}
					*/
					return;
				}
					
			}
			
			Log.d("scfw", "SMS_REJECT");
			
			if (!matched)
			{
				WriteToLog(msgTxt, number);
			}
			else
			{
				WriteToLogForFilterSms(msgTxt, number, firstMatchedFilter);
			}
			
			ctx.getContentResolver().delete(Uri.parse("content://sms/conversations/" + threadId), null, null);
			
			if (CallRejectLogView.getIntance() != null)
	       	{
				CallRejectLogView.getIntance().refreshList_2();
	       	}
			
			isSmsReject = true;
		}
	}
	
	private void WriteToLog(String smsBody, String smsNumber)
	{
		EventLog evt = new EventLog(smsNumber, EventLog.LOG_TYPE_SMS );		
		evt.setSmsTxt(smsBody);
		String tag = PhoneNumberManager.getIntance(ctx).getTagByNumber(smsNumber);
    	evt.setTagOrName(tag);
    	evt.setBlockType(EventLog.SMS_LOG_BLOCK_TYPE_BL);
		
    	PhoneNumberManager.getIntance(ctx).writeLog(evt);	
	}
	
	private void WriteToLogForFilterSms(String smsBody, String smsNumber, String filterTerm)
	{
		EventLog evt = new EventLog(smsNumber, EventLog.LOG_TYPE_SMS );
		evt.setSceneOrKeyword(filterTerm);
		evt.setSmsTxt(smsBody);
		String name = PhoneNumberManager.getIntance(ctx).getNameByNumber(smsNumber);
		
		evt.setTagOrName(name);
		evt.setBlockType(EventLog.SMS_LOG_BLOCK_TYPE_FILTER);
		
		PhoneNumberManager.getIntance(ctx).writeLog(evt);	
	}
	
	private String[] GetSmsFilter()
	{
		int cnt = PhoneNumberManager.getIntance(ctx).getSmsFilterCount();
		
		if (cnt == 0)
		{
			return new String[0];
		}
		else
		{
			String[] terms = new String[cnt];
			PhoneNumberManager.getIntance(ctx).getSmsFilters(terms);
				
			return terms;
		}
	}
	
	private String Delete86String(String number)
	{
		int pos = number.indexOf("+86");
		
		if (pos != -1)
		{
			number = number.substring(pos + 3, number.length());
		}
		return number;
	}
}
