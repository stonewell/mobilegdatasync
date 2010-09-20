package com.angelstone.android.smsblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class ActiveReplySmsEditorView extends Activity implements OnClickListener
{
	private int mReplySmsRecordId = 0;
	private int mStartByActivityId = -1;
	private String mOldSmsBody = new String();
	
	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private final int FC = ViewGroup.LayoutParams.FILL_PARENT;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
	        super.onCreate(savedInstanceState);
	        
	        setContentView(R.layout.reply_sms_editor_view_layout);
	        
	        Bundle bundle = this.getIntent().getExtras();
	        mOldSmsBody = bundle.getString("CURRENT_SMS_CONTENT");
	        
	        EditText et= (EditText)findViewById(R.id.reply_sms_editor);
	        et.setText(mOldSmsBody);
	        et.setSelection(mOldSmsBody.length(), mOldSmsBody.length());
	        
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
			Log.d("scfw", "ActiveReplySmsEditorView:" + e.getClass().toString());
		}
        
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.reply_editor_ok_btn:
			{
				EditText et = (EditText)findViewById(R.id.reply_sms_editor);
				
				if (String.valueOf(et.getText()) == null
					|| String.valueOf(et.getText()).trim().equals(""))
				{
					AlertDialog ad = new AlertDialog.Builder(this)
	                .setIcon(R.drawable.alert_dialog_icon)
	                .setTitle(R.string.alert_dialog_two_buttons_title_1)
	                .setMessage(R.string.alert_dialog_two_buttons_message_1)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) 
	                    {			
	        				Intent intent = new Intent();
	        				Bundle bundle = new Bundle();
	        				bundle.putString("ACTIVE_SMS_MODIFY_RETURN", null);
	        				intent.putExtras(bundle);
	        				setResult(1, intent);
	        				
	        				EditText et = (EditText)findViewById(R.id.reply_sms_editor);
	        				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	        				
	        				finish();
	                    	
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Cancel so do some stuff */
	                    }
	                })
	                .create();
					
					ad.show();	
				}
				else
				{
					//AppMain.db.advUpdateReplySms(mOldSmsBody, String.valueOf(et.getText()));			
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putString("ACTIVE_SMS_MODIFY_RETURN", String.valueOf(et.getText()));
					intent.putExtras(bundle);
					setResult(1, intent);
					
					((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					
					finish();
				}
				break;
			}
			case R.id.reply_editor_cancel_btn:
			{
				EditText et = (EditText)findViewById(R.id.reply_sms_editor);
				
				((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				
				finish();
				break;
			}
			default:
				break;
		}
		
	}
}
