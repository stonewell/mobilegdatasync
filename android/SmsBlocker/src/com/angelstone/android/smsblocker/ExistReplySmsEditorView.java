package com.angelstone.android.smsblocker;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.angelstone.android.smsblocker.store.PhoneNumberManager;
import com.angelstone.android.utils.ToastShowWaitHandler;


public class ExistReplySmsEditorView extends Activity implements OnClickListener
{
	
	private int mReplySmsRecordId = 0;
	private int mStartByActivityId = -1;
	private String mOldSmsBody = new String();
	
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	
	private ToastShowWaitHandler toastShowWaitHandler = new ToastShowWaitHandler();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
	        super.onCreate(savedInstanceState);
	        
	        setContentView(R.layout.reply_sms_editor_view_layout);
	        
	        Button btn = (Button)findViewById(R.id.reply_editor_ok_btn);
	        btn.setOnClickListener(this);
	        
	        Bundle bundle = this.getIntent().getExtras();
	        mReplySmsRecordId = bundle.getInt("REPLY_SMS_RECORD_ID");
	        mStartByActivityId = bundle.getInt("START_BY_ACTIVITY_ID");
	        
	        EditText et= (EditText)findViewById(R.id.reply_sms_editor);
	        	        
	        String[] allReplySmses = PhoneNumberManager.getIntance(this).advGetAllSms();
	        et.setText(allReplySmses[mReplySmsRecordId]);
	        et.setSelection(allReplySmses[mReplySmsRecordId].length(), allReplySmses[mReplySmsRecordId].length());

	        
	        mOldSmsBody = allReplySmses[mReplySmsRecordId];
	        
	        DisplayMetrics dm = new DisplayMetrics(); 
	        dm = this.getResources().getDisplayMetrics(); 
	        int screenWidth = dm.widthPixels; 
	        int screenHeight = dm.heightPixels; 
	        
	        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(screenWidth / 2, WC);
	        Button btn1 = (Button)findViewById(R.id.reply_editor_ok_btn);
	        btn1.setOnClickListener(this);
	        btn1.setLayoutParams(param);
	        
	        btn1 = (Button)findViewById(R.id.reply_editor_cancel_btn);
	        btn1.setOnClickListener(this);
	        btn1.setLayoutParams(param);
		}
		catch (Exception e)
		{
			Log.d("scfw", e.getClass().toString());
		}
        
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.reply_editor_ok_btn:
			{
				EditText et= (EditText)findViewById(R.id.reply_sms_editor);
				
				if (String.valueOf(et.getText()) == null 
				    || String.valueOf(et.getText()).trim().equals(""))
				{
					if (toastShowWaitHandler.IsAllowShow())
					{
						Toast.makeText(this, 
							R.string.EmptySMSIsNotAllowed, Toast.LENGTH_SHORT).show();
					}
					return;
				}
				else if (isExist(mOldSmsBody, String.valueOf(et.getText())))
				{
					if (toastShowWaitHandler.IsAllowShow())
					{
					
						Toast.makeText(this, 
							  R.string.SMSAlreadyExists, Toast.LENGTH_SHORT).show();
					}
					return;
				}
				else
				{
					PhoneNumberManager.getIntance(this).advUpdateReplySms(mOldSmsBody, String.valueOf(et.getText()));
								
					Intent intent = new Intent();
					setResult(1, intent);
					
					((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					
					finish();
				}
			}
			case R.id.reply_editor_cancel_btn:
			{
				EditText et= (EditText)findViewById(R.id.reply_sms_editor);
				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				finish();
			}
		}
		
	}
	
	private boolean isExist(String oidSmsbody, String smsBody)
	{
		String[] allSmsBody = PhoneNumberManager.getIntance(this).advGetAllSms();
		
		if (allSmsBody != null)
		{
			for (int i = 0; i < allSmsBody.length; i++)
			{
				//except self
				if (allSmsBody[i].equals(oidSmsbody))
				{
					continue;
				}
				else if (allSmsBody[i].equals(smsBody))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	

}
