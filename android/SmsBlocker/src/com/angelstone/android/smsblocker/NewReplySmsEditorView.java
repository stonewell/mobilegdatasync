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

public class NewReplySmsEditorView extends Activity implements OnClickListener
{
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
	        
	        DisplayMetrics dm = new DisplayMetrics(); 
	        dm = this.getResources().getDisplayMetrics(); 
	        int screenWidth = dm.widthPixels; 
	        int screenHeight = dm.heightPixels; 
	        
	        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(screenWidth / 2 - 10, WC);
	        Button btn = (Button)findViewById(R.id.reply_editor_ok_btn);
	        btn.setOnClickListener(this);
	        btn.setLayoutParams(param);
	        
	        btn = (Button)findViewById(R.id.reply_editor_cancel_btn);
	        btn.setOnClickListener(this);
	        btn.setLayoutParams(param);
	        
	        

		}
		catch (Exception e)
		{
			Log.d("scfw", this.toString() + ":" + e.getClass().toString());
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
								  R.string.EmptySmsNotAllowed, Toast.LENGTH_SHORT).show();
					}
					return;
				}
				else if (isExist(String.valueOf(et.getText())))
				{
					if (toastShowWaitHandler.IsAllowShow())
					{
						Toast.makeText(this, 
							  R.string.SmsAlreadyExists, Toast.LENGTH_SHORT).show();
					}
					return;
				}
				else
				{
				
					PhoneNumberManager.getIntance(this).advAddSms(String.valueOf(et.getText()));
					Intent intent = new Intent();
					setResult(2, intent);
					
				    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					
					finish();
				}
				break;
				
			}
			case R.id.reply_editor_cancel_btn:
			{
				EditText et= (EditText)findViewById(R.id.reply_sms_editor);
				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				finish();
				break;
			}
		}
		
	}
	
	private boolean isExist(String smsBody)
	{
		String[] allSmsBody = PhoneNumberManager.getIntance(this).advGetAllSms();
		
		if (allSmsBody != null)
		{
			for (int i = 0; i < allSmsBody.length; i++)
			{
				if (allSmsBody[i].equals(smsBody))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	

}
